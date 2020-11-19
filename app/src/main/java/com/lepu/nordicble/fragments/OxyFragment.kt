package com.lepu.nordicble.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.LogUtils
import com.lepu.nordicble.R
import com.lepu.nordicble.ble.OxyBleInterface
import com.lepu.nordicble.objs.Bluetooth
import com.lepu.nordicble.objs.Const
import com.lepu.nordicble.viewmodel.OxyViewModel
import kotlinx.android.synthetic.main.fragment_o2.*

private const val ARG_OXY_DEVICE = "oxy_device"

class OxyFragment : Fragment() {

    private val model: OxyViewModel by viewModels()

    private var device: Bluetooth? = null
    private val oxyInterface = OxyBleInterface()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            device = it.getParcelable(ARG_OXY_DEVICE)
            LogUtils.d("instance: ${device?.name}")
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
        return inflater.inflate(R.layout.fragment_o2, container, false)
    }

    private fun addLiveDataObserver() {
        oxyInterface.setViewModel(model)

        model.info.observe(this, {
            device_sn.text = it.sn
        })

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

        model.pr.observe(this, {
            tv_pr.text = it.toString()
        })
        model.spo2.observe(this, {
            tv_oxy.text = it.toString()
        })
        model.pi.observe(this, {
            tv_pi.text = it.toString()
        })
    }

    private fun addLiveEventObserver() {

    }

    private fun connect() {
        device?.apply {
            oxyInterface.connect(Const.context, this.device)
            LogUtils.d("connect ${device.name}")
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(b: Bluetooth) =
            OxyFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_OXY_DEVICE, b)
                }
            }
    }
}