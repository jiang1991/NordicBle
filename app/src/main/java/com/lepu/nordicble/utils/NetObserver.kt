package com.lepu.nordicble.utils

import android.app.ProgressDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.arialyy.aria.core.Aria
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.*
import com.lepu.nordicble.BuildConfig
import com.lepu.nordicble.R
import com.lepu.nordicble.annotation.CheckVersionType
import com.lepu.nordicble.bean.CheckVersionBean
import com.lepu.nordicble.views.NormalDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers

import java.io.File
import java.util.concurrent.TimeUnit

class NetObserver(private var lifecycleOwner: LifecycleOwner) : LifecycleObserver {

    var checkVerBean: CheckVersionBean? = null

    private val checkDialog by lazy {
        ProgressDialog(ActivityUtils.getTopActivity())
            .also {
                it.setMessage(Utils.getApp().getString(R.string.checking))
                it.setCancelable(true)
            }


    }

    private var taskId: Long? = null
    private val compositeDis by lazy { CompositeDisposable() }

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun onLifecycleCreate() {
        LogUtils.i("${lifecycleOwner.javaClass.simpleName}  onLifecycleCreate")
        Aria.download(lifecycleOwner).register()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onLifecycleDestory() {
        LogUtils.i("${lifecycleOwner.javaClass.simpleName} onLifecycleDestory")
        compositeDis.takeIf { it.isDisposed.not() }?.let {
            it.clear()
            it.dispose()
        }

        cancelDownloadTask()
        Aria.download(lifecycleOwner).unRegister()

    }

    private fun cancelDownloadTask() {
        taskId?.let { taskId ->
            Aria.download(lifecycleOwner).load(taskId).cancel()
        }
    }

    private fun setCheckDialogShow(isShow: Boolean = true) {

        if (isShow) {
            checkDialog.takeIf { it.isShowing.not() }?.show()
        } else {
            checkDialog.takeIf { it.isShowing }?.dismiss()
        }
    }

    fun checkVersion(@CheckVersionType key: String = CheckVersionType.WIRELESS) {

        setCheckDialogShow(true)
        NetUtils.retrofit.create(NetInterface::class.java)
            .checkVersion(NetUtils.getCheckVersion(key))
            ?.delay(500L, TimeUnit.MILLISECONDS)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(Consumer { bean ->
                setCheckDialogShow(false)
                checkVerBean = bean
                LogUtils.json(GsonUtils.toJson(bean))
                var currentCode = AppUtils.getAppVersionCode()
                var cloundCode = bean.version
                if (currentCode < cloundCode) {
                    LogUtils.i("服务器版本高于本地版本,需要更新")

                    PermissionUtils.permission(
                        PermissionConstants.LOCATION,
                        PermissionConstants.STORAGE
                    ).callback(object : PermissionUtils.SimpleCallback {
                        override fun onGranted() {

                            NormalDialog().also {
                                it.setMsgContent(
                                    String.format(
                                        Utils.getApp().getString(R.string.update_version_tip),
                                        AppUtils.getAppVersionName(), bean.versionName
                                    )
                                )
                                it.setSureAction {
                                    var  apkFileName=  getFileName(bean.fileUrl)
                                    taskId = Aria.download(lifecycleOwner)
                                        .load(bean.fileUrl)
                                        .setFilePath(createApkStorePath(apkFileName))
                                        .create()
                                }
                            }.show()
                        }

                        override fun onDenied() {

                        }
                    }).request()
                } else {
                    ToastUtils.showShort(R.string.newest_app_version)
                }

            }, Consumer { error ->
                setCheckDialogShow(false)
                error.printStackTrace()
                LogUtils.i("error->${error.message}")
            })?.let {
                compositeDis.add(it)
            }
    }


    private fun getFileName(pathAndName: String): String  {
        var start = pathAndName.lastIndexOf("/")
        var end = pathAndName.lastIndexOf(".")
        if (start != -1 && end != -1) {
            return pathAndName.substring(start + 1, end)
        } else {
            return  "apk"
        }
    }

    private fun createApkStorePath(apkFileName: String): String {
        var temPath = PathUtils.getExternalStoragePath() + "/apkversion/${BuildConfig.FLAVOR}/"
        FileUtils.createOrExistsDir(temPath)

        var apkFile = File(temPath + "${apkFileName}.apk")
        if (apkFile.exists()) {
            apkFile.delete()
        }
        var path = apkFile.absolutePath
        LogUtils.i("apkPath->${path}")
        return path
    }


}