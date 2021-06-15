import server.DistributorRunner;
import server.ServerDistributor;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ApplicationStarter {

    public static void main(String[] args) throws IOException {
        int[] ports = {13370, 13371};
        //one Thread per Port
        for (int port : ports
        ) {
            Thread thread = new Thread(new DistributorRunner(port));
            thread.start();
        }
        System.out.println("server starts");
    }

}
