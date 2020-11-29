package com.lepu.nordicble.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.lepu.nordicble.ble.obj.Er1DataController
import com.lepu.nordicble.objs.Bluetooth
import com.lepu.nordicble.objs.Const
import com.lepu.nordicble.vals.er1Battery
import com.lepu.nordicble.vals.er1BleError
import com.lepu.nordicble.vals.er1Conn
import com.lepu.nordicble.vals.erBatArr
import com.lepu.nordicble.viewmodel.Er1ViewModel
import com.lepu.nordicble.viewmodel.MainViewModel
import com.lepu.nordicble.views.EcgBkg
import com.lepu.nordicble.views.EcgView
import kotlinx.android.synthetic.main.fragment_er1.*
import kotlinx.android.synthetic.main.fragment_er1.battery
import kotlinx.android.synthetic.main.fragment_er1.ble_state
import kotlinx.android.synthetic.main.fragment_er1.device_sn
import java.text.SimpleDateFormat
import kotlin.math.floor

private const val ARG_ER1_DEVICE = "er1_device"

class Er1Fragment : Fragment() {

    private val model: Er1ViewModel by viewModels()
    private val activityModel: MainViewModel by activityViewModels()

    private lateinit var ecgBkg: EcgBkg
    private lateinit var ecgView: EcgView

    private lateinit var viewEcgBkg: RelativeLayout
    private lateinit var viewEcgView: RelativeLayout

    private var device: Bluetooth? = null

    lateinit var bleService: BleService

    /**
     * rt wave
     */
    private val waveHandler = Handler()

    inner class WaveTask : Runnable {
        override fun run() {
            if (!runWave) {
                return
            }

            val interval: Int = when {
                Er1DataController.dataRec.size > 300 -> {
                    30
                }
                Er1DataController.dataRec.size > 200 -> {
                    35
                }
                Er1DataController.dataRec.size > 150 -> {
                    40
                }
                else -> {
                    45
                }
            }

            waveHandler.postDelayed(this, interval.toLong())
//            LogUtils.d("DataRec: ${DataController.dataRec.size}, delayed $interval")

            val temp = Er1DataController.draw(5)
            model.dataSrc.value = Er1DataController.feed(model.dataSrc.value, temp)
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
        Er1DataController.clear()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        arguments?.let {
//            device = it.getParcelable(ARG_ER1_DEVICE)
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
        val v = inflater.inflate(R.layout.fragment_er1, container, false)

        // add view
        viewEcgBkg = v.findViewById<RelativeLayout>(R.id.ecg_bkg)
        viewEcgView = v.findViewById<RelativeLayout>(R.id.ecg_view)

        viewEcgBkg.post {
            initEcgView()
        }

        return v
    }

    private fun initEcgView() {
        // cal screen
        val dm =resources.displayMetrics
        val index = floor(viewEcgBkg.width / dm.xdpi * 25.4 / 25 * 125).toInt()
        Er1DataController.maxIndex = index

        val mm2px = 25.4f / dm.xdpi
        Er1DataController.mm2px = mm2px

//        LogUtils.d("max index: $index", "mm2px: $mm2px")

        viewEcgBkg.measure(0, 0)
        ecgBkg = EcgBkg(context)
        viewEcgBkg.addView(ecgBkg)

        viewEcgView.measure(0, 0)
        ecgView = EcgView(context)
        viewEcgView.addView(ecgView)

        ecg_view.visibility = View.GONE
    }

    public fun initService(service: BleService) {
        this.bleService = service
        bleService.er1Interface.setViewModel(model)
    }

    // Er1ViewModel
    private fun addLiveDataObserver(){

        activityModel.er1DeviceName.observe(this, {
            device_sn.text = it
        })

        model.dataSrc.observe(this, {
            if (this::ecgView.isInitialized) {
                ecgView.setDataSrc(it)
                ecgView.invalidate()
            }
        })

        model.er1.observe(this, {
            device_sn.text = "SN：${it.sn}"
        })

        model.connect.observe(this, {
            er1Conn = it
            if (it) {
                ble_state.setImageResource(R.mipmap.bluetooth_ok)
                ecg_view.visibility = View.VISIBLE
                startWave()
            } else {
                ble_state.setImageResource(R.mipmap.bluetooth_error)
                ecg_view.visibility = View.GONE
                stopWave()
                er1BleError++
                clearVar()
            }
        })

        model.duration.observe(this, {
            if (it == 0) {
                measure_duration.text = "?"
                start_at.text = "?"
            } else {
                val day = it/60/60/24
                val hour = it/60/60 % 24
                val minute = it/60 % 60

                val start = System.currentTimeMillis() - it*1000
                start_at.text = SimpleDateFormat("yyyy-MM-dd HH:mm").format(start)
                if (day != 0) {
                    measure_duration.text = "$day 天 $hour 小时 $minute 分钟"
                } else {
                    measure_duration.text = "$hour 小时 $minute 分钟"
                }
            }
        })

        model.battery.observe(this, {
            battery.setImageLevel(it)
            battery_left_duration.text = "约${erBatArr[it]}小时"

            er1Battery = it
        })

        model.hr.observe(this, {
            if (it == 0) {
                hr.text = "?"
            } else {
                hr.text = it.toString()
            }
        })
    }

    private fun clearVar() {
        activityModel.er1DeviceName.value = null
        model.battery.value = 0
        model.duration.value = 0
        model.hr.value = 0
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
        fun newInstance(b: Bluetooth) =
            Er1Fragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_ER1_DEVICE, b)
                }
            }

        @JvmStatic
        fun newInstance() = Er1Fragment()
    }
}