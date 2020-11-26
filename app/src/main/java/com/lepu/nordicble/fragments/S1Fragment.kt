package com.lepu.nordicble.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.lepu.nordicble.R
import com.lepu.nordicble.ble.bridge.S1BleBridge
import com.lepu.nordicble.objs.Bluetooth
import com.lepu.nordicble.objs.Const
import com.lepu.nordicble.viewmodel.S1ViewModel
import kotlinx.android.synthetic.main.fragment_s1_scale.*

private const val ARG_S1_SCALE_DEVICE = "s1_scale_device"

class S1Fragment : Fragment() {

    private val model: S1ViewModel by viewModels()

    private var device: Bluetooth? = null

    private val s1Bridge = S1BleBridge()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            device = it.getParcelable(ARG_S1_SCALE_DEVICE)
            Log.d(ARG_S1_SCALE_DEVICE,"instance: ${device?.name}")
            connect()
        }
        addLiveDataObserver()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_s1_scale, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_list_files.setOnClickListener {
            listFiles()
        }
    }

    // Er1ViewModel
    private fun addLiveDataObserver(){
        s1Bridge.setViewModel(model)

        model.deviceName.observe(this, {
            tv_device_name.text = it
        })
        model.connectStateStr.observe(this, {
            tv_connect_state.text = it
        })
        model.runningState.observe(this, {
            val strId = when(it) {
                0 -> R.string.running_state_0
                1 -> R.string.running_state_1
                2 -> R.string.running_state_2
                3 -> R.string.running_state_3
                4 -> R.string.running_state_4
                5 -> R.string.running_state_5
                6 -> R.string.running_state_6
                7 -> R.string.running_state_7
                else -> {
                    R.string.running_state_0
                }
            }
            tv_running_state.text = getString(strId)
        })
        model.weight.observe(this, {
            tv_weight_value.text = "$it"
        })

        model.hrMeasureTime.observe(this, {
            tv_hr_measure_time_value.text = "$it"
        })
        model.hrMeasureValue.observe(this, {
            tv_hr_result_value.text = "$it"
        })
    }

    private fun connect() {
        device?.apply {
            s1Bridge.connect(Const.context, this.device)
            Log.d(ARG_S1_SCALE_DEVICE,"connect ${device.name}")
        }
    }

    private fun listFiles() {
        s1Bridge.listFiles()
    }

    companion object {
        @JvmStatic
        fun newInstance(device: Bluetooth) =
                S1Fragment().apply {

                    arguments = Bundle().apply {
                        putParcelable(ARG_S1_SCALE_DEVICE, device)
                    }
                }
    }
}