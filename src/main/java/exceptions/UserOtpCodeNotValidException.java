package exceptions;

public class UserOtpCodeNotValidException extends RuntimeException {
    public UserOtpCodeNotValidException(String s) {
        this(s, new RuntimeException());
    }
    public UserOtpCodeNotValidException(String s, Exception ex) {
        super(s, ex);
    }
}
