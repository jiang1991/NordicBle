package com.lepu.anxin.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.anxin.R
import com.lepu.anxin.objs.Bluetooth
import com.lepu.anxin.utils.*
import com.lepu.anxin.vals.EventMsgConst
import kotlinx.android.synthetic.main.activity_bind.*

class BindActivity : AppCompatActivity() {

    private var er1DeviceName: String? = null
    private var oxyDeviceName: String? = null
    private var kcaDeviceName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bind)

        iniUI()

        observeLiveEventObserver()
    }

    private fun iniUI() {

        iniDevices()

        container_er1.setOnClickListener {
            if (er1DeviceName == null) {
                val intent = Intent(this, SearchActivity::class.java).apply {
                    putExtra("TYPE", Bluetooth.MODEL_ER1)
                }
                startActivity(intent)
            } else {
                MaterialDialog(this).show {
                    message(text = "确定解绑 $er1DeviceName")
                    positiveButton(text = "确定") {
                        clearEr1Config(this@BindActivity)
                        iniDevices()
                        LiveEventBus.get(EventMsgConst.EventEr1Unbind)
                                .post(true)
                    }
                    negativeButton(text = "取消") {
                        dialog -> dialog.dismiss()
                    }
                }
            }
        }
        container_o2.setOnClickListener {
            if (oxyDeviceName == null) {
                val intent = Intent(this, SearchActivity::class.java).apply {
                    putExtra("TYPE", Bluetooth.MODEL_CHECKO2)
                }
                startActivity(intent)
            } else {
                MaterialDialog(this).show {
                    message(text = "确定解绑 $oxyDeviceName")
                    positiveButton(text = "确定") {
                        clearOxyConfig(this@BindActivity)
                        iniDevices()
                        LiveEventBus.get(EventMsgConst.EventOxyUnbind)
                                .post(true)
                    }
                    negativeButton(text = "取消") {
                        dialog -> dialog.dismiss()
                    }
                }
            }

        }
//        container_o2_max.setOnClickListener {
//            val intent = Intent(this, SearchActivity::class.java).apply {
//                putExtra("TYPE", Bluetooth.MODEL_O2MAX)
//            }
//            startActivity(intent)
//        }
        container_kca.setOnClickListener {
            if (kcaDeviceName == null) {
                val intent = Intent(this, SearchActivity::class.java).apply {
                    putExtra("TYPE", Bluetooth.MODEL_KCA)
                }
                startActivity(intent)
            } else {
                MaterialDialog(this).show {
                    message(text = "确定解绑 $kcaDeviceName")
                    positiveButton(text = "确定") {
                        clearKcaConfig(this@BindActivity)
                        iniDevices()
                        LiveEventBus.get(EventMsgConst.EventKcaUnbind)
                                .post(true)
                    }
                    negativeButton(text = "取消") {
                        dialog -> dialog.dismiss()
                    }
                }
            }

        }
        action_back.setOnClickListener {
            this.finish()
        }


    }

    private fun iniDevices() {

        er1DeviceName = readEr1Config(this)
        oxyDeviceName = readOxyConfig(this)
        kcaDeviceName = readKcaConfig(this)

        if (er1DeviceName == null) {
            er1_name.text = ""
            er1_bind.visibility = View.VISIBLE
            er1_unbind.visibility = View.GONE
        } else {
            er1_name.text = er1DeviceName
            er1_bind.visibility = View.GONE
            er1_unbind.visibility = View.VISIBLE
        }

        if (oxyDeviceName == null) {
            oxy_name.text = ""
            oxy_bind.visibility = View.VISIBLE
            oxy_unbind.visibility = View.GONE
        } else {
            oxy_name.text = oxyDeviceName
            oxy_bind.visibility = View.GONE
            oxy_unbind.visibility = View.VISIBLE
        }

        if (kcaDeviceName == null) {
            kca_name.text = ""
            kca_bind.visibility = View.VISIBLE
            kca_unbind.visibility = View.GONE
        } else {
            kca_name.text = kcaDeviceName
            kca_bind.visibility = View.GONE
            kca_unbind.visibility = View.VISIBLE
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