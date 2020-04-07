package exceptions;

public class UnknownServerDomainException extends RuntimeException {
    public UnknownServerDomainException(String s) {
        this(s, new RuntimeException());
    }
    public UnknownServerDomainException(String s, Exception ex) {
        super(s, ex);
    }
}
