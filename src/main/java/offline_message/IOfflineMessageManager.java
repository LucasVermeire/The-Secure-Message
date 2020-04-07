package offline_message;

import server.IJob;

public interface IOfflineMessageManager {

    void sendOfflineMessageFor(String destination);
    void addOfflineMessageFor(IJob j, String destination);

}
