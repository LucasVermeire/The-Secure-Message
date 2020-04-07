package multicast;

public class MulticastPacket {
    private final String sourceAddress;
    private final String message;

    MulticastPacket(String sourceAddress, String message) {
        this.sourceAddress = sourceAddress;
        this.message = message;
    }

    public String getSourceAddress() { return sourceAddress; }
    public String getMessage() { return message; }

    @Override
    public String toString(){ return String.format("%s -> %s", sourceAddress, message); }
}
