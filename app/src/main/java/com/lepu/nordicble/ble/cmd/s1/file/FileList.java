package com.lepu.nordicble.ble.cmd.s1.file;

import java.util.Arrays;
import java.util.List;

public class FileList {
    private byte fileNum;
    private String[] fileNames;
    public FileList(byte[] data) {
        fileNum = data[0];
        fileNames = new String[fileNum];
        for(int i = 0; i < fileNum; i++) {
            byte[] tmeData = Arrays.copyOfRange(data, (i * 16) + 1, (i + 1) * 16 + 1);
            String name = new String(tmeData).trim();
            fileNames[i] = name;
        }
    }

    public byte getFileNum() {
        return fileNum;
    }

    public String[] getFileNames() {
        return fileNames;
    }

    public List<String> listFileName() {
        return Arrays.asList(fileNames);
    }
}
