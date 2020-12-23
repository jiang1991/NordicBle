package com.lepu.nordicble.objs

import android.app.ProgressDialog
import android.content.Context
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.Utils
import com.lepu.nordicble.R
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

abstract class BaseObserver<BEAN> : Observer<BEAN> {

    private val result by lazy { BaseObsBean<BEAN>() }
    private val loadingDialog
            by lazy {
                ProgressDialog(ActivityUtils.getTopActivity())
            }

    constructor(
        loadingTip: String = Utils.getApp().getString(R.string.net_request_data)
    ) : super() {
        loadingDialog.setMessage(loadingTip)
        setLoadingDialogShowVisiable(true)
    }

    override fun onComplete() {

    }

    override fun onSubscribe(d: Disposable) {

    }

    private fun setLoadingDialogShowVisiable(isShow: Boolean = true) {
        if (isShow) {
            loadingDialog.takeIf { it.isShowing.not() }?.show()
        } else {
            loadingDialog.takeIf { it.isShowing }?.dismiss()
        }
    }

    override fun onNext(bean: BEAN) {
        setLoadingDialogShowVisiable(false)
        result.bean = bean
        onResult(result)
    }

    override fun onError(e: Throwable) {
        setLoadingDialogShowVisiable(false)
        result.error = e
        onResult(result)
    }

    abstract fun onResult(result: BaseObsBean<BEAN>)


}


class BaseObsBean<BEAN>(var bean: BEAN? = null, var error: Throwable? = null)
