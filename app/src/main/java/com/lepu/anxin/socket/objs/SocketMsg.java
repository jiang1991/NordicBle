package com.lepu.anxin.socket.objs;


import com.lepu.anxin.socket.utils.SocketCRC;

public class SocketMsg {
    public static final int TYPE_CLIENT = 0x55;
    public static final int TYPE_SERVER = 0x66;

    public static final int CMD_TOKEN = 0x01;
    public static final int CMD_LOGIN = 0x02;
    public static final int CMD_UPLOAD_INFO = 0x03;
    public static final int CMD_BIND = 0x04;
    public static final int CMD_UNBIND = 0x05;
    public static final int CMD_STATUS = 0x06;
    public static final int CMD_CHANGE_LEAD = 0x09;
    public static final int CMD_UPLOAD_ECG = 0x11;
    // oxy
    public static final int CMD_UPLOAD_OXY_WAVE = 0x12;
    public static final int CMD_UPLOAD_OXY_INFO = 0x16;
    // bp
    public static final int CMD_KCA_BP_MEASURE_CONFIG = 0x17;
    public static final int CMD_UPLOAD_BP_STATE = 0x18;
    public static final int CMD_UPLOAD_BP_RESULT = 0x20;

    public static final int CMD_HEARTBEAT = 0x15;

    public int getType() {
        return type;
    }

    public int getCmd() {
        return cmd;
    }

    public int getLen() {
        return len;
    }

    public byte[] getContent() {
        return content;
    }

    private int type;
    private int cmd;
    private int len;
    private byte[] content;

    public static byte[] tokenRandom = {(byte) 0xB7, (byte) 0x83, (byte) 0x30, (byte) 0x1C, (byte) 0xEE, (byte) 0x57, (byte) 0x4E, (byte) 0x5C, (byte) 0x92, (byte) 0x9D, (byte) 0xCA, (byte) 0xB1, (byte) 0xEA, (byte) 0x44, (byte) 0xE4, (byte) 0x94};

    public SocketMsg(byte[] bytes) {
        if (bytes.length < 6) {
            return;
        }

        type = bytes[1] & 0xff;
        cmd = bytes[2] & 0xff;
        len = (bytes[3] & 0xff) + ((bytes[4] & 0xff) << 8);
        content = new byte[len];
        if (len != 0) {
            System.arraycopy(bytes, 5, content, 0, len);
        }
    }

    public SocketMsg(int type, int cmd, byte[] content) {
        this.type = type;
        this.cmd = cmd;
        this.len = content.length;
        this.content = content;
    }

    public byte[] toBytes() {
        byte[] bytes = new byte[6+len];
        bytes[0] = (byte) 0xBB;
        bytes[1] = (byte) type;
        bytes[2] = (byte) cmd;
        bytes[3] = (byte) len;
        bytes[4] = (byte) (len >> 8);
        if (len >= 0) System.arraycopy(content, 0, bytes, 5, len);
        bytes[6+len-1] = SocketCRC.calCRC8(bytes);
        return bytes;
    }

}
