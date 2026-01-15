package com.racslabs.socket;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static java.lang.Math.min;

public record SocketHandler(Socket socket) {

    public byte[] send(byte[] bytes) throws IOException {
        OutputStream os = socket.getOutputStream();

        byte[] length = ByteBuffer.allocate(Long.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putLong(bytes.length)
                .array();

        os.write(length);
        os.write(bytes);
        os.flush();

        return receive();
    }

    private byte[] receive() throws IOException {
        InputStream is = socket.getInputStream();

        byte[] lengthBytes = new byte[8];
        int r = is.read(lengthBytes);
        if (r == -1) throw new EOFException("Stream closed before length prefix was fully read");

        long totalBytes = ByteBuffer.wrap(lengthBytes)
                .order(ByteOrder.LITTLE_ENDIAN)
                .getLong();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        long bytesRead = 0;

        while (bytesRead < totalBytes) {
            int n = (int) min(buf.length, totalBytes - bytesRead);
            r = is.read(buf, 0, n);
            if (r == -1) throw new EOFException("Stream closed before message was fully read");
            baos.write(buf, 0, r);
            bytesRead += r;
        }

        return baos.toByteArray();
    }

}