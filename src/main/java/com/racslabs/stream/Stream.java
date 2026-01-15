package com.racslabs.stream;

import com.github.luben.zstd.Zstd;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;

import com.racslabs.command.Command;
import com.racslabs.pack.Unpacker;
import com.racslabs.exception.RacsException;
import com.racslabs.socket.SocketHandler;
import com.racslabs.socket.ConnectionPool;
import com.racslabs.util.Pcm;
import com.racslabs.util.SessionId;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Stream extends Command {

    private static final int DEFAULT_CHUNK_SIZE = 1024 * 32; // 32 KB
    private static final int DEFAULT_BATCH_SIZE = 50;
    private static final int DEFAULT_COMPRESSION_LEVEL = 3;

    private final String streamId;
    private int chunkSize = DEFAULT_CHUNK_SIZE;
    private int batchSize = DEFAULT_BATCH_SIZE;
    private int compressionLevel = DEFAULT_COMPRESSION_LEVEL;
    private boolean compression = true;

    public Stream(ConnectionPool connectionPool, String streamId) {
        super(connectionPool);
        this.streamId = streamId;
    }

    public Stream chunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
        return this;
    }

    public Stream batchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public Stream compressionLevel(int compressionLevel) {
        this.compressionLevel = compressionLevel;
        return this;
    }

    public Stream compression(boolean compression) {
        this.compression = compression;
        return this;
    }

    public void execute(int[] pcmData) throws RacsException, IOException, InterruptedException {
        stream(streamId,
                chunkSize,
                pcmData,
                batchSize,
                compression,
                compressionLevel);
    }

    private void stream(String streamId,
                        int chunkSize,
                        int[] pcmData,
                        int batchSize,
                        boolean compression,
                        int compressionLevel)
            throws RacsException, IOException, InterruptedException {

        long bitDepth = (long) executeCommand(String.format("META '%s' 'bit_depth'", streamId));
        executeCommand(String.format("OPEN '%s'", streamId));

        if (chunkSize < 0 || chunkSize > 0xffff)
            throw new RacsException("'chunk_size' must be >= 0 or <= 0xffff");

        int samplesPerChunk = chunkSize / ((int)bitDepth / 8);
        List<byte[]> frames = new ArrayList<>(batchSize);

        Runnable flush = () -> {
            if (frames.isEmpty()) return;

            Socket socket = null;
            try {
                socket = connectionPool.borrowSocket();

                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                buf.write(Frame.CHUNK_ID);

                MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
                packer.packArrayHeader(frames.size());
                for (byte[] frame : frames) {
                    packer.packBinaryHeader(frame.length);
                    packer.writePayload(frame);
                }

                buf.write(packer.toByteArray());
                packer.close();

                byte[] response = new SocketHandler(socket).send(buf.toByteArray());
                new Unpacker().unpack(response);

            } catch (IOException | InterruptedException | RacsException e) {
                throw new RuntimeException(e);
            } finally {
                if (socket != null) {
                    try {
                        connectionPool.returnSocket(socket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                frames.clear();
            }
        };

        byte[] sessionId = SessionId.generate();

        for (int[] chunk : Pcm.chunk(pcmData, samplesPerChunk)) {
            byte[] data = Pcm.pack(chunk, (int)bitDepth);

            if (compression)
                data = Zstd.compress(data, compressionLevel);

            assert data != null;
            byte[] frame = Frame.builder()
                    .streamId(streamId)
                    .sessionId(sessionId)
                    .flags(compression)
                    .block(data)
                    .build()
                    .pack();

            frames.add(frame);

            if (frames.size() == batchSize)
                flush.run();
        }

        flush.run();
        executeCommand(String.format("CLOSE '%s'", streamId));
    }
}
