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
        this.distributor = distributor;
        try {
            dataOutputStream = new ObjectOutputStream(this.client.getOutputStream());
            dataInputStream = new ObjectInputStream(this.client.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            this.deregister();
        }

    }

    public void close() {
        try {
            this.client.close();
            this.dataInputStream.close();
            this.dataOutputStream.close();
            this.client = null;
            this.dataInputStream = null;
            this.dataOutputStream = null;
            System.out.println("Client exits " + this.getID() + " with name " + this.lastName);
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
        this.close();
    }


    @Override
    public void run() {
        while (true) {
            try {
                if (this.client != null && !this.client.isClosed()) {
                    Message message = (Message) this.dataInputStream.readObject();
                    System.out.println("Received message: "+message.getTYPE() + " " + message.getFirstName() + " " + message.getLastName()
                            + " " + message.getId() + " " + message.getMessageText());
                    this.readObjectAndTakeAction(message);
                    continue;

                } else {
                    break;
                }
            }
            catch(SocketException e1){

            }
            catch (IOException | ClassNotFoundException e) {
              e.printStackTrace();
            }

          break;
        }
      this.deregister();
    }

    public void sendMessageToAnotherClient(int msg_id, String type, String msg, ServerThread sender) {
        Message sendMsg = new Message();
        sendMsg.setTYPE(type);
        sendMsg.setMessage_ID(msg_id);
        if (type.equals("ASK_PUBLIC_KEY")) {
            sendMsg.setPublicKey(msg);
        } else if (type.equals("MESSAGE")) {
            //msg_id ist 0
            sendMsg.setMessageText(msg);
            //Sender der Message hinzufuegen
            sendMsg.setLastName(sender.getLastName());
            sendMsg.setFirstName(sender.getFirstName());
        } else if (type.equals("OK") || (type.equals("ERROR"))) {
            sendMsg.setMessageText(msg);
        }

        try {
            this.dataOutputStream.writeObject(sendMsg);
            this.dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("SENT " + sendMsg);

    }

    private void readObjectAndTakeAction(Message msg) {
        System.out.println("READ OBJECT " + msg);
        if (msg.getTYPE().equals("REGISTER")) {
            if (this.distributor.alreadyExists(msg.getId(), msg.getLastName(), msg.getFirstName())) {
                this.sendMessageToAnotherClient(msg.getMessage_ID(), "ERROR", "Client already exist", null);
            } else {
                this.register(msg.getId(), msg.getLastName(), msg.getFirstName(), msg.getPublicKey());
                this.distributor.register(this);
                this.sendMessageToAnotherClient(msg.getMessage_ID(), "OK",
                        "Client " + this.firstName + " " + lastName + " registered", null); //else ERROR +msg
                System.out.println("Server registered " + msg.getId()+" size "+this.distributor.getClientSize());

            }

        } else if (msg.getTYPE().equals("ASK_PUBLIC_KEY")) {
            if (msg.getFirstName() != null && msg.getLastName() != null) {
                String publicKey = this.distributor.retrieve(msg.getLastName(), msg.getFirstName());

                if (publicKey != null) {
                    msg.setPublicKey(publicKey);
                    this.sendMessageToAnotherClient(msg.getMessage_ID(), msg.getTYPE(), publicKey, null);
                } else {

                    this.sendMessageToAnotherClient(msg.getMessage_ID(), "ERROR", "No person found", null);
                }

            } else {
                String publicKey = this.distributor.retrieve(msg.getId());
                msg.setPublicKey(publicKey);
                this.sendMessageToAnotherClient(msg.getMessage_ID(), msg.getTYPE(), publicKey, null);
                if (publicKey != null) {
                    msg.setPublicKey(publicKey);
                    this.sendMessageToAnotherClient(msg.getMessage_ID(), msg.getTYPE(), publicKey, null);
                } else {
                    this.sendMessageToAnotherClient(msg.getMessage_ID(), "ERROR", "No person found", null);
                }
            }
        } else if (msg.getTYPE().equals("MESSAGE")) {
            if (msg.getFirstName() != null && msg.getLastName() != null) {
                boolean successful = this.distributor.sendMessage(msg.getLastName(), msg.getFirstName(), msg.getMessageText(), this);
                if (successful) {
                    this.sendMessageToAnotherClient(msg.getMessage_ID(), "OK", "Message send to "
                            + msg.getFirstName() + " " + msg.getLastName(), null);
                } else {
                    this.sendMessageToAnotherClient(msg.getMessage_ID(), "ERROR", "Can not send message to "
                            + msg.getFirstName() + " " + msg.getLastName(), null);
                }
            } else {
                boolean successful = this.distributor.sendMessage(msg.getId(), msg.getMessageText(), this);
                if (successful) {
                    this.sendMessageToAnotherClient(msg.getMessage_ID(), "OK", "Message send to " + msg.getId(), null);
                } else {
                    this.sendMessageToAnotherClient(msg.getMessage_ID(), "ERROR", "Can not send message to " + msg.getId(), null);
                }
            }
        } else if (msg.getTYPE().equals("CLOSE_CONNECTION")) {
            this.sendMessageToAnotherClient(msg.getMessage_ID(), "CLOSE_CONNECTION", "close and deregister Client from server", null);
            System.out.println("Client " + this.ID + " is disconnected.");
            this.distributor.deregister(this.ID);
            this.close();
        }
    }

    public void register(String id, String lastName, String firstName, String publicKey) {
        this.ID = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.publicKey = publicKey;
    }
}
