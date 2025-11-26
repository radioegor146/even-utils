package by.radioegor146.evenutils;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.Size;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import by.radioegor146.evenutils.ble.BleManager;
import by.radioegor146.evenutils.ble.packets.dashboard.DashboardDisplayMode;
import by.radioegor146.evenutils.ble.packets.dashboard.DashboardUpdatePacket;
import by.radioegor146.evenutils.ble.packets.dashboard.citywalk.CitywalkDisplayMode;
import by.radioegor146.evenutils.ble.packets.dashboard.citywalk.CitywalkHelper;
import by.radioegor146.evenutils.ble.packets.dashboard.citywalk.CitywalkUpdateData;
import by.radioegor146.evenutils.ble.queue.QueuedPacket;
import by.radioegor146.evenutils.ble.queue.QueuedPacketKind;

public class EvenRealitiesAdapter {

    public final static Size DUAL_MAP_SIZE = new Size(376, 136);
    public final static Size FULL_MAP_SIZE = new Size(296, 136);
    public final static Size MAP_CURSOR_SIZE = new Size(32, 32);
    public final static Size STOCK_GRAPH_SIZE = new Size(184, 82);

    private final BleManager bleManager;

    private DashboardDisplayMode displayMode = DashboardDisplayMode.DUAL;

    private byte[] oldMapImage = null;
    private byte[] oldCursorImage = null;
    private int oldCursorX = -1;
    private int oldCursorY = -1;

    public EvenRealitiesAdapter(BleManager bleManager) {
        this.bleManager = bleManager;
    }

    public void setDashboardMode(DashboardDisplayMode displayMode) {
        this.displayMode = displayMode;
        this.oldMapImage = null;
        this.oldCursorImage = null;
        this.oldCursorX = -1;
        this.oldCursorY = -1;
    }

    private static byte[] convertImage(Bitmap bitmap, Size size) {
        if (bitmap.getWidth() != size.getWidth() || bitmap.getHeight() != size.getHeight()) {
            throw new RuntimeException("wrong size: " + bitmap.getWidth() + "x" +
                    bitmap.getHeight() + " != " + size.getWidth() + "x" + size.getHeight());
        }
        byte[] data = new byte[bitmap.getWidth() * bitmap.getHeight() / 8];
        for (int x = 0; x < bitmap.getWidth(); x++) {
            for (int y = 0; y < bitmap.getHeight(); y++) {
                boolean isPixelSet = bitmap.getColor(x, y).red() > 0;
                data[x / 8 + y * bitmap.getWidth() / 8] |= (byte) (isPixelSet ? (1 << x % 8) : 0);
            }
        }
        Log.w(EvenRealitiesAdapter.class.getName(), Utils.bytesToHex(data));
        return data;
    }

    public boolean setMapImage(Bitmap map, boolean force) {
        byte[] mapImage = convertImage(map, this.displayMode == DashboardDisplayMode.DUAL ?
                DUAL_MAP_SIZE : FULL_MAP_SIZE);

        if (!force && Arrays.equals(this.oldMapImage, mapImage)) {
            return false;
        }
        this.oldMapImage = mapImage;

        byte syncId = (byte) 0x80;
        List<QueuedPacket> queuedPackets = new ArrayList<>();
        List<byte[]> mapUpdatePackets = CitywalkHelper.buildCitywalkMapUpdatePackets(syncId,
                this.displayMode, mapImage);
        for (byte[] packet : mapUpdatePackets) {
            queuedPackets.add(new QueuedPacket(packet,
                    DashboardUpdatePacket.createReplyPacketValidator(syncId), QueuedPacketKind.BOTH));
            syncId++;
        }
        for (QueuedPacket packet : queuedPackets) {
            this.bleManager.getQueue().queuePacket(packet);
        }

        return true;
    }

    public boolean setCursorImage(Bitmap cursor, int x, int y, boolean force) {
        byte[] cursorImage = convertImage(cursor, MAP_CURSOR_SIZE);

        if (!force && Arrays.equals(this.oldCursorImage, cursorImage) &&
                this.oldCursorY == y && this.oldCursorX == x) {
            return false;
        }
        this.oldCursorImage = cursorImage;
        this.oldCursorX = x;
        this.oldCursorY = y;

        byte syncId = (byte) (Math.random() * 256);
        List<QueuedPacket> queuedPackets = new ArrayList<>();
        queuedPackets.add(new QueuedPacket(CitywalkHelper.buildCitywalkCursorUpdatePacket(syncId,
                displayMode, CitywalkDisplayMode.SHOW_MAP,
                new CitywalkUpdateData.CursorData((short) x, (short) y, cursorImage)),
                DashboardUpdatePacket.createReplyPacketValidator(syncId), QueuedPacketKind.BOTH));
        for (QueuedPacket packet : queuedPackets) {
            this.bleManager.getQueue().queuePacket(packet);
        }

        return true;
    }
}
