package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import utils.Logger;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ConfigServer {
    private transient String configAbsolutePath;

    private String bind_ucaddress;
    private int bind_ucport;
    private String server_id;
    private boolean check_otp;
    private String bind_mcaddress;
    private int bind_mcport;
    private String bind_network_interface_address;
    private String mgate_domain;
    private String mgate_aes_key;
    private List<User> registered_users;

    ConfigServer(Path configPath){
        this.configAbsolutePath = configPath.toAbsolutePath().toString();

        initConfig();
    }

    private void initConfig() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        try(FileReader fileReader = new FileReader(configAbsolutePath)){

            // 1. JSON file to Java object
            ConfigServer tempConfigServer = gson.fromJson(fileReader, ConfigServer.class);

            this.bind_ucaddress = tempConfigServer.bind_ucaddress;
            this.bind_ucport = tempConfigServer.bind_ucport;
            this.server_id = tempConfigServer.server_id;
            this.check_otp = tempConfigServer.check_otp;
            this.bind_mcaddress = tempConfigServer.bind_mcaddress;
            this.bind_mcport = tempConfigServer.bind_mcport;
            this.bind_network_interface_address = tempConfigServer.bind_network_interface_address;
            this.mgate_domain = tempConfigServer.mgate_domain;
            this.mgate_aes_key = tempConfigServer.mgate_aes_key;

            this.registered_users = tempConfigServer.registered_users;

        }catch (IOException ex){
            System.err.println(ex.getMessage());
        }
    }

    public String getBindUcaddress(){
        return bind_ucaddress;
    }
    public int getBindUcport(){
        return bind_ucport;
    }
    public String getServerId(){
        return server_id;
    }
    public boolean isOtpChecked(){
        return check_otp;
    }
    public String getBindMcaddress(){
        return bind_mcaddress;
    }
    public int getBindMcport(){
        return bind_mcport;
    }
    public String getBindNetworkInterfaceAddress(){ return bind_network_interface_address; }
    public String getMgateDomain(){
        return mgate_domain;
    }
    public String getMgateAesKey(){ return mgate_aes_key; }

    boolean isUserSignedUp(User user){
        return registered_users.contains(user);
    }
    void addSignedUpUser(User user){
        registered_users.add(user);

        compute();
    }
    private void compute() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        try(FileWriter fileWriter = new FileWriter(configAbsolutePath)){

            ConfigServer tempConfigServer = this;

            // 1. Java object to JSON file
            gson.toJson(tempConfigServer, fileWriter);

            System.out.println("Config computed !");
        }catch (IOException e){
            Logger.logError(getClass().getSimpleName(), "compute",
                    String.format("%s\n\tMessage : %s\n\tCause : %s\n",
                            e, e.getMessage(), e.getCause()
                    )
            );
        }
    }


    public User getUserWithNickname(String nickname) {
        User result = null;
        for (User user : registered_users) {
            if(user.getNickname().equals(nickname)) {
                result = user;
                break;
            }
        }

        return result;
    }

    public boolean isUserValid(String destinationClient) {
        return registered_users.contains(new User(destinationClient, ""));
    }
}
