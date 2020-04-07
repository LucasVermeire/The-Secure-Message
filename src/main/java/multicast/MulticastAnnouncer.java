package multicast;

import utils.Logger;

import java.io.IOException;
import java.net.NetworkInterface;

public class MulticastAnnouncer implements Runnable {
    private final String groupAddress;
    private final int port;
    private final NetworkInterface networkInterface;
    private boolean stop = false;
    private final Multicast multicast;
    private final String messageToAnnounce;
    private final int secondsBetweenAnnounce;
    private final boolean DEBUG = false;

    public MulticastAnnouncer(String groupAddress, int port,
                              NetworkInterface networkInterface, int secondsBetweenAnnounce,
                              String messageToAnnounce) throws IOException {
        this.groupAddress = groupAddress;
        this.port = port;
        this.networkInterface = networkInterface;

        this.multicast = new Multicast(groupAddress, port, networkInterface);
        this.messageToAnnounce = messageToAnnounce;
        this.secondsBetweenAnnounce = secondsBetweenAnnounce;
    }

    @Override
    public void run() {
        /* TODO remove this, it's for debug */ logMessage( "Démarrage du MulticastAnnouncer" );
        try {
            while (!stop) {
                announce();
                Thread.sleep(secondsBetweenAnnounce * 1000);
            }
        } catch (InterruptedException ignored) {  }
    }

    private void announce() {
        if (DEBUG) { /* TODO remove this, it's for debug */ logMessage("Announce -> \""+messageToAnnounce+"\""); }
        multicast.sendMessage(messageToAnnounce);
    }
    
    private void logMessage(String message){
        Logger.log(getClass().getSimpleName(), "",
                String.format("%s:%d ( %s - %s ) - %s",
                        groupAddress, port, networkInterface.getDisplayName(), networkInterface.getName(), message
                )
        );
    }
}
