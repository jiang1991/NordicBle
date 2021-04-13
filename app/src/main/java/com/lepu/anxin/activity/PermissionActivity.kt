package com.lepu.anxin.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.LogUtils
import com.lepu.anxin.R
import com.lepu.anxin.vals.lastRestartBt
import com.lepu.anxin.vals.relayId
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PermissionActivity : AppCompatActivity() {

    private val permissionRequestCode = 521

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)

        readRelayId()
        requestLocation()
    }

    private fun requestLocation() {
        /**
         * 检查是否开启location
         */
        val lm = getSystemService(LOCATION_SERVICE) as LocationManager
        val enable = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        LogUtils.d("location enable: $enable")

        if (!enable) {
            Toast.makeText(this, "请打开手机定位", Toast.LENGTH_SHORT).show()
        } else {
            requestPermission()
        }
    }

    private fun requestPermission() {
        val ps : Array<String> = arrayOf (
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        for (p  in ps) {
            if (!checkP(p)) {
                ActivityCompat.requestPermissions(this, ps, permissionRequestCode)
                return
            }
        }

        permissionFinished()
    }

    private fun checkP(p: String) : Boolean {
        return ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED
    }

    private fun permissionFinished() {
        checkBt()
    }

    private fun checkBt() {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) {
            Toast.makeText(this, "不支持蓝牙", Toast.LENGTH_SHORT).show()
            return
        }

        lastRestartBt = System.currentTimeMillis()
        if (!adapter.isEnabled) {
            adapter.enable()
        }
//        val i = Intent(this, MainActivity::class.java)
        val i = Intent(this, UserInfoActivity::class.java)
        startActivity(i)
    }


    @SuppressLint("MissingPermission")
    private fun readRelayId() {
        val tm : TelephonyManager = this.getSystemService(Service.TELEPHONY_SERVICE) as TelephonyManager
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            tm.deviceId?.apply {
                relayId = this.takeLast(6)
            }
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            tm.imei?.apply {
                relayId = this.takeLast(6)
            }
        }
        LogUtils.d("Relay ID: $relayId")
    }

    override fun onResume() {
        super.onResume()
        requestLocation()
    }
}