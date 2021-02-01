package com.lepu.nordicble.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.annotation.NonNull
import com.blankj.utilcode.util.LogUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.nordicble.ble.cmd.KcaBleCmd
import com.lepu.nordicble.ble.cmd.KcaBleCmd.*
import com.lepu.nordicble.ble.cmd.KcaBleResponse
import com.lepu.nordicble.objs.Bluetooth
import com.lepu.nordicble.utils.HexString
import com.lepu.nordicble.utils.add
import com.lepu.nordicble.vals.*
import com.lepu.nordicble.viewmodel.KcaViewModel
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.observer.ConnectionObserver

class KcaBleInterface(context: Context) : ConnectionObserver, KcaBleManger.onNotifyListener {

    private lateinit var model: KcaViewModel
    public fun setViewModel(viewModel: KcaViewModel) {
        this.model = viewModel
    }

    private val mContext = context
    lateinit var manager: KcaBleManger

    var mydevice: BluetoothDevice? = null

    private var pool: ByteArray? = null

    /**
     * interface
     * state
     * connect
     * disconnect
     * getInfo
     * getRtData
     */
    public var state = false
    private var connecting = false

    public fun connect(context: Context, @NonNull device: BluetoothDevice) {
        if (connecting || state) {
            return
        }
        disconnect()
        manager = KcaBleManger(mContext)
        mydevice = device
        manager.setConnectionObserver(this)
        manager.setNotifyListener(this)
        manager.connect(device)
            .useAutoConnect(false)
            .timeout(10000)
            .retry(1, 100)
            .done {
                LogUtils.d("Device Init")
            }
            .enqueue()

    }

    public fun disconnect() {

        mydevice?.apply {
            manager.disconnect()
            manager.close()
            
            onDeviceDisconnected(this, ConnectionObserver.REASON_SUCCESS)
            mydevice = null
        }
    }

    fun syncTime() {
        sendCmd(syncTimeCmd())
    }

    fun getSn() {
        sendCmd(getSnCmd())
    }

    fun getBattery() {
        sendCmd(KcaBleCmd.getBattery())
    }

    fun setNightPeriod(stH: Int, stM: Int, edH: Int, edM: Int) {
        sendCmd(KcaBleCmd.setNightPeriod(stH, stM, edH, edM))
    }

    fun setInterval(dayInt: Int, nightInt: Int) {
        sendCmd(KcaBleCmd.setInterval(dayInt, nightInt))
    }

    private fun sendCmd(bs: ByteArray) {
        if (!state) {
            return
        }
//        val bleJob = BleJobController.BleJob(cmd, bs, timeout)
        manager.sendCmd(bs)
    }

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(pkg: KcaPackage) {
//        controller.onBleResponseReceived(response)
        val kcaContent = KcaContent(pkg.content)
//        LogUtils.d("received cmd: ${kcaContent.cmd}")
//        for (key in kcaContent.keyObjs) {
//            LogUtils.d("received key: ${kcaContent.cmd} -> ${key.key} ~ ${key.`val`.toHex()}")
//        }

        // broadcast
        when(kcaContent.cmd) {
            KcaBleCmd.CMD_CONFIG -> {
                val key = kcaContent.keyObjs[0] as KeyObj
                when (key.key) {
                    KEY_TIME_RES -> {
                        LogUtils.d("设置时间成功")
                    }
                    KEY_NIGHT_PERIOD_RES -> {
                        LogUtils.d("设置夜间区间成功")
                    }
                    KEY_INTERVAL_RES -> {
                        LogUtils.d("设置测量间隔成功")
                    }
                }
            }
            KcaBleCmd.CMD_STATE -> {
                val key: KcaBleCmd.KeyObj = kcaContent.keyObjs[0]
                model.measureState.value = key.key

                when (key.key) {
                    KEY_MEASURE_START -> {
                        LiveEventBus.get(EventMsgConst.EventKcaMeasureState)
                            .post(KcaBleResponse.KcaBpState(KEY_MEASURE_START, 0))
                    }
                    KEY_MEASURING -> {
                        val bp: Int =
                            ((key.`val`[0].toUInt() and 0xFFu) shl 8 or (key.`val`[1].toUInt() and 0xFFu)).toInt()
                        model.rtBp.value = bp

                        LiveEventBus.get(EventMsgConst.EventKcaMeasureState)
                            .post(KcaBleResponse.KcaBpState(KEY_MEASURING, bp))
                    }
                    KEY_MEASURE_RESULT -> {
//                        LogUtils.d("bp result", key.`val`.toHex())
                        val result: KcaBleResponse.KcaBpResult =
                            KcaBleResponse.KcaBpResult(
                                key.`val`
                            )
                        model.bpResult.value = result

                        LiveEventBus.get(EventMsgConst.EventKcaMeasureState)
                            .post(
                                KcaBleResponse.KcaBpState(
                                    KEY_MEASURE_RESULT,
                                    result.sys
                                )
                            )

                        LiveEventBus.get(EventMsgConst.EventKcaBpResult)
                            .post(result)

                        // todo: 获取到测量结果之后下发设置
                        getBattery()
                    }
                }
            }
            KcaBleCmd.CMD_DATA -> {
                val key = kcaContent.keyObjs[0] as KeyObj

                when (key.key) {
                    KEY_SN_RES -> {
                        val sn = HexString.trimStr(String(key.`val`))
                        LogUtils.d("获取到SN: $sn")
                        kcaSn = sn
                        LiveEventBus.get(EventMsgConst.EventKcaSn)
                            .post(sn)
                    }
                    KEY_BATTERY_RES -> {
                        val battery = key.`val`[0].toUInt().toInt()
                        model.battery.value = battery
                        kcaBattery = battery
                        LogUtils.d("获取到电量: $battery")
                    }
                }
            }

        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun hasResponse(bytes: ByteArray?): ByteArray? {
        val bytesLeft: ByteArray? = bytes

        if (bytes == null || bytes.size < 8) {
            return bytes
        }

        loop@ for (i in 0 until bytes.size-7) {
            if (bytes[i] != 0x5A.toByte()) {
                continue@loop
            }

            // need content length
            val len = ((bytes[i + 2].toUInt() and 0xFFu) shl 8 or (bytes[i + 3].toUInt() and 0xFFu)).toInt()
//            LogUtils.d("want bytes length: $len")
            if (i+8+len > bytes.size) {
                continue@loop
            }

            val temp: ByteArray = bytes.copyOfRange(i, i + 8 + len)

            val res = KcaPackage(temp)
            if (!res.crcHasErr) {
                onResponseReceived(res)

                val tempBytes: ByteArray? = if (i+8+len == bytes.size) null else bytes.copyOfRange(
                    i + 8 + len,
                    bytes.size
                )
                return hasResponse(tempBytes)
            }

        }

        return bytesLeft
    }

    private fun setBleState(b: Boolean) {
        state = b
        kcaConn = b
        model.connect.postValue(b)
    }

    override fun onNotify(device: BluetoothDevice?, data: Data?) {
        data?.value?.apply {
            pool = add(pool, this)
//            LogUtils.d(pool!!.toHex())
        }
        pool?.apply {
            pool = hasResponse(pool)
        }
    }

    override fun onDeviceConnected(device: BluetoothDevice) {
        setBleState(true)

        connecting = false
    }

    override fun onDeviceConnecting(device: BluetoothDevice) {
        setBleState(false)

        connecting = true
    }

    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        setBleState(false)

        connecting = false

        LiveEventBus.get(EventMsgConst.EventDeviceDisconnect).post(Bluetooth.MODEL_KCA)
    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {
        setBleState(false)

        connecting = false
    }

    override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
        setBleState(false)

        connecting = false
    }

    override fun onDeviceReady(device: BluetoothDevice) {

//        LogUtils.d(mydevice.name)

        connecting = false
    }
}