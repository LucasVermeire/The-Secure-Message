package exceptions;

public class UserNotRegisteredYetException extends RuntimeException {
    public UserNotRegisteredYetException(String s) {
        this(s, new RuntimeException());
    }
    public UserNotRegisteredYetException(String s, Exception ex) {
        super(s, ex);
    }
}
