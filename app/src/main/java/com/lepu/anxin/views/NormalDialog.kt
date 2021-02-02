package com.lepu.anxin.views

import com.blankj.utilcode.util.LogUtils

import com.lepu.anxin.R
import kotlinx.android.synthetic.main.dialog_normal_layoutview.*

class NormalDialog :  BaseFullScreenDialog() {
    private var sureAction: (() -> Unit)? = null


    fun setSureAction(sureAction: (() -> Unit)? = null) {
        this.sureAction = sureAction
    }

    fun setMsgContent(msg: String = "") {
        LogUtils.i("msg->${msg}")
        tv_normal_layout_view_msg.setText(msg)
    }

    override fun isCanceledOnTouchOutSide(): Boolean = false

    override fun initView() {
        tv_cancel.setOnClickListener {
            dismiss()
        }

        tv_sure.setOnClickListener {
            sureAction?.invoke()
            dismiss()
        }

    }

    override fun getDialogLayoutId(): Int = R.layout.dialog_normal_layoutview


}