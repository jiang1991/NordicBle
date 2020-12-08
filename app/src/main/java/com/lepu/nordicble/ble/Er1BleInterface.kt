package com.lepu.nordicble.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Handler
import androidx.annotation.NonNull
import com.blankj.utilcode.util.LogUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.nordicble.ble.cmd.Er1BleCRC
import com.lepu.nordicble.ble.cmd.Er1BleCmd
import com.lepu.nordicble.ble.cmd.Er1BleResponse
import com.lepu.nordicble.ble.obj.Er1DataController
import com.lepu.nordicble.ble.obj.Er1Device
import com.lepu.nordicble.objs.Bluetooth
import com.lepu.nordicble.utils.add
import com.lepu.nordicble.utils.toHex
import com.lepu.nordicble.utils.toUInt
import com.lepu.nordicble.vals.EventMsgConst
import com.lepu.nordicble.vals.er1Battery
import com.lepu.nordicble.vals.er1Sn
import com.lepu.nordicble.vals.hasEr1
import com.lepu.nordicble.viewmodel.Er1ViewModel
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.observer.ConnectionObserver
import kotlin.experimental.inv

class Er1BleInterface : ConnectionObserver, Er1BleManager.onNotifyListener {

    private lateinit var model: Er1ViewModel
    fun setViewModel(viewModel: Er1ViewModel) {
        this.model = viewModel
    }

    lateinit var manager: Er1BleManager

    lateinit var mydevice: BluetoothDevice

    private var pool: ByteArray? = null

    private val rtHandler = Handler()
    private var count: Int = 0
    inner class RtTask: Runnable {
        override fun run() {
            rtHandler.postDelayed(this, 1000)
            if (state) {
                count++
                getRtData()
//                LogUtils.d("RtTask: $count")
            } else {
                LiveEventBus.get(EventMsgConst.EventEr1InvalidRtData)
                    .postAcrossProcess(true)
            }
        }
    }

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
        LogUtils.d("try connect: ${device.name}")
        manager = Er1BleManager(context)
        mydevice = device
        manager.setConnectionObserver(this)
        manager.setNotifyListener(this)
        manager.connect(device)
            .useAutoConnect(true)
            .timeout(10000)
            .retry(3, 100)
            .done {
                LogUtils.d("Device Init")

            }
            .enqueue()

    }

    public fun disconnect() {
        manager.disconnect()
        manager.close()
        this.onDeviceDisconnected(mydevice, ConnectionObserver.REASON_SUCCESS)
    }

    public fun getInfo() {
        sendCmd(Er1BleCmd.getInfo())
    }

    public fun getRtData() {
        sendCmd(Er1BleCmd.getRtData())
    }

    public fun runRtTask() {
        rtHandler.postDelayed(RtTask(), 200)
    }

    private fun sendCmd(bs: ByteArray) {
        manager.sendCmd(bs)
    }

    private fun onResponseReceived(response: Er1BleResponse.Er1Response) {
//        LogUtils.d("received: ${response.cmd}")
        when(response.cmd) {
            Er1BleCmd.ER1_CMD_GET_INFO -> {
                val erInfo = Er1Device(response.content)
                model.er1.value = erInfo
                LiveEventBus.get(EventMsgConst.EventEr1Info)
                    .postAcrossProcess(erInfo)

                runRtTask()
            }

            Er1BleCmd.ER1_CMD_RT_DATA -> {
                val rtData = Er1BleResponse.RtData(response.content)
                model.hr.value = rtData.param.hr
                model.duration.value = rtData.param.recordTime
                model.lead.value = rtData.param.leadOn
                model.battery.value = rtData.param.battery

                Er1DataController.receive(rtData.wave.wFs)
                LiveEventBus.get(EventMsgConst.EventEr1RtData)
                    .postAcrossProcess(rtData)
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
            if (bytes[i] != 0xA5.toByte() || bytes[i+1] != bytes[i+2].inv()) {
                continue@loop
            }

            // need content length
            val len = toUInt(bytes.copyOfRange(i+5, i+7))
//            Log.d(TAG, "want bytes length: $len")
            if (i+8+len > bytes.size) {
                continue@loop
            }

            val temp: ByteArray = bytes.copyOfRange(i, i+8+len)
            if (temp.last() == Er1BleCRC.calCRC8(temp)) {
                val bleResponse = Er1BleResponse.Er1Response(temp)
//                LogUtils.d("get response: ${temp.toHex()}" )
                onResponseReceived(bleResponse)

                val tempBytes: ByteArray? = if (i+8+len == bytes.size) null else bytes.copyOfRange(i+8+len, bytes.size)

                return hasResponse(tempBytes)
            }
        }

        return bytesLeft
    }

    private fun clearVar() {
        model.battery.value = 0
        model.duration.value = 0
        model.hr.value = 0
    }

    override fun onNotify(device: BluetoothDevice?, data: Data?) {
        data?.value?.apply {
            pool = add(pool, this)
        }
        pool?.apply {
            pool = hasResponse(pool)
        }
    }

    override fun onDeviceConnected(device: BluetoothDevice) {
        state = true
        model.connect.value = state
        LogUtils.d(mydevice.name)

        connecting = false
    }

    override fun onDeviceConnecting(device: BluetoothDevice) {
        state = false
        model.connect.value = state
//        LogUtils.d(mydevice.name)

        connecting = true
    }

    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        state = false
        model.connect.value = state
        LogUtils.d(mydevice.name)
        rtHandler.removeCallbacks(RtTask())

        clearVar()

        connecting = false

        LiveEventBus.get(EventMsgConst.EventDeviceDisconnect).postAcrossProcess(Bluetooth.MODEL_ER1)
    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {
        state = false
        model.connect.value = state
//        LogUtils.d(mydevice.name)

        connecting = false
    }

    override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
        state = false
        LogUtils.d(mydevice.name)
        model.connect.value = state

        connecting = false
    }

    override fun onDeviceReady(device: BluetoothDevice) {
        connecting = false
//        LogUtils.d(mydevice.name)
    }
}