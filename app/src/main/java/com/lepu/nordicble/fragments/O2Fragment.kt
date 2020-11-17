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
import com.lepu.nordicble.objs.Bluetooth

private const val ARG_OXY_DEVICE = "o2_device"

class O2Fragment : Fragment() {

    private var device: Bluetooth? = null
//    private val o2Interface = KcaBleInterface()

    private var o2Receiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerReceiver()
        arguments?.let {
            device = it.getParcelable(ARG_OXY_DEVICE)
            connect()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_o2, container, false)

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
        o2Receiver?.apply {
            context?.unregisterReceiver(this)
            o2Receiver = null
        }
    }

    private fun connect() {
        device?.apply {
//            o2Interface.connect(Const.context, this.device)
            LogUtils.d("connect ${device.name}")
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(b: Bluetooth) =
            O2Fragment().apply {
                arguments = Bundle().apply {
                    arguments = Bundle().apply {
                        putParcelable(ARG_OXY_DEVICE, b)
                    }
                }
            }
    }
}