package com.lepu.nordicble.ble.cmd.s1.file;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.lepu.nordicble.ble.cmd.s1.BleConstant;
import com.lepu.nordicble.ble.manager.S1BleManager;
import com.lepu.nordicble.ble.protocol.CRCUtils;

import java.util.Arrays;

import no.nordicsemi.android.ble.data.Data;

public class BleFileDownloader implements S1BleManager.OnNotifyListener{

    private static volatile BleFileDownloader instance;
    private S1BleManager manager;
    private ReadBleFileListener mReadBleFileListener;
    private Handler handler;

    private String mFileName;
    private byte fileType;
    private volatile int start = 0;
    private int offset;
    private int fileSize = 0;
    private long timeout = BleConstant.BLE_REQ_TIMEOUT;
    private int retryTimes = 0;
    private static final int MAX_RETRY_TIMES = 2;

    private int currentState = BleConstant.BLE_REQUEST_ID_AVAILABLE;
    private volatile byte[] tmpData = new byte[0];
    private byte[] dataPool = new byte[0];
    private boolean isCancelDownload = false;

    public static BleFileDownloader newInstance(S1BleManager manager) {
        if(instance == null) {
            synchronized (BleFileDownloader.class) {
                if(instance == null) {
                    instance = new BleFileDownloader(manager);
                    return instance;
                }
            }
        }
        return instance;
    }

    public BleFileDownloader(S1BleManager manager) {
        this.manager = manager;
        init();
    }

    private void init() {
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                int msgType = msg.what;
                if(mReadBleFileListener != null) {
                    switch (msgType) {
                        case BleConstant.MSG_TYPE_START_READ_FILE:
                            endReadFile();
                            break;
                        case BleConstant.MSG_TYPE_READ_FILE_CONTENT:
                            Log.d("BleFileDownloader", "MSG_TYPE_READ_FILE_CONTENT retryTimes == " + retryTimes);
                            if(retryTimes == MAX_RETRY_TIMES) {
                                endReadFile();
                                retryTimes = 0;
                            } else {
                                readFileContent();
                                retryTimes = retryTimes + 1;
                            }

                            break;
                        case BleConstant.MSG_TYPE_END_READ_FILE:
                            if(retryTimes == MAX_RETRY_TIMES) {
                                Log.d("BleFileDownloader", "mReadBleFileListener.onBleReadSuccess ERR_CODE_TIMEOUT");
                                handler.removeMessages(BleConstant.MSG_TYPE_END_READ_FILE);
                                mReadBleFileListener.onReadFailed(mFileName, fileType, ReadBleFileListener.ERR_CODE_TIMEOUT);
                            } else {
                                endReadFile();
                                retryTimes = retryTimes + 1;
                            }
                            break;
                    }
                }
            }
        };
    }

    private void setListener() {
        manager.setNotifyListener(this);
    }

    public int getCurrentState() {
        return currentState;
    }

    public void setCurrentState(int currentState) {
        this.currentState = currentState;
    }

    public void readFile(String fileName, int offset, long timeout, ReadBleFileListener listener) {
        if(currentState != BleConstant.BLE_REQUEST_ID_AVAILABLE) {
            if(listener != null) {
                listener.onReadFailed(fileName, fileType,  ReadBleFileListener.ERR_CODE_BUSY);
            }
        }
        this.mFileName = fileName;
        this.offset = offset;
        this.start = offset;
        this.timeout = timeout;
        this.mReadBleFileListener = listener;

        setListener();
        startReadFile();
    }

    private void startReadFile() {
        if(isCancelDownload) {
            if(currentState != BleConstant.BLE_REQUEST_ID_AVAILABLE) {
                if(mReadBleFileListener != null) {
                    mReadBleFileListener.onReadFailed(mFileName, fileType, ReadBleFileListener.ERR_CODE_CANCEL);
                }
                return;
            }
        }
        FileReadStart fileReadStart = new FileReadStart();
        fileReadStart.setFileName(mFileName);
        fileReadStart.setOffset(offset);

        RequestPkg requestPkg = new RequestPkg();
        requestPkg.setCmd(BleConstant.CMD_START_READ_FILE)
                .setPkgNo((byte) 0x00)
                .setData(fileReadStart.convert2Data())
                .build();
        sendLongCmd(requestPkg.getBuf());
        currentState = BleConstant.BLE_REQUEST_ID_START_READ_FILE;
        handler.sendMessageDelayed(handler.obtainMessage(BleConstant.MSG_TYPE_START_READ_FILE), 100000);
    }

    private void readFileContent() {
        if(isCancelDownload) {
            if(currentState != BleConstant.BLE_REQUEST_ID_AVAILABLE) {
                if(mReadBleFileListener != null) {
                    mReadBleFileListener.onReadFailed(mFileName, fileType,  ReadBleFileListener.ERR_CODE_CANCEL);
                }
                return;
            }
        }
        byte[] offsetData = new byte[4];
        offsetData[0] = (byte) (offset & 0xFF);
        offsetData[1] = (byte) ((offset >> 8) & 0xFF);
        offsetData[2] = (byte) ((offset >> 16) & 0xFF);
        offsetData[3] = (byte) ((offset >> 24) & 0xFF);

        RequestPkg requestPkg = new RequestPkg();
        requestPkg.setCmd(BleConstant.CMD_READ_FILE_CONTENT)
                .setPkgNo((byte) 0x00)
                .setData(offsetData)
                .build();
        sendLongCmd(requestPkg.getBuf());
        currentState = BleConstant.BLE_REQUEST_ID_READ_FILE_CONTENT;
        handler.sendMessageDelayed(handler.obtainMessage(BleConstant.MSG_TYPE_READ_FILE_CONTENT), 100000);
    }

    private void endReadFile() {
        if(isCancelDownload) {
            if(currentState != BleConstant.BLE_REQUEST_ID_AVAILABLE) {
                if(mReadBleFileListener != null) {
                    mReadBleFileListener.onReadFailed(mFileName, fileType,  ReadBleFileListener.ERR_CODE_CANCEL);
                }
                return;
            }
        }
        RequestPkg requestPkg = new RequestPkg();
        requestPkg.setCmd(BleConstant.CMD_END_READ_FILE)
                .setPkgNo((byte) 0x00)
                .setData(new byte[0])
                .build();
        sendLongCmd(requestPkg.getBuf());
        currentState = BleConstant.BLE_REQUEST_ID_END_READ_FILE;
        handler.sendMessageDelayed(handler.obtainMessage(BleConstant.MSG_TYPE_END_READ_FILE), 100000);
    }

    private void sendCmd(byte[] bytes) {
        manager.sendCmd(bytes);
    }

    private void sendLongCmd(byte[] bytes) {
        manager.sendLongCmd(bytes);
    }

    @Override
    public void onNotify(BluetoothDevice device, Data data) {
        byte[] bytes = data.getValue();
        if (bytes == null) {
            return;
        }
        int length = tmpData.length;
        tmpData = Arrays.copyOf(tmpData, tmpData.length + bytes.length);
        System.arraycopy(bytes, 0, tmpData, length, bytes.length);
        tmpData = hasResponse(tmpData);
    }

    private byte[] hasResponse(byte[] bytes) {
        if (bytes.length < 8) {
            return bytes;
        }
        byte[] responseBytes;
        byte[] tempBytes;
        for (int i = 0; i < bytes.length; i++) {
            if ((bytes[i] & 0xFF) == 0xa5) {
                if(i + 8 > bytes.length) {
                    break;
                }
                int length = (bytes[i+5] & 0xFF) + ((bytes[i+6] & 0xFF) << 8);
                if (i + length + 8 <= bytes.length) {
                    responseBytes = Arrays.copyOfRange(bytes, i, i + length + 8);
                    RespPkg response = new RespPkg(responseBytes);
                    if (response.getCmd() == ~response.get_cmd() && response.getCrc8() == CRCUtils.calCRC8(response.getBuf())) {
                        onCmdCallback.onCmdSuccess(response);
                        tempBytes = Arrays.copyOfRange(bytes, i + length + 8, bytes.length);

                        return hasResponse(tempBytes);
                    }
                }

            } // else continue
        }

        return bytes;
    }

    private OnCmdCallback onCmdCallback = new OnCmdCallback() {
        @Override
        public void onCmdSuccess(RespPkg respPkg) {
            byte cmd = respPkg.getCmd();
            byte pkgType = respPkg.getPkgType();

            if(pkgType == (byte) 0x01) {
                if(cmd == BleConstant.CMD_START_READ_FILE) {
                    if(currentState != BleConstant.BLE_REQUEST_ID_START_READ_FILE) {
                        return;
                    }
                    currentState = BleConstant.BLE_RESP_ID_START_READ_FILE;
                    byte[] data = respPkg.getData();
                    fileSize = 0;
                    for(int i = 0; i < data.length; i++) {
                        if(i == 0) {
                            fileSize = fileSize + (data[i] & 0xFF);
                        } else {
                            fileSize = fileSize + ((data[i] & 0xFF) << ( 8 * i));
                        }
                    }
                    handler.removeMessages(BleConstant.MSG_TYPE_START_READ_FILE);
                    Log.d("BleFileDownloader",  "OnCmdCallback CMD_START_READ_FILE fileSize == " + fileSize);
                    if(fileSize == 0) {
                        handler.post(() -> {
                            dataPool = new byte[0];
                            currentState = BleConstant.BLE_REQUEST_ID_AVAILABLE;
                            mReadBleFileListener.onBleReadPartFinished(mFileName,fileType, 1.0f);
                            mReadBleFileListener.onBleReadSuccess(mFileName,fileType, dataPool);
                        });
                    } else {
                        dataPool = new byte[0];
                        readFileContent();
                    }
                } else if(cmd == BleConstant.CMD_READ_FILE_CONTENT) {
                    if(currentState != BleConstant.BLE_REQUEST_ID_READ_FILE_CONTENT) {
                        return;
                    }
                    currentState = BleConstant.BLE_RESP_ID_READ_FILE_CONTENT;
                    byte[] data = respPkg.getData();
                    // 数据写入文件
                    int dLength = dataPool.length;
                    dataPool = Arrays.copyOf(dataPool, dLength + data.length);
                    System.arraycopy(data, 0, dataPool, dLength, data.length);
                    offset = offset + data.length;
                    handler.removeMessages(BleConstant.MSG_TYPE_READ_FILE_CONTENT);
                    if(mReadBleFileListener != null) {
                        float percent = (offset) * 1.0f / (fileSize);
                        handler.post(() -> {
                            if(mReadBleFileListener != null) {
                                mReadBleFileListener.onBleReadPartFinished(mFileName,fileType, percent);
                            }
                        });
                    }
                    Log.d("BleFileDownloader", "OnCmdCallback CMD_START_READ_FILE offset == " + offset);
                    Log.d("BleFileDownloader", "OnCmdCallback CMD_START_READ_FILE fileSize == " + fileSize);
                    if(offset < fileSize) {
                        readFileContent();
                    } else {
                        endReadFile();
                    }
                } else if(cmd == BleConstant.CMD_END_READ_FILE) {
                    if(currentState != BleConstant.BLE_REQUEST_ID_END_READ_FILE) {
                        return;
                    }
                    currentState = BleConstant.BLE_REQUEST_ID_AVAILABLE;
                    handler.removeMessages(BleConstant.MSG_TYPE_END_READ_FILE);
                    if(mReadBleFileListener != null) {
                        handler.post(() -> {
                            if(dataPool.length < fileSize) {
                                Log.d("BleFileDownloader", "mReadBleFileListener.onBleReadSuccess ERR_CODE_EXP");
                                mReadBleFileListener.onReadFailed(mFileName, fileType, ReadBleFileListener.ERR_CODE_EXP);
                            } else {
                                Log.d("BleFileDownloader", "mReadBleFileListener.onBleReadSuccess CMD_END_READ_FILE");
                                mReadBleFileListener.onBleReadSuccess(mFileName,fileType, dataPool);
                            }
                        });
                    }
                }
            } else {
                Log.d("BleFileDownloader", "mReadBleFileListener pkgType == " + pkgType);
                if(retryTimes == MAX_RETRY_TIMES) {
                    Log.d("BleFileDownloader", "mReadBleFileListener.onBleReadSuccess ERR_CODE_TIMEOUT");
                    handler.removeMessages(BleConstant.MSG_TYPE_END_READ_FILE);
                    mReadBleFileListener.onReadFailed(mFileName, fileType, ReadBleFileListener.ERR_CODE_TIMEOUT);
                } else {
                    endReadFile();
                    retryTimes = retryTimes + 1;
                }
            }
        }

        @Override
        public void onFailed(Throwable throwable, byte errCode) {
            currentState = BleConstant.BLE_REQUEST_ID_AVAILABLE;
            if(mReadBleFileListener != null) {
                handler.post(() -> {
                    if(mReadBleFileListener != null) {
                        mReadBleFileListener.onReadFailed(mFileName, fileType, errCode);
                    }
                });

            }
        }
    };
}
