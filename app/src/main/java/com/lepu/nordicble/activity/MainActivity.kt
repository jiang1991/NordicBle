package com.lepu.nordicble.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.LogUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.nordicble.R
import com.lepu.nordicble.const.BleConst
import com.lepu.nordicble.fragments.Er1Fragment
import com.lepu.nordicble.fragments.KcaFragment
import com.lepu.nordicble.fragments.OxyFragment
import com.lepu.nordicble.fragments.S1Fragment
import com.lepu.nordicble.objs.Bluetooth
import com.lepu.nordicble.objs.Const
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var index = 0

    private val PERMISSION_REQUEST_CODE = 521

    lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Const.context = this

        observeLiveDataBus()

        iniUI()

        if (requestLocation()) {
            requestPermission()
        }

    }

    /**
     * add LiveDataBus observer
     * bind device
     */
    private fun observeLiveDataBus() {
        LiveEventBus.get(BleConst.EventBindEr1Device)
                .observe(this, {
                    addEr1Fragment(it as Bluetooth)
                    LogUtils.d("observeLiveDataBus: ${it.name}")
                })
        LiveEventBus.get(BleConst.EventBindO2Device)
                .observe(this, {
                    addO2Fragment(it as Bluetooth)
                    LogUtils.d("observeLiveDataBus: ${it.name}")
                })
        LiveEventBus.get(BleConst.EventBindKcaDevice)
                .observe(this, {
                    addKcaFragment(it as Bluetooth)
                    LogUtils.d("observeLiveDataBus: ${it.name}")
                })
        LiveEventBus.get(BleConst.EventBindS1ScaleDevice)
                .observe(this, {
                    addS1Fragment(it as Bluetooth)
                    LogUtils.d("observeLiveDataBus: ${it.name}")
                })
    }

    private fun iniUI() {

        bind_device.setOnClickListener {
            val intent = Intent(this, BindActivity::class.java)
            startActivity(intent)
        }
    }

    private fun addS1Fragment(b: Bluetooth) {
        val fragment = S1Fragment.newInstance(b)
        val trans = supportFragmentManager.beginTransaction()
        trans.add(R.id.container_4, fragment)
        trans.commitAllowingStateLoss()
//        BleModuleController.addFragment(fragment)
    }


    private fun addKcaFragment(b: Bluetooth) {
        val fragment = KcaFragment.newInstance(b)
        val trans = supportFragmentManager.beginTransaction()
        trans.add(R.id.container_3, fragment)
        trans.commitAllowingStateLoss()
//        BleModuleController.addFragment(fragment)
    }

    private fun addO2Fragment(b: Bluetooth) {
        val fragment = OxyFragment.newInstance(b)
        val trans = supportFragmentManager.beginTransaction()
        trans.add(R.id.container_2, fragment)
        trans.commitAllowingStateLoss()
//        BleModuleController.addFragment(fragment)
    }

    private fun addEr1Fragment(b: Bluetooth) {
        val fragment = Er1Fragment.newInstance(b)
        val trans = supportFragmentManager.beginTransaction()
        trans.add(R.id.container_1, fragment)
        trans.commitAllowingStateLoss()
//        BleModuleController.addFragment(fragment)
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