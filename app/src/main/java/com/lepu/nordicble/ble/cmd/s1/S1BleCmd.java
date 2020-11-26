package com.lepu.nordicble.ble.cmd.s1;

import com.lepu.nordicble.ble.cmd.er1.Er1BleCRC;

public class S1BleCmd {

    public final static int CMD_GET_INFO = 0xE1;
    public final static int CMD_RT_DATA = 0x03;
    // list file
    public final static int CMD_LIST_FILE = 0xF1;
    // read file
    public final static int CMD_START_READ_FILE = 0xF2;
    public final static int CMD_READ_FILE_CONTENT = 0xF3;
    public final static int CMD_END_READ_FILE = 0xF4;


    private static int seqNo = 0;
    private static void addNo() {
        seqNo++;
        if (seqNo >= 255) {
            seqNo = 0;
        }
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

    public static byte[] getRtState() {
        int len = 0;

        byte[] cmd = new byte[8+len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) 0x02;
        cmd[2] = (byte) ~0x02;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0x00;
        cmd[6] = (byte) 0x00;
        cmd[7] = Er1BleCRC.calCRC8(cmd);

        addNo();
        return cmd;
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

    public static byte[] listFiles() {
        int len = 0;

        byte[] cmd = new byte[8+len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) 0xF1;
        cmd[2] = (byte) ~0xF1;;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0x00;
        cmd[6] = (byte) 0x00;
        cmd[7] = Er1BleCRC.calCRC8(cmd);

        addNo();
        return cmd;
    }
}
