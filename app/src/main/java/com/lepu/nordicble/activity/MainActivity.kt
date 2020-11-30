package com.lepu.nordicble.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.KeyguardManager
import android.app.Service
import android.content.*
import android.net.NetworkInfo
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.telephony.TelephonyManager
import androidx.activity.viewModels
import com.afollestad.materialdialogs.MaterialDialog
import com.blankj.utilcode.util.LogUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.nordicble.R
import com.lepu.nordicble.ble.BleService
import com.lepu.nordicble.ble.cmd.Er1BleResponse
import com.lepu.nordicble.ble.cmd.KcaBleResponse
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
import kotlin.concurrent.thread
import kotlin.experimental.and

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {


    lateinit var bleService: BleService

    lateinit var er1Fragment: Er1Fragment
    lateinit var oxyFragment: OxyFragment
    lateinit var kcaFragment: KcaFragment

    private val mainModel : MainViewModel by viewModels()

    private lateinit var sysReceiver: BroadcastReceiver
    private var wifiLastDisconn = 0L

    private val bleConn = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            bleService = (p1 as BleService.BleBinder).getService()
            er1Fragment.initService(bleService)
            oxyFragment.initService(bleService)
            kcaFragment.initService(bleService)


            bleService.checkNeedAutoScan()
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

//            LogUtils.d("RunRt Task: $count")
            /**
             * 模块状态:  1/60 Hz
             */
            if (count%60 == 1L) {
                getBattery()
                socketSendMsg(SocketCmd.statusResponse())
                connectWifi()
            }

            if (count%20 == 0L) {
                bleService.checkNeedAutoScan()
            }
//
//            if (count%60 == 0L) {
//                // 连接wifi
//                connectWifi()
//            }

            /**
             * socket 心跳包: 1Hz
             */
            socketSendMsg(SocketCmd.heartbeatCmd())

            if (count % 10 == 1L) {
                socketConnect()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Const.context = this

        disableLock()
        registerReceiver()
        initUI()
        initVars()

        observeLiveEventObserver()
        observeLiveDataObserve()

        initService()
        rtHandler.post(RtTask())
    }

    /**
     * disable wakelock
     */
    private lateinit var mWaveup: PowerManager.WakeLock
    private lateinit var wifiManager: WifiManager
    private fun disableLock() {

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        // acquire wifi lock
        val wifilock =  wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "wifi high perf")
        wifilock.acquire()


        val keyguardManager = getSystemService(Activity.KEYGUARD_SERVICE) as KeyguardManager
        val lock = keyguardManager.newKeyguardLock(Context.KEYGUARD_SERVICE)
        lock.disableKeyguard()

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
//        mWakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK , ":MainActivity")
        mWaveup = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK or
                        PowerManager.ACQUIRE_CAUSES_WAKEUP
                        or PowerManager.ON_AFTER_RELEASE, ":MainActivity")

        mWaveup.acquire(48*60*60*1000L /*24 h*/)

//        val wakeTimer = Timer()
//        val waveTimerTask = timerTask {
//            mWaveup.acquire(10)
//        }
//        wakeTimer.schedule(waveTimerTask, 0, 1000*60*10)
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
        val er1 = readEr1Config(this)
        er1?.apply {
            mainModel.er1DeviceName.value = er1
            er1Name = er1
        }

        val oxi = readOxyConfig(this)
        oxi?.apply {
            mainModel.oxyDeviceName.value = oxi
            oxyName = oxi
        }

        val kca = readKcaConfig(this)
        kca?.apply {
            mainModel.kcaDeviceName.value = kca
            kcaName = kca
        }

        lead = readLeadInfo(this)
    }

    /**
     * socket part
     *
     */
    // connect
    private fun socketConnect() {

//        LogUtils.d("socketState: $socketState",
//                "hasEr1: $hasEr1 -> hasOxy: $hasOxy -> hasKca: $hasKca",
//                "${mainModel.hostIp.value}:${mainModel.hostPort.value}"
//        )

        if (mainModel.socketState.value == true) {
            return
        }

        if (!(hasEr1 || hasOxy || hasKca)) {
            return
        }

        if (mainModel.hostIp.value.isNullOrEmpty()) {
            return
        }

        LogUtils.d("try connect socket: ${mainModel.hostIp.value}:${mainModel.hostPort.value}")

        socketThread = SocketThread()
        socketThread.setUrl(mainModel.hostIp.value!!, mainModel.hostPort.value!!)
        socketThread.start()
    }

    private lateinit var socketThread : SocketThread

    /**
     * 处理中央站接收到的消息，响应服务器
     */
    private fun dealMsg(msg: SocketMsg) {
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
                    socketSendMsg(SocketCmd.statusResponse())
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
                    patient_name.text = "${patient.familyName}${patient.lastName}"
//                    mBed.text = patient.bed
                    patient_id.text = "病历号：${patient.pid}"
                    patient_age.text = "${patient.age}岁"
//                    var gender = "--"
                    if (patient.gender == 0) {
                        patient_gender.text = "女"
                    } else if (patient.gender == 1) {
                        patient_gender.text = "男"
                    }
//                    mGender.text = gender
                }
            }

            CMD_UNBIND -> {
                LogUtils.d("收到解绑指令：${msg.content.toHex()}")
                socketSendMsg(SocketCmd.unbindResponse(true))
//                clearBleVars()
//                disconnectBle()
                patient_name.text = "?"
                patient_id.text = "病历号：?"
                patient_age.text = "?岁"
                patient_gender.text = "?"

            }

            CMD_CHANGE_LEAD -> {
                LogUtils.d("收到更换导联指令：${msg.content.toHex()}")
                lead = toUInt(msg.content)
                saveLeadInfo(this, lead)
                socketSendMsg(SocketCmd.changeLeadResponse(true))
            }

            CMD_UPLOAD_ECG -> {
//                LogUtils.d("上传ECG成功： seq: ${msg.content.toHex()}")
            }
            CMD_UPLOAD_OXY_INFO -> {
//                LogUtils.d("上传Oxy Info 成功： seq: ${msg.content.toHex()}")
            }
            CMD_UPLOAD_OXY_WAVE -> {
//                LogUtils.d("上传Oxy Wave 成功： seq: ${msg.content.toHex()}")
            }
            CMD_UPLOAD_BP_STATE -> {
                //
            }
            CMD_UPLOAD_BP_RESULT -> {
                //
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
//        if (socketState) {
//            thread(start = true) {
//                socketThread.sendMessage(bytes)
//            }
//        }
        mainModel.socketState.value?.apply {
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
            } else {
                host_state.setImageResource(R.mipmap.host_error)
//                Timer().schedule(1000) {
//                    socketConnect()
//                }
            }
        })

        mainModel.wifiRssi.observe(this, {
            wifi_rssi.setImageLevel(it)
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

                    if (bleService.er1Interface.state) {
                        bleService.er1Interface.disconnect()
                    }
                    bleService.er1Interface.connect(this, it.device)
                    saveEr1Config(this, it.name)
                })
        LiveEventBus.get(EventMsgConst.EventBindO2Device)
                .observe(this, {
                    mainModel.oxyBluetooth.value = it as Bluetooth
                    mainModel.oxyDeviceName.value = it.name

                    if (bleService.oxyInterface.state) {
                        bleService.oxyInterface.disconnect()
                    }
                    bleService.oxyInterface.connect(this, it.device)
                    saveOxyConfig(this, it.name)
                })
        LiveEventBus.get(EventMsgConst.EventBindKcaDevice)
                .observe(this, {
                    mainModel.kcaBluetooth.value = it as Bluetooth
                    mainModel.kcaDeviceName.value = it.name

                    if (bleService.kcaInterface.state) {
                        bleService.kcaInterface.disconnect()
                    }
                    bleService.kcaInterface.connect(this, it.device)
                    saveKcaConfig(this, it.name)
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
                LogUtils.d("上传模块信息： ${SocketCmd.uploadInfoCmd().toHex()}")
            })

        LiveEventBus.get(EventMsgConst.EventOxyInfo)
            .observe(this, {
                val oxyInfo = it as OxyBleResponse.OxyInfo
                oxySn = oxyInfo.sn
                hasOxy = true
                socketSendMsg(SocketCmd.uploadInfoCmd())
                LogUtils.d("上传模块信息： ${SocketCmd.uploadInfoCmd().toHex()}")
            })

        LiveEventBus.get(EventMsgConst.EventKcaSn)
            .observe(this, {
                val sn = it as String
                kcaSn = sn
                hasKca = true
                socketSendMsg(SocketCmd.uploadInfoCmd())
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

        LiveEventBus.get(EventMsgConst.EventEr1InvalidRtData)
            .observe(this, {
                socketSendMsg(SocketCmd.invalidEcgCmd())
            })

        LiveEventBus.get(EventMsgConst.EventOxyRtData)
            .observe(this, {
                val rtWave = it as OxyBleResponse.RtWave
                socketSendMsg(SocketCmd.uploadOxyInfoCmd(rtWave.spo2, rtWave.pr, rtWave.pi
                , true, 0))
//                LogUtils.d("oxy lead: ${rtWave.state == "1"}  => ${rtWave.state}")
                if (rtWave.len == 0) {
                    socketSendMsg(SocketCmd.invalidOxyWaveCmd())
                } else {
                    socketSendMsg(SocketCmd.uploadOxyWaveCmd(rtWave.wByte))
                }
            })

        LiveEventBus.get(EventMsgConst.EventOxyInvalidRtData)
            .observe(this, {
                socketSendMsg(SocketCmd.invalidOxyInfoCmd())
                SocketCmd.invalidOxyWaveCmd()
            })

        LiveEventBus.get(EventMsgConst.EventKcaMeasureState)
            .observe(this, {
                val state = it as KcaBleResponse.KcaBpState
                socketSendMsg(SocketCmd.uploadKcaState(state.state, state.bp))
            })

        LiveEventBus.get(EventMsgConst.EventKcaBpResult)
            .observe(this, {
                val result = it as KcaBleResponse.KcaBpResult
                socketSendMsg(SocketCmd.uploadKcaResult(result))
            })

        /**
         * 解绑
         */
        LiveEventBus.get(EventMsgConst.EventEr1Unbind)
                .observe(this, {
                    hasEr1 = false
                    bleService.er1Interface.disconnect()
                    er1Name = null

                    mainModel.er1Bluetooth.value = null
                    mainModel.er1DeviceName.value = null

                    socketSendMsg(SocketCmd.uploadInfoCmd())
                })
        LiveEventBus.get(EventMsgConst.EventOxyUnbind)
                .observe(this, {
                    hasOxy = false
                    bleService.oxyInterface.disconnect()
                    oxyName = null

                    mainModel.oxyBluetooth.value = null
                    mainModel.oxyDeviceName.value = null

                    socketSendMsg(SocketCmd.uploadInfoCmd())
                })
        LiveEventBus.get(EventMsgConst.EventKcaUnbind)
                .observe(this, {
                    hasKca = false
                    bleService.kcaInterface.disconnect()
                    kcaName = null

                    mainModel.kcaBluetooth.value = null
                    mainModel.kcaDeviceName.value = null

                    socketSendMsg(SocketCmd.uploadInfoCmd())
                })

        /**
         * device disconnect
         */
        LiveEventBus.get(EventMsgConst.EventDeviceDisconnect)
            .observe(this, {
                bleService.checkNeedAutoScan()
            })

        /**
         * socket
         */
        LiveEventBus.get(EventMsgConst.EventSocketConnect)
                .observe(this, {
                    val connected = it as Boolean
//                    socketState = connected
                    mainModel.socketState.value = connected
                    if (connected) {
                        socketSendMsg(SocketCmd.tokenCmd())
                    }
                    LogUtils.d("Socket connect: $connected")
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

        wifi_rssi.setOnClickListener {

            val msg = if (wifiState) {
                "已连接到Wi-Fi： $wifiSsid"
            } else {
                "当前未连接到Wi-Fi"
            }

            MaterialDialog(this).show {
                message(text = msg)
                positiveButton(text = "确定") {
                    dialog -> dialog.dismiss()
                }
            }
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

    // register receiver
    private fun registerReceiver() {
        sysReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    WifiManager.WIFI_STATE_CHANGED_ACTION -> {
                        LogUtils.d("WifiManager.WIFI_STATE_CHANGED_ACTION")
                        when (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1)) {
//                            WifiManager.WIFI_STATE_ENABLED -> setWifi(true)

//                            else -> setWifi(false)
                        }
                    }
                    WifiManager.NETWORK_STATE_CHANGED_ACTION -> {
                        val network = intent.getParcelableExtra<NetworkInfo>(WifiManager.EXTRA_NETWORK_INFO)
                        val wifiInfo = intent.getParcelableExtra<WifiInfo>(WifiManager.EXTRA_WIFI_INFO)

                        if (NetworkInfo.State.CONNECTED.equals(network?.state)) {
                            if (wifiInfo != null) {
                                wifiSsid = wifiInfo.ssid
                                mainModel.wifiRssi.value = wifiInfo.rssi + 100

                                LogUtils.d("wifi SSID/RSSI: ${wifiInfo.ssid}, ${wifiInfo.rssi}")
                            }
                        } else if (NetworkInfo.State.DISCONNECTED.equals(network?.state)) {
//                            wifiSsid = ""
                            mainModel.wifiRssi.value = 100
                            wifiLastDisconn = System.currentTimeMillis()
                        }

                    }
                }
            }
        }

        val filter = IntentFilter()
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED")
        filter.addAction("android.net.wifi.STATE_CHANGE")
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        filter.addAction("android.bluetooth.adapter.action.STATE_CHANGED")
        filter.addAction(Intent.ACTION_BATTERY_CHANGED)
        filter.addAction(Intent.ACTION_BATTERY_OKAY)
        filter.addAction(Intent.ACTION_BATTERY_LOW)
        filter.addAction("android.intent.action.ACTION_POWER_CONNECTED")
        filter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED")
        registerReceiver(sysReceiver, filter)
    }

    //wifi
    @SuppressLint("MissingPermission")
    private fun connectWifi() {

        if (!wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = true
        } else {
            // wifi 断开一分钟以内不处理
            val current = System.currentTimeMillis()
            if (current - wifiLastDisconn < 1000*60) {
                return
            }

            if (!wifiState) {
//                mWaveup.acquire(10)
                val wifiConfs : List<WifiConfiguration> = wifiManager.configuredNetworks

                for (conf in wifiConfs) {
                    LogUtils.d("wifi conf: ${conf.networkId}, ${conf.SSID}, current: $wifiSsid")
                    if (conf.SSID == wifiSsid) {
                        wifiManager.enableNetwork(conf.networkId, true)
                        return
                    }
                }
            }
        }
    }

    private fun getBattery() {
        val batteryStatus: Intent? = registerReceiver(null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging: Boolean = status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL

        val batteryPct: Int? = batteryStatus?.let { intent ->
            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            level * 100 / scale
        }

        if (batteryPct != null) {
            battery = batteryPct
        }

        LogUtils.d(isCharging, batteryPct)
        when {
            isCharging -> {
                batteryState = 0x01
            }
            battery < 10 -> {
                batteryState = 0x10
            }
            else -> {
                batteryState = 0x00
            }
        }
        relay_battery.setImageLevel(battery)
        battery_left.text = "约${relayBatArr[battery]}小时"

    }

    @SuppressLint("MissingPermission")
    private fun readRelayId() {
        val tm : TelephonyManager = this.getSystemService(Service.TELEPHONY_SERVICE) as TelephonyManager
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            relayId = tm.deviceId.takeLast(6)
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            relayId = tm.imei.takeLast(6)
        }
    }


    override fun onBackPressed() {
        // todo: do nothing
    }

    override fun onDestroy() {
        super.onDestroy()
        rtHandler.removeCallbacks(RtTask())
        unbindService(bleConn)
        unregisterReceiver(sysReceiver)
    }

}