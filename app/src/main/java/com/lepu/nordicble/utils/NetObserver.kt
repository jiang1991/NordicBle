package com.lepu.nordicble.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.lepu.nordicble.annotation.CheckVersionType
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class NetObserver(private var lifecycleOwner: LifecycleOwner) : LifecycleObserver {


    private val compositeDis by lazy { CompositeDisposable() }

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun onLifecycleCreate() {
        LogUtils.i("${lifecycleOwner.javaClass.simpleName}  onLifecycleCreate")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onLifecycleDestory() {
        LogUtils.i("${lifecycleOwner.javaClass.simpleName} onLifecycleDestory")
        compositeDis.takeIf { it.isDisposed.not() }?.let {
            it.clear()
            it.dispose()
        }
    }

    fun checkVersion(@CheckVersionType key: String = CheckVersionType.WIRELESS) {
        NetUtils.retrofit.create(NetInterface::class.java)
            .checkVersion(NetUtils.getCheckVersion(key))
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(Consumer { bean ->
                LogUtils.json(GsonUtils.toJson(bean))
            }, Consumer { error ->
                error.printStackTrace()
                LogUtils.i("error->${error.message}")
            })?.let {
                compositeDis.add(it)
            }
    }


}