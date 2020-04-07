package gateway;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import exceptions.AESKeyNotDefinedException;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;

public class ConfigGateway {
    private transient String configAbsolutePath;

    private String bind_ucaddress;
    private int bind_ucport;
    private boolean enable_tls;
    private String bind_mcaddress;
    private int bind_mcport;
    private String bind_network_interface_address;
    private int announce_interval;
    private String mgate_domain;
    private HashMap<String, String> aes_keys;

    public ConfigGateway(Path configPath) {
        this.configAbsolutePath = configPath.toAbsolutePath().toString();

        initConfig();
    }

    private void initConfig() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        try(FileReader fileReader = new FileReader(configAbsolutePath)){

            // 1. JSON file to Java object
            ConfigGateway tempConfigGateway = gson.fromJson(fileReader, ConfigGateway.class);

            this.bind_ucaddress = tempConfigGateway.bind_ucaddress;
            this.bind_ucport = tempConfigGateway.bind_ucport;
            this.enable_tls = tempConfigGateway.enable_tls;
            this.bind_mcaddress = tempConfigGateway.bind_mcaddress;
            this.bind_mcport = tempConfigGateway.bind_mcport;
            this.bind_network_interface_address = tempConfigGateway.bind_network_interface_address;
            this.announce_interval = tempConfigGateway.announce_interval;
            this.mgate_domain = tempConfigGateway.mgate_domain;

            this.aes_keys = tempConfigGateway.aes_keys;

            

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
    public String getBindMcaddress(){
        return bind_mcaddress;
    }
    public int getBindMcport(){
        return bind_mcport;
    }
    public String getBindNetworkInterfaceAddress(){ return bind_network_interface_address; }
    public int getAnnounceInterval(){ return announce_interval; }
    public String getMgateDomain(){ return mgate_domain; }

    public boolean isServerRegistered(String serverDomain) {
        return aes_keys.containsKey(serverDomain);
    }
    public String getRegisteredServerAesKey(String serverDomain) throws AESKeyNotDefinedException {
        if (!isServerRegistered(serverDomain)){
            throw new AESKeyNotDefinedException("Server Domain not found");
        }

        return aes_keys.get(serverDomain);
    }
}
