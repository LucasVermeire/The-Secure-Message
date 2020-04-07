package server;

import gateway.ForwardJob;

public interface IServer {

    void start();
    void signUpUser(User user);
    void signInUser(User signingInUser);

    void authClient(String nickname, ClientHandler clientHandler);
    void addJobToJobHandler(IJob job);

    String getDomain();
    void disconnectUser(User user);

    void disconnectGateway();

    boolean isServerDomainMatching(String serverDomainToCheck);

    void sendForwardJobToGateway(ForwardJob Job);

    void addOfflineMessageFor(IJob j);

    String getAesKey();

    boolean isClientValid(String destinationClient);
}
