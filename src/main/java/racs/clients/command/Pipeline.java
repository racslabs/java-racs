package racs.clients.command;

import racs.clients.exception.RacsException;
import racs.clients.socket.ConnectionPool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Pipeline extends Command {

    private final List<String> commands;

    public Pipeline(ConnectionPool connectionPool) {
        super(connectionPool);
        this.commands = new ArrayList<>();
    }

    public Pipeline range(String streamId, double start, double duration) {
        String command = String.format("RANGE '%s' %f %f", streamId, start, duration);
        commands.add(command);
        return this;
    }

    public Pipeline encode(String mimeType) {
        String command = String.format("ENCODE '%s'", mimeType);
        commands.add(command);
        return this;
    }

    public Pipeline create(String streamId, int sampleRate, int channels, int bitDepth) {
        String command = String.format("CREATE '%s' %d %d %d", streamId, sampleRate, channels, bitDepth);
        commands.add(command);
        return this;
    }

    public Pipeline meta(String streamId, String attr) {
        String command = String.format("META '%s' '%s'", streamId, attr);
        commands.add(command);
        return this;
    }

    public Pipeline list(String pattern) {
        String command = String.format("LIST '%s'", pattern);
        commands.add(command);
        return this;
    }

    public Pipeline open(String streamId) {
        String command = String.format("OPEN '%s'", streamId);
        commands.add(command);
        return this;
    }

    public Pipeline close(String streamId) {
        String command = String.format("CLOSE '%s'", streamId);
        commands.add(command);
        return this;
    }

    public Pipeline eval(String expr) {
        String command = String.format("EVAL '%s'", expr);
        commands.add(command);
        return this;
    }

    public Pipeline ping() {
        commands.add("PING");
        return this;
    }

    public Pipeline shutdown() {
        commands.add("SHUTDOWN");
        return this;
    }

    public Object execute() throws RacsException, IOException, InterruptedException {
        String joined = String.join(" |> ", commands);
        System.out.println(joined);
        return this.executeCommand(joined);
    }

    public Pipeline reset() {
        commands.clear();
        return this;
    }
}

