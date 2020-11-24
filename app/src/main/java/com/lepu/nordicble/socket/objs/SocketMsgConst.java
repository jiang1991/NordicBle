package com.lepu.nordicble.socket.objs;

import android.util.Log;


import com.lepu.nordicble.vals.RunVarsKt;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.lepu.nordicble.utils.HexString.hexToBytes;

public class SocketMsgConst {

    /**
     * 版本信息
     * 软件版本： 1
     * 协议版本： 1
     */
    public static byte[] SOFTWARE_VERSION = {0x02, 0x00, 0x00, 0x00};
    public static byte[] PROTOCOL_VERSION = {0x02, 0x00, 0x00, 0x00};

    public static int ecgQueue = 0;
    public static int oxyQueue = 0;

    public static final int MSG_CONNECT = 0x01;
    public static final int MSG_RESPONSE = 0x02;

    private static final String SECRET = "B783301CEE574E5C929DCAB1EA44E494";
    private static final byte[] getSecret = hexToBytes(SECRET);
    public static byte[] getToken(byte[] s) {
        byte[] temp = new byte[32];

        System.arraycopy(s, 0, temp, 0, 16);
        System.arraycopy(getSecret, 0, temp, 16, 16);

        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(temp);
            return bytes;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Log.d("TAG", e.toString());
            return null;
        }
    }

    /**
     * 64 -> config
     *
     * 1 -> lead count
     * 4 -> lead info
     * 2 -> sample rate
     * 10 -> 增益信息
     *     4 -> max 采样值 int
     *     4 -> max 毫伏值 int
     *     2 -> 基线 0x00
     * @return
     */
    public static byte[] getEr1Config() {
        byte[] bytes = new byte[64];
        bytes[0] = 0x01;

        /**
         * 0x00000001 代表 I 导联；
         * 0x00000003 代表 I、II 导联
         * 0x0000000A 代表 II/aVR 导联
         * 0x00040000 代表一个未知导联
         */
        bytes[1] = (byte) RunVarsKt.getLead();
        bytes[2] = (byte) (RunVarsKt.getLead() >> 8);
        bytes[3] = (byte) (RunVarsKt.getLead() >> 16);
        bytes[4] = (byte) (RunVarsKt.getLead() >> 24);
        bytes[5] = 0x7d;

        // 6000v / standard1mV = (float) ((1.0035 * 1800) / (4096 * 178.74));
        int max = 2432;
        int voltage = 6000;
        bytes[7] = (byte) voltage;
        bytes[8] = (byte) (voltage >> 8);
        bytes[9] = (byte) (voltage >> 16);
        bytes[10] = (byte) (voltage >> 24);

        bytes[11] = (byte) max;
        bytes[12] = (byte) (max >> 8);
        bytes[13] = (byte) (max >> 16);
        bytes[14] = (byte) (max >> 24);

        bytes[15] = 0x00;
        bytes[16] = 0x00;

        return bytes;
    }

    /**
     * 64 -> config
     * 1 -> type // 3 源动血氧
     * 2 -> sample rate
     *
     * @return
     */
    public static byte[] getOxiConfig() {
        byte[] bytes = new byte[64];
        bytes[0] = 0x03;
        bytes[1] = 0x7d;


        return bytes;
    }


    /**
     * 64 -> config
     * 1 -> type // 2 康康血压
     * 1 -> unit // 1 mmHg
     * 1 -> mode // 1手动  2 自动
     * day
     *      2 -> start
     *      2 -> end
     *      2 -> interval
     *
     * night
     *      2 -> start
     *      2-> end
     *      2 -> interval
     * @return
     */
    public static byte[] getKcaConfig() {
        byte[] bytes = new byte[64];
        bytes[0] = 0x02;
        bytes[1] = 0x01;
        bytes[2] = 0x02;
        // 6 -22, 30m
        bytes[3] = 0x06;
        bytes[4] = 0x00;

        bytes[5] = 0x16;
        bytes[6] = 0x00;

        bytes[7] = 0x1E;
        bytes[8] = 0x00;

        // 22 - 6, 60
        bytes[9] = 0x16;
        bytes[10] = 0x00;

        bytes[11] = 0x06;
        bytes[12] = 0x00;

        bytes[13] = 0x3c;
        bytes[14] = 0x00;

        return bytes;
    }

    /**
     * 获取设备网络状态
     * @return
     */
    public static byte[] getStatus() {
        int moduleLen = 8+64;
        byte[] bytes = new byte[18 + moduleLen*3];

        // 收发器 18 byte
        // 网络 1byte: 1:WIFI 2:4G
        bytes[0] = 0x01;
        // 收发器的网络质量
        int signal = 100;
        bytes[1] = (byte) ( signal);

        // 收发器电池 0x00 电池供电/0x10 低电量报警/0x01 充电中
        bytes[2] = (byte) RunVarsKt.getBatteryState();
        bytes[3] = (byte) (RunVarsKt.getBattery());

        // 网络中断次数
        bytes[4] = (byte) RunVarsKt.getNetworkErrors();

        // gps
        bytes[5] = 0x01;
        int longitude = (int) (113.8840 * 1000000);
        int latitude = (int) (22.5553 * 1000000);
        // Longitude: 4byte
        // Latitude: 4byte
        bytes[6] = (byte) longitude;
        bytes[7] = (byte) (longitude >> 8);
        bytes[8] = (byte) (longitude >> 16);
        bytes[9] = (byte) (longitude >> 24);

        bytes[10] = (byte) latitude;
        bytes[11] = (byte) (latitude >> 8);
        bytes[12] = (byte) (latitude >> 16);
        bytes[13] = (byte) (latitude >> 24);

        // 模块个数
        bytes[14] = 0x03;

        // 每个模块8 + 64 byte
        // module 1
        // 1 正常/>1 异常 (2 表示断开，异常类型可扩展)
        bytes[18] = 0x01;
        bytes[19] = (byte) (RunVarsKt.getEr1Conn() ? 0x01 : 0x02);
        // battery
        /**
         * 0x00 : 电池供电
         * 0x10 : 低电量
         */
        if (RunVarsKt.getEr1Battery() >= 5 || RunVarsKt.getEr1Battery() == 0) {
            bytes[20] = 0x00;
        } else {
            bytes[20] = 0x10;
        }
        bytes[21] = (byte) RunVarsKt.getEr1Battery();
        bytes[22] = (byte) RunVarsKt.getEr1BleError();

        if (RunVarsKt.getBleSN() != null) {
            try {
                byte[] deviceId = RunVarsKt.getEr1Sn().getBytes("UTF-8");
                System.arraycopy(deviceId, 0, bytes, 26, Math.min(deviceId.length, 64));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        // module 2
        bytes[18 + moduleLen] = 0x01;
        bytes[19 + moduleLen] = (byte) (RunVarsKt.getOxyConn() ? 0x01 : 0x02);
        // battery
        /**
         * 0x00 : 电池供电
         * 0x10 : 低电量
         */
        if (RunVarsKt.getOxyBattery() >= 5 || RunVarsKt.getOxyBattery() == 0) {
            bytes[20 + moduleLen] = 0x00;
        } else {
            bytes[20 + moduleLen] = 0x10;
        }
        bytes[21 + moduleLen] = (byte) RunVarsKt.getOxyBattery();
        bytes[22 + moduleLen] = (byte) RunVarsKt.getOxyBleError();

        if (RunVarsKt.getBleSN() != null) {
            try {
                byte[] deviceId = RunVarsKt.getOxySn().getBytes("UTF-8");
                System.arraycopy(deviceId, 0, bytes, 26 + moduleLen, Math.min(deviceId.length, 64));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        // module 3
        bytes[18 + moduleLen*2] = 0x01;
        bytes[19 + moduleLen*2] = (byte) (RunVarsKt.getKcaConn() ? 0x01 : 0x02);
        // battery
        /**
         * 0x00 : 电池供电
         * 0x10 : 低电量
         */
        if (RunVarsKt.getKcaBattery() >= 5 || RunVarsKt.getKcaBattery() == 0) {
            bytes[20 + moduleLen*2] = 0x00;
        } else {
            bytes[20 + moduleLen*2] = 0x10;
        }
        bytes[21 + moduleLen*2] = (byte) RunVarsKt.getKcaBattery();
        bytes[22 + moduleLen*2] = (byte) RunVarsKt.getKcaBleError();

        if (RunVarsKt.getBleSN() != null) {
            try {
                byte[] deviceId = RunVarsKt.getKcaSn().getBytes("UTF-8");
                System.arraycopy(deviceId, 0, bytes, 26 + moduleLen*2, Math.min(deviceId.length, 64));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return bytes;

    }
}
