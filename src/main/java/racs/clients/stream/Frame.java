package racs.clients.stream;

import org.apache.commons.codec.digest.MurmurHash3;
import racs.clients.exception.RacsException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.CRC32C;

public class Frame {
    public static final byte[] CHUNK_ID = new byte[] {'r', 's', 'p'};

    private static final int HEADER_SIZE = 34;

    private final byte[] sessionId;
    private final long streamId;
    private final int checksum;
    private final byte flags;
    private final byte[] block;

    private Frame(FrameBuilder builder) {
        this.sessionId = builder.sessionId;
        this.streamId = builder.streamId;
        this.checksum = builder.checksum;
        this.block = builder.block;
        this.flags = builder.flags;
    }

    public static FrameBuilder builder() {
        return new FrameBuilder();
    }

    public byte[] pack() {
        ByteBuffer buf = ByteBuffer.allocate(HEADER_SIZE + block.length)
                .order(ByteOrder.LITTLE_ENDIAN);

        buf.put(CHUNK_ID);
        buf.put(sessionId);
        buf.putLong(streamId);
        buf.putInt(checksum);
        buf.putShort((short) block.length);
        buf.put(flags);
        buf.put(block);

        return buf.array();
    }

    public static class FrameBuilder {
        private long streamId;
        private int checksum;
        private byte flags;
        private byte[] block;
        private byte[] sessionId;

        public FrameBuilder streamId(String streamId) {
            long[] hash = MurmurHash3.hash128x64(streamId.getBytes());
            this.streamId = hash[0];
            return this;
        }

        public FrameBuilder block(byte[] block) throws RacsException {
            if (block.length > 0xffff)
                throw new RacsException("Block size too large: max 65535 bytes");

            CRC32C crc32c = new CRC32C();
            crc32c.update(block, 0, block.length);

            this.checksum = (int)crc32c.getValue();
            this.block = block;

            return this;
        }

        public FrameBuilder sessionId(byte[] sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public FrameBuilder flags(boolean flag) {
            this.flags = (byte) (flag ? 1 : 0);
            return this;
        }

        public Frame build() {
            return new Frame(this);
        }
    }

}
