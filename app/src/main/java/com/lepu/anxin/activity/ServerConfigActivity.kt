package com.lepu.anxin.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.LogUtils
import com.google.gson.Gson
import com.lepu.anxin.R
import com.lepu.anxin.objs.ServerConfig
import com.lepu.anxin.retrofit.RetrofitManager
import com.lepu.anxin.retrofit.post.GetDepartmentList
import com.lepu.anxin.retrofit.post.RegisterDeviceUser
import com.lepu.anxin.retrofit.response.isSuccess
import com.lepu.anxin.vals.deviceUserId
import com.lepu.anxin.vals.relayId
import com.lepu.anxin.viewmodel.AppViewModel
import com.lepu.anxin.viewmodel.ServerConfigViewModel
import com.yzq.zxinglibrary.android.CaptureActivity
import com.yzq.zxinglibrary.bean.ZxingConfig
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_server_config.*


class ServerConfigActivity : AppCompatActivity() {

    private val model : ServerConfigViewModel by viewModels()
    lateinit var appViewModel: AppViewModel

    private val REQUEST_CODE_SCAN = 988

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server_config)

        appViewModel = ViewModelProvider(this,
            ViewModelProvider.AndroidViewModelFactory(application)).get(AppViewModel::class.java)

        addLiveDataObserver()

        initView()

//        checkServerConfig()
    }

    private fun initView() {
        qrcode_scan.setOnClickListener {
            val intent = Intent(this, CaptureActivity::class.java)
            val config = ZxingConfig()
            config.isPlayBeep = true //是否播放扫描声音 默认为true
            config.isShake = true //是否震动  默认为true
            config.isDecodeBarCode = false //是否扫描条形码 默认为true
            config.isFullScreenScan = true //是否全屏扫描  默认为true  设为false则只会在扫描框中扫描

            intent.putExtra(com.yzq.zxinglibrary.common.Constant.INTENT_ZXING_CONFIG, config)
            startActivityForResult(intent, REQUEST_CODE_SCAN)
        }
    }

    private fun checkServerConfig() {
        LogUtils.d()
        appViewModel.serverConfig.value?.apply {
            if (this.host != "") {
                // init retrofit
                appViewModel.initServer(this.host)
                registerDevice()
            }
            if (this.doctorId != "") {
                getDepartmentList(this.doctorId)
            }
        }
    }

    /**
     * interface
     * 注册设备
     */
    @SuppressLint("CheckResult")
    private fun registerDevice() {
        val registerDeviceUser = RegisterDeviceUser(relayId)
        appViewModel.server.registerRelay(
            RetrofitManager.header(registerDeviceUser),
            registerDeviceUser
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    if (it.isSuccess()) {
                        it.data?.apply {
                            com.lepu.anxin.vals.deviceUserId = this.deviceUserId
                            appViewModel.saveDeviceUserId(this.deviceUserId)
//                            toNext()
                        }
                        LogUtils.d(it.data?.deviceUserId, it.data?.token)

                    }
                },
                {
                    LogUtils.d(it.toString())
                    Toast.makeText(this@ServerConfigActivity, "网络错误，请重新扫描二维码", Toast.LENGTH_SHORT).show()
                }
            )
    }

    /**
     * interface
     * 获取科室列表
     */
    @SuppressLint("CheckResult")
    private fun getDepartmentList(doctorId: String) {
        val getList = GetDepartmentList(doctorId)
        appViewModel.server.queryDepartment(
            RetrofitManager.header(getList),
            getList
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    if (it.isSuccess()) {
                        LogUtils.d(it)
                    }
                },
                {
                    LogUtils.d(it.toString())
                    Toast.makeText(this@ServerConfigActivity, "获取科室列表错误", Toast.LENGTH_SHORT).show()
                }
            )
    }

    /**
     * interface
     * 登记患者
     */
    @SuppressLint("CheckResult")
    private fun registerPatient(){
//        val patient =
//        appViewModel.server.registerPatient()(
//            RetrofitManager.header(),
//
//        ).subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe(
//                {
//                    if (it.isSuccess()) {
//                        LogUtils.d(it)
//                    }
//                },
//                {
//                    LogUtils.d(it.toString())
//                    Toast.makeText(this@ServerConfigActivity, "获取科室列表错误", Toast.LENGTH_SHORT).show()
//                }
//            )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            data?.apply {
                val s = data.getStringExtra(com.yzq.zxinglibrary.common.Constant.CODED_CONTENT)
                LogUtils.d(s)
                val config: ServerConfig = Gson().fromJson(s, ServerConfig::class.java)
                if (config.host != "") {
                    appViewModel.saveServerConfig(
                        config.host,
                        config.port,
                        config.doctorId,
                        config.doctorName
                    )
//                    model.ip.value = config.host
//                    model.port.value = config.port
//                    model.doctorName.value = config.doctorName
//                    model.doctorId.value = config.doctorId

                } else {
                    Toast.makeText(this@ServerConfigActivity, "无效二维码", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun addLiveDataObserver() {
        model.port.observe(this, {
            guard_port.text = it
        })
        model.ip.observe(this, {
            guard_ip.text = it
        })
        model.doctorName.observe(this, {
            guard_doctor.text = it
        })
        model.doctorId.observe(this, {

        })

        appViewModel.serverConfig.observe(this, {
            guard_port.text = it.port
            guard_ip.text = it.host
            guard_doctor.text = it.doctorName

            LogUtils.d(appViewModel.serverConfig.value!!.host)
            checkServerConfig()

//            if (it.deviceId.isNotEmpty()) {
//                deviceUserId = it.deviceId
////                toNext()
//            } else {
//                it.host.isNotEmpty().apply {
//                    checkServerConfig()
//                }
//            }
        })
    }

    private fun toNext() {
        val i = Intent(this, UserInfoActivity::class.java)
        startActivity(i)
    }

    override fun onBackPressed() {
    }
}