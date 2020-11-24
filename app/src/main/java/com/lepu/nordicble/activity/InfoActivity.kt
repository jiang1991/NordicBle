package com.lepu.nordicble.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.lepu.nordicble.R
import com.lepu.nordicble.viewmodel.MainViewModel
import com.yzq.zxinglibrary.encode.CodeCreator
import kotlinx.android.synthetic.main.activity_info.*
import kotlinx.android.synthetic.main.activity_info.action_back

class InfoActivity : AppCompatActivity() {

    private val mainModel : MainViewModel by viewModels()

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
    }

    private fun addLiveDataObserver() {
        mainModel.relayId.observe(this, {
            relay_id.text = "收发器编号: $it"
            val bitmap = CodeCreator.createQRCode(it, 100, 100, null)
            iv_qr.setImageBitmap(bitmap)
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }
}