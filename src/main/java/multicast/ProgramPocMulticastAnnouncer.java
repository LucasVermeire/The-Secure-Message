package multicast;

import utils.NetworkInterfaceUtils;

import java.io.IOException;

public class ProgramPocMulticastAnnouncer {

    private static final String GROUP_IP = "224.12.23.45";
    private static final int PORT = 12345;
    private static final String RECURRENT_MESSAGE = "THIS IS AN HARDCODED MESSAGE, PLEASE NOT TAKE COUNT OF THIS";
    private static final String ADDRESS_OF_INTERFACE = "192.168.1.37";

    public static void main(String[] args) {
        try {
            MulticastAnnouncer multicastAnnouncer = new MulticastAnnouncer(
                    GROUP_IP, PORT,
                    NetworkInterfaceUtils.getNetworkInterfaceForAddress(ADDRESS_OF_INTERFACE),
                    10, RECURRENT_MESSAGE
            );

            (new Thread(multicastAnnouncer)).start();
        } catch (IOException e) { e.printStackTrace(); }
    }
}
