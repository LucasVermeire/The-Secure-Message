package multicast;

import utils.NetworkInterfaceUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class ProgramListNetInterfaces {

    private static final String ipAddress = "";

    public static void main(String[] args) {
        displayInfosForMatchingNetInt();

        displayInfosNetworkInterfaceChoosenByApplication();

        listValidNetInts();

        displayInfosFirstValidNetInt();
    }

    private static void displayInfosNetworkInterfaceChoosenByApplication() {
        System.out.printf("-------------\n\nChoix de l'interface réseau par l'application pour \"%s\"\n", ipAddress);

        try{
            NetworkInterface netIntChoosenByApp = NetworkInterfaceUtils.getNetworkInterfaceForAddress(ipAddress);
            NetworkInterfaceUtils.displayInterfaceInformation(netIntChoosenByApp);
        } catch (UnknownHostException | SocketException e) { e.printStackTrace(); }
    }

    private static void displayInfosForMatchingNetInt() {
        System.out.printf("-------------\n\nInterface correspondant à \"%s\"\n", ipAddress);
        try {
            NetworkInterface netIntByAddress = NetworkInterface.getByInetAddress(InetAddress.getByName(ipAddress));

            if(NetworkInterfaceUtils.isNetworkInterfaceValid(netIntByAddress)) {
                NetworkInterfaceUtils.displayInterfaceInformation(netIntByAddress);
            } else { System.out.println("Aucune interface réseau n'a pu être trouvée"); }

        } catch (UnknownHostException | SocketException e) { e.printStackTrace(); }
    }

    private static void listValidNetInts() {
        System.out.println("\n-------------\n\nListe des interfaces valides");
        try {
            displayAllNetworkInterfaces( NetworkInterfaceUtils::isNetworkInterfaceValid );

        } catch (SocketException e) { e.printStackTrace(); }
    }

    private static void displayInfosFirstValidNetInt() {
        System.out.println("-------------\n\n1ère interface réseau valide");
        try {
            NetworkInterface netIntByAddress = NetworkInterfaceUtils.getFirstValidInterface();
            NetworkInterfaceUtils.displayInterfaceInformation(netIntByAddress);
        } catch (SocketException e) { e.printStackTrace(); }
    }

    private static void displayAllNetworkInterfaces(Predicate<NetworkInterface> filter) throws SocketException {
        List<NetworkInterface> listNetInts = Collections.list(NetworkInterface.getNetworkInterfaces());
        listNetInts.removeIf(filter.negate());
        for (NetworkInterface netint : listNetInts) { NetworkInterfaceUtils.displayInterfaceInformation(netint); }
    }
}
