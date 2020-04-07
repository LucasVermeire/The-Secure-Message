package exceptions;

public class ServerDomainIsAlreadyPresentException extends RuntimeException {
    public ServerDomainIsAlreadyPresentException(String s) {
        this(s, new RuntimeException());
    }
    public ServerDomainIsAlreadyPresentException(String s, Exception ex) {
        super(s, ex);
    }
}
