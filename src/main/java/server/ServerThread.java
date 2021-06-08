package server;

import communication.Message;
import lombok.Getter;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class ServerThread extends Thread {
    private Socket client;
    private ServerDistributor distributor;
    @Getter
    private String ID;
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
        this.distributor=distributor;
        try {
            dataOutputStream = new ObjectOutputStream(this.client.getOutputStream());
            dataInputStream = new ObjectInputStream(this.client.getInputStream());
            //FIXME try to read message / recieves the message ?
//            System.out.println(dataInputStream.toString());
//            Message message = new Message();
//            message = (Message) dataInputStream.readObject(); //changed

//            this.readObjectAndTakeAction(message);
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
        if (this.lastName != null && this.firstName != null) {
            this.distributor.deregister(this.lastName, this.firstName);

        } else {

            this.distributor.deregister(this.ID);
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                Message message = (Message) this.dataInputStream.readObject();
                System.out.println(message.getTYPE() + " " + message.getFirstName() + " " + message.getLastName()
                        + " " + message.getId() + " " + message.getMessageText());
                this.readObjectAndTakeAction(message);
            } catch (SocketException e){
                e.printStackTrace();


            } catch (IOException e) {
                e.printStackTrace();

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            System.out.println("Exception");
            System.out.println("Client "+this.ID +" is disconnected.");
            this.close();
            this.distributor.deregister(this.ID);
            break;
        }
    }

    public void sendMessageToAnotherClient(String type, String msg) {
        Message sendMsg = new Message();
        sendMsg.setTYPE(type);
        if (type.equals("ASK_PUBLIC_KEY")) {

            sendMsg.setPublicKey(msg);
        } else if (type.equals("MESSAGE")) {
            sendMsg.setMessageText(msg);

        }

        try {
            this.dataOutputStream.writeObject(sendMsg);
            this.dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void readObjectAndTakeAction(Message msg) {
        if (msg.getTYPE().equals( "REGISTER")) {
            System.out.println("Server registered " + msg.getId());
            if (this.distributor.alreadyExists(msg.getId(), msg.getLastName(), msg.getFirstName())) {
                this.sendMessageToAnotherClient("ERROR", null);
            } else {
                this.register(msg.getId(), msg.getLastName(), msg.getFirstName(), msg.getPublicKey());
                this.distributor.register(this);
                this.sendMessageToAnotherClient("OK", "Client registered."); //else ERROR +msg

            }

        } else if (msg.getTYPE().equals("ASK_PUBLIC_KEY")) {
            if (msg.getFirstName() != null && msg.getLastName() != null) {
                String publicKey = this.distributor.retrieve(msg.getLastName(), msg.getFirstName());
                msg.setPublicKey(publicKey);
                this.sendMessageToAnotherClient(msg.getTYPE(), publicKey);

            } else {
                String publicKey = this.distributor.retrieve(msg.getId());
                msg.setPublicKey(publicKey);
                this.sendMessageToAnotherClient(msg.getTYPE(), publicKey);
            }
        } else if (msg.getTYPE().equals("MESSAGE")) {
            if (msg.getFirstName() != null && msg.getLastName() != null) {
                this.distributor.sendMessage(msg.getLastName(), msg.getFirstName(), msg.getMessageText());

            } else {
                this.distributor.sendMessage(msg.getId(), msg.getMessageText());
            }
        }
    }

    public void register(String id, String lastName, String firstName, String publicKey) {
        this.ID = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.publicKey = publicKey;
    }
}
