package com.lepu.nordicble.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.lepu.nordicble.R
import com.lepu.nordicble.vals.relayId
import com.lepu.nordicble.viewmodel.MainViewModel
import com.yzq.zxinglibrary.encode.CodeCreator
import kotlinx.android.synthetic.main.activity_info.*
import kotlinx.android.synthetic.main.activity_info.action_back

class InfoActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        addLiveDataObserver()

        initView()
    }

    private fun initView() {
        action_back.setOnClickListener {
            this.finish()
        }

        relay_id.text = "收发器编号: $relayId"
        val bitmap = CodeCreator.createQRCode(relayId, 100, 100, null)
        iv_qr.setImageBitmap(bitmap)

        rl_about_container.setOnClickListener {
            Intent(this,SettingAboutActivity::class.java).let {
                startActivity(it)
            }
        }
    }

    private fun addLiveDataObserver() {

    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }
}