package by.radioegor146.evenutils.ble.packets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PacketHelper {

    public static List<byte[]> split(byte[] data, int sizePerSlice) {
        List<byte[]> result = new ArrayList<>((data.length + sizePerSlice - 1) / sizePerSlice);
        for (int i = 0; i < data.length; i += sizePerSlice) {
            result.add(Arrays.copyOfRange(data, i, i + Math.min(data.length - i, sizePerSlice)));
        }
        return result;
    }
}
