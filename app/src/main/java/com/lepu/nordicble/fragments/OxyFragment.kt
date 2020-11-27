package com.lepu.nordicble.fragments

import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.LogUtils
import com.lepu.nordicble.R
import com.lepu.nordicble.ble.BleService
import com.lepu.nordicble.ble.obj.OxyDataController
import com.lepu.nordicble.objs.Bluetooth
import com.lepu.nordicble.objs.Const
import com.lepu.nordicble.vals.erBatArr
import com.lepu.nordicble.vals.oxyBatArr
import com.lepu.nordicble.vals.oxyBattery
import com.lepu.nordicble.vals.oxyConn
import com.lepu.nordicble.viewmodel.MainViewModel
import com.lepu.nordicble.viewmodel.OxyViewModel
import com.lepu.nordicble.views.OxyView
import kotlinx.android.synthetic.main.fragment_o2.*
import kotlin.math.floor

private const val ARG_OXY_DEVICE = "oxy_device"

class OxyFragment : Fragment() {

    private val model: OxyViewModel by viewModels()
    private val activityModel: MainViewModel by activityViewModels()

    lateinit var bleService: BleService

    private lateinit var oxyView: OxyView
    private lateinit var viewOxyView: RelativeLayout

    /**
     * rt wave
     */
    private val waveHandler = Handler()

    inner class WaveTask : Runnable {
        override fun run() {
            if (!runWave) {
                return
            }

            val interval: Int = if (OxyDataController.dataRec.size > 300) {
                30
            } else if (OxyDataController.dataRec.size > 200) {
                35
            } else if (OxyDataController.dataRec.size > 150) {
                40
            } else {
                45
            }

            waveHandler.postDelayed(this, interval.toLong())
//            LogUtils.d("DataRec: ${DataController.dataRec.size}, delayed $interval")

            val temp = OxyDataController.draw(5)
            model.dataSrc.value = OxyDataController.feed(model.dataSrc.value, temp)
        }
    }

    private var runWave = false
    private fun startWave() {
        if (runWave) {
            return
        }
        runWave = true
        waveHandler.post(WaveTask())
    }

    private fun stopWave() {
        runWave = false
        OxyDataController.clear()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        arguments?.let {
//            device = it.getParcelable(ARG_OXY_DEVICE)
//            LogUtils.d("instance: ${device?.name}")
//            connect()
//        }

        addLiveDataObserver()
        addLiveEventObserver()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_o2, container, false)

        viewOxyView = v.findViewById(R.id.oxi_view)
        viewOxyView.post {
            initOxyView()
        }

        return v
    }

    private fun initOxyView() {
        // cal screen
        val dm =resources.displayMetrics
        val index = floor(viewOxyView.width / dm.xdpi * 25.4 / 25 * 125).toInt()
        OxyDataController.maxIndex = index

        val mm2px = 25.4f / dm.xdpi
        OxyDataController.mm2px = mm2px

//        LogUtils.d("max index: $index", "mm2px: $mm2px")

        viewOxyView.measure(0, 0)
        oxyView = OxyView(context)
        viewOxyView.addView(oxyView)

        model.dataSrc.value = OxyDataController.iniDataSrc(index)
    }

    public fun initService(service: BleService) {
        this.bleService = service
        bleService.oxyInterface.setViewModel(model)
    }

    private fun addLiveDataObserver() {

        activityModel.oxyDeviceName.observe(this, {
            device_sn.text = it
        })

        model.dataSrc.observe(this, {
            if (this::oxyView.isInitialized) {
                oxyView.setDataSrc(it)
                oxyView.invalidate()
            }
        })

        model.info.observe(this, {
            device_sn.text = it.sn
        })

        model.connect.observe(this, {
            oxyConn = it
            if (it) {
                ble_state.setImageResource(R.mipmap.bluetooth_ok)
                oxyView.visibility = View.VISIBLE
                startWave()
            } else {
                ble_state.setImageResource(R.mipmap.bluetooth_error)
                oxyView.visibility = View.GONE
                stopWave()
            }
        })

        model.battery.observe(this, {
            battery.setImageLevel(it)

            oxyBattery = it
            battery_left_duration.text = "约${oxyBatArr[it]}小时"
        })

        model.pr.observe(this, {
            if (it == 0) {
                tv_pr.text = "?"
            } else {
                tv_pr.text = it.toString()
            }
        })
        model.spo2.observe(this, {
            if (it == 0) {
                tv_oxy.text = "?"
            } else {
                tv_oxy.text = it.toString()
            }
        })
        model.pi.observe(this, {
            if (it == 0.0f) {
                tv_pi.text = "?"
            } else {
                tv_pi.text = it.toString()
            }
        })
    }

    private fun addLiveEventObserver() {

    }

    private fun connect(b: Bluetooth) {
        b.apply {
            bleService.oxyInterface.connect(Const.context, device)
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

        @JvmStatic
        fun newInstance() = OxyFragment()
    }
}