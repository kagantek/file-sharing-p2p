package p2p;

public class BroadcastThread extends Thread {
    private Peer peer;
    private int ttl;

    public BroadcastThread(Peer peer, int ttl) {
        this.peer = peer;
        this.ttl = ttl;
        setName("BroadcastThread");
    }
}
