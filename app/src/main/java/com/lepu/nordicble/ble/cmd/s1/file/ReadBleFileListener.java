package com.lepu.nordicble.ble.cmd.s1.file;

public interface ReadBleFileListener {
    byte ERR_CODE_NORMAL = -1;
    byte ERR_CODE_TIMEOUT = -2;
    byte ERR_CODE_BUSY = -3;
    byte ERR_CODE_EXP = -4;
    byte  ERR_CODE_CMD_SEND_ERROR = -5;
    byte  ERR_CODE_NOTIFICATION_SUB_FAILURE = -6;
    byte  ERR_CODE_CANCEL = -7;

    /**
     * Called when downloading file from bluetooth
     * @param fileName
     * @param fileType
     * @param percentage
     */
    default void onBleReadPartFinished(String fileName, byte fileType, float percentage){}

    /**
     * Called when the download is completed
     * @param fileName
     * @param fileType
     * @param fileBuf
     */
    default void onBleReadSuccess(String fileName, byte fileType, byte[] fileBuf){}

    /**
     * Called when the download is failed
     * @param fileName
     * @param fileType
     * @param errCode
     */
    default void onReadFailed(String fileName, byte fileType, byte errCode){}
}
