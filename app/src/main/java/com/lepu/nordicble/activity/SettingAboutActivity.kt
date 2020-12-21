package com.lepu.nordicble.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.arialyy.annotations.Download
import com.arialyy.aria.core.task.DownloadTask
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.LogUtils
import com.lepu.nordicble.R
import com.lepu.nordicble.utils.NetObserver
import kotlinx.android.synthetic.main.activity_setting_about.*


class SettingAboutActivity : AppCompatActivity() {

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

        LogUtils.i("downloadingApk > ${task.percent}")

    }

    @Download.onTaskComplete
    fun downloadCompleteApk(task: DownloadTask) {

        LogUtils.i("downloadCompleteApk ->"+ task.filePath)
        AppUtils.installApp(task.filePath)

    }


    private fun initView() {
        action_back.setOnClickListener {
            this.finish()
        }

        rl_disclaimer.setOnClickListener {

        }

        rl_check_update.setOnClickListener {
            netObserver?.checkVersion()
        }


        tv_app_version.text = AppUtils.getAppName().toString()
    }

    private fun addLiveDataObserver() {

    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }
}