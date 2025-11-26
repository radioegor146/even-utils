package by.radioegor146.evenutils.ble.queue;

import java.util.function.Consumer;
import java.util.function.Function;

public class QueuedPacket {
    private final byte[] packet;
    private final Function<byte[], Boolean> validator;
    private final QueuedPacketKind kind;

    public QueuedPacket(byte[] packet, Function<byte[], Boolean> validator, QueuedPacketKind kind) {
        this.packet = packet;
        this.validator = validator;
        this.kind = kind;
    }

    public byte[] getPacket() {
        return packet;
    }

    public Function<byte[], Boolean> getValidator() {
        return validator;
    }

    public QueuedPacketKind getKind() {
        return kind;
    }
}
