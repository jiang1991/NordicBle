package com.lepu.nordicble.socket.objs;


import com.lepu.nordicble.vals.RunVarsKt;

import java.io.UnsupportedEncodingException;

import static com.lepu.nordicble.socket.objs.SocketMsgConst.ecgQueue;
import static com.lepu.nordicble.socket.objs.SocketMsgConst.getEr1Config;
import static com.lepu.nordicble.socket.objs.SocketMsgConst.getKcaConfig;
import static com.lepu.nordicble.socket.objs.SocketMsgConst.getOxiConfig;
import static com.lepu.nordicble.socket.objs.SocketMsgConst.getStatus;
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
     * @param id
     * @return
     */
    public static byte[] uploadInfoCmd() {


        /**
         * 1 -> 模块个数
         *
         * 模块信息
         *  1 -> 模块索引
         *  64 -> id
         *  64 -> config
         *      1 -> lead count
         *      4 -> lead info
         *      2 -> sample rate
         *      10 -> 增益信息
         *          4 -> max 采样值 int
         *          4 -> max 毫伏值 int
         *          2 -> 基线 0x00
         *
         */
        int moduleLen = 1+64+64;

        byte[] content = new byte[1+ moduleLen*3];

        // 模块个数
        content[0] = 0x03;

        // 模块索引：1: 心电 2：血压 3：血氧 4：体温
        // 模块1： 心电
        int index_1 = 1;
        content[index_1] = 0x01;
        if (RunVarsKt.getEr1Sn() != null) {
            try {
                byte[] deviceId  = RunVarsKt.getEr1Sn().getBytes("UTF-8");
                System.arraycopy(deviceId, 0, content, index_1 + 1, Math.min(deviceId.length, 64));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }
        System.arraycopy(getEr1Config(), 0, content, index_1 + 1 + 64, 64);
        // 模块2： 血氧
        int index_2 = 1 + moduleLen;
        content[index_2] = 0x02;
        if (RunVarsKt.getOxySn() != null) {
            try {
                byte[] deviceId  = RunVarsKt.getOxySn().getBytes("UTF-8");
                System.arraycopy(deviceId, 0, content, index_2 + 1, Math.min(deviceId.length, 64));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }
        System.arraycopy(getOxiConfig(), 0, content, index_2 + 1 + 64, 64);

        // 模块3： 血压计
        int index_3 = 1 + moduleLen*2;
        content[index_3] = 0x03;
        if (RunVarsKt.getKcaSn() != null) {
            try {
                byte[] deviceId  = RunVarsKt.getKcaSn().getBytes("UTF-8");
                System.arraycopy(deviceId, 0, content, index_3 + 1, Math.min(deviceId.length, 64));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }
        System.arraycopy(getKcaConfig(), 0, content, index_3 + 1 + 64, 64);

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

    public static byte[] statusResponse() {
        SocketMsg msg = new SocketMsg(SocketMsg.TYPE_CLIENT, SocketMsg.CMD_STATUS, getStatus());

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
        content[6] = (byte) (RunVarsKt.getBleConnected() ? 0x01 : 0x02);
        System.arraycopy(invalidEcgdata(), 0, content, 7, 2*len);

        SocketMsg msg = new SocketMsg(SocketMsg.TYPE_CLIENT, SocketMsg.CMD_UPLOAD_ECG, content);

        return msg.toBytes();
    }

    public static byte[] uploadOxyCmd(int spo2, byte[] oxyBytes) {
        byte[] content = new byte[5 + oxyBytes.length];
        content[0] = (byte) oxyQueue;
        content[1] = (byte) (oxyQueue >> 8);

        oxyQueue++;

        content[2] = (byte) spo2;
        content[3] = (byte) (spo2 >> 8);
        content[4] = (byte) oxyBytes.length;

        System.arraycopy(oxyBytes, 0, content, 5, oxyBytes.length);
        SocketMsg msg = new SocketMsg(SocketMsg.TYPE_CLIENT, SocketMsg.CMD_UPLOAD_OXY, content);

        return msg.toBytes();
    }

    public static byte[] invalidOxyCmd() {
        int len = 125;
        byte[] content = new byte[5 + len];
        content[0] = (byte) oxyQueue;
        content[1] = (byte) (oxyQueue >> 8);

        oxyQueue++;

        content[2] = (byte) 0x00;
        content[3] = (byte) 0x00;
        content[4] = (byte) len;

        SocketMsg msg = new SocketMsg(SocketMsg.TYPE_CLIENT, SocketMsg.CMD_UPLOAD_OXY, content);

        return msg.toBytes();
    }


    public static byte[] heartbeatCmd() {

        SocketMsg msg = new SocketMsg(SocketMsg.TYPE_CLIENT, SocketMsg.CMD_HEARTBEAT, new byte[0]);

        return msg.toBytes();
    }
}
