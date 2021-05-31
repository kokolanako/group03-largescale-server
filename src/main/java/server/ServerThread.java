package server;

import lombok.Getter;

import java.io.*;
import java.net.Socket;

public class ServerThread extends Thread {
    private Socket client;
    private ServerDistributor distributor;
    @Getter
    private int id;
    @Getter
    private String firstName;
    @Getter
    private String lastName;
    @Getter
    private String publicKey;
    private ObjectOutputStream dataOutputStream;
    private ObjectInputStream dataInputStream;

    public ServerThread(Socket client, ServerDistributor distributor) {
        this.client = client;
        try {
            dataOutputStream = new ObjectOutputStream(this.client.getOutputStream());
            dataInputStream = new ObjectInputStream(this.client.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            this.close();
            this.deregister();
        }

    }

    public void close() {
        try {
            this.client.close();
            this.dataInputStream.close();
            this.dataOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deregister() {
        this.distributor.deregister(this.id);
    }

    @Override
    public void run() {
        while (true) {
            try {
                Message msg = (Message) this.dataInputStream.readObject();
                this.readObjectAndTakeAction(msg);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessageToClient(String type, String msg) {
        //Encrypt with public key
        //TODO
        Message sendMsg = new Message();
        sendMsg.setTYPE(type);
        sendMsg.setMessageText(msg);
        try {
            this.dataOutputStream.writeObject(sendMsg);
            this.dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void readObjectAndTakeAction(Message msg) {
        if (msg.getTYPE() == "REGISTER") {
            System.out.println("Server registered " + msg.getId());
            if (this.distributor.alreadyExists(msg.getId(), msg.getLastName(), msg.getFirstName())) {
                this.sendMessageToClient("ERROR", "Identical client already exists");
            } else {
                this.register(msg.getId(), msg.getLastName(), msg.getFirstName(), msg.getPublicKey());
                this.distributor.register(this);
                this.sendMessageToClient("OK", null); //else ERROR +msg

            }

        } else if (msg.getTYPE() == "MESSAGE") {
            //TODO decrypt
            System.out.println("Server received from client " + msg.getMessageText());
        }
    }

    public void register(int id, String lastName, String firstName, String publicKey) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.publicKey = publicKey;
    }
}
