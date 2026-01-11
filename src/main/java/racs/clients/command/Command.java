package racs.clients.command;

import racs.clients.pack.Unpacker;
import racs.clients.exception.RacsException;
import racs.clients.socket.SocketHandler;
import racs.clients.socket.ConnectionPool;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Command {

    protected final ConnectionPool connectionPool;

    public Command(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public Object executeCommand(String command) throws InterruptedException, IOException, RacsException {
        command += '\0';
        Socket socket = connectionPool.borrowSocket();
        SocketHandler socketHandler = new SocketHandler(socket);

        try {
            byte[] bytes = socketHandler.send(command.getBytes(StandardCharsets.US_ASCII));
            return new Unpacker().unpack(bytes);
        } finally {
            connectionPool.returnSocket(socket);
        }
    }

    public ConnectionPool getConnectionPool() {
        return connectionPool;
    }
}
