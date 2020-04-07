package client;

public class ProgramClient {
    public static final String DEFAULT_DESTINATION="server01.group2.chat";
    public static final int DEFAULT_PORT=58001;

    public static void main(String[] args) {
        System.setProperty("javax.net.ssl.trustStore","group2.p12");
        System.setProperty("javax.net.ssl.trustStorePassword","group2");
        Client c = new Client(
                args.length >= 1 && args[0] != null ? args[0] : DEFAULT_DESTINATION,
                args.length >= 2 && args[1] != null ? Integer.parseInt(args[1]) : DEFAULT_PORT);
        c.startClient();
    }
}
