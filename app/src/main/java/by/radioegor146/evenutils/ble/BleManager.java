package by.radioegor146.evenutils.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;

import java.util.Set;
import java.util.function.Consumer;

import by.radioegor146.evenutils.ble.queue.BleQueue;

public class BleManager extends BroadcastReceiver {

    private BleDevice leftGlass;
    private BleDevice rightGlass;
    private boolean leftGlassConnecting = false;
    private boolean rightGlassConnecting = false;

    private BluetoothAdapter adapter;

    private final Context context;
    private final Consumer<Boolean> connectionStateCallback;

    private final BleQueue queue;

    private boolean initialized = false;

    public BleManager(Context context, Consumer<Boolean> connectionStateCallback, Runnable queueEmptyCallback) {
        this.context = context;
        this.connectionStateCallback = connectionStateCallback;
        this.queue = new BleQueue(this, queueEmptyCallback);
    }

    public void init() {
        if (!this.initialized) {
            this.initialized = true;
            BluetoothManager manager = (BluetoothManager) this.context.getSystemService(Context.BLUETOOTH_SERVICE);
            this.adapter = manager.getAdapter();
            this.context.registerReceiver(this, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
            this.context.registerReceiver(this, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
            this.context.registerReceiver(this, new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED));
            this.refreshDevices();
        }
    }

    public void deInit() {
        if (this.initialized) {
            this.initialized = false;
            this.context.unregisterReceiver(this);
        }
        if (this.leftGlass != null) {
            this.leftGlass.close();
        }
        if (this.rightGlass != null) {
            this.rightGlass.close();
        }
    }

    @SuppressLint("MissingPermission")
    private void refreshDevices() {
        if (this.adapter.getState() != BluetoothAdapter.STATE_ON) {
            if (this.leftGlass != null) {
                this.leftGlass.close();
                this.leftGlass = null;
            }
            if (this.rightGlass != null) {
                this.rightGlass.close();
                this.rightGlass = null;
            }
            this.triggerConnectionStateUpdate();
            return;
        }
        Set<BluetoothDevice> devices = this.adapter.getBondedDevices();
        for (BluetoothDevice device : devices) {
            if (!device.getName().contains("Even G1")) {
                continue;
            }
            if (device.getName().contains("_L_") && this.leftGlass == null && !this.leftGlassConnecting) {
                this.leftGlassConnecting = true;
                this.initGlass(device, glass -> {
                    if (glass == null && this.leftGlass != null) {
                        this.leftGlass.close();
                    }
                    this.leftGlass = glass;
                    this.leftGlassConnecting = false;
                    this.triggerConnectionStateUpdate();
                }, false);
            }
            if (device.getName().contains("_R_") && this.rightGlass == null && !this.rightGlassConnecting) {
                this.rightGlassConnecting = true;
                this.initGlass(device, glass -> {
                    if (glass == null && this.rightGlass != null) {
                        this.rightGlass.close();
                    }
                    this.rightGlass = glass;
                    this.rightGlassConnecting = false;
                    this.triggerConnectionStateUpdate();
                }, true);
            }
        }
    }

    private void triggerConnectionStateUpdate() {
        this.connectionStateCallback.accept(this.isConnected());
    }

    @SuppressLint("MissingPermission")
    private void initGlass(BluetoothDevice device, Consumer<BleDevice> updateCallback, boolean isRight) {
        device.connectGatt(this.context, true, new BluetoothGattCallback() {
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    BleDevice bleDevice = new BleDevice(gatt);
                    bleDevice.init();
                    updateCallback.accept(bleDevice);
                } else {
                    gatt.close();
                    updateCallback.accept(null);
                }
            }

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                        gatt.close();
                        updateCallback.accept(null);
                    }
                    if (newState == BluetoothGatt.STATE_CONNECTED) {
                        gatt.discoverServices();
                    }
                } else {
                    gatt.close();
                    updateCallback.accept(null);
                }
            }

            @Override
            public void onCharacteristicChanged(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value) {
                super.onCharacteristicChanged(gatt, characteristic, value);
                queue.onPacketReceived(value, isRight);
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    queue.onPacketWritten(isRight);
                } else {
                    gatt.close();
                    updateCallback.accept(null);
                }
            }
        }, BluetoothDevice.TRANSPORT_LE);
    }

    public boolean isConnected() {
        return this.leftGlass != null && this.rightGlass != null;
    }

    public BleDevice getLeftGlass() {
        return this.leftGlass;
    }

    public BleDevice getRightGlass() {
        return this.rightGlass;
    }

    public BleQueue getQueue() {
        return queue;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        refreshDevices();
    }
}
