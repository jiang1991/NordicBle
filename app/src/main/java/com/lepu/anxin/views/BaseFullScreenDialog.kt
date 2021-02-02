package com.lepu.anxin.views

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.view.Window
import com.blankj.utilcode.util.ActivityUtils

import com.lepu.anxin.R

abstract class BaseFullScreenDialog(context: Context = ActivityUtils.getTopActivity(), themeResId: Int = R.style.fullscreenDialogStyle) : Dialog(context, themeResId) {

    constructor(context: Context = ActivityUtils.getTopActivity()) : this(context, R.style.fullscreenDialogStyle) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(getDialogLayoutId())
        setCanceledOnTouchOutside(isCanceledOnTouchOutSide())
        initWindowParams()

        initView()
    }

    protected fun initWindowParams(){
        window?.let {
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            it.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)


        }
    }

    abstract fun isCanceledOnTouchOutSide(): Boolean

    abstract fun initView()

    abstract fun getDialogLayoutId(): Int


}