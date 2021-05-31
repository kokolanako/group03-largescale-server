package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerDistributor {

    private int PORT;
    private ServerSocket serverSocket = null;

    //every client is listened in an individual thread
    private List<ServerThread> clients;

    public ServerDistributor(int port) {
        this.PORT = port;
        this.clients = new ArrayList<>();

        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    public void start() {
        while (true) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                System.out.println("I/O error: " + e);
            }
            // new thread for a client
            ServerThread client = new ServerThread(socket, this);
            client.start();

        }
    }


    public void deregister(int id) {
        for (int i = 0; i < this.clients.size(); i++) {
            if (this.clients.get(i).getId() == id) {
                this.clients.remove(i);
            }
        }
    }

    public String retrieve(int id) {
        for (int i = 0; i < this.clients.size(); i++) {
            if (this.clients.get(i).getId() == id) {
                return this.clients.get(i).getPublicKey();
            }
        }
        return null;
    }

    public String retrieve(String lastName, String firstName) {
        for (int i = 0; i < this.clients.size(); i++) {
            if (this.clients.get(i).getLastName() == lastName && this.clients.get(i).getFirstName() == firstName) {
                return this.clients.get(i).getPublicKey();
            }
        }
        return null;
    }

    public void sendMessage(int id, String clearMessage) {
        for (int i = 0; i < this.clients.size(); i++) {
            if (this.clients.get(i).getId() == id) {
                this.clients.get(i).sendMessageToClient("SERVER_RESPONSE", clearMessage);
            }
        }
    }

    public void sendMessage(String lastName, String firstName, String clearMessage) {
        for (int i = 0; i < this.clients.size(); i++) {
            if (this.clients.get(i).getLastName() == lastName && this.clients.get(i).getFirstName() == firstName) {
                this.clients.get(i).sendMessageToClient("SERVER_RESPONSE", clearMessage);
            }
        }

    }

    public boolean alreadyExists(int id, String lastName, String firstName) {
        if (lastName != null && firstName != null) {

            for (int i = 0; i < this.clients.size(); i++) {
                if (this.clients.get(i).getLastName() == lastName && this.clients.get(i).getFirstName() == firstName) {
                    return true;
                }
            }
        }
        if (id >= 0) {
            for (int i = 0; i < this.clients.size(); i++) {
                if (this.clients.get(i).getId() == id) {
                    return true;
                }
            }
        }
        return false;
    }


}
