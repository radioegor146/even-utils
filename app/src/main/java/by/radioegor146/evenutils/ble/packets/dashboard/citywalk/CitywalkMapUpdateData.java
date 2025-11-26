package by.radioegor146.evenutils.ble.packets.dashboard.citywalk;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import by.radioegor146.evenutils.ble.packets.dashboard.DashboardCustomDisplayAreaKind;
import by.radioegor146.evenutils.ble.packets.dashboard.DashboardDisplayMode;

public class CitywalkMapUpdateData {
    private final DashboardDisplayMode dashboardDisplayMode;
    private final DashboardCustomDisplayAreaKind dashboardCustomDisplayAreaKind;
    private final CitywalkDisplayMode citywalkDisplayMode;
    private final byte[] map;

    public CitywalkMapUpdateData(DashboardDisplayMode dashboardDisplayMode,
                                 DashboardCustomDisplayAreaKind dashboardCustomDisplayAreaKind,
                                 CitywalkDisplayMode citywalkDisplayMode, byte[] map) {
        this.dashboardDisplayMode = dashboardDisplayMode;
        this.dashboardCustomDisplayAreaKind = dashboardCustomDisplayAreaKind;
        this.citywalkDisplayMode = citywalkDisplayMode;
        this.map = map;
    }

    public byte[] serialize() {
        ByteBuffer buffer = ByteBuffer.allocate(1 + 1 + 1 + 1 + this.map.length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte) this.dashboardDisplayMode.ordinal());
        buffer.put((byte) this.dashboardCustomDisplayAreaKind.ordinal());
        buffer.put((byte) this.citywalkDisplayMode.ordinal());
        buffer.put((byte) 0x01);
        buffer.put(this.map);
        return buffer.array();
    }
}
