package server;

import exceptions.ConnectionException;
import protocol.Protocol;
import utils.Logger;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

public class UnicastOnServerHandler implements Runnable{
    private Socket socket;
    private IServer server;
    private BufferedReader in;
    private PrintWriter out;

    private boolean isConnected = false;
    private boolean stop = false;

    public UnicastOnServerHandler(Socket socket, IServer server){
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        isConnected = true;
        try {
            listenForMessage();
        } catch (IOException e){
            System.err.printf("%s:run -> %s\n\tMessage : %s\n\tCause : %s\n", getClass().getSimpleName(), e, e.getMessage(), e.getCause());
            disconnect();
        }
    }

    private void listenForMessage() throws IOException {
        while (isConnected() && !stop) {
            if (in == null) { disconnect(); break; }

            char[] buff = new char[4096];
            int value = in.read(buff); // Replace readLine with read to get CRLF

            if (value == 0) { disconnect(); break; }
            if (value > 0){
                String messageReceived = new String(buff, 0,  value); // if is to Fix java.lang.StringIndexOutOfBoundsException: String index out of range: -1
                manageReceivedMessage(messageReceived);
            }
        }
    }

    public void sendMessage(String message) {
        out.write(message);
        out.flush();
    }

    public boolean isConnected(){return isConnected;}

    protected UnicastOnServerHandler connect() throws ConnectionException {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")), true);
            isConnected = true;

            /* TODO remove this, it's for debug */ Logger.log(getClass().getSimpleName(), "connect", formatConnectionMessage());

            return this;
        } catch (IOException e) {
            isConnected = false;
            throw new ConnectionException(e.getMessage());
        }
    }

    public void stop(){ stop = false; }

    protected void disconnect(){
        isConnected = false;

        try {
            in.close(); out.close();

            /* TODO remove this, it's for debug */ Logger.log(getClass().getSimpleName(), "disconnect", formatDisconnectionMessage());
        } catch (IOException e) {
//            System.err.printf("%s:disconnect -> %s\n\tMessage : %s\n\tCause : %s\n", getClass().getSimpleName(), e, e.getMessage(), e.getCause());

            Logger.logError(getClass().getSimpleName(), "disconnect",
                    String.format("%s\n\tMessage : %s\n\tCause : %s\n",
                            e, e.getMessage(), e.getCause()
                    )
            );
        }
    }

    protected String getIpAddress(){return socket.getInetAddress().getHostAddress();}
    protected String formatConnectionMessage(){ return String.format("Connexion de %s", getIpAddress()); }
    protected String formatDisconnectionMessage(){ return String.format("Déconnexion de %s", getIpAddress()); }


    protected void manageReceivedMessage(String message) {
        int messageId = Protocol.parse(message);
        /* TODO remove this, it's for debug */ Logger.log(getClass().getSimpleName(), "manageReceivedMessage - \""+getIpAddress()+"\"",
                String.format("Type du message %d, message -> \"%s\"", messageId, message)
        );
    }
}
