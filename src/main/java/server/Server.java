package server;

import exceptions.*;
import gateway.ForwardJob;
import multicast.MulticastListener;
import multicast.MulticastPacket;
import offline_message.OfflineMessageManagerServer;
import protocol.Protocol;
import utils.DomainUtils;
import utils.GoogleAuthOtp;
import utils.Logger;
import utils.NetworkInterfaceUtils;

import javax.net.SocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Server implements IServer, MulticastListener.MulticastListenerCallback{
    private int port;
    private boolean stop = false;
    private Map<String,ClientHandler> clients;
    private BlockingQueue<IJob> tasks = new ArrayBlockingQueue<>(20);
    private GatewayHandler gatewayHandler;
    private ConfigServer configServer; // Config du serveur
    private JobHandler jobHandler;
    private OfflineMessageManagerServer omm;

    Server(ConfigServer configServer) {
        this.configServer = configServer;
        this.port = configServer.getBindUcport();
        clients = Collections.synchronizedMap(new HashMap<>()); // pour "Threader" une collection
        jobHandler = new JobHandler(clients, this, tasks);
        (new Thread(jobHandler)).start();
        omm = new OfflineMessageManagerServer(this.getDomain(),tasks);

        try {
            MulticastListener multicastListener = new MulticastListener(
                    this.configServer.getBindMcaddress(),
                    this.configServer.getBindMcport(),
                    NetworkInterfaceUtils.getNetworkInterfaceForAddress(this.configServer.getBindNetworkInterfaceAddress()),
                    this
            );

            (new Thread(multicastListener)).start();
        } catch (IOException e) { e.printStackTrace(); }
    }


    public void start() {
        SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        try(SSLServerSocket ecoute = (SSLServerSocket) factory.createServerSocket(port)) {
            Logger.log(String.format("Démarrage du serveur %s (%s:%d)", configServer.getServerId(), InetAddress.getLocalHost().getHostAddress(), configServer.getBindUcport()));

            while (!stop) {
                SSLSocket client = (SSLSocket) ecoute.accept(); // instruction bloquante
                ClientHandler clientHandler = new ClientHandler(client, this);
                (new Thread(clientHandler.init())).start();
            }

        } catch (IOException | ConnectionException e) {
//            System.err.printf("%s:start -> %s\n\tMessage : %s\n\tCause : %s\n", getClass().getSimpleName(), e, e.getMessage(), e.getCause());

            Logger.logError(getClass().getSimpleName(), "start",
                    String.format("%s\n\tMessage : %s\n\tCause : %s\n",
                            e, e.getMessage(), e.getCause()
                    )
            );
        }
    }

    public void addJobToJobHandler(IJob job){
        jobHandler.addJob(job);
    }

    public String getDomain() {
        return configServer.getServerId();
    }

    public void signUpUser(User user) throws UserAlreadyRegisteredException{
        Logger.log(getClass().getSimpleName(),
                "signUpUser",
                user.getNickname()
        );

        if(configServer.isUserSignedUp(user)){ // Check if user is already signed up
            throw new UserAlreadyRegisteredException(String.format("User %s is already registered", user.getNickname()));
        }else if(isUserAuth(user)) {
            throw new UserAlreadyAuthException("User is already auth");
        } else{
            configServer.addSignedUpUser(user);
        }
    }

    public void signInUser(User signingInUser) throws UserNotRegisteredYetException, UserAlreadyAuthException, PasswordNotValidException, UserOtpCodeNotValidException {
        if(!configServer.isUserSignedUp(signingInUser)){ // Check if user is already signed up
            throw new UserNotRegisteredYetException(String.format("User %s is not registered, please Sign Up before", signingInUser.getNickname()));
        }else if(isUserAuth(signingInUser)) {
            throw new UserAlreadyAuthException("User is already auth");
        } else {
            User registeredUser = configServer.getUserWithNickname(signingInUser.getNickname());
            if(!registeredUser.checkPass(signingInUser.getSha384pass())){
                throw new PasswordNotValidException("Password is not valid, please check and retry");
            } else {
                if(configServer.isOtpChecked() && !GoogleAuthOtp.validateOtpCode(getOtpDecoded(registeredUser.getBase64otpkey()), signingInUser.getOtpCode())){
                    throw new UserOtpCodeNotValidException("OTP is not valid, please check and retry");
                }
            }
        }
    }

    private static String getOtpDecoded(String encodedString){
        byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
        return new String(decodedBytes);
    }

    public void authClient(String nickname, ClientHandler clientHandler) {
        clients.put(nickname,clientHandler);
        omm.sendOfflineMessageFor(Protocol.makeUserDomain(nickname, getDomain()));
    }

    private boolean isUserAuth(User user){
        return clients.containsKey(user.getNickname());
    }

    public void disconnectUser(User user){
        clients.remove(user.getNickname(), clients.get(user.getNickname()));
    }

    @Override
    public void disconnectGateway() { gatewayHandler = null; }

    @Override
    public void multicastMessageReceivedCallback(MulticastPacket multicastPacket) {
        if(Protocol.parse(multicastPacket.getMessage()) == Protocol.PARSE_MGATE){
            String[] parseMGate = Protocol.parseMgate(multicastPacket.getMessage());

            if (isMgateDomainMatching(parseMGate[0]) && gatewayHandler == null){
                Logger.log(getClass().getSimpleName(), "multicastMessageReceivedCallback", "A Matching MGate announced by Multicast");

                try {
                    Socket gatewaySocket = SocketFactory.getDefault().createSocket(multicastPacket.getSourceAddress(), Integer.parseInt(parseMGate[1]));

                    this.gatewayHandler = new GatewayHandler(gatewaySocket, this).init();
                    new Thread(gatewayHandler).start();
                } catch (ConnectionException | IOException e) {
//                    System.err.printf("%s:multicastMessageReceivedCallback -> %s\n\tMessage : %s\n\tCause : %s\n", getClass().getSimpleName(), e, e.getMessage(), e.getCause());

                    Logger.logError(getClass().getSimpleName(), "musticastMessageReceivedCallback",
                            String.format("%s\n\tMessage : %s\n\tCause : %s\n",
                                    e, e.getMessage(), e.getCause()
                            )
                    );
                }
            }
        }
    }

    /**
     * If the Serves AES Key is defined, return it
     * throw AESKeyNotDefinedException otherwise
     *
     * @return AES Key for serverDomain
     * @throws AESKeyNotDefinedException
     */
    public String getAesKey() throws AESKeyNotDefinedException {
        /* Get the Server Aes128Gcm Key
        If the Serves AES Key is defined, return it
        throw AESKeyNotDefinedException otherwise
         */
        String aesKey = configServer.getMgateAesKey();

        if(aesKey != null && aesKey.length() > 0){ return aesKey; } // return the Server AES Key

        throw new AESKeyNotDefinedException("Aes128Gcm key is not defined");
    }

    public boolean isClientValid(String destinationClient) {
        return configServer.isUserValid(destinationClient);
    }

    public boolean isServerDomainMatching(String serverDomainToCheck) {
        return configServer.getServerId().equals(serverDomainToCheck);
    }

    @Override
    public void sendForwardJobToGateway(ForwardJob job) {
        if (gatewayHandler != null) {
            gatewayHandler.sendEncryptedMessageToServer(Protocol.makeForward(job), getAesKey());
        } else {
            job.setStatut(Statut.ERROR);
            String errorMessage = "Une erreur est survenue, si cela se reproduit, contactez l'un des responsables du Serveur de Messagerie";

            Logger.logError(getClass().getSimpleName(), "sendForwardJobToGateway",
                    errorMessage + ", aucun Gateway n'est lié à ce serveur"
            );

            job.setErrorMessage(errorMessage);

            ClientHandler client = clients.get(job.getSourceClient());
            client.sendMessage(Protocol.makeHeyErr(job.getErrorMessage()));
        }
    }

    @Override
    public void addOfflineMessageFor(IJob j) {
        omm.addOfflineMessageFor(j, Protocol.makeUserDomain(j.getDestinationClient(), j.getDestinationServer()));
    }

    private boolean isMgateDomainMatching(String mGateDomain){
        return DomainUtils.isDomainMatching(configServer.getMgateDomain(), mGateDomain);
    }
}
