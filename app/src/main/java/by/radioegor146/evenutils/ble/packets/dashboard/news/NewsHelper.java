package by.radioegor146.evenutils.ble.packets.dashboard.news;

import java.util.ArrayList;
import java.util.List;

import by.radioegor146.evenutils.ble.packets.PacketHelper;
import by.radioegor146.evenutils.ble.packets.dashboard.DashboardCustomDisplayAreaKind;
import by.radioegor146.evenutils.ble.packets.dashboard.DashboardDisplayMode;
import by.radioegor146.evenutils.ble.packets.dashboard.DashboardNewsUpdateData;
import by.radioegor146.evenutils.ble.packets.dashboard.DashboardUpdatePacket;

public class NewsHelper {
    public static List<byte[]> buildNewsUpdatePackets(byte startSyncId,
                                                      DashboardDisplayMode displayMode,
                                                      NewsDisplayMode newsDisplayMode,
                                                      int newsIndex,
                                                      NewsOperation operation,
                                                      NewsUpdateData.NewsData newsData) {
        NewsUpdateData newsUpdateData = new NewsUpdateData(displayMode,
                DashboardCustomDisplayAreaKind.NEWS, newsDisplayMode, newsIndex, operation, newsData);
        byte[] rawNewsUpdateData = newsUpdateData.serialize();
        List<byte[]> packetParts = PacketHelper.split(rawNewsUpdateData, 180);

        List<byte[]> packets = new ArrayList<>();
        int syncIdOffset = 0;
        for (byte[] part : packetParts) {
            packets.add(new DashboardUpdatePacket((byte) (startSyncId + syncIdOffset),
                    new DashboardNewsUpdateData((short) packetParts.size(),
                            (short) (syncIdOffset + 1), part).serialize()).serialize());
            syncIdOffset++;
        }
        return packets;
    }
}
