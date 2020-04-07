package exceptions;

public class PasswordNotValidException extends RuntimeException {
    public PasswordNotValidException(String s) {
        this(s, new RuntimeException());
    }
    public PasswordNotValidException(String s, Exception ex) {
        super(s, ex);
    }
}
