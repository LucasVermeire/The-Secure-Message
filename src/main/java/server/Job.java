package server;

import protocol.Protocol;

public class Job implements IJob {

    private int type;
    private String source;
    private String destination;
    private String protocol;
    private Statut statut;
    private String errorMessage = "";

    public Job (int type, String source, String destination, String protocol, Statut statut) {
        this.type = type;
        this.source = source;
        this.destination = destination;
        this.protocol = protocol;
        this.statut = statut;
    }

    public Job(String source, String forwarded) {
        String[] param = Protocol.parseBlah(forwarded);
        this.type = Protocol.parse(forwarded);
        this.source = source;
        this.destination = param[0];
        this.protocol = param[1];
        this.statut = Statut.NULL;
    }

    public boolean sameCommand(int numCommand) {
        return this.type == numCommand;
    }

    public void setStatut(Statut statut) {
        this.statut = statut;
    }

    public int getType() { return type; }

    public String getProtocol() {
        return this.protocol;
    }

    public String getSourceClient() { return Protocol.parseUserDomain(source)[0]; }

    public String getSourceServer() { return Protocol.parseUserDomain(source)[1]; }

    public String getDestinationClient() { return Protocol.parseUserDomain(destination)[0]; }

    public String getDestinationServer() {
        return Protocol.parseUserDomain(destination)[1];
    }

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
