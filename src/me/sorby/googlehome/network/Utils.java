package me.sorby.googlehome.network;

import java.util.Random;

public class Utils {
    static byte[] toArrayLE(int value) {
        return new byte[] {
                (byte) (value >> 24),
                (byte) (value >> 16),
                (byte) (value >> 8),
                (byte) value };
    }

    static int fromArrayLE(byte[] payload) {
        return payload[0] << 24 | (payload[1] & 0xFF) << 16 | (payload[2] & 0xFF) << 8 | (payload[3] & 0xFF);
    }

    static String randomString(int len) {
        String CHARS = "abcdefghijklmnopqrstuvwxyz1234567890";
        StringBuilder str = new StringBuilder();
        Random rand = new Random();
        while (str.length() < len) {
            int index = (int) (rand.nextFloat() * CHARS.length());
            str.append(CHARS.charAt(index));
        }
        String saltStr = str.toString();
        return saltStr;
    }
}
