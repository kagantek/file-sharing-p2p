package p2p;

//Kagan Tek - 20210702027 - P2P File Sharing Project - Yeditepe University CSE471 Course

public class DiscoveryThread extends Thread {
    private Peer peer;

    public DiscoveryThread(Peer peer) {
        this.peer = peer;
        setName("DiscoveryThread");
    }
}
