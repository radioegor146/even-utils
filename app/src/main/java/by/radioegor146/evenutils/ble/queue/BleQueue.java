package by.radioegor146.evenutils.ble.queue;

import android.util.Log;

import java.util.ArrayDeque;
import java.util.Queue;

import by.radioegor146.evenutils.Utils;
import by.radioegor146.evenutils.ble.BleManager;

public class BleQueue {

    private final BleManager manager;
    private final Runnable queueEmptyCallback;
    private final Queue<QueuedPacket> packetQueue = new ArrayDeque<>();
    private volatile QueuedPacket currentBothPacket = null;
    private volatile QueuedPacket currentLeftPacket = null;
    private volatile QueuedPacket currentRightPacket = null;
    private volatile boolean bothPacketReadyOnLeft = false;
    private volatile boolean bothPacketReadyOnRight = false;

    public BleQueue(BleManager manager, Runnable queueEmptyCallback) {
        this.manager = manager;
        this.queueEmptyCallback = queueEmptyCallback;
    }

    public void queuePacket(QueuedPacket queuedPacket) {
        Log.d(BleQueue.class.getName(), "Queued packet: " + queuedPacket.getKind() + " " +
                Utils.bytesToHex(queuedPacket.getPacket()));
        packetQueue.add(queuedPacket);
        synchronized (this) {
            processNextPacket();
        }
    }

    private void processNextPacket() {
        Log.d(BleQueue.class.getName(), "Queue size: " + packetQueue.size());

        QueuedPacket queuedPacket = packetQueue.peek();
        if (queuedPacket == null) {
            return;
        }

        if (queuedPacket.getKind() == QueuedPacketKind.BOTH && currentBothPacket != null) {
            return;
        }
        if (queuedPacket.getKind() == QueuedPacketKind.ONLY_LEFT && currentLeftPacket != null) {
            return;
        }
        if (queuedPacket.getKind() == QueuedPacketKind.ONLY_RIGHT && currentRightPacket != null) {
            return;
        }

        queuedPacket = packetQueue.poll();
        if (queuedPacket == null) {
            throw new RuntimeException("wut? peek was not null, but poll is null?");
        }

        if (!this.manager.isConnected()) {
            Log.d(BleQueue.class.getName(), "Skipping packet as now is not connected");
            return;
        }

        switch (queuedPacket.getKind()) {
            case ONLY_LEFT: {
                currentLeftPacket = queuedPacket;
                Log.d(BleQueue.class.getName(), "Sent ONLY_LEFT packet: " + Utils.bytesToHex(currentLeftPacket.getPacket()));
                this.manager.getLeftGlass().send(currentLeftPacket.getPacket(), currentLeftPacket.getValidator() != null);
                break;
            }
            case ONLY_RIGHT: {
                currentRightPacket = queuedPacket;
                Log.d(BleQueue.class.getName(), "Sent ONLY_RIGHT packet: " + Utils.bytesToHex(currentRightPacket.getPacket()));
                this.manager.getRightGlass().send(currentRightPacket.getPacket(), currentRightPacket.getValidator() != null);
                break;
            }
            case BOTH: {
                currentBothPacket = queuedPacket;
                Log.d(BleQueue.class.getName(), "Sent BOTH packet: " + Utils.bytesToHex(currentBothPacket.getPacket()));
                this.manager.getLeftGlass().send(currentBothPacket.getPacket(), currentBothPacket.getValidator() != null);
                this.manager.getRightGlass().send(currentBothPacket.getPacket(), currentBothPacket.getValidator() != null);
                break;
            }
        }

        if (packetQueue.isEmpty()) {
            queueEmptyCallback.run();
        }
    }

    public void onPacketWritten(boolean isRight) {
        synchronized (this) {
            if (isRight) {
                if (this.currentRightPacket != null && this.currentRightPacket.getValidator() == null) {
                    Log.d(BleQueue.class.getName(), "Received ONLY_RIGHT callback on right glass");
                    this.currentRightPacket = null;
                    this.processNextPacket();
                } else if (!this.bothPacketReadyOnRight && this.currentBothPacket != null
                        && this.currentBothPacket.getValidator() == null) {
                    Log.d(BleQueue.class.getName(), "Received BOTH callback on right glass");
                    if (this.bothPacketReadyOnLeft) {
                        this.bothPacketReadyOnLeft = false;
                        this.currentBothPacket = null;
                        this.processNextPacket();
                    } else {
                        this.bothPacketReadyOnRight = true;
                    }
                }
            } else {
                if (this.currentLeftPacket != null && this.currentLeftPacket.getValidator() == null) {
                    Log.d(BleQueue.class.getName(), "Received ONLY_LEFT callback on left glass");
                    this.currentLeftPacket = null;
                    this.processNextPacket();
                } else if (!this.bothPacketReadyOnLeft && this.currentBothPacket != null
                        && this.currentBothPacket.getValidator() == null) {
                    Log.d(BleQueue.class.getName(), "Received BOTH callback on left glass");
                    if (this.bothPacketReadyOnRight) {
                        this.bothPacketReadyOnRight = false;
                        this.currentBothPacket = null;
                        this.processNextPacket();
                    } else {
                        this.bothPacketReadyOnLeft = true;
                    }
                }
            }
        }
    }

    public void onPacketReceived(byte[] data, boolean isRight) {
        synchronized (this) {
            if (isRight) {
                if (this.currentRightPacket != null && this.currentRightPacket.getValidator() != null) {
                    if (this.currentRightPacket.getValidator().apply(data)) {
                        Log.d(BleQueue.class.getName(), "Received ONLY_RIGHT callback packet on right glass: " + Utils.bytesToHex(data));
                        this.currentRightPacket = null;
                        this.processNextPacket();
                    }
                } else if (!this.bothPacketReadyOnRight && this.currentBothPacket != null
                        && this.currentBothPacket.getValidator() != null) {
                    if (this.currentBothPacket.getValidator().apply(data)) {
                        Log.d(BleQueue.class.getName(), "Received BOTH callback packet on right glass: " + Utils.bytesToHex(data));
                        if (this.bothPacketReadyOnLeft) {
                            this.bothPacketReadyOnLeft = false;
                            this.currentBothPacket = null;
                            this.processNextPacket();
                        } else {
                            this.bothPacketReadyOnRight = true;
                        }
                    }
                }
            } else {
                if (this.currentLeftPacket != null && this.currentLeftPacket.getValidator() != null) {
                    if (this.currentLeftPacket.getValidator().apply(data)) {
                        Log.d(BleQueue.class.getName(), "Received ONLY_LEFT callback packet on left glass: " + Utils.bytesToHex(data));
                        this.currentLeftPacket = null;
                        this.processNextPacket();
                    }
                } else if (!this.bothPacketReadyOnLeft && this.currentBothPacket != null
                        && this.currentBothPacket.getValidator() != null) {
                    if (this.currentBothPacket.getValidator().apply(data)) {
                        Log.d(BleQueue.class.getName(), "Received BOTH callback packet on left glass: " + Utils.bytesToHex(data));
                        if (this.bothPacketReadyOnRight) {
                            this.bothPacketReadyOnRight = false;
                            this.currentBothPacket = null;
                            this.processNextPacket();
                        } else {
                            this.bothPacketReadyOnLeft = true;
                        }
                    }
                }
            }
        }
    }

    public boolean isEmpty() {
        return this.packetQueue.isEmpty();
    }
}
