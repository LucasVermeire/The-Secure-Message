package client;

import utils.Logger;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.nio.charset.Charset;
import java.util.Scanner;

public class Client {
    private boolean stop = false;
    private String destination;
    private int port;

    public Client(String destination, int port) {
        this.destination = destination;
        this.port = port;
    }

    public void startClient(){
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try(SSLSocket server = (SSLSocket) factory.createSocket(destination, port)) {

            (new Thread(new ServerThread(server))).start();
            /* TODO remove this, it's for debug */ Logger.log(String.format("Connexion à %s sur le port %d", destination, port));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(server.getOutputStream(), Charset.forName("UTF-8")));

            sendUsingConsole(out);

        } catch (ConnectException e) {
//            System.err.printf("%s:startClient -> %s\n\tMessage : %s\n\tCause : %s\n", getClass().getSimpleName(), e, e.getMessage(), e.getCause());

            Logger.logError(getClass().getSimpleName(), "startClient",
                    String.format("%s\n\tMessage : %s\n\tCause : %s\n",
                            e, e.getMessage(), e.getCause()
                    )
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendUsingConsole(PrintWriter out) {
        Scanner console = new Scanner(System.in);
        String line = console.nextLine();

        while (!stop) {
            out.print(String.format("%s\r\n", line));
            out.flush();
            line = console.nextLine();
        }
    }
}
