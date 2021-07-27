import io.ConfigParser;
import pojo.Config;
import server.ServerDistributor;

import java.io.IOException;

public class ApplicationStarter {

  public static void main(String[] args) throws IOException {

    Config parsedConfig = ConfigParser.parse("orgs.json");
    System.out.println(parsedConfig);
    int[] ports = {13370, 13371};
    ServerDistributor thread = new ServerDistributor(ports, parsedConfig);
    thread.start();

    System.out.println("server starts");
  }

}
