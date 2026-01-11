package racs.clients.exception;

public class RacsException extends Exception {

    public RacsException(String message) {
        super(message);
    }

    public RacsException(Throwable throwable) {
        super(throwable);
    }

    public RacsException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
