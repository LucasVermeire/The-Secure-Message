package multicast;

import utils.NetworkInterfaceUtils;

import java.io.IOException;

public class ProgramPocMulticastListener {

    private static final String GROUP_IP = "224.12.23.45";
    private static final int PORT = 12345;
    private static final String ADDRESS_OF_INTERFACE = "192.168.1.37";

    public static void main(String[] args) {
        try {
            MulticastListener multicastListener = new MulticastListener( GROUP_IP, PORT, NetworkInterfaceUtils.getNetworkInterfaceForAddress(ADDRESS_OF_INTERFACE), new ProgramPoc() );

            (new Thread(multicastListener)).start();
        } catch (IOException e) { e.printStackTrace(); }
    }

    static class ProgramPoc implements MulticastListener.MulticastListenerCallback {

        @Override
        public void multicastMessageReceivedCallback(MulticastPacket multicastPacket) {
            System.out.println("\t---> Callback called !");
        }
    }
}
