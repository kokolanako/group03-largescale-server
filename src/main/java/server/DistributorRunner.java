package server;

import java.util.List;

public class DistributorRunner implements Runnable{

    ServerDistributor distributor;
    public DistributorRunner(int port, List<ServerThread> clients){
        this.distributor = new ServerDistributor(port, clients);
    }
    @Override
    public void run() {
        this.distributor.start();
    }
}
