package com.lepu.nordicble.objs

import android.bluetooth.BluetoothDevice
import com.blankj.utilcode.util.LogUtils
import com.lepu.nordicble.ble.cmd.er1.Er1BleCmd
import com.lepu.nordicble.ble.cmd.er1.Er1BleResponse
import com.lepu.nordicble.ble.obj.Er1Device

class BleJobController(device: BluetoothDevice) {

    var device: BluetoothDevice = device

    val jobs : ArrayList<BleJob> = arrayListOf<BleJob>()

    public fun onBleResponseReceived(response: Er1BleResponse.Er1Response) {
        if (response.cmd == Er1BleCmd.ER1_CMD_GET_INFO) {
            val info: Er1Device = Er1Device(response.content)
            LogUtils.d("${device.name} : ${info.sn}" )
        } else if (response.cmd == Er1BleCmd.ER1_CMD_RT_DATA) {
            val rtData: Er1BleResponse.RtData = Er1BleResponse.RtData(response.content)
            LogUtils.d("${device.name} battery: ${rtData.param.battery}, hr: ${rtData.param.hr}")
        }

        for (blejob in jobs) {
            if (blejob.cmd == response.cmd) {
//                blejob.job.cancel()
                jobs.remove(blejob)
//                LogUtils.d("${device.name} job finished ${blejob.cmd}")
            }
        }
    }

    public fun addJob(bleJob: BleJob) {
//        runBlocking {
//            bleJob.job = launch(Dispatchers.IO) {
//                delay(bleJob.timeout.toLong())
//                jobs.remove(bleJob)
//                LogUtils.d("${device.name} job timeout ${bleJob.cmd}")
//            }
//            jobs.add(bleJob)
//        }
        jobs.add(bleJob)
    }

    class BleJob {
        var cmd: Int
        var content: ByteArray
        var timeout: Int
//        lateinit var job: Job

        constructor(cmd: Int, content: ByteArray, timeout: Int) {
            this.cmd = cmd
            this.content = content
            this.timeout = timeout
        }
    }
}