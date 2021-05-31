import server.ServerDistributor;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ApplicationStarter {


  static final int PORT =5001;

  public static void main(String[] args) throws IOException {

    ServerDistributor distributor = new ServerDistributor(5001);
    distributor.start();
    System.out.println("server starts");


  }

}
