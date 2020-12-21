package com.lepu.nordicble.socket.objs;

import android.util.Log;


import com.blankj.utilcode.util.LogUtils;
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
    public static int kcaQueue = 0;

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

        int moduleSize = (RunVarsKt.getHasEr1() ? 1 : 0)
                + (RunVarsKt.getHasOxy() ? 1 : 0)
                + (RunVarsKt.getHasKca() ? 1 : 0);

        LogUtils.d(RunVarsKt.getHasEr1(), RunVarsKt.getHasOxy(), RunVarsKt.getHasKca(), moduleSize);

        int moduleLen = 8+64;
        byte[] bytes = new byte[18 + moduleLen*moduleSize];

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

        // 模块个数
        bytes[5] = (byte) moduleSize;
        // reserve 1 byte

        // gps
        bytes[7] = 0x01;
        int longitude = (int) (113.8840 * 1000000);
        int latitude = (int) (22.5553 * 1000000);
        // Longitude: 4byte
        // Latitude: 4byte
        bytes[8] = (byte) longitude;
        bytes[9] = (byte) (longitude >> 8);
        bytes[10] = (byte) (longitude >> 16);
        bytes[11] = (byte) (longitude >> 24);

        bytes[12] = (byte) latitude;
        bytes[13] = (byte) (latitude >> 8);
        bytes[14] = (byte) (latitude >> 16);
        bytes[15] = (byte) (latitude >> 24);
        // reserve 2 byte

        int currentIndex = 18;

        // 每个模块8 + 64 byte

        if (RunVarsKt.getHasEr1()) {
            // module 1
            // 1 正常/>1 异常 (2 表示断开，异常类型可扩展)
            byte[] moduleEr1 = new byte[moduleLen];
            moduleEr1[0] = 0x01;
            moduleEr1[1] = (byte) (RunVarsKt.getEr1Conn() ? 0x01 : 0x02);
            // battery
            /**
             * 0x00 : 电池供电
             * 0x10 : 低电量
             */
            if (RunVarsKt.getEr1Battery() >= 5 || RunVarsKt.getEr1Battery() == 0) {
                moduleEr1[2] = 0x00;
            } else {
                moduleEr1[2] = 0x10;
            }
            moduleEr1[3] = (byte) RunVarsKt.getEr1Battery();
            moduleEr1[4] = (byte) RunVarsKt.getEr1BleError();

            if (RunVarsKt.getEr1Sn() != null) {
                try {
                    byte[] deviceId = RunVarsKt.getEr1Sn().getBytes("UTF-8");
                    System.arraycopy(deviceId, 0, moduleEr1, 8, Math.min(deviceId.length, 64));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            System.arraycopy(moduleEr1, 0, bytes, currentIndex, moduleLen);
            currentIndex += moduleLen;
        }

        // module 2
        if (RunVarsKt.getHasOxy()) {
            byte[] moduleOxy = new byte[moduleLen];
            moduleOxy[0] = 0x03;
            moduleOxy[1] = (byte) (RunVarsKt.getOxyConn() ? 0x01 : 0x02);
            // battery
            /**
             * 0x00 : 电池供电
             * 0x10 : 低电量
             */
            if (RunVarsKt.getOxyBattery() >= 5 || RunVarsKt.getOxyBattery() == 0) {
                moduleOxy[2] = 0x00;
            } else {
                moduleOxy[2] = 0x10;
            }
            moduleOxy[3] = (byte) RunVarsKt.getOxyBattery();
            moduleOxy[4] = (byte) RunVarsKt.getOxyBleError();

            if (RunVarsKt.getOxySn() != null) {
                try {
                    byte[] deviceId = RunVarsKt.getOxySn().getBytes("UTF-8");
                    System.arraycopy(deviceId, 0, moduleOxy, 8, Math.min(deviceId.length, 64));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            System.arraycopy(moduleOxy, 0, bytes, currentIndex, moduleLen);
            currentIndex += moduleLen;
        }

        // module 3
        if (RunVarsKt.getHasKca()) {
            byte[] moduleKca = new byte[moduleLen];
            moduleKca[0] = 0x02;
            moduleKca[1] = (byte) (RunVarsKt.getKcaConn() ? 0x01 : 0x02);
            // battery
            /**
             * 0x00 : 电池供电
             * 0x10 : 低电量
             */
            if (RunVarsKt.getKcaBattery() >= 5 || RunVarsKt.getKcaBattery() == 0) {
                moduleKca[2] = 0x00;
            } else {
                moduleKca[2] = 0x10;
            }
            moduleKca[3] = (byte) RunVarsKt.getKcaBattery();
            moduleKca[4] = (byte) RunVarsKt.getKcaBleError();

            if (RunVarsKt.getKcaSn() != null) {
                try {
                    byte[] deviceId = RunVarsKt.getKcaSn().getBytes("UTF-8");
                    System.arraycopy(deviceId, 0, moduleKca, 8, Math.min(deviceId.length, 64));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            System.arraycopy(moduleKca, 0, bytes, currentIndex, moduleLen);
            currentIndex += moduleLen;
        }

        return bytes;

    }
}
