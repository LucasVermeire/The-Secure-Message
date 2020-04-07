package server;

import gateway.ForwardJob;
import protocol.Protocol;
import utils.Logger;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class JobHandler implements Runnable {
    private BlockingQueue<IJob> tasks;
    private Map<String,ClientHandler> clients;
    private boolean stop = false;
    private final int DEFAULT_TIMEOUT_IN_SECONDS = 15;
    private IServer server;

    JobHandler(Map<String, ClientHandler> clients, IServer server, BlockingQueue<IJob> tasks) {
        this.clients = clients;
        this.server = server;
        this.tasks = tasks;
    }

    @Override
    public void run() {
        try {
            while (!stop) {
                IJob job = tasks.poll(DEFAULT_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS); // si la queue est vide, on ne restera pas bloqué indéfiniment

                if(job != null) {
                    if(job.sameCommand(Protocol.PARSE_BLAH)) {
                        if(server.isServerDomainMatching(job.getDestinationServer())) {
                            sendMessageToClient(job);
                        } else {
                            sendToGateway(job);
                        }
                    } else if(job.sameCommand(Protocol.PARSE_FORWARD)) {
                        sendForwardMessageToClient((ForwardJob) job);
                    }
                }
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    private void sendForwardMessageToClient(ForwardJob forwardJob) {
        sendMessageToClient(forwardJob.getForwarded());
    }

    private void sendToGateway(IJob job) {
        job.setStatut(Statut.OK);
        server.sendForwardJobToGateway(new ForwardJob((Job) job));
    }

    private void sendMessageToClient(IJob job) {
        /* TODO remove this, it's for debug */ Logger.log(getClass().getSimpleName(),"sendMessageToClient",
                String.format("Job n°%d, %s TO %s -> %s",
                        job.getType(),
                        Protocol.makeUserDomain(job.getSourceClient(), job.getSourceServer()),
                        Protocol.makeUserDomain(job.getDestinationClient(), job.getDestinationServer()),
                        job.getProtocol()
                )
        );

        String destinationClient = job.getDestinationClient();
        if (server.isClientValid(destinationClient)) {
            if(clients.containsKey(job.getDestinationClient())){
                job.setStatut(Statut.OK);
                ClientHandler client = clients.get(job.getDestinationClient());
                client.sendMessage(Protocol.makeBlah(job.getSourceClient()+"@"+job.getSourceServer(), job.getProtocol()));
            } else {
                String errorMessage = "Client is Offline";
                /* TODO remove this, it's for debug */ Logger.logError(getClass().getSimpleName(),"sendMessageToClient", errorMessage );

                server.addOfflineMessageFor(job);
                job.setErrorMessage(errorMessage);
                job.setStatut(Statut.ERROR);
            }
        } else {
            String errorMessage = "Client is not valid";
            /* TODO remove this, it's for debug */ Logger.logError(getClass().getSimpleName(),"sendMessageToClient", errorMessage );

            job.setErrorMessage(errorMessage);
            job.setStatut(Statut.ERROR);
        }
    }

    public void addJob(IJob job){
        if(!tasks.offer(job)) job.setStatut(Statut.NULL); //offer renvoie false si il ne peut pas add le job dans la liste
    }

}
