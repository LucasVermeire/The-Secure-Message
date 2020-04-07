package exceptions;

public class UserAlreadyRegisteredException extends RuntimeException {
    public UserAlreadyRegisteredException(String s) {
        this(s, new RuntimeException());
    }
    public UserAlreadyRegisteredException(String s, Exception ex) {
        super(s, ex);
    }
}
