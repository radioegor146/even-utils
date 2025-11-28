package by.radioegor146.evenutils.ble.packets.dashboard.citywalk;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import by.radioegor146.evenutils.ble.packets.dashboard.DashboardCustomDisplayAreaKind;
import by.radioegor146.evenutils.ble.packets.dashboard.DashboardDisplayMode;

public class CitywalkUpdateData {

    public static class CursorData {
        private final short cursorX;
        private final short cursorY;
        private final byte[] cursor;

        public CursorData(short cursorX, short cursorY, byte[] cursor) {
            this.cursorX = cursorX;
            this.cursorY = cursorY;
            this.cursor = cursor;
        }

        public byte[] serialize() {
            ByteBuffer buffer = ByteBuffer.allocate(2 + 2 + this.cursor.length);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.putShort(this.cursorX);
            buffer.putShort(this.cursorY);
            buffer.put(this.cursor);
            return buffer.array();
        }
    }

    private final DashboardDisplayMode dashboardDisplayMode;
    private final DashboardCustomDisplayAreaKind dashboardCustomDisplayAreaKind;
    private final CitywalkDisplayMode citywalkDisplayMode;
    private final CursorData cursorData;

    public CitywalkUpdateData(DashboardDisplayMode dashboardDisplayMode,
                              DashboardCustomDisplayAreaKind dashboardCustomDisplayAreaKind,
                              CitywalkDisplayMode citywalkDisplayMode, CursorData cursorData) {
        this.dashboardDisplayMode = dashboardDisplayMode;
        this.dashboardCustomDisplayAreaKind = dashboardCustomDisplayAreaKind;
        this.citywalkDisplayMode = citywalkDisplayMode;
        this.cursorData = cursorData;
    }

    public byte[] serialize() {
        byte[] rawCursorData = this.cursorData == null ? new byte[0] : this.cursorData.serialize();
        ByteBuffer buffer = ByteBuffer.allocate(1 + 1 + 1 + rawCursorData.length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte) this.dashboardDisplayMode.ordinal());
        buffer.put((byte) this.dashboardCustomDisplayAreaKind.ordinal());
        buffer.put((byte) this.citywalkDisplayMode.ordinal());
        buffer.put(rawCursorData);
        return buffer.array();
    }
}
