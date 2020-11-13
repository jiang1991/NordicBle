package com.lepu.nordicble

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.LogUtils
import com.lepu.nordicble.fragments.KcaFragment
import com.lepu.nordicble.objs.Bluetooth
import com.lepu.nordicble.objs.Bluetooth.*
import com.lepu.nordicble.objs.BluetoothController
import com.lepu.nordicble.objs.Const
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var index = 0

    private val PERMISSION_REQUEST_CODE = 521

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Const.context = this

        iniUI()
        iniBLE()
        if (requestLocation()) {
            requestPermission()
        }

    }

    private fun iniUI() {
//        val search = findViewById<TextView>(R.id.search)
//        search.setOnClickListener { startDiscover() }
//
//        val connect = findViewById<TextView>(R.id.connect)
//        connect.setOnClickListener {
//            val list = BluetoothController.getDevices(MODEL_ER1)
//            if (list.size >= index) {
//                connect(list[index])
//            }
//        }

        val bind_device = findViewById<TextView>(R.id.bind_device)

        bind_device.setOnClickListener {
            // show module pick dialog

//            val dialog: AlertDialog = AlertDialog.Builder(this)
//                .setTitle(R.string.add_module)
//                .setItems(R.array.modules) { dialog, which ->
//                    LogUtils.d("clicked $which")
//                }
//                .create()
//            dialog.show()

            startDiscover()
        }
    }

    fun connect(bluetooth: Bluetooth) {
        stopDiscover()
        val observer = BleInterface()
        observer.connect(this, bluetooth.device)
        index++
    }

    fun addKcaFragment(b: Bluetooth) {
        val kcaFragment = KcaFragment.newInstance(b)
        val trans = supportFragmentManager.beginTransaction()
        trans.add(R.id.container_1, kcaFragment)
        trans.commit()
    }

    private fun requestLocation() : Boolean {
        /**
         * 检查是否开启location
         */
        val lm = getSystemService(LOCATION_SERVICE) as LocationManager
        val enable = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        LogUtils.d("location enable: $enable")
        return enable
    }

    private fun requestPermission() {
        val ps : Array<String> = arrayOf (
//                Manifest.permission.ACCESS_WIFI_STATE,
//                Manifest.permission.CHANGE_WIFI_STATE,
//                Manifest.permission.ACCESS_NETWORK_STATE,
//                Manifest.permission.READ_PHONE_STATE,
//                Manifest.permission.READ_EXTERNAL_STORAGE,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                Manifest.permission.CAMERA,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        )

        for (p  in ps) {
            if (!checkP(p)) {
                ActivityCompat.requestPermissions(this, ps, PERMISSION_REQUEST_CODE)
                return
            }
        }

        permissionFinished()
    }

    private fun checkP(p: String) : Boolean {
        return ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED
    }

    private fun permissionFinished() {

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
            if (model == MODEL_UNRECOGNIZED) {
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
            }

            if (b.model == MODEL_KCA) {
                addKcaFragment(b)
                stopDiscover()
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
}