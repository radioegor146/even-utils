package by.radioegor146.evenutils;

import androidx.annotation.NonNull;

public class Utils {

    @NonNull
    public static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02x", b & 0xFF));
        }
        return hexString.toString();
    }
}
