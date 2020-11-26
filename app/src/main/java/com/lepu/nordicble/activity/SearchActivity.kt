package com.lepu.nordicble.activity

import android.app.ProgressDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.AdapterView
import android.widget.Toast
import com.blankj.utilcode.util.ToastUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.nordicble.R
import com.lepu.nordicble.ble.BleService
import com.lepu.nordicble.ble.cmd.OxyBleResponse
import com.lepu.nordicble.ble.obj.Er1Device
import com.lepu.nordicble.objs.BleAdapter
import com.lepu.nordicble.objs.Bluetooth
import com.lepu.nordicble.objs.BluetoothController
import com.lepu.nordicble.vals.*
import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : AppCompatActivity() {

    private lateinit var adapter: BleAdapter
    private var currentModel: Int = Bluetooth.MODEL_ER1
    private lateinit var list : ArrayList<Bluetooth>

    private var dialog: ProgressDialog? = null
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = Runnable {

        dialog?.dismiss()
        Toast.makeText(this, "连接失败，请重试", Toast.LENGTH_SHORT).show()
    }

    lateinit var bleService: BleService

    private val bleConn = object : ServiceConnection  {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            bleService = (p1 as BleService.BleBinder).getService()
            bleService.startDiscover()
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            TODO("Not yet implemented")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initService()
        currentModel = intent.extras?.get("TYPE") as Int
        setContentView(R.layout.activity_search)
        initUI()
        observeLiveDataBus()
    }

    private fun initService() {
        BleService.startService(this)

        Intent(this, BleService::class.java).also {
            intent -> bindService(intent, bleConn, Context.BIND_AUTO_CREATE)
        }
    }

    private fun initUI() {
        when(currentModel) {
            Bluetooth.MODEL_ER1 -> {
                toolbar_title.text = getString(R.string.name_er1)
            }
            Bluetooth.MODEL_CHECKO2 -> {
                toolbar_title.text = getString(R.string.name_o2)
            }
            Bluetooth.MODEL_O2MAX -> {
                toolbar_title.text = getString(R.string.name_o2_max)
            }
            Bluetooth.MODEL_KCA -> {
                toolbar_title.text = getString(R.string.name_kca)
            }
        }

        setAdapter()

        action_back.setOnClickListener {
            this.finish()
        }
        refresh.setOnClickListener {
            bleService.startDiscover()
        }
    }

    private fun observeLiveDataBus() {
        LiveEventBus.get(EventMsgConst.EventDeviceFound)
            .observe(this, {
                val b = it as Bluetooth
                if (b.model == currentModel) {
                    adapter.deviceList = BluetoothController.getDevices(currentModel)
                    adapter.notifyDataSetChanged()
                }
            })

        LiveEventBus.get(EventMsgConst.EventEr1Info)
            .observe(this, {
//                (it as Boolean).apply {
//                    socketSendMsg(SocketCmd.uploadInfoCmd())
//                }
                val info = it as Er1Device
                hasEr1 = true
                er1Sn = info.sn

                if (currentModel == Bluetooth.MODEL_ER1) {
                    finishBind()
                }
            })

        LiveEventBus.get(EventMsgConst.EventOxyInfo)
            .observe(this, {
                val oxyInfo = it as OxyBleResponse.OxyInfo
                oxySn = oxyInfo.sn
                hasOxy = true

                if (currentModel == Bluetooth.MODEL_CHECKO2 || currentModel == Bluetooth.MODEL_O2MAX) {
                    finishBind()
                }
            })

        LiveEventBus.get(EventMsgConst.EventKcaSn)
            .observe(this, {
                val sn = it as String

                if (currentModel == Bluetooth.MODEL_KCA) {
                    finishBind()
                }
            })
    }

    private fun setAdapter() {
        list = BluetoothController.getDevices(currentModel)
        adapter = BleAdapter(this, list)
        ble_list.adapter = adapter
        ble_list.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
//                connect(BluetoothController.getDevices()[position])
                val b = adapter.deviceList[position]
                processBindDevice(b)
            }

    }

    private fun finishBind() {
        ToastUtils.showShort("蓝牙连接成功")
        dialog?.dismiss()
        dialog = null
        handler.removeCallbacks(runnable)
        this.finish()
    }

    private fun processBindDevice(b : Bluetooth) {

        dialog = ProgressDialog(this)
        dialog?.setMessage("正在连接 ${b.name}...")
        dialog?.setCancelable(false)
        dialog?.show()

        handler.postDelayed(runnable, 10000)

        when(currentModel) {
            Bluetooth.MODEL_ER1 -> {
                LiveEventBus.get(EventMsgConst.EventBindEr1Device)
                    .postAcrossProcess(b)
//                bleService.er1Interface.connect(Const.context, b.device)
            }
            Bluetooth.MODEL_O2MAX ->{
                LiveEventBus.get(EventMsgConst.EventBindO2Device)
                    .postAcrossProcess(b)
//                bleService.oxyInterface.connect(Const.context, b.device)
            }
            Bluetooth.MODEL_CHECKO2 -> {
                LiveEventBus.get(EventMsgConst.EventBindO2Device)
                    .postAcrossProcess(b)
//                bleService.oxyInterface.connect(Const.context, b.device)
            }
            Bluetooth.MODEL_KCA -> {
                LiveEventBus.get(EventMsgConst.EventBindKcaDevice)
                    .postAcrossProcess(b)
//                bleService.kcaInterface.connect(Const.context, b.device)
            }
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        bleService.stopDiscover()
    }
}