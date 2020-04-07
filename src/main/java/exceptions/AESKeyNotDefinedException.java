package exceptions;

public class AESKeyNotDefinedException extends RuntimeException {
    public AESKeyNotDefinedException(String s) {
        this(s, new RuntimeException());
    }
    public AESKeyNotDefinedException(String s, Exception ex) {
        super(s, ex);
    }
}
