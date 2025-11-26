package by.radioegor146.evenutils.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import androidx.annotation.NonNull;

import java.util.Set;
import java.util.function.Consumer;

import by.radioegor146.evenutils.ble.queue.BleQueue;

public class BleManager {
    private final static int RECHECK_INTERVAL = 1000;

    private final Thread connectionStatusUpdateThread = new Thread(this::updateConnectionStatus);

    private BleDevice leftGlass;
    private BleDevice rightGlass;

    private BluetoothAdapter adapter;

    private final Context context;
    private final Consumer<Boolean> connectionStateCallback;

    private boolean running = true;

    private final BleQueue queue;

    public BleManager(Context context, Consumer<Boolean> connectionStateCallback, Runnable queueEmptyCallback) {
        this.context = context;
        this.connectionStateCallback = connectionStateCallback;
        this.queue = new BleQueue(this, queueEmptyCallback);
    }

    private void updateConnectionStatus() {
        while (this.running) {
            this.refreshDevices();
            try {
                Thread.sleep(RECHECK_INTERVAL);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void init() {
        BluetoothManager manager = (BluetoothManager) this.context.getSystemService(Context.BLUETOOTH_SERVICE);
        this.adapter = manager.getAdapter();

        if (!this.connectionStatusUpdateThread.isAlive()) {
            this.connectionStatusUpdateThread.start();
        }
    }

    public void deInit() {
        this.running = false;
        try {
            this.connectionStatusUpdateThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressLint("MissingPermission")
    private void refreshDevices() {
        Set<BluetoothDevice> devices = this.adapter.getBondedDevices();
        for (BluetoothDevice device : devices) {
            if (!device.getName().contains("Even G1")) {
                continue;
            }
            if (device.getName().contains("_L_") && this.leftGlass == null) {
                this.initGlass(device, glass -> {
                    if (glass == null && this.leftGlass != null) {
                        this.leftGlass.close();
                    }
                    this.leftGlass = glass;
                    this.triggerConnectionStateUpdate();
                }, false);
            }
            if (device.getName().contains("_R_") && this.rightGlass == null) {
                this.initGlass(device, glass -> {
                    if (glass == null && this.rightGlass != null) {
                        this.rightGlass.close();
                    }
                    this.rightGlass = glass;
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
        device.connectGatt(this.context, false, new BluetoothGattCallback() {
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    BleDevice bleDevice = new BleDevice(gatt);
                    bleDevice.init();
                    updateCallback.accept(bleDevice);
                }
            }

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    updateCallback.accept(null);
                }
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    gatt.discoverServices();
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
                queue.onPacketWritten(isRight);
            }
        });
    }

    private void closeDevices() {
        if (this.leftGlass != null) {
            this.leftGlass.close();
        }

        if (this.rightGlass != null) {
            this.rightGlass.close();
        }
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
}
