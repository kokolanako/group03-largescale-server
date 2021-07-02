import server.DistributorRunner;
import server.ServerDistributor;
import server.ServerThread;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ApplicationStarter {

    public static void main(String[] args) throws IOException {
        //every client is listened in an individual thread
        List<ServerThread> clients= Collections.synchronizedList(new ArrayList<>());;
        int[] ports = {13370, 13371};
        //one Thread per Port
        for (int port : ports) {
            Thread thread = new Thread(new DistributorRunner(port,clients));
            thread.start();
        }
        System.out.println("server starts");
    }
//  static int k=0;
//public static void main(String[] args) {
//  System.out.println(k);
//  int g= incr();
//  System.out.println(g);
//}
//static int incr(){
//  return ++k;
//}
}
