package com.lepu.anxin

import android.app.Application
import androidx.room.Room
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.anxin.room.MyDatabase

class MyBleApp : Application() {


    override fun onCreate() {
        super.onCreate()

        // https://github.com/JeremyLiao/LiveEventBus/blob/master/docs/config.md
        LiveEventBus.config()
                .lifecycleObserverAlwaysActive(true)
                .enableLogger(false)

//        MyCrashHandler.newInstance().init(this)
        val db = Room.databaseBuilder(this, MyDatabase::class.java, "anxinbao-db").build()
    }


}