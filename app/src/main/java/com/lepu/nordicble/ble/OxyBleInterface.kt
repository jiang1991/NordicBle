package com.lepu.nordicble.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Handler
import androidx.annotation.NonNull
import com.blankj.utilcode.util.LogUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.nordicble.ble.cmd.Er1BleCRC
import com.lepu.nordicble.ble.cmd.OxyBleCmd
import com.lepu.nordicble.ble.cmd.OxyBleResponse
import com.lepu.nordicble.ble.obj.OxyDataController
import com.lepu.nordicble.objs.Bluetooth
import com.lepu.nordicble.utils.add
import com.lepu.nordicble.utils.toHex
import com.lepu.nordicble.utils.toUInt
import com.lepu.nordicble.vals.EventMsgConst
import com.lepu.nordicble.viewmodel.OxyViewModel
import kotlinx.coroutines.*
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.observer.ConnectionObserver
import java.lang.Runnable
import java.util.*
import kotlin.concurrent.schedule
import kotlin.experimental.inv

class OxyBleInterface(context: Context) : ConnectionObserver, OxyBleManager.onNotifyListener {

    private lateinit var model: OxyViewModel
    fun setViewModel(viewModel: OxyViewModel) {
        this.model = viewModel
    }

    private var curCmd: Int = 0
    private var timeout: Job? = null

    private val mContext = context
    lateinit var manager: OxyBleManager
    var mydevice: BluetoothDevice? = null

    /**
     * last response received
     * > 10 min => disconnect
     */
    private var lastResponseReceived = 0L

    private var pool: ByteArray? = null
    private var count: Int = 0

    private val rtHandler = Handler()
    inner class RtTask: Runnable {
        override fun run() {

            count++
//            LogUtils.d("RtTask: $count")

            if (state) {
                rtHandler.postDelayed(this, 1000)
                getRtData()
            }
        }
    }

    public var state = false
    private var connecting = false

    public fun connect(context: Context, @NonNull device: BluetoothDevice) {
        if (connecting || state) {
            return
        }
        LogUtils.d("try connect: ${device.name}")
        disconnect()
        manager = OxyBleManager(mContext)
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

//    var timeoutCount: Int = 0
    private fun sendCmd(cmd: Int, bs: ByteArray) {

        if (!state) {
            return
        }

        if (lastResponseReceived != 0L) {
            if (System.currentTimeMillis() - lastResponseReceived > 10*60*1000) {
                disconnect()
                return
            }
        }
//        LogUtils.d("try send cmd: $cmd, ${bs.toHex()}")
        if (curCmd != 0) {
            // busy
            LogUtils.d("busy: $cmd =>$curCmd")
            return
        }

//        if (timeoutCount >= 3) {
//            disconnect()
//            return
//        }

        curCmd = cmd
        pool = null
        manager.sendCmd(bs)
        timeout = GlobalScope.launch {
            delay(3000)
            // timeout
//            timeoutCount++
            LogUtils.d("timeout: $curCmd")
            when(curCmd) {

                OxyBleCmd.OXY_CMD_PARA_SYNC -> {
                    curCmd = 0
                    getInfo()
                }

                OxyBleCmd.OXY_CMD_INFO -> {
                    curCmd = 0
                    getInfo()
                }
//                OxyBleCmd.OXY_CMD_RT_DATA -> {
//                    curCmd = 0
//                }
                else -> {
                    curCmd = 0
                }
            }
        }
    }

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: OxyBleResponse.OxyResponse) {
//        LogUtils.d("Response: $curCmd, ${response.content.toHex()}")
        if (curCmd == 0) {
            return
        }

        lastResponseReceived = System.currentTimeMillis()

        when(curCmd) {
            OxyBleCmd.OXY_CMD_PARA_SYNC -> {
                clearTimeout()

                getInfo()
            }

            OxyBleCmd.OXY_CMD_INFO -> {
                if (response.content.size < 20) {
                    return
                }
                clearTimeout()

                val info = OxyBleResponse.OxyInfo(response.content)
                model.info.postValue(info)

                LiveEventBus.get(EventMsgConst.EventOxyInfo)
                    .post(info)
//                model.battery.value = info.battery
                runRtTask()
//                downloadFiles(oxyInfo = info)
            }

            OxyBleCmd.OXY_CMD_RT_DATA -> {
                clearTimeout()

                val rtWave = OxyBleResponse.RtWave(response.content)
                model.battery.postValue(rtWave.battery)
                model.pr.postValue(rtWave.pr)
                model.spo2.postValue(rtWave.spo2)
//                model.pi.value = rtWave.pi / 10.0f

//                LogUtils.d(response.content.toHex(), "battery: ${rtWave.battery}")
                OxyDataController.receive(rtWave.wFs)
//                LogUtils.d("Oxy Controller: ${OxyDataController.dataRec.size}")

                LiveEventBus.get(EventMsgConst.EventOxyRtData)
                    .post(rtWave)
            }
            OxyBleCmd.OXY_CMD_READ_START -> {
                clearTimeout()
                if (response.state) {
                    val fileSize = toUInt(response.content)
                    curFile = OxyBleResponse.OxyFile(curFileName!!, fileSize)
                    sendCmd(OxyBleCmd.OXY_CMD_READ_CONTENT, OxyBleCmd.readFileContent())
                } else {
                    LogUtils.d("读文件失败：${response.content.toHex()}")
                }
            }

            OxyBleCmd.OXY_CMD_READ_CONTENT -> {
                clearTimeout()
                curFile?.apply {
                    this.addContent(response.content)
                    LogUtils.d("读文件：${curFile?.fileName}   => ${curFile?.index} / ${curFile?.fileSize}")
                    if (this.index < this.fileSize) {
                        sendCmd(OxyBleCmd.OXY_CMD_READ_CONTENT, OxyBleCmd.readFileContent())
                    } else {
                        sendCmd(OxyBleCmd.OXY_CMD_READ_END, OxyBleCmd.readFileEnd())
                    }
                }
            }
            OxyBleCmd.OXY_CMD_READ_END -> {
                clearTimeout()
                LogUtils.d("读文件完成: ${curFile?.fileName} ==> ${curFile?.fileSize}")
                curFileName = null
                curFile = null
            }

            else -> {
                clearTimeout()
            }
        }
    }

    private fun clearVar() {
//        model.battery.value = 0
        model.pr.postValue(0)
        model.spo2.postValue(0)
//        model.pi.value = 0.0f
    }

    private fun clearTimeout() {
        curCmd = 0
//        timeoutCount = 0
        timeout?.cancel()
        timeout = null
    }

    @ExperimentalUnsignedTypes
    fun hasResponse(bytes: ByteArray?) : ByteArray? {
        val bytesLeft: ByteArray? = bytes

        if (bytes == null || bytes.size < 8) {
            return bytes
        }

        loop@ for (i in 0 until bytes.size-7) {
            if (bytes[i] != 0x55.toByte() || bytes[i+1] != bytes[i+2].inv()) {
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
                val bleResponse = OxyBleResponse.OxyResponse(temp)
//                Log.d(TAG, "get response: " + temp.toHex())
                onResponseReceived(bleResponse)

                val tempBytes: ByteArray? = if (i+8+len == bytes.size) null else bytes.copyOfRange(i+8+len, bytes.size)

                return hasResponse(tempBytes)
            }
        }

        return bytesLeft
    }

    fun disconnect() {

        mydevice?.apply {
            manager.disconnect()
            manager.close()

            onDeviceDisconnected(this, ConnectionObserver.REASON_SUCCESS)
            mydevice = null
        }
    }

    fun syncTime() {
        sendCmd(OxyBleCmd.OXY_CMD_PARA_SYNC, OxyBleCmd.syncTime())
    }

    fun getInfo() {
        sendCmd(OxyBleCmd.OXY_CMD_INFO, OxyBleCmd.getInfo())
    }

    fun getRtData() {
        sendCmd(OxyBleCmd.OXY_CMD_RT_DATA, OxyBleCmd.getRtWave())
    }

    fun runRtTask() {
        rtHandler.postDelayed(RtTask(), 200)
    }

    var curFileName: String? = null
    var curFile: OxyBleResponse.OxyFile? = null
    fun readFile(fileName: String) {
        curFileName = fileName
        sendCmd(OxyBleCmd.OXY_CMD_READ_START, OxyBleCmd.readFileStart(fileName))
    }

    private fun downloadFiles(oxyInfo: OxyBleResponse.OxyInfo) {
        val files = oxyInfo.fileList.split(",")
        if (files.isNotEmpty()) {
            readFile(files[0])
        }
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
        LogUtils.d("${device.name} connected")
        state = true
        model.connect.postValue(state)

        connecting = false
    }

    override fun onDeviceConnecting(device: BluetoothDevice) {
        LogUtils.d("${device.name} Connecting")
        state = false
        model.connect.postValue(state)

        connecting = true
    }

    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        LogUtils.d("${device.name} Disconnected")
        state = false
        model.connect.postValue(state)
        curCmd = 0
        rtHandler.removeCallbacks(RtTask())

        clearVar()

        connecting = false

        LiveEventBus.get(EventMsgConst.EventDeviceDisconnect).post(Bluetooth.MODEL_CHECKO2)
    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {
        LogUtils.d("${device.name} Disconnecting")
        state = false
        model.connect.postValue(state)
//        LogUtils.d(mydevice.name)

        connecting = false
    }

    override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
        LogUtils.d("${device.name} FailedToConnect")
        state = false
        model.connect.postValue(state)

        connecting = false
    }

    override fun onDeviceReady(device: BluetoothDevice) {
//        runRtTask()
        LogUtils.d("${device.name} isReady")
        curCmd = 0

        connecting = false
        Timer().schedule(500) {
            syncTime()
        }
    }
}