package racs.clients.socket;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ConnectionPool {
    private final int poolSize;
    private final int port;
    private final String host;
    private final BlockingQueue<Socket> pool;

    public ConnectionPool(String host, int port, int poolSize) throws IOException {
        this.host = host;
        this.port = port;
        this.poolSize = poolSize;
        this.pool = new ArrayBlockingQueue<>(poolSize);
        this.initialize();
    }

    private void initialize() throws IOException {
        for (int i = 0; i < poolSize; i++) {
            pool.add(createSocket());
        }
    }

    private Socket createSocket() throws IOException {
        return new Socket(host, port);
    }

    public Socket borrowSocket() throws InterruptedException {
        return pool.take();
    }

    public boolean returnSocket(Socket socket) throws IOException {
        if (socket.isClosed() || !socket.isConnected())
            socket = createSocket();

        return pool.offer(socket);
    }

    public void closeAll() throws IOException {
        for (Socket socket: pool) {
            socket.close();
        }
    }

}
