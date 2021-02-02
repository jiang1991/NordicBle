package com.lepu.anxin.activity

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.arialyy.annotations.Download
import com.arialyy.aria.core.task.DownloadTask
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.LogUtils
import com.lepu.anxin.BuildConfig
import com.lepu.anxin.R
import com.lepu.anxin.annotation.CheckVersionType
import com.lepu.anxin.utils.NetObserver
import kotlinx.android.synthetic.main.activity_main.*
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
            val intent = Intent(this, AgreementReadActivity::class.java)
            startActivity(intent)
        }

        rl_check_update.setOnClickListener {
            var app = ""
            app = CheckVersionType.ANXINBAO
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