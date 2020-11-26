package com.lepu.nordicble.ble.manager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.LogUtils;
import com.lepu.nordicble.ble.cmd.s1.S1BleCmd;
import com.lepu.nordicble.utils.ByteArrayKt;

import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.PhyRequest;
import no.nordicsemi.android.ble.data.Data;

public class S1BleManager extends BleManager {

    public final static UUID SERVICE_UUID =
            UUID.fromString("14839ac4-7d7e-415c-9a42-167340cf2339");
    public final static UUID WRITE_UUID =
            UUID.fromString("8B00ACE7-EB0B-49B0-BBE9-9AEE0A26E1A3");
    public final static UUID NOTIFY_UUID =
            UUID.fromString("0734594A-A8E7-4B1A-A6B1-CD5243059A57");

    private BluetoothGattCharacteristic writeChar, notifyChar;

    private OnNotifyListener listener;

    public S1BleManager(@NonNull final Context context) {
        super(context);
    }

    public void setNotifyListener(OnNotifyListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return new MyManagerGattCallback();
    }

    @Override
    public void log(final int priority, @NonNull final String message) {
//        if (priority == Log.ERROR) {
//            Log.println(priority, "S1BleManager", message);
//        }
        Log.println(priority, "S1BleManager", message);
    }

    /**
     * BluetoothGatt callbacks object.
     */
    private class MyManagerGattCallback extends BleManagerGattCallback {

        // This method will be called when the device is connected and services are discovered.
        // You need to obtain references to the characteristics and descriptors that you will use.
        // Return true if all required services are found, false otherwise.
        @Override
        public boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
            final BluetoothGattService service = gatt.getService(SERVICE_UUID);
            log(Log.INFO, "service ==  " + service);
            if (service != null) {
                writeChar = service.getCharacteristic(WRITE_UUID);
                notifyChar = service.getCharacteristic(NOTIFY_UUID);
            }
            log(Log.INFO, "writeChar ==  " + writeChar);
            log(Log.INFO, "notifyChar ==  " + notifyChar);
            // Validate properties
            boolean notify = false;
            if (notifyChar != null) {
                final int properties = notifyChar.getProperties();
                log(Log.INFO, "notifyChar properties ==  " + properties);
                notify = (properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
                log(Log.INFO, "notifyChar notify ==  " + notify);
            }
            boolean writeRequest = false;
            if (writeChar != null) {
                final int properties = writeChar.getProperties();
                log(Log.INFO, "writeChar properties ==  " + properties);
                int writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;
                if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {
                    writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE;
                }
                writeChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                writeRequest = (properties & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0 || (properties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0;
                log(Log.INFO, "writeChar writeRequest ==  " + writeRequest);

            }
            // Return true if all required services have been found
            return writeChar != null && notifyChar != null
                    && notify && writeRequest;
        }

        // If you have any optional services, allocate them here. Return true only if
        // they are found.
        @Override
        protected boolean isOptionalServiceSupported(@NonNull final BluetoothGatt gatt) {
            return super.isOptionalServiceSupported(gatt);
        }

        // Initialize your device here. Often you need to enable notifications and set required
        // MTU or write some initial data. Do it here.
        @Override
        protected void initialize() {
            // You may enqueue multiple operations. A queue ensures that all operations are
            // performed one after another, but it is not required.
            beginAtomicRequestQueue()
                    .add(requestMtu(247) // Remember, GATT needs 3 bytes extra. This will allow packet size of 244 bytes.
                            .with((device, mtu) -> log(Log.INFO, "MTU set to " + mtu))
                            .fail((device, status) -> log(Log.WARN, "Requested MTU not supported: " + status)))
                    .add(setPreferredPhy(PhyRequest.PHY_LE_2M_MASK, PhyRequest.PHY_LE_2M_MASK, PhyRequest.PHY_OPTION_NO_PREFERRED)
                            .fail((device, status) -> log(Log.WARN, "Requested PHY not supported: " + status)))
                    .add(enableNotifications(notifyChar))
                    .done(device -> log(Log.INFO, "Target initialized"))
                    .enqueue();

             setNotificationCallback(notifyChar)
                    .with((device, data) -> {
                        LogUtils.d(device.getName() + " received: " + ByteArrayKt.bytesToHex(data.getValue()));
                        listener.onNotify(device, data);
                    });
             enableNotifications(notifyChar).enqueue();

            // get info
            getInfo();
        }

        @Override
        protected void onDeviceDisconnected() {
            // Device disconnected. Release your references here.
            writeChar = null;
            notifyChar = null;
        }
    }

    private void getInfo() {
        sendCmd(S1BleCmd.getInfo());
    }

    public void sendCmd(byte[] bytes) {
        writeCharacteristic(writeChar, bytes)
                .done(device -> {
                    LogUtils.d(device.getName() + " send: " + ByteArrayKt.bytesToHex(bytes));
                })
                .enqueue();
    }

    public void sendLongCmd(byte[] bytes) {
        writeCharacteristic(writeChar, bytes)
                .split()
                .done(device -> {
                    LogUtils.d(device.getName() + " send: " + ByteArrayKt.bytesToHex(bytes));
                })
                .enqueue();
    }

    public interface OnNotifyListener {
        void onNotify(BluetoothDevice device, Data data);
    }
}
