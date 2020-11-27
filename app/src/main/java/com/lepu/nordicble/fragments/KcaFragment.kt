package com.lepu.nordicble.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.lepu.nordicble.R
import com.lepu.nordicble.ble.BleService
import com.lepu.nordicble.ble.cmd.KcaBleCmd
import com.lepu.nordicble.objs.Bluetooth
import com.lepu.nordicble.vals.kcaBatArr
import com.lepu.nordicble.vals.kcaBleError
import com.lepu.nordicble.vals.kcaConn
import com.lepu.nordicble.viewmodel.KcaViewModel
import com.lepu.nordicble.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.fragment_kca.*
import kotlinx.android.synthetic.main.fragment_kca.battery
import kotlinx.android.synthetic.main.fragment_kca.ble_state
import kotlinx.android.synthetic.main.fragment_kca.device_sn
import kotlinx.android.synthetic.main.fragment_kca.tv_pr
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_KCA_DEVICE = "kca_device"

class KcaFragment : Fragment() {

    private val model: KcaViewModel by viewModels()
    private val activityModel: MainViewModel by activityViewModels()

    private var device: Bluetooth? = null

    lateinit var bleService: BleService


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

    public fun initService(service: BleService) {
        this.bleService = service
        bleService.kcaInterface.setViewModel(model)
    }

    // KcaViewModel
    private fun addLiveDataObserver(){

        activityModel.kcaDeviceName.observe(this, {
            device_sn.text = it
        })
//        activityModel.kcaBluetooth.observe(this, {
//            connect(it)
//        })

        model.connect.observe(this, {
            kcaConn = it
            if (it) {
                ble_state.setImageResource(R.mipmap.bluetooth_ok)
            } else {
                ble_state.setImageResource(R.mipmap.bluetooth_error)
                kcaBleError++
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