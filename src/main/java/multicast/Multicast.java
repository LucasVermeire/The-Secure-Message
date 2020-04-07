package multicast;

import utils.Logger;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class MulticastReceiver {
    private MulticastSocket socketReception;

    MulticastReceiver(InetAddress groupeIP, int port, NetworkInterface netInt) throws IOException {
        socketReception = new MulticastSocket(port);
        socketReception.joinGroup(groupeIP);
        socketReception.setNetworkInterface(netInt);
        socketReception.setTimeToLive(15); // pour un site

        /* TODO remove this, it's for debug */ Logger.log(getClass().getSimpleName(), "MulticastReceiver",
                String.format("Receiver connected to %s:%d on NetworkInterface \"%s\" (%s)", groupeIP.getHostAddress(), port,
                    netInt.getDisplayName(), netInt.getName()
                )
        );
    }

    public MulticastPacket recevoir() throws IOException {
        byte[] contenuMessage = new byte[1024];
        DatagramPacket message = new DatagramPacket(contenuMessage, contenuMessage.length);

        try {
            socketReception.receive(message);
            (new DataInputStream(new ByteArrayInputStream(contenuMessage))).readFully(contenuMessage);

            contenuMessage = Arrays.copyOf(contenuMessage, message.getLength()); // Need to truncate the message to the "correct" size of message, sorry

        } catch (IOException ex) { throw new IOException(String.format("MulticastReceiver: %s, %s", ex, ex.getMessage())); }

        return new MulticastPacket(message.getAddress().getHostAddress(), new String(contenuMessage));
    }
}

class MulticastEmitter {
    private InetAddress groupIP;
    private int port;
    private MulticastSocket socketEmission;

    MulticastEmitter(InetAddress groupIP, int port, NetworkInterface netInt) throws IOException {
        this.groupIP = groupIP;
        this.port = port;

        socketEmission = new MulticastSocket(port);
        socketEmission.joinGroup(this.groupIP);
        socketEmission.setNetworkInterface(netInt);
        socketEmission.setTimeToLive(15); // pour un site

        /* TODO remove this, it's for debug */ Logger.log(getClass().getSimpleName(), "MulticastEmitter",
                String.format("Emitter connected to %s:%d on NetworkInterface \"%s\" (%s)", this.groupIP.getHostAddress(), port,
                        netInt.getDisplayName(), netInt.getName()
                )
        );
    }

    void sendMessage(byte[] message) throws Exception {
        try {
            DatagramPacket messageDP = new DatagramPacket(message, message.length, groupIP, port);
            socketEmission.send(messageDP);
        } catch (Exception ex) {
            throw new Exception(String.format("MulticastReceiver: %s, %s", ex, ex.getMessage()));
        }
    }
}

public class Multicast {

    private MulticastEmitter transmitter;
    private MulticastReceiver receiver;

    private String lastError = "";
    private final boolean DEBUG = false;

    public Multicast(String groupIP, int port) throws IOException {
        this(groupIP, port, null);
    }

    public Multicast(String groupIP, int port, NetworkInterface networkInterface) throws IOException {
        networkInterface = getNetworkInterface(networkInterface);

        transmitter = new MulticastEmitter(InetAddress.getByName(groupIP), port, networkInterface);
        receiver = new MulticastReceiver(InetAddress.getByName(groupIP), port, networkInterface);
    }

    public boolean sendMessage(String message) { return sendMessage(message.getBytes(StandardCharsets.UTF_8)); }

    public boolean sendMessage(byte[] message) {
        boolean state = true;
        /* TODO remove this, it's for debug */ if(DEBUG){ Logger.log(getClass().getSimpleName(), "sendMessage", "Multicast: \"" + new String(message)+"\""); }

        try {
            transmitter.sendMessage(message);
        } catch (Exception e) {
            lastError = String.format("Multicast: %s, %s", e, e.getMessage());
            state = false;
        }

        return state;
    }

    public MulticastPacket receiveMessage() throws IOException {
        try {
            MulticastPacket mCastPacket = receiver.recevoir();
            /* TODO remove this, it's for debug */ if(DEBUG) { Logger.log(getClass().getSimpleName(), "receiveMessage",
                        String.format("Received message from \"%s\" -> \"%s\"",
                                mCastPacket.getSourceAddress(), mCastPacket.getMessage()
                        )
                );
            }
            return mCastPacket;
        } catch (IOException e) {
            lastError = String.format("Multicast: %s, %s", e, e.getMessage());
            throw new IOException(lastError, e);
        }
    }

    public String getLastError() {
        return lastError;
    }

    private NetworkInterface getNetworkInterface(NetworkInterface networkInterface) throws SocketException {
        List<NetworkInterface> netInts = Collections.list(NetworkInterface.getNetworkInterfaces());
        netInts.removeIf(e -> {
            try { return !e.isUp() && !e.isLoopback(); } catch (SocketException e1) { e1.printStackTrace(); }
            return false;
        });
        if (networkInterface == null || !networkInterface.isUp() || networkInterface.isLoopback()) {
            networkInterface = netInts.get(0);
        }

        return networkInterface;
    }
}