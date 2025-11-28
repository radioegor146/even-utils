package by.radioegor146.evenutils.ble.packets.dashboard;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DashboardNewsUpdateData {
    private final short totalPackets;
    private final short currentPacketIndex;
    private final byte[] data;

    public DashboardNewsUpdateData(short totalPackets, short currentPacketIndex, byte[] data) {
        this.totalPackets = totalPackets;
        this.currentPacketIndex = currentPacketIndex;
        this.data = data;
    }

    public byte[] serialize() {
        ByteBuffer buffer = ByteBuffer.allocate(1 + 2 + 2 + this.data.length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte) 0x05);
        buffer.putShort(this.totalPackets);
        buffer.putShort(this.currentPacketIndex);
        buffer.put(this.data);
        return buffer.array();
    }
}
