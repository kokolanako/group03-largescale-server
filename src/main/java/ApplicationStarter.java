import io.ConfigParser;
import pojo.Config;
import server.ServerDistributor;
import server.ServerThread;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ApplicationStarter {

    public static void main(String[] args) throws IOException {

      Config parsedConfig = ConfigParser.parse("orgs.json");
      System.out.println(parsedConfig);
        //every client is listened in an individual thread
        List<ServerThread> clients= Collections.synchronizedList(new ArrayList<>());;
        int[] ports = {13370, 13371};
        //one Thread per Port
        for (int port : ports) {
            Thread thread = new Thread(new ServerDistributor(port,clients, parsedConfig));
            thread.start();
        }
        System.out.println("server starts");
    }

}
