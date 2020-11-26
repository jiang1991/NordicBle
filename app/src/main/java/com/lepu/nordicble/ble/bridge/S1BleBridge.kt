package com.lepu.nordicble.ble.bridge

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Handler
import android.util.Log
import androidx.annotation.NonNull
import com.blankj.utilcode.util.LogUtils
import com.lepu.nordicble.ble.cmd.er1.Er1BleCRC
import com.lepu.nordicble.ble.cmd.s1.BleConstant
import com.lepu.nordicble.ble.cmd.s1.S1BleCmd
import com.lepu.nordicble.ble.cmd.s1.S1BleResponse
import com.lepu.nordicble.ble.cmd.s1.file.*
import com.lepu.nordicble.ble.manager.S1BleManager
import com.lepu.nordicble.ble.protocol.Utils.Companion.bytesToHex
import com.lepu.nordicble.utils.add
import com.lepu.nordicble.utils.toUInt
import com.lepu.nordicble.viewmodel.S1ViewModel
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.observer.ConnectionObserver
import kotlin.experimental.inv

class S1BleBridge  : ConnectionObserver, S1BleManager.OnNotifyListener {

    private lateinit var model: S1ViewModel
    fun setViewModel(viewModel: S1ViewModel) {
        this.model = viewModel
    }

    lateinit var manager: S1BleManager
    lateinit var mydevice: BluetoothDevice
    lateinit var fileDownloader: BleFileDownloader

    var state = false
    /**
     * 是否开启自动获取设备实时数据的标志, true开启，false关闭
     * 获取文件列表和下载文件的时候会设置为关闭状态
     */
    var syncState = true
    private var pool: ByteArray? = null
    private val rtHandler = Handler()
    private var count: Int = 0
    inner class RtTask: Runnable {
        override fun run() {
            rtHandler.postDelayed(this, 1000)
            if (state && syncState) {
                count++
                getRtData()
            }
        }
    }

   fun connect(context: Context, @NonNull device: BluetoothDevice) {
        LogUtils.d("try connect: ${device.name}")
        manager = S1BleManager(context)
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

    fun getRtData() {
        sendCmd(S1BleCmd.getRtData())
    }

    fun listFiles() {
        syncState = false
        sendCmd(S1BleCmd.listFiles())
    }

    fun download(fileName: String) {
        syncState = false
        fileDownloader = BleFileDownloader(manager)
        val fileListener = object: ReadBleFileListener {
            override fun onBleReadPartFinished(fileName: String?, fileType: Byte, percentage: Float) {
                super.onBleReadPartFinished(fileName, fileType, percentage)

                Log.d("S1BleBridge", "download onBleReadPartFinished $fileName == $percentage")
            }

            override fun onBleReadSuccess(fileName: String?, fileType: Byte, fileBuf: ByteArray?) {
                super.onBleReadSuccess(fileName, fileType, fileBuf)
                Log.d("S1BleBridge", "download onBleReadSuccess $fileName")
                syncState = true
                manager.setNotifyListener(this@S1BleBridge)
            }

            override fun onReadFailed(fileName: String?, fileType: Byte, errCode: Byte) {
                super.onReadFailed(fileName, fileType, errCode)
                Log.d("S1BleBridge", "download onReadFailed $fileName")
                syncState = true
                manager.setNotifyListener(this@S1BleBridge)
            }
        }
        fileDownloader.readFile(fileName, 0, 2000, fileListener)
    }

    private fun runRtTask() {
        rtHandler.postDelayed(RtTask(), 200)
    }

    private fun sendCmd(bs: ByteArray) {
        manager.sendCmd(bs)
    }

    override fun onNotify(device: BluetoothDevice?, data: Data?) {
        data?.value?.apply {
            pool = add(pool, this)
        }
        pool?.apply {
            pool = hasResponse(pool)
        }
    }


    private fun hasResponse(bytes: ByteArray?): ByteArray? {
        val bytesLeft: ByteArray? = bytes

        if (bytes == null || bytes.size < 8) {
            return bytes
        }

        loop@ for (i in 0 until bytes.size-7) {
            if (bytes[i] != 0xA5.toByte() || bytes[i + 1] != bytes[i + 2].inv()) {
                continue@loop
            }

            // need content length
            val len = toUInt(bytes.copyOfRange(i + 5, i + 7))
            if (i+8+len > bytes.size) {
                continue@loop
            }

            val temp: ByteArray = bytes.copyOfRange(i, i + 8 + len)
            if (temp.last() == Er1BleCRC.calCRC8(temp)) {
                val bleResponse = S1BleResponse.S1Response(temp)
                onResponseReceived(bleResponse)
//                Log.d("S1BleBridge", "onResponseReceived == " + bytesToHex(temp))

                val tempBytes: ByteArray? = if (i+8+len == bytes.size) null else bytes.copyOfRange(i + 8 + len, bytes.size)

                return hasResponse(tempBytes)
            }
        }

        return bytesLeft
    }

    private fun onResponseReceived(response: S1BleResponse.S1Response) {
//        LogUtils.d("received: ${response.cmd}")
        when(response.cmd) {
            S1BleCmd.CMD_GET_INFO -> { // 设备信息返回

            }

            S1BleCmd.CMD_RT_DATA -> { // 实时数据返回
                val rtParam = S1BleResponse.RtParam(response.content.copyOfRange(0, 16))
                model.runningState.value = rtParam.runStatus.toInt()
                model.hrMeasureTime.value = rtParam.recordTime
                model.hrMeasureValue.value = rtParam.hr

                val scaleData = S1BleResponse.ScaleData(response.content.copyOfRange(16, 27))
                model.weight.value = scaleData.getWeightResult()
                model.weightUnit.value = scaleData.getUnit()
                model.weightPrecision.value = scaleData.getPrecision()
                if (scaleData.measureMask == 0xBB.toByte()) { // 0xBB定格数据
                    val resultData = scaleData.content // 上传resultData到SDK服务器解析
                } else { // 0xB0实时数据
                    scaleData.content
                }

                // 获取波形数据
                val size = response.content.size
                val wave = S1BleResponse.RtWave(response.content.copyOfRange(27, size))
                val waveData = wave.wFs // 波形数据用于绘制波形

            }
            S1BleCmd.CMD_LIST_FILE -> { // 读取文件列表
                if (response.content.isNotEmpty()) {
                    val fileArray = FileList(response.content)
                    val fileNames: List<String> = fileArray.listFileName()
                    Log.d("S1BleBridge", "fileNames : ${fileNames[0]}")
                    syncState = true
                    download(fileNames[0])
                } else {
                    Log.d("S1BleBridge", "CMD_LIST_FILE : no file exist.")
                    syncState = true
                }
            }
        }
    }


    override fun onDeviceConnecting(device: BluetoothDevice) {
        state = false
        model.deviceName.value = device.name
        model.connectStateStr.value = "Connecting"
        Log.d("S1BleBridge", "${device.name} : Connecting")
    }

    override fun onDeviceConnected(device: BluetoothDevice) {
        state = true
        model.deviceName.value = device.name
        model.connectStateStr.value = "Connected"
        Log.d("S1BleBridge", "${device.name} : Connected")
    }

    override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
        state = false
        model.deviceName.value = device.name
        model.connectStateStr.value = "FailedToConnect"
        Log.d("S1BleBridge", "${device.name} : FailedToConnect")
    }

    override fun onDeviceReady(device: BluetoothDevice) {
        state = true
        model.deviceName.value = device.name
        model.connectStateStr.value = "Ready"
        Log.d("S1BleBridge", "${device.name} : Ready")
        runRtTask()
    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {
        state = false
        model.deviceName.value = device.name
        model.connectStateStr.value = "Disconnecting"
        Log.d("S1BleBridge", "${device.name} : Disconnecting")
    }

    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        state = false
        model.deviceName.value = device.name
        model.connectStateStr.value = "Disconnected"
        Log.d("S1BleBridge", "${device.name} : Disconnected")

    }
}