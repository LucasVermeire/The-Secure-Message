package exceptions;

public class UserAlreadyAuthException extends RuntimeException {
    public UserAlreadyAuthException(String s) {
        this(s, new RuntimeException());
    }
    public UserAlreadyAuthException(String s, Exception ex) {
        super(s, ex);
    }
}
