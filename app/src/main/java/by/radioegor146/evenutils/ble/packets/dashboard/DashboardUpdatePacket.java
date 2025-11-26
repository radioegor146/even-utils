package by.radioegor146.evenutils.ble.packets.dashboard;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.function.Function;

public class DashboardUpdatePacket {
    private final byte syncId;
    private final byte[] data;

    public DashboardUpdatePacket(byte syncId, byte[] data) {
        this.syncId = syncId;
        this.data = data;
    }

    public byte[] serialize() {
        ByteBuffer buffer = ByteBuffer.allocate(1 + 2 + 1 + data.length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte) 0x06);
        buffer.putShort((short) (1 + 2 + 1 + this.data.length));
        buffer.put(this.syncId);
        buffer.put(this.data);
        return buffer.array();
    }

    public static Function<byte[], Boolean> createReplyPacketValidator(byte syncId) {
        return (reply) -> reply.length >= 4 && reply[0] == 0x06 && reply[3] == syncId;
    }
}
