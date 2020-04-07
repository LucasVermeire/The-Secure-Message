package client;

import utils.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ServerThread implements Runnable {
    private BufferedReader in;
    private boolean isConnected = false;
    private boolean stop = false;

    public ServerThread(Socket server){
        try {
            in = new BufferedReader(new InputStreamReader(server.getInputStream()));
            isConnected = true;
        } catch (IOException e) {
            isConnected = false;
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (isConnected && !stop){
                String message = in.readLine();

                if(message != null && message.length() > 0){
                    /* TODO remove this, it's for debug */ Logger.log(String.format("Message reçu: %s", message));
                }
            }
        } catch (IOException e) {
            try{ in.close(); } catch(IOException ignored){}

            isConnected = false;
        }
    }
}
