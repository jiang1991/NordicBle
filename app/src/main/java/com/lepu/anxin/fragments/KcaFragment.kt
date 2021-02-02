package com.lepu.anxin.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.LogUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.anxin.R
import com.lepu.anxin.activity.KcaConfigActivity
import com.lepu.anxin.ble.BleService
import com.lepu.anxin.ble.cmd.KcaBleCmd
import com.lepu.anxin.ble.cmd.KcaBleResponse
import com.lepu.anxin.objs.Bluetooth
import com.lepu.anxin.ble.obj.KcaBpConfig
import com.lepu.anxin.vals.EventMsgConst
import com.lepu.anxin.vals.kcaBatArr
import com.lepu.anxin.vals.kcaBleError
import com.lepu.anxin.viewmodel.KcaViewModel
import com.lepu.anxin.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.fragment_kca.*
import kotlinx.android.synthetic.main.fragment_kca.battery
import kotlinx.android.synthetic.main.fragment_kca.battery_left_duration
import kotlinx.android.synthetic.main.fragment_kca.ble_state
import kotlinx.android.synthetic.main.fragment_kca.device_sn
import kotlinx.android.synthetic.main.fragment_kca.tv_pr
import kotlinx.android.synthetic.main.fragment_kca.view.*
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_KCA_DEVICE = "kca_device"

class KcaFragment : Fragment() {

    private val model: KcaViewModel by viewModels()
    private val activityModel: MainViewModel by activityViewModels()

    private var device: Bluetooth? = null

    lateinit var bleService: BleService

    private var bpResult: KcaBleResponse.KcaBpResult? = null
    private var kcaConfig: KcaBpConfig.MeasureConfig? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addLiveDataObserver()
        addLiveEventObserver()
        initVars()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_kca, container, false)

            v.action_measure_config.setOnClickListener {
            val i = Intent(activity, KcaConfigActivity::class.java)
            i.putExtra("kca_measure_config", kcaConfig)
            startActivity(i)
        }

        return v
    }

    public fun initService(service: BleService) {
        this.bleService = service
        bleService.kcaInterface.setViewModel(model)
    }

    private fun initVars() {
        kcaConfig = KcaBpConfig.MeasureConfig(null)
    }

    // KcaViewModel
    private fun addLiveDataObserver(){

        activityModel.kcaDeviceName.observe(this, {
            if (it == null) {
                device_sn.text = "未绑定设备"
            } else {
                device_sn.text = "SN：$it"
            }
        })
//        activityModel.kcaBluetooth.observe(this, {
//            connect(it)
//        })

        model.connect.observe(this, {
            if (it) {
                ble_state.setImageResource(R.mipmap.bluetooth_ok)
                battery.visibility = View.VISIBLE
                battery_left_duration.visibility = View.VISIBLE
            } else {
                ble_state.setImageResource(R.mipmap.bluetooth_error)
                battery.visibility = View.INVISIBLE
                battery_left_duration.visibility = View.INVISIBLE
                kcaBleError++
                clearVar()
            }
        })

        model.battery.observe(this, {
            battery.setImageLevel(it)
            battery_left_duration.text = "可测量${kcaBatArr[it]}次"
        })

//        model.device.observe(this, deviceObserver)
        model.measureState.observe(this, {
            when(it) {
                KcaBleCmd.KEY_MEASURE_START -> {
                    measure_time.text = "?"
                    tv_sys.text = "?"
                    tv_dia.text = "?"
                    tv_avg.text = "?"
                    tv_pr.text = "?"
                }
                KcaBleCmd.KEY_MEASURING -> {
                    measure_time.text = ""
                    tv_dia.text = ""
                    tv_avg.text = ""
                    tv_pr.text = ""
                }
                KcaBleCmd.KEY_MEASURE_RESULT -> {

                }
            }
        })
        model.rtBp.observe(this, {
            tv_sys.text = it.toString()
        })
        model.bpResult.observe(this, {
            val time = Calendar.getInstance().time
            val f = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
            measure_time.text = f.format(it.date)
            tv_sys.text = it.sys.toString()
            tv_dia.text = it.dia.toString()
            tv_avg.text = ((it.sys + it.dia)/2).toString()
            tv_pr.text = it.pr.toString()
        })
    }


    private fun clearVar() {
//        activityModel.kcaDeviceName.value = null
//        model.battery.value = 0

        bpResult?.apply {
            model.bpResult.value = this
        }

    }

    /**
     * observe LiveDataBus
     * receive from KcaBleInterface
     * 考虑直接从interface来控制，不需要所有的都传递
     */
    private fun addLiveEventObserver() {
         LiveEventBus.get(EventMsgConst.EventKcaBpConfig)
             .observe(this, {
                 val config = it as KcaBpConfig.MeasureConfig
                 kcaConfig = config
                 bleService.kcaInterface.setNightPeriod(config.nightStH, config.nightStM, config.nightEdH, config.nightEdM)
                 bleService.kcaInterface.setInterval(config.dayInt, config.nightInt)

                 LogUtils.d(config.toString())
             })
    }


    companion object {
        @JvmStatic
        fun newInstance(device: Bluetooth) =
            KcaFragment().apply {

                arguments = Bundle().apply {
                    putParcelable(ARG_KCA_DEVICE, device)
                }
            }

        @JvmStatic
        fun newInstance() = KcaFragment()
    }
}