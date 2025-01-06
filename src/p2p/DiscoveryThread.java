package p2p;

public class DiscoveryThread extends Thread {
    private Peer peer;

    public DiscoveryThread(Peer peer) {
        this.peer = peer;
        setName("DiscoveryThread");
    }
}
