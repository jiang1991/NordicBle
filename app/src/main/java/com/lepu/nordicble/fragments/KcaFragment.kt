package com.lepu.nordicble.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.LogUtils
import com.lepu.nordicble.R
import com.lepu.nordicble.ble.bridge.KcaBleInterface
import com.lepu.nordicble.ble.cmd.kcal.KcaBleCmd
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

        model.connect.observe(this, {
            if (it) {
                ble_state.setImageResource(R.mipmap.bluetooth_ok)
            } else {
                ble_state.setImageResource(R.mipmap.bluetooth_error)
            }
        })

        model.battery.observe(this, {
            battery.setImageLevel(it)
        })

//        model.device.observe(this, deviceObserver)
        model.measureState.observe(this, {
            when(it) {
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

                }
            }
        })
        model.rtBp.observe(this, {
            tv_sys.text = it.toString()
        })
        model.bpResult.observe(this, {
            val time = Calendar.getInstance().time
            val f = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
            measure_time.text = f.format(it.date)
            tv_sys.text = it.sys.toString()
            tv_dia.text = it.dia.toString()
            tv_avg.text = ((it.sys + it.dia)/2).toString()
            tv_pr.text = it.pr.toString()
        })
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