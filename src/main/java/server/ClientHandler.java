package server;

import exceptions.*;
import offline_message.CryptoBase64;
import offline_message.Sha384;
import protocol.Protocol;
import utils.Logger;

import java.net.Socket;

import static java.lang.Thread.sleep;

public class ClientHandler extends UnicastOnServerHandler {
    private IServer server;

    private User linkedUser;

    ClientHandler(Socket client, IServer server) {
        super(client, server);
        this.server = server;
    }

    public ClientHandler init() throws ConnectionException { return connect(); }

    @Override
    protected ClientHandler connect() throws ConnectionException {
        ClientHandler clientHandler = (ClientHandler) super.connect();
        super.sendMessage(Protocol.makeProtocol(server.getDomain()));
        return clientHandler;
    }

    @Override
    protected void disconnect(){
        if(this.linkedUser != null) {
            server.disconnectUser(linkedUser);
            this.linkedUser = null;
        }
        super.disconnect();
    }

    @Override
    protected void manageReceivedMessage(String message) {
        int messageId = Protocol.parse(message);
        boolean isClientAuth = this.linkedUser != null;

        switch (messageId){
            case Protocol.PARSE_SIGNUP:
                /* TODO remove this, it's for debug */ Logger.log(getClass().getSimpleName(), "manageReceivedMessage - \""+getIpAddress()+"\"", "SIGNUP");

                if(!isClientAuth) { tryToSignUp(Protocol.parseSignUp(message)); }
                else { Protocol.makeSignErr("Already Auth"); }
                break;
            case Protocol.PARSE_SIGNIN:
                /* TODO remove this, it's for debug */ Logger.log(getClass().getSimpleName(), "manageReceivedMessage - \""+getIpAddress()+"\"", "SIGNIN");

                if(!isClientAuth) { tryToSignIn(Protocol.parseSignIn(message)); }
                else { Protocol.makeSignErr("Already Auth"); }
                break;
            case Protocol.PARSE_HEY:
                if (isClientAuth) {
                    /* TODO remove this, it's for debug */ Logger.log(getClass().getSimpleName(), "manageReceivedMessage - \""+getIpAddress()+"\"", "HEY");

                    String source = Protocol.makeUserDomain(linkedUser.getNickname(), server.getDomain());
                    String[] messageHey = Protocol.parseHey(message);

                    Job job = new Job(Protocol.PARSE_BLAH, source, messageHey[0], messageHey[1], Statut.NULL);
                    server.addJobToJobHandler(job);
                    try {
                        while(job.sameStatut(Statut.NULL)) {
                            sleep(1000);
                        }
                        if(job.sameStatut(Statut.OK)) {
                            super.sendMessage(Protocol.makeHeyOk());
                        } else if(job.sameStatut(Statut.ERROR)){
                            super.sendMessage(Protocol.makeHeyErr(job.getErrorMessage()));
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case Protocol.PARSE_EXIT:
                /* TODO remove this, it's for debug */ Logger.log(getClass().getSimpleName(), "manageReceivedMessage - \""+getIpAddress()+"\"", "EXIT");

                super.sendMessage(Protocol.makeExitOk());
                disconnect();
                break;
            default:
                defaultManageMessage(isClientAuth, messageId, message);
                break;
        }
    }

    private void defaultManageMessage(boolean isClientAuth, int messageId, String message){
        /* Will send a ERR message when received message isn't managed */
        String errorMessage = String.format("Une erreur est survenue, veuillez contacter un des gestionnaires du Serveur de Messagerie \"%s\"", server.getDomain());

        if(isClientAuth) { errorMessage = Protocol.makeHeyErr(errorMessage); }
        else { errorMessage = Protocol.makeSignErr(errorMessage); }

        super.sendMessage(errorMessage);

        /* TODO remove this, it's for debug */ Logger.log(getClass().getSimpleName(), "defaultManageMessage - \""+getIpAddress()+"\"",
                String.format("%s\n\t\"%s\"",
                        "Ce message n'est pas " + (messageId == Protocol.PARSE_UNKNOWN ? "reconnu" : "géré") + " par le Serveur de Messagerie",
                        message.replaceAll("\\x0d", "\\\\r")
                                .replaceAll("\\x0a", "\\\\n")
                )
        );
    }

    private void tryToSignUp(String[] signUpInfos) {
        /* TODO remove this, it's for debug */ Logger.log(getClass().getSimpleName(), "tryToSignUp",
                String.format("Nickname: \"%s\"", signUpInfos[0])
        );

        try {
            User user = new User( signUpInfos[0], Sha384.encryptSHA384(signUpInfos[1]), CryptoBase64.encryptBase64(signUpInfos[2]));
            server.signUpUser(user);

            signOk(user);

        } catch (UserAlreadyRegisteredException ex){
            super.sendMessage(Protocol.makeSignErr(ex.getMessage()));
        }
    }

    private void tryToSignIn(String[] signUpInfos) {
        /* TODO remove this, it's for debug */ Logger.log(getClass().getSimpleName(), "tryToSignIn", String.format("Nickname: \"%s\"", signUpInfos[0]) );

        try {
            User user = User.signinInUser( signUpInfos[0], Sha384.encryptSHA384(signUpInfos[1]),signUpInfos[2]);
            server.signInUser(user);

            signOk(user);
        } catch (UserNotRegisteredYetException | UserAlreadyAuthException | PasswordNotValidException | UserOtpCodeNotValidException ex){
            super.sendMessage(Protocol.makeSignErr(ex.getMessage()));
        }
    }

    private void signOk(User user){
        super.sendMessage(Protocol.makeSignOk());
        server.authClient(user.getNickname(),this);
        this.linkedUser = user;
    }
}
