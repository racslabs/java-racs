package racs.clients.util;

import java.nio.ByteBuffer;
import java.util.UUID;

public class SessionId {

    public static byte[] generate() {
        UUID uuid = UUID.randomUUID();
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return buffer.array();
    }
}
