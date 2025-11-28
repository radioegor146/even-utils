package by.radioegor146.evenutils.ble.packets.dashboard.news;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import by.radioegor146.evenutils.ble.packets.dashboard.DashboardCustomDisplayAreaKind;
import by.radioegor146.evenutils.ble.packets.dashboard.DashboardDisplayMode;

public class NewsUpdateData {

    public static class NewsData {
        private final String source;
        private final String text;

        public NewsData(String source, String text) {
            this.source = source;
            this.text = text;
        }

        public byte[] serialize() {
            byte[] rawSource = this.source.getBytes(StandardCharsets.UTF_8);
            if (rawSource.length > 64) {
                throw new RuntimeException("news source length > 64");
            }
            byte[] rawText = this.text.getBytes(StandardCharsets.UTF_8);
            if (rawText.length > 280) {
                throw new RuntimeException("news text length > 280");
            }
            ByteBuffer buffer = ByteBuffer.allocate(1 + 1 + rawSource.length + 1 + 2 + rawText.length);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.put((byte) 0x01);
            buffer.put((byte) rawSource.length);
            buffer.put(rawSource);
            buffer.put((byte) 0x02);
            buffer.putShort((short) rawText.length);
            buffer.put(rawText);
            return buffer.array();
        }
    }

    private final DashboardDisplayMode dashboardDisplayMode;
    private final DashboardCustomDisplayAreaKind dashboardCustomDisplayAreaKind;
    private final NewsDisplayMode newsDisplayMode;
    private final int newsIndex;
    private final NewsOperation operation;
    private final NewsData data;

    public NewsUpdateData(DashboardDisplayMode dashboardDisplayMode, DashboardCustomDisplayAreaKind dashboardCustomDisplayAreaKind, NewsDisplayMode newsDisplayMode, int newsIndex, NewsOperation operation, NewsData data) {
        this.dashboardDisplayMode = dashboardDisplayMode;
        this.dashboardCustomDisplayAreaKind = dashboardCustomDisplayAreaKind;
        this.newsDisplayMode = newsDisplayMode;
        this.newsIndex = newsIndex;
        this.operation = operation;
        this.data = data;
    }

    public byte[] serialize() {
        if (this.newsIndex < 1) {
            throw new RuntimeException("news index < 1");
        }
        if (this.newsIndex > 4) {
            throw new RuntimeException("news index > 5");
        }
        byte[] rawNewsData = this.data == null ? new byte[0] : this.data.serialize();
        ByteBuffer buffer = ByteBuffer.allocate(1 + 1 + 1 + 1 + 1 + rawNewsData.length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte) this.dashboardDisplayMode.ordinal());
        buffer.put((byte) this.dashboardCustomDisplayAreaKind.ordinal());
        buffer.put((byte) this.newsDisplayMode.ordinal());
        buffer.put((byte) this.newsIndex);
        buffer.put((byte) this.operation.ordinal());
        buffer.put(rawNewsData);
        return buffer.array();
    }
}
