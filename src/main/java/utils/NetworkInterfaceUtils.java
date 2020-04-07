package utils;

import java.net.*;
import java.util.Collections;
import java.util.List;

public class NetworkInterfaceUtils {
    public static NetworkInterface getFirstValidInterface() throws SocketException {
        List<NetworkInterface> netInts = Collections.list(NetworkInterface.getNetworkInterfaces());
        netInts.removeIf(e -> !NetworkInterfaceUtils.isNetworkInterfaceValid(e));
        return netInts.get(0);
    }
    public static NetworkInterface getNetworkInterfaceForAddress(String net_int_address) throws SocketException, UnknownHostException {
        NetworkInterface netIntByAddress = NetworkInterface.getByInetAddress(InetAddress.getByName(net_int_address));

        return isNetworkInterfaceValid(netIntByAddress) ? netIntByAddress : getFirstValidInterface();
    }

    public static boolean isNetworkInterfaceValid(NetworkInterface networkInterface){
        try {
            return networkInterface != null && networkInterface.isUp() && !networkInterface.isLoopback();
        } catch (SocketException e) { return false; }
    }

    public static void displayInterfaceInformation(NetworkInterface networkInterface) throws SocketException {
        System.out.printf("Display name: %s\n", networkInterface.getDisplayName());
        System.out.printf("\tName: %s\n", networkInterface.getName());
        System.out.printf("\tIs Up ? : %b\n", networkInterface.isUp());
        System.out.println("\tinetInterfacesAddresses:");
        List<InterfaceAddress> inetInterfacesAddresses = networkInterface.getInterfaceAddresses();
        for (InterfaceAddress inetInterfacesAddress :inetInterfacesAddresses) {
            final InetAddress inet_addr = inetInterfacesAddress.getAddress();

            if ( !( inet_addr instanceof Inet4Address ) ) { continue; }

            System.out.printf(
                    "\t\taddress: %s/%d\n",
                    inet_addr.getHostAddress(), inetInterfacesAddress.getNetworkPrefixLength()
            );

            System.out.printf(
                    "\t\tbroadcast address: %s\n",
                    inetInterfacesAddress.getBroadcast().getHostAddress()
            );
        }
        System.out.println();
    }
}
