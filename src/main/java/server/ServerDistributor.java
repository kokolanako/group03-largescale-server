package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ServerDistributor {

    private int PORT;
    private ServerSocket serverSocket = null;

    //every client is listened in an individual thread
    private List<ServerThread> clients;

    public ServerDistributor(int port) {
        this.PORT = port;
        this.clients = Collections.synchronizedList(new ArrayList<>());

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


    public void deregister(String id) {
        for (int i = 0; i < this.clients.size(); i++) {
            if (this.clients.get(i).getID() == id) {
                this.clients.remove(i);
            }
        }
    }

    public void deregister(String lastName, String firstName) {
        for (int i = 0; i < this.clients.size(); i++) {
            if (this.clients.get(i).getLastName().equals(lastName) && this.clients.get(i).getFirstName().equals(firstName)) {
                this.clients.remove(i);
            }
        }
    }

    public synchronized void register(ServerThread client) {
        this.clients.add(client);
    }

    public synchronized String retrieve(String id) {
        for (int i = 0; i < this.clients.size(); i++) {
            if (this.clients.get(i).getID().equals(id)) {
                return this.clients.get(i).getPublicKey();
            }
        }
        return null;
    }

    public synchronized String retrieve(String lastName, String firstName) {
        for (int i = 0; i < this.clients.size(); i++) {
            if (this.clients.get(i).getLastName().equals(lastName) && this.clients.get(i).getFirstName().equals(firstName)) {
                return this.clients.get(i).getPublicKey();
            }
        }
        return null;
    }

    public synchronized void sendMessage(String id, String encryptedMessage) {
        for (int i = 0; i < this.clients.size(); i++) {
            if (this.clients.get(i).getID().equals(id)) {
                this.clients.get(i).sendMessageToAnotherClient("MESSAGE", encryptedMessage);
            }
        }
    }

    public synchronized void sendMessage(String lastName, String firstName, String encryptedMessage) {
        for (int i = 0; i < this.clients.size(); i++) {
            if (this.clients.get(i).getLastName().equals(lastName) && this.clients.get(i).getFirstName().equals(firstName)) {
                this.clients.get(i).sendMessageToAnotherClient("MESSAGE", encryptedMessage);
            }
        }

    }

    public boolean alreadyExists(String id, String lastName, String firstName) {
        if (lastName != null && firstName != null) {

            for (int i = 0; i < this.clients.size(); i++) {
                if (this.clients.get(i).getLastName().equals(lastName) && this.clients.get(i).getFirstName().equals(firstName)) {
                    return true;
                }
            }
        }
        if (!id.isEmpty()) {
            for (int i = 0; i < this.clients.size(); i++) {
                if (this.clients.get(i).getID().equals(id)) {
                    return true;
                }
            }
        }
        return false;
    }


}
