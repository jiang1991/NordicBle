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
import android.widget.TextView
import com.blankj.utilcode.util.LogUtils
import com.lepu.nordicble.R
import com.lepu.nordicble.ble.KacBleInterface
import com.lepu.nordicble.ble.cmd.KacBleCmd
import com.lepu.nordicble.ble.cmd.KacBleResponse
import com.lepu.nordicble.objs.Bluetooth
import com.lepu.nordicble.objs.Const
import kotlinx.android.synthetic.main.fragment_kca.*
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_DEVICE = "kac_device"

class KcaFragment : Fragment() {
    private var device: Bluetooth? = null
    private val kacInterface = KacBleInterface()

    private var kacReceiver: BroadcastReceiver? = null

    private var lastBpResult : KacBleResponse.KacBpResult? = null
    private var state: Int =  KacBleCmd.KEY_MEASURE_RESULT
    private var bp : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerReceiver()
        arguments?.let {
            device = it.getParcelable(ARG_DEVICE)
            connect()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_kca, container, false)

        val id = view.findViewById<TextView>(R.id.id)
        id.text = device!!.name

        updateUI()

        return view
    }

    private fun updateUI() {
        when(state) {
            KacBleCmd.KEY_MEASURE_START -> {
                measure_time.text = "--"
                tv_sys.text = "--"
                tv_dia.text = "--"
                tv_avg.text = "--"
                tv_pr.text = "--"
            }
            KacBleCmd.KEY_MEASURING -> {
                measure_time.text = "--"
                if (bp == 0) {
                    tv_sys.text = "--"
                } else {
                    tv_sys.text = bp.toString()
                }
                tv_dia.text = "--"
                tv_avg.text = "--"
                tv_pr.text = "--"
            }
            KacBleCmd.KEY_MEASURE_RESULT -> {
                lastBpResult?.apply {
                    val time = Calendar.getInstance().time
                    val f = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
                    measure_time.text = f.format(time)
                    tv_sys.text = sys.toString()
                    tv_dia.text = dia.toString()
                    tv_avg.text = ((sys + dia)/2).toString()
                    tv_pr.text = pr.toString()
                }
            }
        }
    }

    private fun registerReceiver() {
        kacReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when(intent?.action) {
                    KacBleCmd.ACTION_KAC_STATE -> {
                        val s : Int? = intent?.getIntExtra("state", 0)
                        s?.apply {
                            when(this) {
                                KacBleCmd.KEY_MEASURE_START -> {

                                }
                                KacBleCmd.KEY_MEASURING -> {
                                    bp = intent.getIntExtra("bp", 0)
                                    LogUtils.d(s, bp)
                                }
                                KacBleCmd.KEY_MEASURE_RESULT -> {
                                    lastBpResult = intent.getParcelableExtra("result")
                                    LogUtils.d(s, lastBpResult?.sys)
                                }
                            }
                            state = this
                            updateUI()
                        }
                    }
                    KacBleCmd.ACTION_KAC_DATA -> {


                    }
                    KacBleCmd.ACTION_KAC_CONFIG -> {

                    }
                }
            }
        }
        val filter = IntentFilter()
        filter.addAction(KacBleCmd.ACTION_KAC_CONFIG)
        filter.addAction(KacBleCmd.ACTION_KAC_DATA)
        filter.addAction(KacBleCmd.ACTION_KAC_STATE)
        context?.registerReceiver(kacReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        kacReceiver?.apply {
            context?.unregisterReceiver(this)
            kacReceiver = null
        }
    }

    private fun connect() {
        device?.apply {
            kacInterface.connect(Const.context, this.device)
            LogUtils.d("connect ${device.name}")
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(device: Bluetooth) =
            KcaFragment().apply {

                arguments = Bundle().apply {
                    putParcelable(ARG_DEVICE, device)
                }
            }
    }
}