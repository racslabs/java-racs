package racs.clients.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Pcm {

    public static List<int[]> chunk(int[] data, int chunkSize) {
        List<int[]> chunks = new ArrayList<>();
        for (int start = 0; start < data.length; start += chunkSize) {
            int end = Math.min(data.length, start + chunkSize);
            int[] chunk = Arrays.copyOfRange(data, start, end);
            chunks.add(chunk);
        }
        return chunks;
    }

    public static byte[] pack(int[] data, int bitDepth) {
        return switch (bitDepth) {
            case 16 -> pack16(data);
            case 24 -> pack24(data);
            default -> null;
        };
    }

    private static byte[] pack16(int[] data) {
        ByteBuffer buf = ByteBuffer
                .allocate(data.length * 2)
                .order(ByteOrder.LITTLE_ENDIAN);

        for (int x : data) {
            buf.putShort((short) x);
        }

        return buf.array();
    }

    private static byte[] pack24(int[] data) {
        byte[] out = new byte[data.length * 3];
        int i = 0;

        for (int x : data) {
            out[i++] = (byte) (x);
            out[i++] = (byte) (x >> 8);
            out[i++] = (byte) (x >> 16);
        }

        return out;
    }
}
