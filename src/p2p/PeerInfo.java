package p2p;

//Kagan Tek - 20210702027 - P2P File Sharing Project - Yeditepe University CSE471 Course

public class PeerInfo {
    private String nodeId;
    private String ip;
    private int port;
    private long lastSeen;

    public PeerInfo(String nodeId, String ip, int port) {
        this.nodeId = nodeId;
        this.ip = ip;
        this.port = port;
        this.lastSeen = System.currentTimeMillis();
    }

    public String getId() {
        return nodeId;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void updateLastSeen() {
        this.lastSeen = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "PeerInfo{" +
                "nodeId='" + nodeId + '\'' +
                ", address='" + ip + '\'' +
                ", port=" + port +
                ", lastSeen=" + lastSeen +
                '}'; 
     }
}
