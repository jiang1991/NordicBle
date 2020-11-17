package com.lepu.nordicble.activity

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.AdapterView
import com.blankj.utilcode.util.LogUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.nordicble.R
import com.lepu.nordicble.const.BleConst
import com.lepu.nordicble.objs.BleAdapter
import com.lepu.nordicble.objs.Bluetooth
import com.lepu.nordicble.objs.BluetoothController
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {

    private lateinit var adapter: BleAdapter
    private var currentModel: Int = Bluetooth.MODEL_ER1
    private lateinit var list : ArrayList<Bluetooth>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        iniBLE()
        currentModel = intent.extras?.get("TYPE") as Int
        setContentView(R.layout.activity_search)
        iniUI()
    }

    private fun iniUI() {
        when(currentModel) {
            Bluetooth.MODEL_ER1 -> {
                toolbar_title.text = getString(R.string.name_er1)
            }
            Bluetooth.MODEL_CHECKO2 -> {
                toolbar_title.text = getString(R.string.name_o2)
            }
            Bluetooth.MODEL_KCA -> {
                toolbar_title.text = getString(R.string.name_kca)
            }
        }

        setAdapter()

        action_back.setOnClickListener {
            this.finish()
        }
        refresh.setOnClickListener {
            startDiscover()
        }

        startDiscover()
    }

    private fun setAdapter() {
        list = BluetoothController.getDevices(currentModel)
        adapter = BleAdapter(this, list)
        ble_list.adapter = adapter
        ble_list.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
//                connect(BluetoothController.getDevices()[position])
                val b = BluetoothController.getDevices(currentModel)[position]
                LogUtils.d(b.name)
                when(currentModel) {
                    Bluetooth.MODEL_ER1 -> {
                        LiveEventBus.get(BleConst.EventBindEr1Device)
                                .postAcrossProcess(b)
                        this.finish()
                    }
                    Bluetooth.MODEL_O2MAX -> {
                        LiveEventBus.get(BleConst.EventBindO2Device)
                                .postAcrossProcess(b)
                        this.finish()
                    }
                    Bluetooth.MODEL_KCA -> {
                        LiveEventBus.get(BleConst.EventBindKcaDevice)
                                .postAcrossProcess(b)
                        this.finish()
                    }
                }
            }

    }

    // search
    var isDiscovery : Boolean = false
    lateinit var bluetoothAdapter : BluetoothAdapter
    lateinit var leScanner : BluetoothLeScanner
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
                LogUtils.d(b.name)
                adapter.deviceList = BluetoothController.getDevices(currentModel)
                adapter.notifyDataSetChanged()
//                ble_list.invalidate()
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

    private fun startDiscover() {
        stopDiscover()
        BluetoothController.clear()
        LogUtils.d("start discover")
        isDiscovery = true
        scanDevice(true)
    }

    fun stopDiscover() {
        LogUtils.d("stop discover")
        isDiscovery = false
        scanDevice(false)

    }

    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000

    fun iniBLE() { // todo: ini ble
        val bluetoothManager =
            getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        leScanner = bluetoothAdapter.bluetoothLeScanner
        //        startDiscover();
    }

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

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopDiscover()
    }
}