package com.lepu.anxin.activity

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
import com.lepu.anxin.R
import com.lepu.anxin.ble.BleService
import com.lepu.anxin.ble.cmd.OxyBleResponse
import com.lepu.anxin.ble.obj.Er1Device
import com.lepu.anxin.objs.BleAdapter
import com.lepu.anxin.objs.Bluetooth
import com.lepu.anxin.objs.BluetoothController
import com.lepu.anxin.utils.saveEr1Config
import com.lepu.anxin.utils.saveKcaConfig
import com.lepu.anxin.utils.saveOxyConfig
import com.lepu.anxin.vals.*
import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : AppCompatActivity() {

    private lateinit var adapter: BleAdapter
    private var currentModel: Int = Bluetooth.MODEL_ER1
    private lateinit var list : ArrayList<Bluetooth>

    private var curDevice: Bluetooth? = null

    private var dialog: ProgressDialog? = null
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = Runnable {

        dialog?.dismiss()
        Toast.makeText(this, "连接失败，请重试", Toast.LENGTH_SHORT).show()

        when(currentModel) {
            Bluetooth.MODEL_ER1 -> {
                bleService.er1Interface.disconnect()
            }
//            Bluetooth.MODEL_O2MAX ->{
//                bleService.oxyInterface.disconnect()
//            }
            Bluetooth.MODEL_CHECKO2 -> {
                bleService.oxyInterface.disconnect()
            }
            Bluetooth.MODEL_KCA -> {
                bleService.kcaInterface.disconnect()
            }
        }
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
        BluetoothController.clear()
        when(currentModel) {
            Bluetooth.MODEL_ER1 -> {
                toolbar_title.text = getString(R.string.name_er1)
            }
            Bluetooth.MODEL_CHECKO2 -> {
                toolbar_title.text = getString(R.string.name_o2)
            }
//            Bluetooth.MODEL_O2MAX -> {
//                toolbar_title.text = getString(R.string.name_o2_max)
//            }
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

                saveEr1Config(this, curDevice!!.device.name)

                if (currentModel == Bluetooth.MODEL_ER1) {
                    LiveEventBus.get(EventMsgConst.EventBindEr1Device)
                        .post(curDevice)
                    finishBind()
                }
            })

        LiveEventBus.get(EventMsgConst.EventOxyInfo)
            .observe(this, {
                val oxyInfo = it as OxyBleResponse.OxyInfo

                saveOxyConfig(this, curDevice!!.device.name)

                if (currentModel == Bluetooth.MODEL_CHECKO2) {
                    LiveEventBus.get(EventMsgConst.EventBindO2Device)
                        .post(curDevice)
                    finishBind()
                }
            })

        LiveEventBus.get(EventMsgConst.EventKcaSn)
            .observe(this, {

                saveKcaConfig(this, curDevice!!.device.name)

                if (currentModel == Bluetooth.MODEL_KCA) {
                    LiveEventBus.get(EventMsgConst.EventBindKcaDevice)
                        .post(curDevice)
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

        curDevice = b

        // todo: 应该是绑定完成才发送次信息
        when(currentModel) {
            Bluetooth.MODEL_ER1 -> {
                if (bleService.er1Interface.state) {
                    bleService.er1Interface.disconnect()
                }
                bleService.er1Interface.connect(this, b.device)
//                LiveEventBus.get(EventMsgConst.EventBindEr1Device)
//                    .post(b)
            }
//            Bluetooth.MODEL_O2MAX ->{
//                LiveEventBus.get(EventMsgConst.EventBindO2Device)
//                    .post(b)
//            }
            Bluetooth.MODEL_CHECKO2 -> {
                if (bleService.oxyInterface.state) {
                    bleService.oxyInterface.disconnect()
                }
                bleService.oxyInterface.connect(this, b.device)
//                LiveEventBus.get(EventMsgConst.EventBindO2Device)
//                    .post(b)
            }
            Bluetooth.MODEL_KCA -> {
                if (bleService.kcaInterface.state) {
                    bleService.kcaInterface.disconnect()
                }
                bleService.kcaInterface.connect(this, b.device)
//                LiveEventBus.get(EventMsgConst.EventBindKcaDevice)
//                    .post(b)
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