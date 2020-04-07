package server;

import exceptions.AESKeyNotDefinedException;
import exceptions.AesException;
import exceptions.ConnectionException;
import gateway.ForwardJob;
import offline_message.Aes128Gcm;
import protocol.Protocol;
import utils.Logger;

import java.net.Socket;

public class GatewayHandler extends UnicastOnServerHandler {
    private final IServer server;
    private boolean isConnectOk = false;

    public GatewayHandler(Socket gateway, IServer server){
        super(gateway, server);
        this.server = server;
    }

    public GatewayHandler init() throws ConnectionException {
        return connect();
    }

    @Override
    protected GatewayHandler connect() throws ConnectionException {
        GatewayHandler gatewayHandler = (GatewayHandler) super.connect();
        super.sendMessage(Protocol.makeConnect(server.getDomain()));
        return gatewayHandler;
    }

    @Override
    protected void disconnect(){
        server.disconnectGateway();
        super.disconnect();
    }

    @Override
    protected void manageReceivedMessage(String message) {
        if (isConnectOk){
            try {
                message = Aes128Gcm.decrypt(message, server.getAesKey());
            } catch (AESKeyNotDefinedException | AesException e){
//                System.err.printf("%s:manageReceivedMessage -> %s\n\tMessage : %s\n\tCause : %s\n", getClass().getSimpleName(), e, e.getMessage(), e.getCause());

                Logger.logError(getClass().getSimpleName(), "manageReceivedMessage",
                        String.format("%s\n\tMessage : %s\n\tCause : %s\n",
                                e, e.getMessage(), e.getCause()
                        )
                );
            }
        }

        int messageId = Protocol.parse(message, false);

        switch (messageId){
            case Protocol.PARSE_CONNECTOK:
                /* TODO remove this, it's for debug */ Logger.log(getClass().getSimpleName(), "manageReceivedMessage - \""+getIpAddress()+"\"", "CONNECTOK");
                if(!isConnectOk) { isConnectOk = true; }
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
                if(isConnectOk) {
                    String[] messageForward = Protocol.parseForward(message);

                    ForwardJob j = new ForwardJob(Protocol.PARSE_FORWARD, messageForward[0], messageForward[1], messageForward[2], Statut.NULL);
                    server.addJobToJobHandler(j);

                }
                break;
            default:
                defaultManageMessage(isConnectOk, messageId, message);
                break;
        }
    }

    private void defaultManageMessage(boolean isConnectOk, int messageId, String message){
        String errorMessage = String.format("Une erreur est survenue, veuillez contacter un des gestionnaires du Serveur de Messagerie \"%s\"", server.getDomain());

        if(isConnectOk) {  sendEncryptedMessageToServer(Protocol.makeHeyErr(errorMessage), server.getAesKey()); }
        else { sendMessage(Protocol.makeConnectErr(errorMessage)); }

        /* TODO remove this, it's for debug */ Logger.log(getClass().getSimpleName(), "defaultManageMessage - \""+getIpAddress()+"\"",
                String.format("%s\n\t\"%s\"",
                        "Ce message n'est pas " + (messageId == Protocol.PARSE_UNKNOWN ? "reconnu" : "géré") + " par le Serveur de Messagerie",
                        message.replaceAll("\\x0d", "\\\\r")
                                .replaceAll("\\x0a", "\\\\n")
                )
        );
    }

    public void sendEncryptedMessageToServer(String message, String aesKey) {
        sendMessage(Aes128Gcm.encrypt(message, aesKey));
    }
}
