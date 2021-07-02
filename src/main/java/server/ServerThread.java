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
          System.out.println("Received message: " + message.toString());
          Message answer = this.readObjectAndTakeAction(message);
          this.sendMessage(answer);
          continue;

        } else {
          break;
        }
      } catch (SocketException e1) {

      } catch (IOException | ClassNotFoundException e) {
        e.printStackTrace();
      }

      break;
    }
    this.deregister();
  }

  public void sendMessage(Message sendMsg) {
    if (sendMsg == null) {
      return;
    }
    try {
      this.dataOutputStream.writeObject(sendMsg);
      this.dataOutputStream.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (sendMsg.getTYPE().equals("CLOSE_CONNECTION")) {
      System.out.println("Client " + this.ID + " is disconnected.");
      this.deregister();
    }
    System.out.println("SENT " + sendMsg);

  }

  private Message readObjectAndTakeAction(Message msg) {
    System.out.println("READ OBJECT " + msg);
    Message answer = new Message();
    if (msg.getTYPE().equals("REGISTER")) {
      if (this.distributor.alreadyExists(msg.getId(), msg.getLastName(), msg.getFirstName())) {
        answer.setMessage_ID(msg.getMessage_ID());
        answer.setTYPE("ERROR");
        answer.setMessageText("Client already exist");
        return answer;
      } else {
        this.register(msg.getId(), msg.getLastName(), msg.getFirstName(), msg.getPublicKey());
        this.distributor.register(this);
        System.out.println("Server registered " + msg.getId() + " size " + this.distributor.getClientSize());
        answer.setMessage_ID(msg.getMessage_ID());
        answer.setTYPE("OK");
        answer.setMessageText("Client " + this.firstName + " " + lastName + " registered");
        return answer;
      }

    } else if (msg.getTYPE().equals("ASK_PUBLIC_KEY")) {
      if (msg.getFirstNameReceiver() != null && msg.getLastNameReceiver() != null) {
        String publicKey = this.distributor.retrieve(msg.getLastNameReceiver(), msg.getFirstNameReceiver());

        if (publicKey != null) {
          answer.setMessage_ID(msg.getMessage_ID());
          answer.setTYPE(msg.getTYPE());
          answer.setPublicKey(publicKey);
          return answer;
        } else {
          answer.setMessage_ID(msg.getMessage_ID());
          answer.setTYPE("ERROR");
          answer.setMessageText("No person found");
          return answer;
        }

      } else {
        String publicKey = this.distributor.retrieve(msg.getIdReceiver());
        if (publicKey != null) {
          answer.setMessage_ID(msg.getMessage_ID());
          answer.setTYPE(msg.getTYPE());
          answer.setPublicKey(publicKey);
          return answer;

        } else {
          answer.setMessage_ID(msg.getMessage_ID());
          answer.setTYPE("ERROR");
          answer.setMessageText("No person found");
          return answer;
        }
      }
    } else if (msg.getTYPE().equals("MESSAGE")) {
      if (msg.getFirstNameReceiver() != null && msg.getLastNameReceiver() != null) {
        boolean successful = this.distributor.sendMessage(msg.getLastNameReceiver(), msg.getFirstNameReceiver(), msg);
        if (successful) {
//          answer.setMessage_ID(msg.getMessage_ID());
//          answer.setTYPE("OK");
//          answer.setMessageText("Message sent to "
//              + msg.getFirstNameReceiver() + " " + msg.getLastNameReceiver());
//          return answer;
          return null;
        } else {
          answer.setMessage_ID(msg.getMessage_ID());
          answer.setTYPE("ERROR");
          answer.setMessageText("Can not send message to "
              + msg.getFirstNameReceiver() + " " + msg.getLastNameReceiver());
          return answer;
        }
      } else {
        boolean successful = this.distributor.sendMessageByID(msg.getIdReceiver(), msg);
        if (successful) {
//          answer.setMessage_ID(msg.getMessage_ID());
//          answer.setTYPE("OK");
//          answer.setMessageText("Message sent to " + msg.getIdReceiver());
//          return answer;
          return null;
        } else {
          answer.setMessage_ID(msg.getMessage_ID());
          answer.setTYPE("ERROR");
          answer.setMessageText("Can not send message to " + msg.getIdReceiver());
          return answer;
        }
      }
    } else if (msg.getTYPE().equals("MESSAGE_RECEIVED")) {
      if (msg.getFirstNameReceiver() != null && msg.getLastNameReceiver() != null) {
        boolean successful = this.distributor.sendMessage(msg.getLastNameReceiver(), msg.getFirstNameReceiver(), msg);
        if (successful) {
//          answer.setMessage_ID(msg.getMessage_ID());
//          answer.setTYPE("OK");
//          answer.setMessageText("Message sent to "
//              + msg.getFirstNameReceiver() + " " + msg.getLastNameReceiver());
//          return answer;
          return null;
        } else {
          answer.setMessage_ID(msg.getMessage_ID());
          answer.setTYPE("ERROR");
          answer.setMessageText("Can not send message to "
              + msg.getFirstNameReceiver() + " " + msg.getLastNameReceiver());
          return answer;
        }
      } else {
        boolean successful = this.distributor.sendMessageByID(msg.getIdReceiver(), msg);
        if (successful) {
//          answer.setMessage_ID(msg.getMessage_ID());
//          answer.setTYPE("OK");
//          answer.setMessageText("Message sent to " + msg.getIdReceiver());
//          return answer;
          return null;
        } else {
          answer.setMessage_ID(msg.getMessage_ID());
          answer.setTYPE("ERROR");
          answer.setMessageText("Can not send message to " + msg.getIdReceiver());
          return answer;
        }
      }

    } else if (msg.getTYPE().equals("CLOSE_CONNECTION")) {
      answer.setMessage_ID(msg.getMessage_ID());
      answer.setTYPE("CLOSE_CONNECTION");
      answer.setMessageText("Close and deregister Client from server");
      return answer;
    }
    return null;
  }

  public void register(String id, String lastName, String firstName, String publicKey) {
    this.ID = id;
    this.lastName = lastName;
    this.firstName = firstName;
    this.publicKey = publicKey;
  }
}
