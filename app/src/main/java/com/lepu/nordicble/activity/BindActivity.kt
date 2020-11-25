package com.lepu.nordicble.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.nordicble.R
import com.lepu.nordicble.objs.Bluetooth
import com.lepu.nordicble.vals.EventMsgConst
import kotlinx.android.synthetic.main.activity_bind.*

class BindActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bind)

        iniUI()

        observeLiveEventObserver()
    }

    private fun iniUI() {
        container_er1.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java).apply {
                putExtra("TYPE", Bluetooth.MODEL_ER1)
            }
            startActivity(intent)
        }
        container_o2.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java).apply {
                putExtra("TYPE", Bluetooth.MODEL_CHECKO2)
            }
            startActivity(intent)
        }
        container_o2_max.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java).apply {
                putExtra("TYPE", Bluetooth.MODEL_O2MAX)
            }
            startActivity(intent)
        }
        container_kca.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java).apply {
                putExtra("TYPE", Bluetooth.MODEL_KCA)
            }
            startActivity(intent)
        }
        action_back.setOnClickListener {
            this.finish()
        }
    }

    private fun observeLiveEventObserver() {
        LiveEventBus.get(EventMsgConst.EventBindEr1Device)
                .observe(this, {
                    this.finish()
                })
        LiveEventBus.get(EventMsgConst.EventBindO2Device)
                .observe(this, {
                    this.finish()
                })
        LiveEventBus.get(EventMsgConst.EventBindKcaDevice)
                .observe(this, {
                    this.finish()
                })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }
}