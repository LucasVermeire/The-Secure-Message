package exceptions;

public class AesException extends RuntimeException {
    public AesException(String s) {
        this(s, new RuntimeException());
    }
    public AesException(String s, Exception ex) {
        super(s, ex);
    }
}
