package com.lepu.nordicble.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.lepu.nordicble.BuildConfig
import com.lepu.nordicble.R
import com.lepu.nordicble.utils.readAgreementConfig
import com.lepu.nordicble.utils.saveAgreementConfig
import kotlinx.android.synthetic.main.activity_agreement.*

class AgreementActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agreement)

        initUI()
    }

    private fun initUI() {
        agreement.loadUrl("file:///android_asset/agreement.html")

        agree.setOnClickListener {

            val builder = AlertDialog.Builder(this)
            builder.setTitle("用户协议")
            builder.setMessage("我已阅读并同意乐普安心宝APP用户服务协议")
            builder.setPositiveButton("确认") {_, _ ->
                saveAgreementConfig(this, true)
                jumpToNext()
            }
            builder.setNegativeButton("取消") {dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }

        if (BuildConfig.FLAVOR != "anxin") {
            jumpToNext()
        }

        if (readAgreementConfig(this)) {
            jumpToNext()
        }
    }

    private fun jumpToNext() {
        val i = Intent(this, PermissionActivity::class.java)
        startActivity(i)
        this.finish()
    }
}