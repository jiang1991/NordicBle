package com.lepu.nordicble.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.LogUtils
import com.lepu.nordicble.R
import com.lepu.nordicble.ble.bridge.Er1BleInterface
import com.lepu.nordicble.objs.Bluetooth
import com.lepu.nordicble.objs.Const
import com.lepu.nordicble.viewmodel.Er1ViewModel
import kotlinx.android.synthetic.main.fragment_er1.*

private const val ARG_ER1_DEVICE = "er1_device"

class Er1Fragment : Fragment() {

    private val model: Er1ViewModel by viewModels()

    private var device: Bluetooth? = null
    private val er1Interface = Er1BleInterface()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            device = it.getParcelable(ARG_ER1_DEVICE)
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
        return inflater.inflate(R.layout.fragment_er1, container, false)
    }

    // Er1ViewModel
    private fun addLiveDataObserver(){

        er1Interface.setViewModel(model)

        model.er1.observe(this, {
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

        model.hr.observe(this, {
            hr.text = it.toString()
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
            er1Interface.connect(Const.context, this.device)
            LogUtils.d("connect ${device.name}")
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(b: Bluetooth) =
            Er1Fragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_ER1_DEVICE, b)
                }
            }
    }
}