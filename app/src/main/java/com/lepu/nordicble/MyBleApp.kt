package com.lepu.nordicble

import android.app.Application
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.nordicble.utils.MyCrashHandler

class MyBleApp : Application() {


    override fun onCreate() {
        super.onCreate()

        // https://github.com/JeremyLiao/LiveEventBus/blob/master/docs/config.md
        LiveEventBus.config()
                .lifecycleObserverAlwaysActive(true)
                .enableLogger(false)

        MyCrashHandler.newInstance().init(this)
    }


}