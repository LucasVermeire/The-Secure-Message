package gateway;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ProgramGateway {

    private static Path cheminConfigGateway = Paths.get("src/gatewayConfig.json");

    private ProgramGateway() {
        Gateway g = new Gateway(new ConfigGateway(cheminConfigGateway));
        g.start();
    }

    public static void main(String[] args) { new ProgramGateway(); }

}
