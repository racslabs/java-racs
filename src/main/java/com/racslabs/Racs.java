package com.racslabs;

import com.racslabs.command.Command;
import com.racslabs.command.Pipeline;
import com.racslabs.socket.ConnectionPool;
import com.racslabs.stream.Stream;

import java.io.IOException;

public class Racs extends Command {

    private static final int DEFAULT_POOL_SIZE = 3;

    public Racs(String host, int port) throws IOException {
        this(host, port, DEFAULT_POOL_SIZE);
    }

    public Racs(String host, int port, int poolSize) throws IOException {
        super(new ConnectionPool(host, port, poolSize));
    }

    public Pipeline pipeline() {
        return new Pipeline(this.connectionPool);
    }

    public Stream stream(String streamId) {
        return new Stream(this.connectionPool, streamId);
    }

    public void close() throws IOException {
        this.connectionPool.closeAll();
    }

}
