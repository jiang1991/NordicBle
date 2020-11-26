package com.lepu.nordicble.ble.cmd.s1.file;

import com.lepu.nordicble.ble.protocol.Convertible;

public class FileReadStart implements Convertible {

    private String fileName;
    private int offset;

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    public byte[] convert2Data() {
        char[] fileNameCharArray = fileName.toCharArray();
        byte[] data = new byte[16 + 4];
        for(int i = 0; i < 16; i++) {
            if(i < fileNameCharArray.length) {
                data[i] = (byte) fileNameCharArray[i];
            } else {
                data[i] = (byte) 0x00;
            }
        }

        data[data.length - 4] = (byte) (offset & 0xFF);
        data[data.length - 3] = (byte) ((offset >> 8) & 0xFF);
        data[data.length - 2] = (byte) ((offset >> 16) & 0xFF);
        data[data.length - 1] = (byte) ((offset >> 24) & 0xFF);
        return data;
    }
}
