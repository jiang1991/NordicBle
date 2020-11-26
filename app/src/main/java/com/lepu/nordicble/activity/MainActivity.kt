package com.lepu.nordicble.activity

import android.Manifest
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.telephony.TelephonyManager
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.LogUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.nordicble.R
import com.lepu.nordicble.ble.BleService
import com.lepu.nordicble.ble.cmd.Er1BleResponse
import com.lepu.nordicble.ble.cmd.OxyBleResponse
import com.lepu.nordicble.ble.obj.Er1Device
import com.lepu.nordicble.fragments.Er1Fragment
import com.lepu.nordicble.fragments.KcaFragment
import com.lepu.nordicble.fragments.OxyFragment
import com.lepu.nordicble.objs.Bluetooth
import com.lepu.nordicble.objs.Const
import com.lepu.nordicble.socket.SocketThread
import com.lepu.nordicble.socket.objs.SocketCmd
import com.lepu.nordicble.socket.objs.SocketMsg
import com.lepu.nordicble.socket.objs.SocketMsg.*
import com.lepu.nordicble.socket.objs.SocketMsgConst
import com.lepu.nordicble.socket.objs.SocketResponse
import com.lepu.nordicble.utils.*
import com.lepu.nordicble.vals.*
import com.lepu.nordicble.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.thread
import kotlin.experimental.and

class MainActivity : AppCompatActivity() {

    private val permissionRequestCode = 521

    lateinit var bleService: BleService

    lateinit var er1Fragment: Er1Fragment
    lateinit var oxyFragment: OxyFragment
    lateinit var kcaFragment: KcaFragment

    private val mainModel : MainViewModel by viewModels()

    private val bleConn = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            bleService = (p1 as BleService.BleBinder).getService()
            er1Fragment.initService(bleService)
            oxyFragment.initService(bleService)
            kcaFragment.initService(bleService)
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            TODO("Not yet implemented")
        }
    }

    /**
     * rt task
     */
    private val rtHandler = Handler(Looper.getMainLooper())
    private var count: Long = 0L
    inner class RtTask : Runnable {
        override fun run() {
            count++
            rtHandler.postDelayed(this, 1000)

            /**
             * 模块状态:  1/60 Hz
             */
//            if (count%60 == 0L) {
//                getBattery()
//            }
//
//            if (count%60 == 0L) {
//                // 连接wifi
//                connectWifi()
//            }

            /**
             * socket 心跳包: 1Hz
             */
            socketSendMsg(SocketCmd.heartbeatCmd())

            if (count % 10 == 0L) {
                socketConnect()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Const.context = this

        if (requestLocation()) {
            requestPermission()
        }

        initUI()
        initVars()

        observeLiveEventObserver()
        observeLiveDataObserve()

        initService()
        rtHandler.post(RtTask())
    }

    /**
     * init vars
     * host config
     * bind device
     */
    private fun initVars() {
        readRelayId()

        readHostConfig()
        /**
         * name 为空则未绑定
         */
        val er1Name = readEr1Config(this)
        er1Name?.apply {
            mainModel.er1DeviceName.value = er1Name
        }

        val oxiName = readEr1Config(this)
        oxiName?.apply {
            mainModel.oxyDeviceName.value = oxiName
        }

        val kcaName = readEr1Config(this)
        kcaName?.apply {
            mainModel.kcaDeviceName.value = kcaName
        }

        lead = readLeadInfo(this)
    }

    /**
     * socket part
     *
     */
    // connect
    private fun socketConnect() {

        LogUtils.d("socketState: $socketState",
                "hasEr1: $hasEr1 -> hasOxy: $hasOxy -> hasKca: $hasKca",
                "${mainModel.hostIp.value}:${mainModel.hostPort.value}"
        )

        if (socketState) {
            return
        }

//        if (!(hasEr1 || hasOxy || hasKca)) {
//            return
//        }

        if (mainModel.hostIp.value.isNullOrEmpty()) {
            return
        }

        LogUtils.d("try connect socket: ${mainModel.hostIp.value}:${mainModel.hostPort.value}")

        socketThread =  SocketThread()
        socketThread.setUrl(mainModel.hostIp.value!!, mainModel.hostPort.value!!)
        socketThread.start()
    }

    private lateinit var socketThread : SocketThread

    /**
     * 处理中央站接收到的消息，响应服务器
     */
    fun dealMsg(msg: SocketMsg) {
        when (msg.cmd) {
            CMD_TOKEN -> {
                val serverToken = msg.content.copyOfRange(16,32)
                socketToken = SocketMsgConst.getToken(serverToken)

                if (socketToken != null) {
                    LogUtils.d("md5 token： ${socketToken?.toHex()}")
                    socketSignIn()
                } else {
                    LogUtils.d("收到token响应: null")
                }
            }

            CMD_LOGIN -> {
                // 1成功； other 失败
                val status = msg.content[0]
                if (status == 0x01.toByte()) {
                    LogUtils.d("登录成功")
                    // 上传模块信息
                    socketSendMsg(SocketCmd.uploadInfoCmd())
                    LogUtils.d("上传模块信息： ${SocketCmd.uploadInfoCmd().toHex()}")
                } else {
                    LogUtils.d("登录失败: ${status and 0xffu.toByte()}")
                }
            }

            CMD_UPLOAD_INFO -> {
                // 1成功； other 失败
                val status = msg.content[0]
                if (status == 0x01.toByte()) {
                    LogUtils.d("上传模块信息成功")
                } else {
                    LogUtils.d("上传模块信息失败")
                }
            }

            CMD_STATUS -> {
                LogUtils.d("收到上报模块状态指令")
                socketSendMsg(SocketCmd.statusResponse())
            }

            CMD_BIND -> {
                LogUtils.d("收到绑定指令： ${msg.content.toHex()}")
                socketSendMsg(SocketCmd.bindResponse(true))
                val patient = SocketResponse.BindPatientObj(msg.content)
                runOnUiThread {
                    patient_name.text = "姓名：${patient.familyName} ${patient.lastName}"
//                    mBed.text = patient.bed
                    patient_id.text = "病历号：${patient.pid}"
                    patient_age.text = "年龄：${patient.age}岁"
//                    var gender = "--"
                    if (patient.gender == 0) {
                        patient_gender.text = "性别：女"
                    } else if (patient.gender == 1) {
                        patient_gender.text = "性别：男"
                    }
//                    mGender.text = gender
                }
            }

            CMD_UNBIND -> {
                LogUtils.d("收到解绑指令：${msg.content.toHex()}")
                socketSendMsg(SocketCmd.unbindResponse(true))
//                clearBleVars()
//                disconnectBle()
            }

            CMD_CHANGE_LEAD -> {
                LogUtils.d("收到更换导联指令：${msg.content.toHex()}")
                lead = toUInt(msg.content)
                saveLeadInfo(this, lead)
                socketSendMsg(SocketCmd.changeLeadResponse(true))
            }

            CMD_UPLOAD_ECG -> {
                LogUtils.d("上传ECG成功： seq: ${msg.content.toHex()}")
            }
            CMD_UPLOAD_OXY_INFO -> {
                LogUtils.d("上传Oxy Info 成功： seq: ${msg.content.toHex()}")
            }
            CMD_UPLOAD_OXY_WAVE -> {
                LogUtils.d("上传Oxy Wave 成功： seq: ${msg.content.toHex()}")
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun socketSignIn() {
        // 登录
        val deviceId = relayId.encodeToByteArray()
        socketSendMsg(SocketCmd.loginCmd(socketToken, deviceId))
    }

    private fun socketSendMsg(bytes: ByteArray) {
        if (socketState) {
            thread(start = true) {
                socketThread.sendMessage(bytes)
            }
        }

    }

    /**
     * observe LiveData
     */
    private fun observeLiveDataObserve() {
//        mainModel.hostIp.observe(this, {
//            it?.apply {
//                socketConnect()
//            }
//        })

//        mainModel.relayId.observe(this, {
//            socketConnect()
//        })

        mainModel.socketState.observe(this, {
            if (it) {
                host_state.setImageResource(R.mipmap.host_ok)
                socketSendMsg(SocketCmd.tokenCmd())
            } else {
                host_state.setImageResource(R.mipmap.host_error)
//                Timer().schedule(1000) {
//                    socketConnect()
//                }
            }
        })
    }

    /**
     * add LiveDataBus observer
     * bind device
     */
    private fun observeLiveEventObserver() {

        // bind
        LiveEventBus.get(EventMsgConst.EventBindEr1Device)
                .observe(this, {
                    mainModel.er1Bluetooth.value = it as Bluetooth
                    mainModel.er1DeviceName.value = it.name

                    bleService.er1Interface.connect(this, it.device)
                })
        LiveEventBus.get(EventMsgConst.EventBindO2Device)
                .observe(this, {
                    mainModel.oxyBluetooth.value = it as Bluetooth
                    mainModel.oxyDeviceName.value = it.name

                    bleService.oxyInterface.connect(this, it.device)
                })
        LiveEventBus.get(EventMsgConst.EventBindKcaDevice)
                .observe(this, {
                    mainModel.kcaBluetooth.value = it as Bluetooth
                    mainModel.kcaDeviceName.value = it.name

                    bleService.kcaInterface.connect(this, it.device)
                })

        // info
        LiveEventBus.get(EventMsgConst.EventEr1Info)
            .observe(this, {
//                (it as Boolean).apply {
//                    socketSendMsg(SocketCmd.uploadInfoCmd())
//                }
                val info = it as Er1Device
                hasEr1 = true
                er1Sn = info.sn
                socketSendMsg(SocketCmd.uploadInfoCmd())
                socketSendMsg(SocketCmd.statusResponse())
                LogUtils.d("上传模块信息： ${SocketCmd.uploadInfoCmd().toHex()}")
            })

        LiveEventBus.get(EventMsgConst.EventOxyInfo)
            .observe(this, {
                val oxyInfo = it as OxyBleResponse.OxyInfo
                oxySn = oxyInfo.sn
                hasOxy = true
                socketSendMsg(SocketCmd.uploadInfoCmd())
                socketSendMsg(SocketCmd.statusResponse())
                LogUtils.d("上传模块信息： ${SocketCmd.uploadInfoCmd().toHex()}")
            })

        // wavedata
        LiveEventBus.get(EventMsgConst.EventEr1RtData)
            .observe(this, {
                val rtData = it as Er1BleResponse.RtData
                if (rtData.wave.len == 0) {
                    socketSendMsg(SocketCmd.invalidEcgCmd())
                } else {
                    socketSendMsg(SocketCmd.uploadEcgCmd(rtData.param.hr
                    , rtData.param.leadOn, rtData.wave.wave))
                }
            })

        LiveEventBus.get(EventMsgConst.EventOxyRtData)
            .observe(this, {
                val rtWave = it as OxyBleResponse.RtWave
                socketSendMsg(SocketCmd.uploadOxyInfoCmd(rtWave.spo2, rtWave.pr, rtWave.pi
                , rtWave.state == "1", 0))
                LogUtils.d("Oxy RtWave: ${rtWave.spo2} => ${rtWave.len}")
                if (rtWave.len == 0) {
                    socketSendMsg(SocketCmd.invalidOxyWaveCmd())
                } else {
                    socketSendMsg(SocketCmd.uploadOxyWaveCmd(rtWave.wByte))
                }
            })

        /**
         * socket
         */
        LiveEventBus.get(EventMsgConst.EventSocketConnect)
                .observe(this, {
                    val connected = it as Boolean
                    socketState = connected
                    mainModel.socketState.value = connected
                })

        LiveEventBus.get(EventMsgConst.EventSocketMsg)
                .observe(this, {
                    val res = it as ByteArray
                    dealMsg(SocketMsg(res))
                })
    }

    private fun initUI() {

        //todo: read saved devices

        bind_device.setOnClickListener {
            val intent = Intent(this, BindActivity::class.java)
            startActivity(intent)
        }

        host_config.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }

        relay_info.setOnClickListener {
            val intent = Intent(this, InfoActivity::class.java)
            startActivity(intent)
        }

        addKcaFragment()
        addO2Fragment()
        addEr1Fragment()
    }

    private fun initService() {
        BleService.startService(this)

        Intent(this, BleService::class.java).also {
            intent -> bindService(intent, bleConn, Context.BIND_AUTO_CREATE)
        }
    }


    private fun addKcaFragment() {
        kcaFragment = KcaFragment.newInstance()
        val trans = supportFragmentManager.beginTransaction()
        trans.add(R.id.container_3, kcaFragment)
        trans.commitAllowingStateLoss()
    }

    private fun addO2Fragment() {
        oxyFragment = OxyFragment.newInstance()
        val trans = supportFragmentManager.beginTransaction()
        trans.add(R.id.container_2, oxyFragment)
        trans.commitAllowingStateLoss()
    }

    private fun addEr1Fragment() {
        er1Fragment = Er1Fragment.newInstance()
        val trans = supportFragmentManager.beginTransaction()
        trans.add(R.id.container_1, er1Fragment)
        trans.commitAllowingStateLoss()
    }

    private fun requestLocation() : Boolean {
        /**
         * 检查是否开启location
         */
        val lm = getSystemService(LOCATION_SERVICE) as LocationManager
        val enable = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        LogUtils.d("location enable: $enable")
        return enable
    }

    private fun requestPermission() {
        val ps : Array<String> = arrayOf (
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        )

        for (p  in ps) {
            if (!checkP(p)) {
                ActivityCompat.requestPermissions(this, ps, permissionRequestCode)
                return
            }
        }

        permissionFinished()
    }

    private fun checkP(p: String) : Boolean {
        return ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED
    }

    private fun permissionFinished() {

    }

    override fun onResume() {
        super.onResume()

        readHostConfig()
    }

    private fun readHostConfig() {
        val (ip, port) = readHostConfig(this)
        ip?.apply {
            mainModel.hostIp.value = ip
            mainModel.hostPort.value = port
        }
    }

    private fun readRelayId() {
        val tm : TelephonyManager = this.getSystemService(Service.TELEPHONY_SERVICE) as TelephonyManager
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            relayId = tm.getDeviceId().takeLast(6)
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            relayId = tm.getImei().takeLast(6)
        }
    }


}