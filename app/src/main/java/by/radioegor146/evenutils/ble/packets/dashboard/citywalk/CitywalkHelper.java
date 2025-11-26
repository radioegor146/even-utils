package by.radioegor146.evenutils.ble.packets.dashboard.citywalk;

import java.util.ArrayList;
import java.util.List;

import by.radioegor146.evenutils.ble.packets.PacketHelper;
import by.radioegor146.evenutils.ble.packets.dashboard.DashboardCitywalkUpdateData;
import by.radioegor146.evenutils.ble.packets.dashboard.DashboardCustomDisplayAreaKind;
import by.radioegor146.evenutils.ble.packets.dashboard.DashboardDisplayMode;
import by.radioegor146.evenutils.ble.packets.dashboard.DashboardUpdatePacket;

public class CitywalkHelper {

    public static List<byte[]> buildCitywalkMapUpdatePackets(byte startSyncId,
                                                             DashboardDisplayMode displayMode,
                                                             byte[] map) {
        CitywalkMapUpdateData mapUpdateData = new CitywalkMapUpdateData(displayMode,
                DashboardCustomDisplayAreaKind.CITYWALK, CitywalkDisplayMode.UPDATING_MAP, map);
        byte[] rawMapUpdateData = mapUpdateData.serialize();
        List<byte[]> packetParts = PacketHelper.split(rawMapUpdateData, 180);

        List<byte[]> packets = new ArrayList<>();
        int syncIdOffset = 0;
        for (byte[] part : packetParts) {
            packets.add(new DashboardUpdatePacket((byte) (startSyncId + syncIdOffset),
                    new DashboardCitywalkUpdateData((short) packetParts.size(),
                            (short) (syncIdOffset + 1), part).serialize()).serialize());
            syncIdOffset++;
        }
        return packets;
    }

    public static byte[] buildCitywalkCursorUpdatePacket(byte syncId,
                                                         DashboardDisplayMode displayMode,
                                                         CitywalkDisplayMode citywalkDisplayMode,
                                                         CitywalkUpdateData.CursorData cursorData) {
        CitywalkUpdateData updateData = new CitywalkUpdateData(displayMode,
                DashboardCustomDisplayAreaKind.CITYWALK,
                citywalkDisplayMode, cursorData);
        DashboardCitywalkUpdateData dashboardCitywalkUpdateData =
                new DashboardCitywalkUpdateData((short) 1, (short) 1, updateData.serialize());
        return new DashboardUpdatePacket(syncId, dashboardCitywalkUpdateData.serialize()).serialize();
    }
}
