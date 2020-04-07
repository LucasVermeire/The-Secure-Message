package gateway;

import protocol.Protocol;
import server.IJob;
import server.Job;
import server.Statut;

public class ForwardJob implements IJob {
    private int type;
    private String source;
    private String destination;
    private Job forwarded;
    private Statut statut;
    private String errorMessage = "";

    public ForwardJob(Job job) {
        this.type = Protocol.PARSE_FORWARD;
        this.source = Protocol.makeUserDomain(job.getSourceClient(), job.getSourceServer());
        this.destination = Protocol.makeUserDomain(job.getDestinationClient(), job.getDestinationServer());
        this.forwarded = job;
        this.statut = Statut.NULL;
    }

    public ForwardJob(int type, String source, String destination, String forwarded, Statut statut) {
        this.type = type;
        this.source = source;
        this.destination = destination;
        this.forwarded = new Job(source, forwarded);
        this.statut = statut;
    }

    public boolean sameCommand(int numCommand) {
        return this.type == numCommand;
    }

    public int getType() { return type; }

    public String getSourceClient() { return Protocol.parseUserDomain(source)[0]; }

    public String getSourceServer() { return Protocol.parseUserDomain(source)[1]; }

    public String getSource() { return source; }

    public String getDestinationClient() { return Protocol.parseUserDomain(destination)[0]; }

    public String getDestinationServer() {
        return Protocol.parseUserDomain(destination)[1];
    }

    public String getDestination() { return destination; }

    public void setStatut(Statut statut) {
        this.statut = statut;
    }

    public IJob getForwarded() {
        return this.forwarded;
    }

    public String getProtocol() { return this.forwarded.getProtocol(); }

    public boolean sameStatut(Statut statut) {
        return this.statut.equals(statut);
    }

    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public String getErrorMessage() {
        String result = errorMessage;
        errorMessage = "";
        return result;
    }
}
