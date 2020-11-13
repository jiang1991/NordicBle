package com.lepu.nordicble.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.Handler
import androidx.annotation.NonNull
import com.blankj.utilcode.util.LogUtils
import com.lepu.nordicble.ble.cmd.BleCmd
import com.lepu.nordicble.ble.cmd.KacBleCmd
import com.lepu.nordicble.ble.cmd.KacBleCmd.*
import com.lepu.nordicble.ble.cmd.KacBleResponse
import com.lepu.nordicble.objs.*
import com.lepu.nordicble.utils.add
import com.lepu.nordicble.utils.toHex
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.observer.ConnectionObserver

class KacBleInterface : ConnectionObserver, KacBleManger.onNotifyListener {
    lateinit var manager: KacBleManger

    lateinit var mydevice: BluetoothDevice

    private var pool: ByteArray? = null

    private val rtHandler = Handler()
    private var count: Long = 0L
    inner class RtTask: Runnable {
        override fun run() {
            rtHandler.postDelayed(this, 1000)
            if (state) {
                getRtData()
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

    public fun connect(context: Context, @NonNull device: BluetoothDevice) {
        manager = KacBleManger(context)
        mydevice = device
        manager.setConnectionObserver(this)
        manager.setNotifyListener(this)
        manager.connect(device)
            .useAutoConnect(true)
            .timeout(10000)
            .retry(3, 100)
            .done {
                LogUtils.d("Device Init")
//                runRtTask()
            }
            .enqueue()

    }

    public fun disconnect() {
        manager.disconnect()
    }

    public fun getInfo() {
        sendCmd(BleCmd.BLE_CMD_GET_INFO, BleCmd.getInfo())
    }

    public fun getRtData() {
        sendCmd(BleCmd.BLE_CMD_RT_DATA, BleCmd.getRtData())
    }

    public fun runRtTask() {

        rtHandler.postDelayed(RtTask(), 200)
    }

    private fun sendCmd(cmd : Int, bs: ByteArray) {
//        val bleJob = BleJobController.BleJob(cmd, bs, timeout)
        manager.sendCmd(bs)
    }

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(pkg: KacBleCmd.KacPackage) {
//        controller.onBleResponseReceived(response)
        val kacContent = KacBleCmd.KacContent(pkg.content)
//        LogUtils.d("received cmd: ${kacContent.cmd}")
        for (key in kacContent.keyObjs) {
            LogUtils.d("received key: ${kacContent.cmd} -> ${key.key} ~ ${key.`val`.toHex()}")
        }

        // broadcast
        when(kacContent.cmd) {
            KacBleCmd.CMD_CONFIG -> {
                //
            }
            KacBleCmd.CMD_STATE -> {
                val key : KacBleCmd.KeyObj = kacContent.keyObjs[0]
                when(key.key) {
                    KEY_MEASURE_START -> {
                        val intent = Intent(KacBleCmd.ACTION_KAC_STATE)
                        intent.putExtra("state", KEY_MEASURE_START)
                        Const.context.sendBroadcast(intent)
                    }
                    KEY_MEASURING -> {
                        val bp: Int = ((key.`val`[0].toUInt() and 0xFFu) shl 8 or (key.`val`[1].toUInt() and 0xFFu)).toInt()
                        val intent = Intent(KacBleCmd.ACTION_KAC_STATE)
                        intent.putExtra("state", KEY_MEASURING)
                        intent.putExtra("bp", bp)
                        Const.context.sendBroadcast(intent)
                    }
                    KEY_MEASURE_RESULT -> {
                        val result: KacBleResponse.KacBpResult = KacBleResponse.KacBpResult(key.`val`)
                        val intent = Intent(KacBleCmd.ACTION_KAC_STATE)
                        intent.putExtra("state", KEY_MEASURE_RESULT)
                        intent.putExtra("result", result)
                        Const.context.sendBroadcast(intent)
                    }
                }
            }
            KacBleCmd.CMD_DATA -> {
                //
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
            val len = ((bytes[i+2].toUInt() and 0xFFu) shl 8 or (bytes[i+3].toUInt() and 0xFFu)).toInt()
//            LogUtils.d("want bytes length: $len")
            if (i+8+len > bytes.size) {
                continue@loop
            }

            val temp: ByteArray = bytes.copyOfRange(i, i+8+len)

            val res = KacBleCmd.KacPackage(temp)
            if (!res.crcHasErr) {
                onResponseReceived(res)

                val tempBytes: ByteArray? = if (i+8+len == bytes.size) null else bytes.copyOfRange(i+8+len, bytes.size)
                return hasResponse(tempBytes)
            }

        }

        return bytesLeft
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
        state = true
        LogUtils.d(mydevice.name)

    }

    override fun onDeviceConnecting(device: BluetoothDevice) {
        state = false
//        LogUtils.d(mydevice.name)
    }

    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        state = false
        LogUtils.d(mydevice.name)
        rtHandler.removeCallbacks(RtTask())
    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {
        state = false
//        LogUtils.d(mydevice.name)
    }

    override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
        state = false
        LogUtils.d(mydevice.name)
    }

    override fun onDeviceReady(device: BluetoothDevice) {

//        LogUtils.d(mydevice.name)
    }
}