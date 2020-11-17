package com.lepu.nordicble.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.blankj.utilcode.util.LogUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.nordicble.R
import com.lepu.nordicble.ble.KcaBleInterface
import com.lepu.nordicble.ble.cmd.KcaBleCmd
import com.lepu.nordicble.ble.cmd.KcaBleResponse
import com.lepu.nordicble.const.BleConst
import com.lepu.nordicble.objs.Bluetooth
import com.lepu.nordicble.objs.Const
import com.lepu.nordicble.viewmodel.KcaViewModel
import kotlinx.android.synthetic.main.fragment_kca.*
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_KCA_DEVICE = "kca_device"

class KcaFragment : Fragment() {

    private val model: KcaViewModel by viewModels()

    private var device: Bluetooth? = null
    private val kcaInterface = KcaBleInterface()

    private var kcaReceiver: BroadcastReceiver? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            device = it.getParcelable(ARG_KCA_DEVICE)
            connect()
        }

        addLiveDataObserver()
        addLiveEventObserver()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_kca, container, false)
    }

    // KcaViewModel
    private fun addLiveDataObserver(){

        kcaInterface.setViewModel(model)

//        val deviceObserver = Observer<Bluetooth> { newDevice ->
//            device_sn.text = newDevice.name
//            device = newDevice
//            connect()
//        }
        val stateObserver = Observer<Int> { state ->
            when(state) {
                KcaBleCmd.KEY_MEASURE_START -> {
                    measure_time.text = "--"
                    tv_sys.text = "--"
                    tv_dia.text = "--"
                    tv_avg.text = "--"
                    tv_pr.text = "--"
                }
                KcaBleCmd.KEY_MEASURING -> {
                    measure_time.text = "--"
                    tv_dia.text = "--"
                    tv_avg.text = "--"
                    tv_pr.text = "--"
                }
                KcaBleCmd.KEY_MEASURE_RESULT -> {
//                    lastBpResult?.apply {
//                        val time = Calendar.getInstance().time
//                        val f = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
//                        measure_time.text = f.format(time)
//                        tv_sys.text = sys.toString()
//                        tv_dia.text = dia.toString()
//                        tv_avg.text = ((sys + dia)/2).toString()
//                        tv_pr.text = pr.toString()
//                    }
                }
            }
        }
        val rtBpObserver = Observer<Int> { bp ->
            tv_sys.text = bp.toString()
        }
        val bpResult = Observer<KcaBleResponse.KcaBpResult> { result ->
            val time = Calendar.getInstance().time
            val f = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
            measure_time.text = f.format(result.date)
            tv_sys.text = result.sys.toString()
            tv_dia.text = result.dia.toString()
            tv_avg.text = ((result.sys + result.dia)/2).toString()
            tv_pr.text = result.pr.toString()
        }

//        model.device.observe(this, deviceObserver)
        model.measureState.observe(this, stateObserver)
        model.rtBp.observe(this, rtBpObserver)
        model.bpResult.observe(this, bpResult)
    }

    /**
     * observe LiveDataBus
     * receive from KcaBleInterface
     * 考虑直接从interface来控制，不需要所有的都传递
     */
    private fun addLiveEventObserver() {
//        LiveEventBus.get(BleConst.EventKcaBleConnect)
//                .observe(this, object : Observer<Boolean> {
//
//                } )
    }



    override fun onDestroy() {
        super.onDestroy()
        kcaReceiver?.apply {
            context?.unregisterReceiver(this)
            kcaReceiver = null
        }
    }

    private fun connect() {
        device?.apply {
            kcaInterface.connect(Const.context, this.device)
            LogUtils.d("connect ${device.name}")
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(device: Bluetooth) =
            KcaFragment().apply {

                arguments = Bundle().apply {
                    putParcelable(ARG_KCA_DEVICE, device)
                }
            }
    }
}