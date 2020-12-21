package com.lepu.nordicble.socket.objs;


import com.blankj.utilcode.util.LogUtils;
import com.lepu.nordicble.ble.cmd.KcaBleResponse;
import com.lepu.nordicble.utils.ByteArrayKt;
import com.lepu.nordicble.vals.RunVarsKt;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;

import static com.lepu.nordicble.socket.objs.SocketMsgConst.PROTOCOL_VERSION;
import static com.lepu.nordicble.socket.objs.SocketMsgConst.ecgQueue;
import static com.lepu.nordicble.socket.objs.SocketMsgConst.getEr1Config;
import static com.lepu.nordicble.socket.objs.SocketMsgConst.getKcaConfig;
import static com.lepu.nordicble.socket.objs.SocketMsgConst.getOxiConfig;
import static com.lepu.nordicble.socket.objs.SocketMsgConst.getStatus;
import static com.lepu.nordicble.socket.objs.SocketMsgConst.kcaQueue;
import static com.lepu.nordicble.socket.objs.SocketMsgConst.oxyQueue;


public class SocketCmd {

    public static byte[] tokenCmd() {
        SocketMsg msg = new SocketMsg(SocketMsg.TYPE_CLIENT, SocketMsg.CMD_TOKEN, SocketMsg.tokenRandom);

        return msg.toBytes();
    }

    public static byte[] loginCmd(byte[] token, byte[] deviceId) {

        /**
         *  md5 token
         *  64 device id
         *  4 版本信息
         *  4 协议版本
         */
        byte[] content = new byte[16+64+4+4+128];
        System.arraycopy(token, 0, content, 0, 16);
        System.arraycopy(deviceId, 0, content, 16, Math.min(deviceId.length, 64));
        System.arraycopy(SocketMsgConst.SOFTWARE_VERSION, 0, content, 80, 4);
        System.arraycopy(SocketMsgConst.PROTOCOL_VERSION, 0, content, 84, 4);


        SocketMsg msg = new SocketMsg(SocketMsg.TYPE_CLIENT, SocketMsg.CMD_LOGIN, content);

        return msg.toBytes();
    }

    /**
     * 上传模块信息
     * @return
     */
    public static byte[] uploadInfoCmd() {

        int moduleSize = (RunVarsKt.getHasEr1() ? 1 : 0)
                        + (RunVarsKt.getHasOxy() ? 1 : 0)
                        + (RunVarsKt.getHasKca() ? 1 : 0);

        int moduleLen = 1+64+64;

        byte[] content = new byte[1+ moduleLen*moduleSize];

        // 模块个数
        content[0] = (byte) moduleSize;
        int currentIndex = 1;

        // 模块索引：1: 心电 2：血压 3：血氧 4：体温
        // 模块1： 心电
        if (RunVarsKt.getHasEr1()) {
            byte[] moduleEr1 = new byte[moduleLen];

            moduleEr1[0] = 0x01;
            if (RunVarsKt.getEr1Sn() != null) {
                try {
                    byte[] deviceId  = RunVarsKt.getEr1Sn().getBytes("UTF-8");
                    System.arraycopy(deviceId, 0, moduleEr1, 1, Math.min(deviceId.length, 64));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            System.arraycopy(getEr1Config(), 0, moduleEr1, 1 + 64, 64);

            System.arraycopy(moduleEr1, 0, content, currentIndex, moduleLen);
            currentIndex += moduleLen;
        }


        // 模块2： 血氧
        if (RunVarsKt.getHasOxy()) {
            byte[] moduleOxy = new byte[moduleLen];

            moduleOxy[0] = 0x03;
            if (RunVarsKt.getOxySn() != null) {
                try {
                    byte[] deviceId  = RunVarsKt.getOxySn().getBytes("UTF-8");
                    System.arraycopy(deviceId, 0, moduleOxy, 1, Math.min(deviceId.length, 64));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            System.arraycopy(getOxiConfig(), 0, moduleOxy, 1 + 64, 64);

            System.arraycopy(moduleOxy, 0, content, currentIndex, moduleLen);
            currentIndex += moduleLen;
        }


        // 模块3： 血压计
        if (RunVarsKt.getHasKca()) {
            byte[] moduleKca = new byte[moduleLen];

            moduleKca[0] = 0x02;
            if (RunVarsKt.getEr1Sn() != null) {
                try {
                    byte[] deviceId  = RunVarsKt.getKcaSn().getBytes("UTF-8");
                    System.arraycopy(deviceId, 0, moduleKca, 1, Math.min(deviceId.length, 64));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            System.arraycopy(getKcaConfig(), 0, moduleKca, 1 + 64, 64);

            System.arraycopy(moduleKca, 0, content, currentIndex, moduleLen);
            currentIndex += moduleLen;
        }

        SocketMsg msg = new SocketMsg(SocketMsg.TYPE_CLIENT, SocketMsg.CMD_UPLOAD_INFO, content);

        return msg.toBytes();
    }

    public static byte[] bindResponse(boolean isSuccess) {
        byte[] content = new byte[1];
        content[0] = (byte) (isSuccess? 0x01 : 0x00);

        SocketMsg msg = new SocketMsg(SocketMsg.TYPE_CLIENT, SocketMsg.CMD_BIND, content);

        return msg.toBytes();
    }

    public static byte[] unbindResponse(boolean isSuccess) {
        byte[] content = new byte[1];
        content[0] = (byte) (isSuccess? 0x01 : 0x00);

        SocketMsg msg = new SocketMsg(SocketMsg.TYPE_CLIENT, SocketMsg.CMD_UNBIND, content);

        return msg.toBytes();
    }

    public static byte[] changeLeadResponse(boolean isSuccess) {
        byte[] content = new byte[1];
        content[0] = (byte) (isSuccess? 0x01 : 0x00);

        SocketMsg msg = new SocketMsg(SocketMsg.TYPE_CLIENT, SocketMsg.CMD_CHANGE_LEAD, content);

        return msg.toBytes();
    }

    public static byte[] kcaBpConfigResponse(boolean isSuccess) {
        byte[] content = new byte[1];
        content[0] = (byte) (isSuccess? 0x01 : 0x00);

        SocketMsg msg = new SocketMsg(SocketMsg.TYPE_CLIENT, SocketMsg.CMD_KCA_BP_MEASURE_CONFIG, content);

        return msg.toBytes();
    }

    public static byte[] statusResponse() {
        SocketMsg msg = new SocketMsg(SocketMsg.TYPE_CLIENT, SocketMsg.CMD_STATUS, getStatus());
//        LogUtils.d("模块状态: ", ByteArrayKt.toHex(msg.toBytes()));
        return msg.toBytes();
    }

    public static byte[] uploadEcgCmd(int hr, boolean lead, byte[] ecgdata) {
        int len = ecgdata.length / 2;
        byte[] content = new byte[6+1+len*2];
        content[0] = (byte) ecgQueue;
        content[1] = (byte) (ecgQueue >> 8);

        ecgQueue++;

        // hr
        content[2] = (byte) hr;
        content[3] = (byte) (hr >> 8);

        // ecg count
        content[4] = (byte) len;
        content[5] = (byte) (len >> 8);

        // lead
        content[6] = (byte) (lead ? 0x00 : 0x01);
        System.arraycopy(ecgdata, 0, content, 7, 2*len);

        SocketMsg msg = new SocketMsg(SocketMsg.TYPE_CLIENT, SocketMsg.CMD_UPLOAD_ECG, content);

        return msg.toBytes();
    }

    public static byte[] invalidEcgdata() {
        byte[] bytes = new byte[125*2];
        for (int i = 0; i<125*2; i+=2) {
            bytes[i] = (byte) 0xFF;
            bytes[i+1] = (byte) 0x7F;
        }
        return bytes;
    }

    /**
     * 无效值补点
     * 0x00 正常
     * 0x01 导联脱落
     * 0x02 蓝牙掉线
     * @return
     */
    public static byte[] invalidEcgCmd() {
        int len = 125;
        byte[] content = new byte[6+1+len*2];
        content[0] = (byte) ecgQueue;
        content[1] = (byte) (ecgQueue >> 8);

        ecgQueue++;

        // hr
        content[2] = (byte) 0x00;
        content[3] = (byte) 0x00;

        // ecg count
        content[4] = (byte) len;
        content[5] = (byte) (len >> 8);

        // lead
        content[6] = (byte) (RunVarsKt.getEr1Conn() ? 0x01 : 0x02);
        System.arraycopy(invalidEcgdata(), 0, content, 7, 2*len);

        SocketMsg msg = new SocketMsg(SocketMsg.TYPE_CLIENT, SocketMsg.CMD_UPLOAD_ECG, content);

        return msg.toBytes();
    }

    public static byte[] uploadOxyInfoCmd(int spo2, int pr, int pi, boolean lead, int motion) {
        byte[] content = new byte[9];
        content[0] = (byte) oxyQueue;
        content[1] = (byte) (oxyQueue >> 8);

        oxyQueue++;
        content[2] = (byte) spo2;
        content[3] = (byte) pr;
        content[4] = (byte) (pr >> 8);
        content[5] = (byte) (pi * 10);
        content[6] = (byte) ((pi * 10) >> 8);
        content[7] = (byte) (lead ? 0x00 : 0x01);
        content[8] = (byte) motion;

        SocketMsg msg = new SocketMsg(SocketMsg.TYPE_CLIENT, SocketMsg.CMD_UPLOAD_OXY_INFO, content);

        return msg.toBytes();

    }

    public static byte[] invalidOxyInfoCmd() {
        byte[] content = new byte[9];
        content[0] = (byte) oxyQueue;
        content[1] = (byte) (oxyQueue >> 8);

        oxyQueue++;

        SocketMsg msg = new SocketMsg(SocketMsg.TYPE_CLIENT, SocketMsg.CMD_UPLOAD_OXY_INFO, content);

        return msg.toBytes();
    }

    public static byte[] uploadOxyWaveCmd(byte[] oxyBytes) {
        byte[] content = new byte[4 + oxyBytes.length];
        content[0] = (byte) oxyQueue;
        content[1] = (byte) (oxyQueue >> 8);

        oxyQueue++;

        content[2] = (byte) oxyBytes.length;
        content[3] = (byte) (oxyBytes.length >> 8);

        System.arraycopy(oxyBytes, 0, content, 4, oxyBytes.length);
        SocketMsg msg = new SocketMsg(SocketMsg.TYPE_CLIENT, SocketMsg.CMD_UPLOAD_OXY_WAVE, content);

        return msg.toBytes();
    }

    public static byte[] invalidOxyWaveCmd() {
        int len = 125;
        byte[] content = new byte[4 + len];
        content[0] = (byte) oxyQueue;
        content[1] = (byte) (oxyQueue >> 8);

        oxyQueue++;

        content[2] = (byte) len;
        content[3] = (byte) (len >> 8);

        SocketMsg msg = new SocketMsg(SocketMsg.TYPE_CLIENT, SocketMsg.CMD_UPLOAD_OXY_WAVE, content);

        return msg.toBytes();
    }

    public static byte[] uploadKcaState(int state, int bp) {
        byte[] content = new byte[5];
        content[0] = (byte) kcaQueue;
        content[1] = (byte) (kcaQueue >> 8);

        kcaQueue++;

        content[2] = (byte) state;
        content[3] = (byte) bp;
        content[4] = (byte) (bp >> 8);

        SocketMsg msg = new SocketMsg(SocketMsg.TYPE_CLIENT, SocketMsg.CMD_UPLOAD_BP_STATE, content);

        return msg.toBytes();
    }

    public static byte[] uploadKcaResult(KcaBleResponse.KcaBpResult result) {
        byte[] content = new byte[18];
        content[0] = (byte) kcaQueue;
        content[1] = (byte) (kcaQueue >> 8);

        kcaQueue++;

        content[2] = (byte) result.sys;
        content[3] = (byte) (result.sys >> 8);
        content[4] = (byte) result.dia;
        content[5] = (byte) (result.dia >> 8);
        int mean = (result.sys + result.dia)/2;
        content[6] = (byte) mean;
        content[7] = (byte) (mean >> 8);
        content[8] = (byte) result.pr;
        content[9] = (byte) (result.pr >> 8);
        Calendar c = Calendar.getInstance();

        c.setTimeInMillis(result.date);
        content[10] = (byte) (c.get(Calendar.YEAR) / 100);
        content[11] = (byte) (c.get(Calendar.YEAR) % 100);
        content[12] = (byte) (c.get(Calendar.MONTH) + 1);
        content[13] = (byte) c.get(Calendar.DAY_OF_MONTH);
        content[14] = (byte) c.get(Calendar.HOUR_OF_DAY);
        content[15] = (byte) c.get(Calendar.MINUTE);
        content[16] = (byte) c.get(Calendar.SECOND);
//        content[17] = (byte) state;
        LogUtils.d(c.toString());
        LogUtils.d(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        SocketMsg msg = new SocketMsg(SocketMsg.TYPE_CLIENT, SocketMsg.CMD_UPLOAD_BP_RESULT, content);

        return msg.toBytes();
    }


    public static byte[] heartbeatCmd() {

        SocketMsg msg = new SocketMsg(SocketMsg.TYPE_CLIENT, SocketMsg.CMD_HEARTBEAT, new byte[0]);

        return msg.toBytes();
    }
}
