package gateway;

import exceptions.*;
import offline_message.Aes128Gcm;
import protocol.Protocol;
import server.Statut;
import utils.Logger;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

public class ServerHandler implements Runnable {
    private Socket server;
    private boolean isConnected = false;
    private boolean stop = false;
    private BufferedReader fromServer;
    private PrintWriter toServer;
    private Gateway gateway;
    private String connectedServer;

    ServerHandler(Socket server, Gateway gateway) {
        this.server = server;
        this.gateway = gateway;
    }

    public ServerHandler init() throws ConnectionException { return connect(); }

    @Override
    public void run() {
        try {
            listenForMessage();
        } catch (IOException e){
            disconnect();
            System.err.printf("%s:run -> %s\n\tMessage : %s\n\tCause : %s\n", getClass().getSimpleName(), e, e.getMessage(), e.getCause());
        }
    }

    public void sendEncryptedMessageToServer(String message, String aesKey) {
        sendMessageToServer(Aes128Gcm.encrypt(message, aesKey));
    }

    public void sendMessageToServer(String message) {

        Logger.log(getClass().getSimpleName(), "sendMessageToServer",
                String.format("\"%s\"",
                        message.replaceAll("\\x0d", "\\\\r")
                                .replaceAll("\\x0a", "\\\\n")
                )
        );

        toServer.write(message);
        toServer.flush();
    }

    private String getIpAddress(){return server.getInetAddress().getHostAddress();}
    private String formatConnectionMessage(){ return String.format("Connexion de %s", getIpAddress()); }
    private String formatDisconnectionMessage(){ return String.format("Déconnexion de %s", getIpAddress()); }

    private ServerHandler connect() throws ConnectionException {
        try {
            fromServer = new BufferedReader(new InputStreamReader(server.getInputStream(), Charset.forName("UTF-8")));
            toServer = new PrintWriter(new OutputStreamWriter(server.getOutputStream(), Charset.forName("UTF-8")), true);
            isConnected = true;
            /* TODO remove this, it's for debug */ Logger.log(getClass().getSimpleName(), "init", formatConnectionMessage());
            return this;
        } catch (IOException ex) {
            throw new ConnectionException("Impossible d'établir la connexion " + ex.getMessage(), ex);
        }
    }

    private void disconnect(){
        isConnected = false;

        gateway.disconnectServer(connectedServer);

        try {
            fromServer.close(); toServer.close();
            /* TODO remove this, it's for debug */ Logger.log(getClass().getSimpleName(), "disconnect", formatDisconnectionMessage());
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void tryToConnectAuthServer(String serverDomain){
        /* TODO remove this, it's for debug */ Logger.log(getClass().getSimpleName(), "tryToConnectAuthServer", serverDomain );

        try {
            gateway.checkServer(serverDomain);

            connectOk(serverDomain);
        } catch (UnknownServerDomainException | ServerDomainIsAlreadyPresentException | AESKeyNotDefinedException ex){
            /* TODO remove this, it's for debug */ Logger.log(getClass().getSimpleName(), "tryToConnectAuthServer", String.format("ERROR : %s", ex.getMessage()) );
            sendMessageToServer(Protocol.makeConnectErr(ex.getMessage()));
        }
    }

    private void connectOk(String serverDomain) {
        connectedServer = serverDomain;
        gateway.connectServer(serverDomain, this);
        sendMessageToServer(Protocol.makeConnectOk());
    }

    private void listenForMessage() throws IOException {
        while (isConnected && !stop) {
            if (fromServer == null) { disconnect(); break; }

            char[] buff = new char[1024];
            int value = fromServer.read(buff); // Replace readLine with read to get CRLF

            if (value == 0) { disconnect(); break; }
            if (value > 0){ manageReceivedMessage(new String(buff, 0,  value)); } // if is to Fix java.lang.StringIndexOutOfBoundsException: String index out of range: -1
        }
    }

    /**
     * TODO This method need to be improved
     *
     * @param message message to manage
     */
    private void manageReceivedMessage(String message) {
        boolean isServerAuth = this.connectedServer != null;

        try {
            if (isServerAuth){
                String AesKey = gateway.getServerAesKey(this.connectedServer);
                if(!AesKey.equals("")){ message = Aes128Gcm.decrypt(message, AesKey); }
            }
        } catch (AESKeyNotDefinedException | AesException e){
            System.err.printf("%s:manageReceivedMessage -> %s\n\tMessage : %s\n\tCause : %s\n", getClass().getSimpleName(), e, e.getMessage(), e.getCause());
        }

        int messageId = Protocol.parse(message, false);

        switch (messageId){
            case Protocol.PARSE_CONNECT:
                /* TODO remove this, it's for debug */ Logger.log(getClass().getSimpleName(), "manageReceivedMessage - \""+getIpAddress()+"\"", "CONNECT");
                if(!isServerAuth) {
                    String[] messageConnect = Protocol.parseConnect(message);
                    tryToConnectAuthServer(messageConnect[0]);
                }
                break;
            case Protocol.PARSE_CONNECTERR:
                /* TODO remove this, it's for debug */ Logger.log(getClass().getSimpleName(), "manageReceivedMessage - \""+getIpAddress()+"\"", "CONNECTERR");
                break;
            case Protocol.PARSE_HEYOK:
                /* TODO remove this, it's for debug */ Logger.log(getClass().getSimpleName(), "manageReceivedMessage - \""+getIpAddress()+"\"", "HEYOK");
                break;
            case Protocol.PARSE_HEYERR:
                /* TODO remove this, it's for debug */ Logger.log(getClass().getSimpleName(), "manageReceivedMessage - \""+getIpAddress()+"\"", "HEYERR");
                break;
            case Protocol.PARSE_FORWARD:
                /* TODO remove this, it's for debug */ Logger.log(getClass().getSimpleName(), "manageReceivedMessage - \""+getIpAddress()+"\"", "FORWARD");
                if(isServerAuth) {
                    String[] messageForward = Protocol.parseForward(message);

                    ForwardJob j = new ForwardJob(Protocol.PARSE_FORWARD, messageForward[0], messageForward[1], messageForward[2], Statut.NULL);
                    if (gateway.isServerAutorized(j.getDestinationServer())) {
                        gateway.addJobToJobGHandler(j);
                    } else {
                        j.setStatut(Statut.ERROR);
                        String errorMessage = "Une erreur est survenue, si cela se reproduit, contactez l'un des responsables du Message Gateway";
//                        System.err.println(errorMessage + ", le serveur destination n'est pas autorisé");

                        Logger.logError(getClass().getSimpleName(), "manageReceivedMessage",
                                errorMessage + ", le serveur destination n'est pas autorisé"
                        );
                        j.setErrorMessage(errorMessage);

                        sendMessageToServer(Protocol.makeHeyErr(j.getErrorMessage()));
                    }
                }
                break;
            default:
                defaultManageMessage(isServerAuth, messageId, message);
                break;
        }
    }

    private void defaultManageMessage(boolean isServerAuth, int messageId, String message){
        String errorMessage = String.format("Une erreur est survenue, veuillez contacter un des gestionnaires du Message Gateway \"%s\"", gateway.getDomain());

        if(isServerAuth) { errorMessage = Protocol.makeHeyErr(errorMessage); }
        else { errorMessage = Protocol.makeConnectErr(errorMessage); }

        sendMessageToServer(errorMessage);

        /* TODO remove this, it's for debug */ Logger.log(getClass().getSimpleName(), "defaultManageMessage - \""+getIpAddress()+"\"",
                String.format("%s\n\t\"%s\"",
                        "Ce message n'est pas " + (messageId == Protocol.PARSE_UNKNOWN ? "reconnu" : "géré") + " par le Message Gateway",
                        message.replaceAll("\\x0d", "\\\\r")
                                .replaceAll("\\x0a", "\\\\n")
                )
        );
    }

}
