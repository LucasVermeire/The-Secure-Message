package gateway;

import exceptions.AESKeyNotDefinedException;
import exceptions.ConnectionException;
import exceptions.ServerDomainIsAlreadyPresentException;
import exceptions.UnknownServerDomainException;
import multicast.MulticastAnnouncer;
import offline_message.OfflineMessageManagerGateway;
import protocol.Protocol;
import server.IJob;
import utils.DomainUtils;
import utils.Logger;
import utils.NetworkInterfaceUtils;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Gateway {
    private boolean stop = false;
    private Map<String, ServerHandler> servers;
    private BlockingQueue<IJob> tasks = new ArrayBlockingQueue<>(20);
    private ConfigGateway configGateway; // Config du Gateway
    private JobGHandler jobGHandler;
    private OfflineMessageManagerGateway omm;

    Gateway(ConfigGateway config) {
        this.configGateway = config;

        servers = Collections.synchronizedMap(new HashMap<>()); // pour "Threader" une collection
        jobGHandler = new JobGHandler(servers, this, tasks);
        (new Thread(jobGHandler)).start();
        omm = new OfflineMessageManagerGateway(this.getDomain(),tasks);

        try {
            MulticastAnnouncer mCastAnnouncer = new MulticastAnnouncer(
                    config.getBindMcaddress(),
                    config.getBindMcport(),
                    NetworkInterfaceUtils.getNetworkInterfaceForAddress(config.getBindNetworkInterfaceAddress()),
                    config.getAnnounceInterval(),
                    Protocol.makeMgate(config.getMgateDomain(), config.getBindUcport())
            );
            (new Thread(mCastAnnouncer)).start();
        } catch (IOException e) {
//            System.err.printf("%s:Gateway() (can't start MCastAnnouncer) -> %s\n\tMessage : %s\n\tCause : %s\n", getClass().getSimpleName(), e, e.getMessage(), e.getCause());

            Logger.logError(getClass().getSimpleName(), "Gateway() (can't start MCastAnnouncer)",
                    String.format("%s\n\tMessage : %s\n\tCause : %s\n",
                            e, e.getMessage(), e.getCause()
                    )
            );
        }
    }


    public void start() {
        try {
            /* TODO remove this, it's for debug */ Logger.log( String.format("Démarrage du gateway %s (%s:%d)\n",
                            configGateway.getMgateDomain(), InetAddress.getLocalHost().getHostAddress(), configGateway.getBindUcport()
                    )
            );
        } catch (UnknownHostException e) {
//            System.err.printf("%s:start -> %s\n\tMessage : %s\n\tCause : %s\n", getClass().getSimpleName(), e, e.getMessage(), e.getCause());

            Logger.logError(getClass().getSimpleName(), "start",
                    String.format("%s\n\tMessage : %s\n\tCause : %s\n",
                            e, e.getMessage(), e.getCause()
                    )
            );
        }

        ServerSocketFactory factory = ServerSocketFactory.getDefault();
        try(ServerSocket ecoute = factory.createServerSocket(configGateway.getBindUcport())) {
            while (!stop) {
                Socket server = ecoute.accept(); // instruction bloquante
                ServerHandler serverHandler = new ServerHandler(server, this);
                (new Thread(serverHandler.init())).start();
            }
        } catch (ConnectionException e) {
//            System.err.printf("%s:start (Impossible d'établir la connexion) -> %s\n\tMessage : %s\n\tCause : %s\n", getClass().getSimpleName(), e, e.getMessage(), e.getCause());

            Logger.logError(getClass().getSimpleName(), "start (Impossible d'établir la connexion)",
                    String.format("%s\n\tMessage : %s\n\tCause : %s\n",
                            e, e.getMessage(), e.getCause()
                    )
            );
        } catch (IOException e) {
//            System.err.printf("%s:start -> %s\n\tMessage : %s\n\tCause : %s\n", getClass().getSimpleName(), e, e.getMessage(), e.getCause());

            Logger.logError(getClass().getSimpleName(), "start",
                    String.format("%s\n\tMessage : %s\n\tCause : %s\n",
                            e, e.getMessage(), e.getCause()
                    )
            );
        }
    }

    public String getDomain() {
        return configGateway.getMgateDomain();
    }

    public void addJobToJobGHandler(ForwardJob job){ jobGHandler.addJob(job); }

    public void checkServer(String serverDomain) throws UnknownServerDomainException, ServerDomainIsAlreadyPresentException, AESKeyNotDefinedException {
        if(!DomainUtils.isDomainMatching(configGateway.getMgateDomain(), serverDomain)){ throw new UnknownServerDomainException("Server domain is not valid"); }

        if(servers.containsKey(serverDomain)){ throw new ServerDomainIsAlreadyPresentException("Server domain is already present"); }

        getServerAesKey(serverDomain);
    }

    void connectServer(String serverDomain, ServerHandler serverHandler) {
        servers.put(serverDomain, serverHandler);
        omm.sendOfflineMessageFor(serverDomain);
    }


    void disconnectServer(String serverDomain) {
        servers.remove(serverDomain);
    }

    /**
     * If the Serves AES Key is defined, return it
     * throw AESKeyNotDefinedException otherwise
     *
     * @param serverDomain serverDomain associated
     * @return AES Key for serverDomain
     * @throws AESKeyNotDefinedException
     */
    String getServerAesKey(String serverDomain) throws AESKeyNotDefinedException {
        if(serverDomain != null){ return configGateway.getRegisteredServerAesKey(serverDomain);  } // return the Server AES Key

        throw new AESKeyNotDefinedException("Aes128Gcm key is not defined for Server domain");
    }

    public void addOfflineMessageFor(IJob j) {
        omm.addOfflineMessageFor(j,j.getDestinationServer());
    }

    public boolean isServerAutorized(String destinationServer) {
        return configGateway.isServerRegistered(destinationServer);
    }
}


