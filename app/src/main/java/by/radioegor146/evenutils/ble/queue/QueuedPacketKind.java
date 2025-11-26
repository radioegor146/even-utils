package by.radioegor146.evenutils.ble.queue;

public enum QueuedPacketKind {
    ONLY_LEFT(true, false),
    ONLY_RIGHT(false, true),
    BOTH(true, true);

    private final boolean toLeft;
    private final boolean toRight;

    QueuedPacketKind(boolean toLeft, boolean toRight) {
        this.toLeft = toLeft;
        this.toRight = toRight;
    }

    public boolean isToLeft() {
        return toLeft;
    }

    public boolean isToRight() {
        return toRight;
    }
}
