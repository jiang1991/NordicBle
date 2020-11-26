package com.lepu.nordicble.ble.cmd.s1.file;

public interface OnCmdCallback {
    byte ERR_CODE_NORMAL = -1;
    byte ERR_CODE_TIMEOUT = -2;
    byte ERR_CODE_BUSY = -3;
    byte ERR_CODE_EXP = -4;
    byte ERR_CODE_CMD_SEND_ERROR = -5;
    byte ERR_CODE_NOTIFICATION_SUB_FAILURE = -6;
    byte ERR_CODE_CANCEL = -7;

    default void onSendSuccess(byte[] bytes) {}

    default void onSuccess(byte[] bytes){}

    default void onCmdSuccess(RespPkg pkg) {}

    default void onFailed(Throwable throwable,byte errCode) {}

    default void onFailed(byte cmd, Throwable throwable,byte errCode) {}
}
