package gateway;

import protocol.Protocol;
import server.IJob;
import server.Statut;
import utils.Logger;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class JobGHandler implements Runnable {
    private BlockingQueue<IJob> tasks;
    private Map<String, ServerHandler> servers;
    private boolean stop = false;
    private int DEFAULT_TIMEOUT_IN_SECONDS = 15;
    private Gateway gateway;

    JobGHandler(Map<String, ServerHandler> servers, Gateway gateway,BlockingQueue<IJob> tasks) {
        this.servers = servers;
        this.gateway = gateway;
        this.tasks = tasks;
    }

    @Override
    public void run() {
        try {
            while (!stop) {
                ForwardJob j = (ForwardJob)tasks.poll(DEFAULT_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS); // si la queue est vide, on ne restera pas bloqué indéfiniment
                if(j != null && j.sameCommand(Protocol.PARSE_FORWARD)) sendMessageToServer(j);
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    private void sendMessageToServer(ForwardJob job) {
        Logger.log(getClass().getSimpleName(), "sendMessageToServer",
                String.format("Job n°%d, %s@%s TO %s@%s -> %s",
                        job.getType(),
                        job.getSourceClient(), job.getSourceServer(),
                        job.getDestinationClient(), job.getDestinationServer(),
                        job.getProtocol()
                )
        );
        if(servers.containsKey(job.getDestinationServer())){
            job.setStatut(Statut.OK);
            ServerHandler server = servers.get(job.getDestinationServer());
            server.sendEncryptedMessageToServer(Protocol.makeForward(job), gateway.getServerAesKey(job.getDestinationServer()));
        } else {
            Logger.logError(getClass().getSimpleName(), "sendMessageToServer", "Server Offline" );

            gateway.addOfflineMessageFor(job);

            job.setStatut(Statut.ERROR);
            job.setErrorMessage("Server Offline");
            String sourceServer = job.getSourceServer();
            ServerHandler server = servers.get(sourceServer);

            server.sendEncryptedMessageToServer(Protocol.makeHeyErr(job.getErrorMessage()), gateway.getServerAesKey(sourceServer));
        }
    }

    public void addJob(ForwardJob job){
        if(!tasks.offer(job)) job.setStatut(Statut.ERROR);
    }
}
