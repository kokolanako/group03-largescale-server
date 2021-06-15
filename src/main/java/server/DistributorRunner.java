package server;

public class DistributorRunner implements Runnable{

    ServerDistributor distributor;
    public DistributorRunner(int port){
        this.distributor = new ServerDistributor(port);
    }
    @Override
    public void run() {
        this.distributor.start();
    }
}
