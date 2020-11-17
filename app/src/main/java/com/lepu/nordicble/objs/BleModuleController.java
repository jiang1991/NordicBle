package com.lepu.nordicble.objs;

import android.os.Handler;
import android.os.Message;

import com.lepu.nordicble.fragments.Er1Fragment;
import com.lepu.nordicble.fragments.KcaFragment;
import com.lepu.nordicble.fragments.O2Fragment;

public class BleModuleController {

    public static final int MSG_ADD_DEVICE = 1114;

    private static Handler mHandler;

    public static void setHandler(Handler handler) {
        mHandler = handler;
    }

    private static KcaFragment kcaFragment;
    private static Er1Fragment er1Fragment;
    private static O2Fragment o2Fragment;

    public static Bluetooth er1Device;
    public static Bluetooth o2Device;
    public static Bluetooth kcaDevice;

    public static void addFragment(KcaFragment fragment) {
        kcaFragment = fragment;
    }

    public static void addFragment(Er1Fragment fragment) {
        er1Fragment = fragment;
    }

    public static void addFragment(O2Fragment fragment) {
        o2Fragment = fragment;
    }

    public static void addDevice(Bluetooth b) {
        if (b == null) {
            return;
        }

        switch (b.getModel()) {
            case Bluetooth.MODEL_ER1:
                if (er1Fragment != null) {
                    return;
                }
                break;

            case Bluetooth.MODEL_CHECKO2:
                if (o2Fragment != null) {
                    return;
                }
                break;

            case Bluetooth.MODEL_KCA:
                if (kcaFragment != null) {
                    return;
                }
                break;

            default:
                return;
        }
        Message msg = Message.obtain();
        msg.what = MSG_ADD_DEVICE;
        msg.obj = b;
        mHandler.sendMessage(msg);
    }

}
