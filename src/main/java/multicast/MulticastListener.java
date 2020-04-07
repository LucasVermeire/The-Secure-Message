package multicast;

import utils.Logger;

import java.io.IOException;
import java.net.NetworkInterface;

public class MulticastListener implements Runnable {
    private final String groupAddress;
    private final int port;
    private final NetworkInterface networkInterface;
    private boolean stop = false;
    private final Multicast multicast;
    private final MulticastListenerCallback multicastListenerCallback;
    private final boolean DEBUG = false;

    public MulticastListener(String groupAddress, int port, NetworkInterface networkInterface,
                             MulticastListenerCallback multicastListenerCallback) throws IOException {
        this.groupAddress = groupAddress;
        this.port = port;
        this.networkInterface = networkInterface;
        this.multicastListenerCallback = multicastListenerCallback;

        this.multicast = new Multicast(groupAddress, port, networkInterface);
    }

    @Override
    public void run() {
        /* TODO remove this, it's for debug */ logMessage("Démarrage du MulticastListener");

        startListenning();
    }

    private void startListenning() {
        while (!stop) {
            try {
                MulticastPacket mCastPacket = multicast.receiveMessage();
                if (DEBUG) { /* TODO remove this, it's for debug */ logMessage( String.format("Message reçu de \"%s\" -> \"%s\"",  mCastPacket.getSourceAddress(), mCastPacket.getMessage()) ); }
                multicastListenerCallback.multicastMessageReceivedCallback(mCastPacket);
            } catch (IOException e) {
                Logger.logError(getClass().getSimpleName(), "startListenning",
                        String.format("%s\n\tMessage : %s\n\tCause : %s\n",
                                e, e.getMessage(), e.getCause()
                        )
                );
            }
        }
    }

    private void logMessage(String message){
        Logger.log(getClass().getSimpleName(), "",
                String.format("%s:%d ( %s - %s ) - %s",
                        groupAddress, port, networkInterface.getDisplayName(), networkInterface.getName(), message
                )
        );
    }

    public interface MulticastListenerCallback{
        void multicastMessageReceivedCallback(MulticastPacket multicastPacket);
    }
}