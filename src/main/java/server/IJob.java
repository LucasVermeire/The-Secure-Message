package server;

public interface IJob {
    boolean sameCommand(int parseBlah);

    int getType();

    String getSourceClient();

    String getSourceServer();

    String getDestinationClient();

    String getDestinationServer();

    void setStatut(Statut statut);

    String getProtocol();

    boolean sameStatut(Statut statut);

    void setErrorMessage(String errorMessage);

    String getErrorMessage();
}
