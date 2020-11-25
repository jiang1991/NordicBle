package com.lepu.nordicble.activity

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import com.afollestad.materialdialogs.MaterialDialog
import com.blankj.utilcode.util.ToastUtils
import com.lepu.nordicble.R
import com.lepu.nordicble.utils.readHostConfig
import com.lepu.nordicble.utils.saveHostConfig
import com.lepu.nordicble.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.activity_search.action_back
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() {

    private val mainModel : MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        initView()
        addLiveDataObserver()

    }

    private fun addLiveDataObserver() {
        mainModel.hostIp.observe(this, {
            et_ip.setText(it)
        })
        mainModel.hostPort.observe(this, {
            et_port.setText(it.toString())
        })
    }

    private fun initView() {
        action_back.setOnClickListener {
            this.finish()
        }

        setting_save.setOnClickListener {
            save()
        }

        val pm = this.packageManager
        tv_version.text = pm.getPackageInfo(this.packageName, 0).versionName.toString()

        val (ip, port) = readHostConfig(this)
        ip?.apply {
            et_ip.setText(ip)
        }
        port?.apply {
            et_port.setText(port.toString())
        }
    }

    private fun save() {
        val imm = this@SettingActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(et_ip.windowToken, 0)
        imm.hideSoftInputFromWindow(et_port.windowToken, 0)
        // 断开当前链接

        // 判断是否有效
        val ip = et_ip.editableText.toString()
        val port = et_port.editableText.toString().toIntOrNull()

//        val pattern = Pattern.compile("((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}")
//        val b = pattern.matcher(ip).matches()
//        if (!b) {
//            MaterialDialog(this).show {
//                title(text = "提醒")
//                message(text = "IP输入不正确")
//                positiveButton(text = "确定") {
//                    dialog -> dialog.dismiss()
//                }
//            }
//            return
//        }

        if (port == null || port > 65535) {
            MaterialDialog(this).show {
                title(text = "提醒")
                message(text = "端口输入不正确")
                positiveButton(text = "确定") {
                        dialog -> dialog.dismiss()
                }
            }
            return
        }

        // 测试 ping

        saveHostConfig(this, ip, port)
//        hostIp = ip
//        hostPort = port
//
//        hostNeedConnect = true

        ToastUtils.showShort("保存成功")

        this@SettingActivity.finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}