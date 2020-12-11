package com.lepu.nordicble.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.blankj.utilcode.util.LogUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.nordicble.R
import com.lepu.nordicble.ble.obj.KcaBpConfig
import com.lepu.nordicble.vals.EventMsgConst
import com.lepu.nordicble.viewmodel.KcaConfigModel
import kotlinx.android.synthetic.main.activity_kca_config.*
import kotlinx.android.synthetic.main.activity_kca_config.action_back


class KcaConfigActivity : AppCompatActivity() {

    private val model: KcaConfigModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kca_config)

        initUI()
        addLiveDataObserver()
        addLiveEventObserver()
    }

    private fun initUI() {

        val c: KcaBpConfig.MeasureConfig? = intent.getParcelableExtra("kca_measure_config")

        c?.apply {
            model.config.value = c
        }

        action_back.setOnClickListener {
            this.finish()
        }
    }

    private fun addLiveDataObserver() {
        model.config.observe(this, {
            day_start.text = "${formatInt(it.dayStH)}:${formatInt(it.dayEdM)}"
            day_end.text = "${formatInt(it.dayEdH)}:${formatInt(it.dayEdM)}"
            day_interval_val.text = formatMinute(it.dayInt)

            night_start.text = "${formatInt(it.nightStH)}:${formatInt(it.dayStM)}"
            night_end.text = "${formatInt(it.nightEdH)}:${formatInt(it.nightEdM)}"
            night_interval_val.text = formatMinute(it.nightInt)
        })
    }

    /**
     * 补成2位
     */
    private fun formatInt(i: Int) : String {
        return String.format("%02d", i)
    }
    private fun formatMinute(min: Int): String {
        return if (min <= 60) {
            "${min}m"
        } else {
            "${min%60}h${min/60}m"
        }
    }

    private fun addLiveEventObserver() {
        LiveEventBus.get(EventMsgConst.EventKcaBpConfig)
            .observe(this, {
                val config = it as KcaBpConfig.MeasureConfig
                model.config.value = config

                LogUtils.d(config.toString())
            })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}