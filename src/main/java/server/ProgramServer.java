package server;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ProgramServer {
    private ProgramServer(String[] args) {
        System.setProperty("javax.net.ssl.keyStore","group2.p12");
        System.setProperty("javax.net.ssl.keyStorePassword","group2");


        IServer s = new Server(new ConfigServer(getPathOfConfig(args)));
        s.start();
    }

    public static void main(String[] args) {
        new ProgramServer(args);
    }


    /**
     * A des fins de démonstration, et pour facilité la gestion des config, on peut passer en argument l'id du serveur:
     * Exemple: server01 -> pour prendre la config pré-faite pour le server01
     *
     * Et si jamais l'argument n'est pas valide (n'est pas un de ceux gerés ou inexistant, prendra la config par défaut
     * Pour l'instant, gère server01, server02 et server03
     */
    private static Path getPathOfConfig(String[] args) {
        Path defaultPath = Paths.get("src/serverConfig.json");

        if (args.length > 0){
            switch (args[0]){
                case "server01": return Paths.get("src/server01Config.json");
                case "server02": return Paths.get("src/server02Config.json");
                case "server03": return Paths.get("src/server03Config.json");
                default: return defaultPath;
            }
        } else {
            return defaultPath;
        }
    }
}
