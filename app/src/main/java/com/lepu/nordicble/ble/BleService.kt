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
import android.os.IBinder
import android.text.TextUtils
import com.blankj.utilcode.util.LogUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.nordicble.vals.EventMsgConst
import com.lepu.nordicble.objs.Bluetooth
import com.lepu.nordicble.objs.BluetoothController
import com.lepu.nordicble.utils.*
import com.lepu.nordicble.vals.er1Name
import com.lepu.nordicble.vals.kcaName
import com.lepu.nordicble.vals.oxyName
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
        leScanner = bluetoothAdapter.bluetoothLeScanner
    }

    private fun initInterfaces() {
        er1Interface = Er1BleInterface()
        oxyInterface = OxyBleInterface()
        kcaInterface = KcaBleInterface()
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
    }

    /**
     * search
     */
    public fun startDiscover() {
        stopDiscover()
        BluetoothController.clear()
        LogUtils.d("start discover")
        isDiscovery = true
        scanDevice(true)
        Timer().schedule(20000) {
            stopDiscover()
            checkNeedAutoScan()
        }
    }

    public fun stopDiscover() {
        LogUtils.d("stop discover")
        isDiscovery = false
        scanDevice(false)
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
                        .postAcrossProcess(b)

                if (b.name == er1Name) {
                    er1Interface.connect(this@BleService, b.device)
                }
                if (b.name == oxyName) {
                    oxyInterface.connect(this@BleService, b.device)
                }
                if (b.name == kcaName) {
                    kcaInterface.connect(this@BleService, b.device)
                }
            }

        }

        override fun onBatchScanResults(results: List<ScanResult>) { //
        }

        override fun onScanFailed(errorCode: Int) {
            LogUtils.d("scan error: $errorCode")
            if (errorCode == SCAN_FAILED_ALREADY_STARTED) {
                LogUtils.d("already start")
            }
            if (errorCode == SCAN_FAILED_FEATURE_UNSUPPORTED) {
                LogUtils.d("scan settings not supported")
            }
            if (errorCode == 6) {
                LogUtils.d("too frequently")
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