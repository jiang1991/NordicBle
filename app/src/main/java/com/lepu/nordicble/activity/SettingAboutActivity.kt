package com.lepu.nordicble.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.lepu.nordicble.R
import kotlinx.android.synthetic.main.activity_setting_about.*


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

        rl_disclaimer.setOnClickListener {

        }

        rl_check_update.setOnClickListener {

        }

    }

    private fun addLiveDataObserver() {

    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }
}