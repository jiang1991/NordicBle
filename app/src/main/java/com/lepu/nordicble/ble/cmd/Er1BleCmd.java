package com.lepu.nordicble.ble.cmd;

public class Er1BleCmd {

    public static int BLE_CMD_GET_INFO = 0xE1;
    public static int BLE_CMD_RT_DATA = 0x03;

    public static String ACTION_ER1_INFO = "com.lepu.ble_er1_info";
    public static String ACTION_ER1_RT_DATA = "com.lepu.ble_er1_rtData";

    private static int seqNo = 0;
    private static void addNo() {
        seqNo++;
        if (seqNo >= 255) {
            seqNo = 0;
        }
    }

    public static byte[] getRtData() {
        int len = 1;

        byte[] cmd = new byte[8+len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) 0x03;
        cmd[2] = (byte) ~0x03;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0x01;
        cmd[6] = (byte) 0x00;
        cmd[7] = (byte) 0x7D;  // 0 -> 125hz;  1-> 62.5hz
        cmd[8] = Er1BleCRC.calCRC8(cmd);

        addNo();
        return cmd;
    }

    public static byte[] getInfo() {
        int len = 0;

        byte[] cmd = new byte[8+len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) 0xE1;
        cmd[2] = (byte) ~0xE1;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0;
        cmd[6] = (byte) 0;
        cmd[7] = Er1BleCRC.calCRC8(cmd);

        addNo();

        return cmd;
    }
}
