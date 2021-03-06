package offline_message;

import com.google.gson.Gson;
import exceptions.AesException;
import gateway.ForwardJob;
import server.IJob;
import utils.Logger;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class OfflineMessageManagerGateway implements IOfflineMessageManager{
    private final static String FOLDER_PATH = "offline_message/";
    private final int MAX_MESSAGES_OFFLINE = 50;

    private ICryptage crypteur = new AES();
    private KeyStoreManager ksm = new KeyStoreManager();
    private String actualServer;
    private BlockingQueue<IJob> tasks;

    public OfflineMessageManagerGateway(String actualServer, BlockingQueue<IJob> tasks) {
        this.actualServer = actualServer;
        this.tasks = tasks;
    }

    public void sendOfflineMessageFor(String destination) {
        String pathFileName = getPathFileName(destination);

        if(Files.exists( Paths.get(pathFileName) )){ // This check if file exists => so if there are offline messages
            try {
                String json = crypteur.decrypt(pathFileName,getKey(destination));

                deleteFile(pathFileName);
                this.ksm.deleteKey(destination);

                List<ForwardJob> listJob = fromJson(json);
                sendAllMessage(listJob);
            } catch ( AesException e){
//                System.err.printf("%s:sendOfflineMessageFor -> %s\n\tMessage : %s\n\tCause : %s\n", getClass().getSimpleName(), e, e.getMessage(), e.getCause());

                Logger.logError(getClass().getSimpleName(), "sendOfflineMessageFor",
                        String.format("%s\n\tMessage : %s\n\tCause : %s\n",
                                e, e.getMessage(), e.getCause()
                        )
                );
            }
        }
    }

    public void addOfflineMessageFor(IJob iJob, String destination) {
        String pathFileName = getPathFileName(destination);
        SecretKey sk = getKey(destination);
        List<ForwardJob> jobList = new ArrayList<>();

        if(Files.exists( Paths.get(pathFileName) )){ // This check if file exists => so if there are offline messages
            String json = crypteur.decrypt(pathFileName,sk);
            jobList = fromJson(json);
        }

        if(jobList.size() < MAX_MESSAGES_OFFLINE){
            jobList.add((ForwardJob)iJob);
        }
        String newJson = this.toJson(jobList);

        crypteur.encrypt(newJson, pathFileName, sk);
    }

    private SecretKey getKey(String destination) {
        SecretKey sk = ksm.getKey(destination,this.actualServer);

        if (sk == null) { // Check if retrieved key is null --> so if key is not defined, will generate a new Key and store it
            sk = crypteur.generateSecretKey();
            ksm.storeKey(destination, sk, this.actualServer);
        }
        return sk;
    }

    private void deleteFile(String pathFileName) {
        try {
            Files.deleteIfExists(Paths.get(pathFileName));
        } catch (IOException e) {
            Logger.logError(getClass().getSimpleName(), String.format("deleteFile(%s)",pathFileName),
                    String.format("%s\n\tMessage : %s\n\tCause : %s\n",
                            e, e.getMessage(), e.getCause()
                    )
            );
        }
    }

    private void sendAllMessage(List<ForwardJob> jobList) {
        (new Thread(() -> {
            for (ForwardJob forwardJob : jobList) {
                try {
                    while(!tasks.offer(forwardJob)){
                        Thread.sleep(100); // This is used to prevent sending jobs too fast (and so sending job together, in the same time, cause them unable to decrypt on server)
                    }
                    Thread.sleep(100); // This is used to prevent sending jobs too fast (and so sending job together, in the same time, cause them unable to decrypt on server)
                } catch (InterruptedException ignored) { }
            }
        })).start();
    }

    private List<ForwardJob> fromJson(String message) {
        return new ArrayList<>(Arrays.asList(new Gson().fromJson(message, ForwardJob[].class)));
    }

    private String toJson(List<ForwardJob> jobList) {
        return new Gson().toJson(jobList);
    }

    private String getPathFileName(String destination) {
        return FOLDER_PATH + destination + ".ser";
    }
}
