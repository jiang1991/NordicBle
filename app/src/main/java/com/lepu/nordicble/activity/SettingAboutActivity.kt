package com.lepu.nordicble.activity

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.arialyy.annotations.Download
import com.arialyy.aria.core.task.DownloadTask
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.LogUtils
import com.lepu.nordicble.BuildConfig
import com.lepu.nordicble.R
import com.lepu.nordicble.annotation.CheckVersionType
import com.lepu.nordicble.utils.NetObserver
import com.lepu.nordicble.views.NormalDialog
import kotlinx.android.synthetic.main.activity_setting_about.*


class SettingAboutActivity : AppCompatActivity() {

    private val downloadDialog by lazy {
        ProgressDialog(this)
    }
    private var netObserver: NetObserver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_about)

        netObserver = NetObserver(this)

        addLiveDataObserver()

        initView()
    }


    @Download.onTaskRunning
    fun downloadingApk(task: DownloadTask) {


        downloadDialog.setMessage("下载进度->${task.percent}%")

        if (downloadDialog.isShowing.not()) {
            downloadDialog.show()
        }
    }

    @Download.onTaskComplete
    fun downloadCompleteApk(task: DownloadTask) {

        LogUtils.i("downloadCompleteApk ->" + task.filePath)

        downloadDialog.takeIf { it.isShowing }?.let { it.dismiss() }
        AppUtils.installApp(task.filePath)
    }


    private fun initView() {
        action_back.setOnClickListener {
            this.finish()
        }

        rl_disclaimer.setOnClickListener {

        }

        rl_check_update.setOnClickListener {
            var app = ""
            if (BuildConfig.FLAVOR == "anxin") {
                app = CheckVersionType.ANXINBAO
            } else if (BuildConfig.FLAVOR == "wireless") {
                app = CheckVersionType.WIRELESS
            }
            netObserver?.checkVersion(app)
        }


        tv_app_version.text = AppUtils.getAppVersionName().toString()
    }

    private fun addLiveDataObserver() {

    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }
}