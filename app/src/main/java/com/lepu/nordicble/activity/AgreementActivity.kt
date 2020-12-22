package com.lepu.nordicble.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
            saveAgreementConfig(this, true)
            jumpToNext()
        }

        if (getString(R.string.key_need_agreement) == "false") {
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