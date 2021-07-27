package server;

import communication.Message;
import pojo.Config;
import pojo.PersonDTO;
import pojo.Organisation;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ServerDistributor extends Thread {

    private int[] PORTS;

    private List<ServerThread> clients;

    private List<ServerThread> organisations;

    private BlockingQueue<Message> waitingAnswersFromBank = new LinkedBlockingQueue<>();

    private Config configuration;

    public ServerDistributor(int[] ports, List<ServerThread> clients, Config configuration) {
        this.PORTS = ports;
        this.clients = clients;
        this.configuration = configuration;
        this.organisations = Collections.synchronizedList(new ArrayList<>());

    }

    @Override
    public void run() {
        this.start();
    }

    public void start() {
        for (int port : this.PORTS) {
            Thread startAcceptionClients = new Thread(() -> {
              ServerSocket serverSocket= null;
              try {
                serverSocket = new ServerSocket(port);
              } catch (IOException e) {
                e.printStackTrace();
              }
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
            });
            startAcceptionClients.start();
        }


    }

    public synchronized void deregister(String id) {
        for (int i = 0; i < this.clients.size(); i++) {
            if (this.clients.get(i).getID().equals(id)) {
                this.clients.get(i).interrupt();
                this.clients.remove(i);
                break;
            }
        }
    }

    public synchronized void deregister(String lastName, String firstName) {
        for (int i = 0; i < this.clients.size(); i++) {
            if (this.clients.get(i).getLastName().equals(lastName) && this.clients.get(i).getFirstName().equals(firstName)) {
                this.clients.remove(i);
                break;
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

    public synchronized int getClientSize() {
        return this.clients.size();
    }

    public synchronized String retrieve(String lastName, String firstName) {
        for (int i = 0; i < this.clients.size(); i++) {
            if (this.clients.get(i).getLastName().equals(lastName) && this.clients.get(i).getFirstName().equals(firstName)) {
                return this.clients.get(i).getPublicKey();
            }
        }
        return null;
    }

    public synchronized boolean sendMessageByID(String id, Message msg) {
        for (int i = 0; i < this.clients.size(); i++) {
            if (this.clients.get(i).getID().equals(id)) {
                this.clients.get(i).sendMessage(msg);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean sendMessage(String lastName, String firstName, Message msg) {
        for (int i = 0; i < this.clients.size(); i++) {
            if (this.clients.get(i).getLastName().equals(lastName) && this.clients.get(i).getFirstName().equals(firstName)) {
                this.clients.get(i).sendMessage(msg);
                return true;
            }
        }
        return false;
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

    /**
     * Authorisation
     *
     * @param msg
     * @return
     */
    public synchronized Message sendBusinessMessage(Message msg) {
        System.out.println("BUSINESS: " + msg);
        Message answer = new Message();
        String senderId = msg.getId();
        String receiverId = msg.getIdReceiver();
        //1. retrieve ids of both communicators from clients
        if (receiverId == null) {
            for (ServerThread client : clients) {
                if (client.getLastName().equals(msg.getLastNameReceiver()) && client.getFirstName().equals(msg.getFirstNameReceiver())) {
                    receiverId = client.getID();
                    break;
                }
            }
            if (receiverId == null) {
                //unknown Receiver
                answer.setMessage_ID(msg.getMessage_ID());
                answer.setTYPE("ERROR");
                answer.setMessageText("Cannot send message to " + msg.getIdReceiver() + " type: " + msg.getTYPE() + " Reason: Unknown receiver id");
                return answer;
            }
            List<String> senderRole = this.getRoles(senderId);
            List<String> receiverRole = this.getRoles(receiverId);
            if (senderRole == null || receiverRole == null) {
                answer.setMessage_ID(msg.getMessage_ID());
                answer.setTYPE("ERROR");
                answer.setMessageText("Cannot send message to " + msg.getIdReceiver() + " type: " + msg.getTYPE() + " Reason: Unknown roles.");
                return answer;
            }
            boolean sameOrga = this.employedInTheSameOrganisation(senderId, receiverId);
            //valid transfer if
            if (sameOrga && this.validateTransferWithinOrganisation(senderRole, receiverRole)
                    || !sameOrga && this.validateTransferBetweenOrganisations(senderRole, receiverRole)) {
                boolean successful = this.sendMessageByID(receiverId, msg);
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
            } else {
                //invalid message transfer due to roles of communication partners
                answer.setMessage_ID(msg.getMessage_ID());
                answer.setTYPE("ERROR");
                answer.setMessageText("Cannot send message to " + msg.getIdReceiver() + " type: " + msg.getTYPE() + " Reason: invalid message transfer due to roles");
                return answer;
            }
        }


        return null;
    }

    private boolean validateTransferBetweenOrganisations(List<String> senderRole, List<String> receiverRole) {
        boolean checkedRepresenter = false;
        for (String roleSender : senderRole) {
            if (roleSender.equals("REPRESENTER")) {
                checkedRepresenter = true;
                for (String roleReceiver : receiverRole) {
                    if (roleReceiver.equals("REPRESENTER")) {
                        return true;
                    }
                }
            } else if (checkedRepresenter) {
                break;
            }
        }
        return false;
    }

    private boolean validateTransferWithinOrganisation(List<String> senderRole, List<String> receiverRole) {
        for (String roleSender : senderRole) {
            for (String roleReceiver : receiverRole) {
                if (roleSender.equals("REPRESENTER")) {
                    if (roleReceiver.equals("REPRESENTER") || roleReceiver.equals("EMPLOYEE") || roleReceiver.equals("ADMIN")) {
                        return true;
                    }
                } else if (roleSender.equals("ADMIN")) {
                    if (roleReceiver.equals("REPRESENTER") || roleReceiver.equals("EMPLOYEE") || roleReceiver.equals("ADMIN")) {
                        return true;
                    }
                } else if (roleSender.equals("EMPLOYEE")) {
                    if (roleReceiver.equals("EMPLOYEE") || roleReceiver.equals("ADMIN")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private List<String> getRoles(String senderId) {
        for (Organisation org : this.configuration.getOrganizations()) {
            for (PersonDTO employee : org.getEmployees()) {
                if (employee.getId().equals(senderId)) {
                    return employee.getRoles();
                }
            }
        }
        return null;
    }

    private boolean employedInTheSameOrganisation(String senderId, String receiverId) {

        Organisation orga = null;
        outer:
        for (Organisation org : this.configuration.getOrganizations()) {
            for (PersonDTO employee : org.getEmployees()) {
                if (employee.getId().equals(senderId)) {
                    orga = org;
                    break outer;
                }
            }
        }
        if (orga == null) {
            return false;
        }
        for (PersonDTO employee : orga.getEmployees()) {
            if (employee.getId().equals(receiverId)) {
                return true;
            }
        }
        return false;
    }

    public synchronized Message transactionMessage(Message msg) {

        for (ServerThread bank : this.organisations) {
            if (bank.getID().equals(msg.getId())) {
                bank.sendMessage(msg);
            }
        }
        return null; //must be null!!!!!! dont change!
    }

    public synchronized void registerOrganisation(ServerThread serverThread) {
        this.organisations.add(serverThread);
    }

    public void deregisterOrganisation(String id) {
        for (int i = 0; i < this.organisations.size(); i++) {
            if (this.organisations.get(i).getID().equals(id)) {
                this.organisations.get(i).interrupt();
                this.organisations.remove(i);
                break;
            }
        }
    }

    public synchronized boolean orgaAlreadyExists(String id) {
        for (int i = 0; i < this.organisations.size(); i++) {
            if (this.organisations.get(i).getID().equals(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * bank answers to central server
     * <p>
     * idReceiver is filled with clientID that sent initially the transaction request
     *
     * @param msg
     * @return
     */
    public synchronized Message transactionMessageAnswer(Message msg) {
        for (ServerThread client : this.clients) {
            if (client.getID().equals(msg.getIdReceiver())) {
                client.sendMessage(msg);
            }
        }
        return null; //remains null!!
    }
}
