package com.lepu.nordicble.ble

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.text.TextUtils
import com.blankj.utilcode.util.LogUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.nordicble.objs.Bluetooth
import com.lepu.nordicble.objs.BluetoothController
import com.lepu.nordicble.objs.Const
import com.lepu.nordicble.utils.*
import com.lepu.nordicble.vals.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.schedule

class BleService : Service() {

    override fun onCreate() {
        super.onCreate()

        initBle()
        initInterfaces()
    }

    private fun initBle() {
        val bluetoothManager =
            getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter.bluetoothLeScanner != null) {
            leScanner = bluetoothAdapter.bluetoothLeScanner
        }
    }

    private fun initInterfaces() {
        er1Interface = Er1BleInterface(applicationContext)
        oxyInterface = OxyBleInterface(applicationContext)
        kcaInterface = KcaBleInterface(applicationContext)
    }

    private val binder = BleBinder()

    /**
     * ble interfaces
     * manage all ble client
     */
    lateinit var er1Interface: Er1BleInterface
    lateinit var oxyInterface: OxyBleInterface
    lateinit var kcaInterface: KcaBleInterface

    /**
     * check auto scan
     * 有未连接的绑定设备则继续搜索
     */
    public fun checkNeedAutoScan() {
        var reScan = false
        if (er1Name != null && !er1Interface.state) {
            reScan = true
        }
        if (oxyName != null && !oxyInterface.state) {
            reScan = true
        }
        if (kcaName != null && !kcaInterface.state) {
            reScan = true
        }
        if (reScan) {
            startDiscover()
        }

        LogUtils.d(
            "$er1Name => ${er1Interface.state}",
            "$oxyName => ${oxyInterface.state}",
            "$kcaName => ${kcaInterface.state}",
            "ReScan: $reScan"
        )

        if ((!er1Interface.state && !oxyInterface.state && !kcaInterface.state) && (er1Name != null || oxyName != null || kcaName != null)) {
            MyCrashHandler.saveImportantLog("$er1Name  $oxyName  $kcaName 全断开")
            if (System.currentTimeMillis() - lastRestartBt > 10*60*1000) {
                restartBt()
            }
        }
    }

    private fun restartBt() {
        MyCrashHandler.saveImportantLog("restart BT")
        lastRestartBt = System.currentTimeMillis()
        bluetoothAdapter.disable()
        Timer().schedule(2000) {
            bluetoothAdapter.enable()
        }
    }

    /**
     * search
     */
    public fun startDiscover() {
//        stopDiscover()
        if (isDiscovery) {
            return
        }
        BluetoothController.clear()
        LogUtils.d("start discover")
        isDiscovery = true
        scanDevice(true)

        Timer().schedule(20000) {
            stopDiscover()
        }
    }

    public fun stopDiscover() {
        LogUtils.d("stop discover")
        isDiscovery = false
        scanDevice(false)
//        checkNeedAutoScan()
    }

    private var isDiscovery : Boolean = false
    private lateinit var bluetoothAdapter : BluetoothAdapter
    private lateinit var leScanner : BluetoothLeScanner

    private fun scanDevice(enable: Boolean) {
        GlobalScope.launch {
            if (enable) {

                if (bluetoothAdapter.isEnabled) {
                    val settings: ScanSettings = ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .build()
                    //                    List<ScanFilter> filters = new ArrayList<ScanFilter>();
                    //                    filters.add(new ScanFilter.Builder().build());
                    leScanner.startScan(null, settings, leScanCallback)
                } else {
                    bluetoothAdapter.enable()
                }
            } else {
                if (bluetoothAdapter.isEnabled) {
                    leScanner.stopScan(leScanCallback)
                }
            }
        }

    }
    /**
     * lescan callback
     */
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(
            callbackType: Int,
            result: ScanResult
        ) {
            super.onScanResult(callbackType, result)
            val device = result.device
            var deviceName = result.device.name
            val deviceAddress = result.device.address
            if (TextUtils.isEmpty(deviceName)) {
                deviceName = BluetoothController.getDeviceName(deviceAddress)
            }
            @Bluetooth.MODEL val model: Int = Bluetooth.getDeviceModel(deviceName)
            if (model == Bluetooth.MODEL_UNRECOGNIZED) {
                return
            }
            val b = Bluetooth(
                model,  /*ecgResult.getScanRecord().getDeviceName()*/
                deviceName,
                device,
                result.rssi
            )
            if (BluetoothController.addDevice(b)) { // notify
//                LogUtils.d(b.name)
//                ble_list.invalidate()
                LiveEventBus.get(EventMsgConst.EventDeviceFound)
                        .post(b)

                if (b.name == er1Name && !er1Interface.state) {
                    er1Interface.connect(this@BleService, b.device)
                    LogUtils.d("bind ER1 found: ${b.device.name}")
                }
                if (b.name == oxyName && !oxyInterface.state) {
                    oxyInterface.connect(this@BleService, b.device)
                    LogUtils.d("bind Oxy found: ${b.device.name}")
                }
                if (b.name == kcaName && !kcaInterface.state) {
                    kcaInterface.connect(this@BleService, b.device)
                    LogUtils.d("bind Kca found: ${b.device.name}")
                }
            }

        }

        override fun onBatchScanResults(results: List<ScanResult>) { //
        }

        override fun onScanFailed(errorCode: Int) {
            LogUtils.d("scan error: $errorCode")
            when(errorCode) {
                SCAN_FAILED_ALREADY_STARTED -> {
                    LogUtils.d("already start")
                }
                SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> {}
                SCAN_FAILED_FEATURE_UNSUPPORTED -> {
                    LogUtils.d("scan settings not supported")
                }
                6-> {
                    LogUtils.d("too frequently")
                }

            }
        }
    }

    override fun onBind(p0: Intent): IBinder {
        return binder
    }



    inner class BleBinder: Binder() {
        fun getService(): BleService = this@BleService
    }

    companion object {
        @JvmStatic
        fun startService(context: Context) {
            Intent(context, BleService::class.java).also { intent -> context.startService(intent)
            }
        }

        @JvmStatic
        fun stopService(context: Context) {
            val intent = Intent(context, BleService::class.java)
            context.stopService(intent)
        }

    }
}