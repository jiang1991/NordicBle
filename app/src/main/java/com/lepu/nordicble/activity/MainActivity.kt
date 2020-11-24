package com.lepu.nordicble.activity

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.LogUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.nordicble.R
import com.lepu.nordicble.ble.BleService
import com.lepu.nordicble.const.BleConst
import com.lepu.nordicble.fragments.Er1Fragment
import com.lepu.nordicble.fragments.KcaFragment
import com.lepu.nordicble.fragments.OxyFragment
import com.lepu.nordicble.objs.Bluetooth
import com.lepu.nordicble.objs.Const
import com.lepu.nordicble.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_CODE = 521

    lateinit var bleService: BleService

    lateinit var er1Fragment: Er1Fragment
    lateinit var oxyFragment: OxyFragment
    lateinit var kcaFragment: KcaFragment

    private val mainModel : MainViewModel by viewModels()

    private val bleConn = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            bleService = (p1 as BleService.BleBinder).getService()
            er1Fragment.initService(bleService)
            oxyFragment.initService(bleService)
            kcaFragment.initService(bleService)
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            TODO("Not yet implemented")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Const.context = this

        observeLiveDataBus()

        iniUI()

        if (requestLocation()) {
            requestPermission()
        }

        initService()
    }

    /**
     * add LiveDataBus observer
     * bind device
     */
    private fun observeLiveDataBus() {
        LiveEventBus.get(BleConst.EventBindEr1Device)
                .observe(this, {
                    mainModel.er1Bluetooth.value = it as Bluetooth
                    mainModel.er1DeviceName.value = it.name
                })
        LiveEventBus.get(BleConst.EventBindO2Device)
                .observe(this, {
                    mainModel.oxyBluetooth.value = it as Bluetooth
                    mainModel.oxyDeviceName.value = it.name
                })
        LiveEventBus.get(BleConst.EventBindKcaDevice)
                .observe(this, {
                    mainModel.kcaBluetooth.value = it as Bluetooth
                    mainModel.kcaDeviceName.value = it.name
                })
    }

    private fun iniUI() {

        //todo: read saved devices

        bind_device.setOnClickListener {
            val intent = Intent(this, BindActivity::class.java)
            startActivity(intent)
        }

        addKcaFragment()
        addO2Fragment()
        addEr1Fragment()
    }

    private fun initService() {
        BleService.startService(this)

        Intent(this, BleService::class.java).also {
            intent -> bindService(intent, bleConn, Context.BIND_AUTO_CREATE)
        }
    }


    private fun addKcaFragment() {
        kcaFragment = KcaFragment.newInstance()
        val trans = supportFragmentManager.beginTransaction()
        trans.add(R.id.container_3, kcaFragment)
        trans.commitAllowingStateLoss()
    }

    private fun addO2Fragment() {
        oxyFragment = OxyFragment.newInstance()
        val trans = supportFragmentManager.beginTransaction()
        trans.add(R.id.container_2, oxyFragment)
        trans.commitAllowingStateLoss()
    }

    private fun addEr1Fragment() {
        er1Fragment = Er1Fragment.newInstance()
        val trans = supportFragmentManager.beginTransaction()
        trans.add(R.id.container_1, er1Fragment)
        trans.commitAllowingStateLoss()
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


}