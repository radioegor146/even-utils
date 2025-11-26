package by.radioegor146.evenutils.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.UUID;

public class BleDevice {
    private static final UUID SERVICE = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    private static final UUID READ_CHARACTERISTIC = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
    private static final UUID WRITE_CHARACTERISTIC = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");

    private final BluetoothGatt gatt;
    private final BluetoothGattCharacteristic readCharacteristic;
    private final BluetoothGattCharacteristic writeCharacteristic;

    public BleDevice(BluetoothGatt gatt) {
        this.gatt = gatt;
        BluetoothGattService service = gatt.getService(SERVICE);
        this.readCharacteristic = service.getCharacteristic(READ_CHARACTERISTIC);
        this.writeCharacteristic = service.getCharacteristic(WRITE_CHARACTERISTIC);
    }

    @SuppressLint("MissingPermission")
    public void init() {
        this.gatt.setCharacteristicNotification(readCharacteristic, true);
    }

    @SuppressLint("MissingPermission")
    public void send(byte[] data, boolean noResponse) {
        this.gatt.writeCharacteristic(this.writeCharacteristic, data, noResponse ?
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE :
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
    }

    @SuppressLint("MissingPermission")
    public void close() {
        this.gatt.close();
    }
}
