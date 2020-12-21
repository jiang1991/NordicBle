package com.lepu.nordicble.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.lepu.nordicble.R
import com.lepu.nordicble.vals.relayId
import com.lepu.nordicble.viewmodel.MainViewModel
import com.yzq.zxinglibrary.encode.CodeCreator
import kotlinx.android.synthetic.main.activity_info.*
import kotlinx.android.synthetic.main.activity_info.action_back

class SettingAboutActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_about)

        addLiveDataObserver()

        initView()
    }

    private fun initView() {
        action_back.setOnClickListener {
            this.finish()
        }

    }

    private fun addLiveDataObserver() {

    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }
}