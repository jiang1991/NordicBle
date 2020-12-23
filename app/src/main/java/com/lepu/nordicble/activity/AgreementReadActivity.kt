package com.lepu.nordicble.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.lepu.nordicble.R
import kotlinx.android.synthetic.main.activity_agreement.*
import kotlinx.android.synthetic.main.activity_bind.*

class AgreementReadActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agreement_read)

        iniUI()
    }

    private fun iniUI() {

        agreement.loadUrl("file:///android_asset/agreement.html")

        action_back.setOnClickListener {
            this.finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }
}