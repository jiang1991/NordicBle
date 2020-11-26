package com.lepu.nordicble.ble.cmd.s1;

public class BleConstant {
    public final static int COMMON_PKG_HEAD_LENGTH = 7;

    // read file
    public final static byte CMD_START_READ_FILE = (byte) 0xF2;
    public final static byte CMD_READ_FILE_CONTENT = (byte) 0xF3;
    public final static byte CMD_END_READ_FILE = (byte) 0xF4;

    // file read states
    public static final int BLE_REQUEST_ID_AVAILABLE = -1;
    public static final int BLE_REQUEST_ID_START_READ_FILE = 1;
    public static final int BLE_RESP_ID_START_READ_FILE = 4;
    public static final int BLE_REQUEST_ID_READ_FILE_CONTENT = 2;
    public static final int BLE_RESP_ID_READ_FILE_CONTENT = 5;
    public static final int BLE_REQUEST_ID_END_READ_FILE = 3;
    public static final int BLE_RESP_ID_END_READ_FILE = 6;
    public final static long BLE_REQ_TIMEOUT = 2000L;

    public static final byte MSG_TYPE_START_READ_FILE = 0x01;
    public static final byte MSG_TYPE_READ_FILE_CONTENT = 0x02;
    public static final byte MSG_TYPE_END_READ_FILE = 0x03;
}
