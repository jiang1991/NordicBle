package com.lepu.nordicble.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Handler
import androidx.annotation.NonNull
import com.blankj.utilcode.util.LogUtils
import com.lepu.nordicble.ble.cmd.Er1BleCRC
import com.lepu.nordicble.ble.cmd.Er1BleCmd
import com.lepu.nordicble.objs.*
import com.lepu.nordicble.utils.add
import com.lepu.nordicble.utils.toUInt
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.observer.ConnectionObserver
import kotlin.experimental.inv

class Er1BleInterface : ConnectionObserver, Er1BleManager.onNotifyListener {
    lateinit var manager: Er1BleManager

    lateinit var mydevice: BluetoothDevice

    lateinit var controller: BleJobController

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
        manager = Er1BleManager(context)
        mydevice = device
        controller = BleJobController(device)
        manager.setConnectionObserver(this)
        manager.setNotifyListener(this)
        manager.connect(device)
            .useAutoConnect(true)
            .timeout(10000)
            .retry(3, 100)
            .done {
                LogUtils.d("Device Init")
                runRtTask()
            }
            .enqueue()

    }

    public fun disconnect() {
        manager.disconnect()
    }

    public fun getInfo() {
        sendCmd(Er1BleCmd.BLE_CMD_GET_INFO, Er1BleCmd.getInfo(), 3000)
    }

    public fun getRtData() {
        sendCmd(Er1BleCmd.BLE_CMD_RT_DATA, Er1BleCmd.getRtData(), 3000)
    }

    public fun runRtTask() {

        rtHandler.postDelayed(RtTask(), 200)
    }

    private fun sendCmd(cmd : Int, bs: ByteArray, timeout: Int) {
        val bleJob = BleJobController.BleJob(cmd, bs, timeout)
        controller.addJob(bleJob)
        manager.sendCmd(bs)
    }

    private fun onResponseReceived(response: BtResponse.BleResponse) {
        controller.onBleResponseReceived(response)
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
                val bleResponse = BtResponse.BleResponse(temp)
//                Log.d(TAG, "get response: " + temp.toHex())
                onResponseReceived(bleResponse)

                val tempBytes: ByteArray? = if (i+8+len == bytes.size) null else bytes.copyOfRange(i+8+len, bytes.size)

                return hasResponse(tempBytes)
            }
        }

        return bytesLeft
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