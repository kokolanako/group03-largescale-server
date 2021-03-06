package server;

import communication.Message;
import lombok.Getter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

public class ServerThread extends Thread {
  private Socket client;
  private ServerDistributor distributor;
  @Getter
  private String ID;
  @Getter
  private String firstName; //null, if it is Orga
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
      this.close();
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
      if (this.lastName != null && this.firstName == null) { //organisation
        this.distributor.deregisterOrganisation(this.ID);
      } else {

        this.distributor.deregister(this.ID);
      }
    }
    this.close();
  }


  @Override
  public void run() {
    while (true) {
      try {
        if (this.client != null && !this.client.isClosed()) {
          Message message = (Message) this.dataInputStream.readObject();
          System.out.println("RECEIVED " + this.toString() + " MESSAGE " + message.toString());
          Message answer = this.readObjectAndTakeAction(message);
//          System.out.println("CREATED MESSAGE "+answer);
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

    if (this.client != null && !this.client.isClosed()) {
      this.deregister();
    }
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
      System.out.println("Client " + this.toString() + " is disconnected.");
      this.deregister();
    }
    System.out.println("SEND: " + this.toString() + " SENDS " + sendMsg);

  }

  private Message readObjectAndTakeAction(Message msg) {
    Message answer = new Message();
    if (msg.getTYPE().equals("REGISTER")) {
      boolean isOrganisation = (msg.getFirstName() == null || msg.getFirstName().isEmpty()) && msg.getLastName() != null;
      if (isOrganisation) { //register organisation
        System.out.println("****ORGAAAAAAA "+msg);
        boolean orgaAlreadyExists = this.distributor.orgaAlreadyExists(this.ID);
        if (orgaAlreadyExists) {//register Organisation
          answer.setMessage_ID(msg.getMessage_ID());
          answer.setTYPE("ERROR");
          answer.setMessageText("Client organisation with" + lastName + " already exists.");
          return answer;
        }
        boolean registered = this.register(msg.getId(), msg.getLastName(), msg.getFirstName(), msg.getPublicKey());
        if (!registered) {
          answer.setMessage_ID(msg.getMessage_ID());
          answer.setTYPE("ERROR");
          answer.setMessageText("Client of type organisation " + this.firstName + " " + lastName + " is not registered due to missing publicKey.");
          return answer;
        }

        this.distributor.registerOrganisation(this);
        System.out.println("REGISTERED organization " + msg.getLastName() + " " + msg.getId() + " size " + this.distributor.getOrganisationsSize());
        answer.setMessage_ID(msg.getMessage_ID());
        answer.setTYPE("OK");
        answer.setMessageText("Client of type organisation" + lastName + " is registered.");
        return answer;
      } else { //register Person
        boolean personAlreadyExists = this.distributor.alreadyExists(msg.getId(), msg.getLastName(), msg.getFirstName());
        if (personAlreadyExists) {
          answer.setMessage_ID(msg.getMessage_ID());
          answer.setTYPE("ERROR");
          answer.setMessageText("Client already exist");
          return answer;
        }
        boolean registered = this.register(msg.getId(), msg.getLastName(), msg.getFirstName(), msg.getPublicKey());
        if (!registered) {
          answer.setMessage_ID(msg.getMessage_ID());
          answer.setTYPE("ERROR");
          answer.setMessageText("Client of type person " + this.firstName + " " + lastName + " is not registered due to missing publicKey.");
          return answer;
        }
        this.distributor.register(this);
        System.out.println("REGISTERED person " + msg.getId() + " size " + this.distributor.getClientSize());
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
    } else if (msg.getTYPE().equals("MESSAGE") || msg.getTYPE().equals("MESSAGE_PRIVATE")) {
      if (msg.getFirstNameReceiver() != null && msg.getLastNameReceiver() != null) {
        boolean successful = this.distributor.sendMessage(msg.getLastNameReceiver(), msg.getFirstNameReceiver(), msg);
        if (successful) {
          answer.setMessage_ID(msg.getMessage_ID());
          answer.setTYPE("OK");
          answer.setMessageText("Message sent to "
              + msg.getFirstNameReceiver() + " " + msg.getLastNameReceiver());
          return answer;
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
          answer.setMessage_ID(msg.getMessage_ID());
          answer.setTYPE("OK");
          answer.setMessageText("Message of type" + msg.getTYPE() + " sent to " + msg.getIdReceiver());
          return answer;
        } else {
          answer.setMessage_ID(msg.getMessage_ID());
          answer.setTYPE("ERROR");
          answer.setMessageText("Cannot send message to " + msg.getIdReceiver() + " type: " + msg.getTYPE());
          return answer;
        }
      }

    } else if (msg.getTYPE().equals("MESSAGE_BUSINESS")) {
      Message answerBusiness = this.distributor.sendBusinessMessage(msg);
      if (answerBusiness == null) {
        answer.setMessage_ID(msg.getMessage_ID());
        answer.setTYPE("ERROR");
        answer.setMessageText("You have no right to send business message due to no role.");
        return answer;
      } else {
        System.out.println("++++++++ " + answerBusiness);
        return answerBusiness;
      }
    } else if (msg.getTYPE().equals("TRANSACTION_SUB")) { // "SEND [5856e6cd-0da6-4573-9a04-cbb11f5e68d399] (BANK ID) SUB [DE0355667] (idan) [7]"//TODO how to check identity
      System.out.println("TRANSACTION " + msg);
      boolean transferred = this.distributor.transactionMessageToOrganisation(msg);
      if (!transferred) {
        msg.setTYPE("TRANSACTION_SUB_ERROR");
        msg.setMessageText("No organisation with id " + msg.getIdReceiver() + " is detected at central server.");
        return msg;
      }
      return null;
    } else if (msg.getTYPE().equals("TRANSACTION_ADD")) {
      boolean transferred = this.distributor.transactionMessageToOrganisation(msg);
      if (!transferred) {
        msg.setTYPE("TRANSACTION_ADD_ERROR");
        msg.setMessageText("No organisation with id " + msg.getIdReceiver() + " is detected at central server.");
        return msg;
      }
      return null;
    } else if (msg.getTYPE().equals("TRANSACTION_SUB_OK")) {
      return this.distributor.transactionMessageAnswerToClient(msg);
    } else if (msg.getTYPE().equals("TRANSACTION_ADD_OK")) {
      return this.distributor.transactionMessageAnswerToClient(msg);
    } else if (msg.getTYPE().equals("TRANSACTION_SUB_ERROR")) {
      return this.distributor.transactionMessageAnswerToClient(msg);
    } else if (msg.getTYPE().equals("TRANSACTION_ADD_ERROR")) {
      return this.distributor.transactionMessageAnswerToClient(msg);
    } else if (msg.getTYPE().equals("CLOSE_CONNECTION")) {
      answer.setMessage_ID(msg.getMessage_ID());
      answer.setTYPE("CLOSE_CONNECTION");
      answer.setMessageText("Close and deregister Client from server");
      boolean isOrganisation = this.firstName == null && lastName != null;
      if (isOrganisation) {
        this.distributor.deregisterOrganisation(this.getID());
      } else {
        this.distributor.deregister(this.getID());
      }
      return answer;
    }
    return null;
  }

  public boolean register(String id, String lastName, String firstName, String publicKey) {
    if (publicKey == null) {
      return false;
    }
    System.out.println("REGISTERED "+lastName+" "+id);
    this.ID = id;
    this.lastName = lastName;
    this.firstName = firstName;
    this.publicKey = publicKey;
    return true;
  }

  @Override
  public String toString() {
    return "Client: " + this.firstName + " " + this.lastName;
  }
}
