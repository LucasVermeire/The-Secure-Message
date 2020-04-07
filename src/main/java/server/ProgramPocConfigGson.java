package server;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ProgramPocConfigGson {
    private static Path testChemin = Paths.get("src/serverConfigTest.json");

    public static void main (String [] args){

        ConfigServer configServer = new ConfigServer(testChemin);

        configServer.addSignedUpUser(new User("Nouvel Utilisateur "+Math.round(Math.random()*100), "passSha64", "MyOtpKey"));
    }
}
