import io.ConfigParser;
import pojo.Config;
import server.ServerDistributor;

public class ApplicationStarter {

  public static void main(String[] args)  {

    Config parsedConfig = ConfigParser.parse("orgs.json");
    System.out.println(parsedConfig);
    int[] ports = {13370, 13371};
    ServerDistributor thread = new ServerDistributor(ports, parsedConfig);
    thread.start();

    System.out.println("server starts");
  }

}
