package com.lepu.nordicble.fragments

import android.content.BroadcastReceiver
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.blankj.utilcode.util.LogUtils
import com.lepu.nordicble.R
import com.lepu.nordicble.ble.Er1BleInterface
import com.lepu.nordicble.objs.Bluetooth
import com.lepu.nordicble.objs.Const

private const val ARG_ER1_DEVICE = "o2_device"

class Er1Fragment : Fragment() {

    private var device: Bluetooth? = null
    private val er1Interface = Er1BleInterface()

    private var er1Receiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerReceiver()
        arguments?.let {
            device = it.getParcelable(ARG_ER1_DEVICE)
            connect()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_er1, container, false)

        val id = view.findViewById<TextView>(R.id.id)
        id.text = device!!.name

        updateUI()

        return view
    }

    private fun updateUI() {

    }

    private fun registerReceiver() {

    }

    override fun onDestroy() {
        super.onDestroy()
        er1Receiver?.apply {
            context?.unregisterReceiver(this)
            er1Receiver = null
        }
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
                    arguments = Bundle().apply {
                        putParcelable(ARG_ER1_DEVICE, b)
                    }
                }
            }
    }
}