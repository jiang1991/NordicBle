package com.lepu.nordicble.ble.cmd.s1.file;

import com.lepu.nordicble.ble.cmd.s1.BleConstant;
import com.lepu.nordicble.ble.protocol.CRCUtils;
import com.lepu.nordicble.ble.protocol.Convertible;

public class RequestPkg {

    protected final byte head = (byte) 0xA5;
    protected byte cmd;
    protected byte _cmd;
    protected final byte pkgType = (byte) 0x00;
    protected byte pkgNo;
    protected int length;
    protected byte[] data;
    protected byte crc8;

    protected byte[] buf;

    public RequestPkg() {
        this.data = new byte[0];
        this.length = data.length;
    }

    public RequestPkg setCmd(byte cmd) {
        this.cmd = cmd;
        this._cmd = (byte) ~cmd;
        return this;
    }

    public RequestPkg setPkgNo(byte pkgNo) {
        this.pkgNo = pkgNo;
        return this;
    }

    public RequestPkg setData(byte[] data) {
        if(data == null) {
            return this;
        }
        this.data = data;
        this.length = data.length;
        return this;
    }

    public <T extends Convertible> RequestPkg setData(T data) {
        if(data == null) {
            return this;
        }
        this.data = data.convert2Data();
        this.length = this.data.length;
        return this;
    }

    public RequestPkg build() {
        int cmdLength = BleConstant.COMMON_PKG_HEAD_LENGTH + data.length + 1;
        buf = new byte[cmdLength];
        buf[0] = head;
        buf[1] = cmd;
        buf[2] = _cmd;
        buf[3] = pkgType;
        buf[4] = pkgNo;
        buf[5] = (byte) (length & 0xFF);
        buf[6] = (byte) ((length >> 8) & 0xFF);

        for(int i = 0; i < data.length; i++) {
            buf[i + BleConstant.COMMON_PKG_HEAD_LENGTH] = data[i];
        }

        crc8 = CRCUtils.calCRC8(buf);
        buf[cmdLength - 1] = crc8;
        return this;
    }

    public byte[] getBuf() {
        return buf;
    }
}
