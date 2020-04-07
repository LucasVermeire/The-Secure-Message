package exceptions;

public class ConnectionException extends Exception {

    public ConnectionException(String message) { super(message); }
    public ConnectionException(String message, Exception ex) { super(message, ex); }
}
