package com.lepu.nordicble.objs

import android.bluetooth.BluetoothDevice
import com.blankj.utilcode.util.LogUtils
import com.lepu.nordicble.ble.cmd.Er1BleCmd

class BleJobController(device: BluetoothDevice) {

    var device: BluetoothDevice = device

    val jobs : ArrayList<BleJob> = arrayListOf<BleJob>()

    public fun onBleResponseReceived(response: BtResponse.BleResponse) {
        if (response.cmd == Er1BleCmd.BLE_CMD_GET_INFO) {
            val info: Er1 = Er1(response.content)
            LogUtils.d("${device.name} : ${info.sn}" )
        } else if (response.cmd == Er1BleCmd.BLE_CMD_RT_DATA) {
            val rtData: BtResponse.RtData = BtResponse.RtData(response.content)
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