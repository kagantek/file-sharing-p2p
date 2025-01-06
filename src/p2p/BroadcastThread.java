package p2p;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.UUID;

//Kagan Tek - 20210702027 - P2P File Sharing Project - Yeditepe University CSE471 Course

public class BroadcastThread extends Thread {
    private Peer peer;
    private int ttl;
    private boolean isRunning = true;
    private int port = 9000;
    private String nodeId; 

    public BroadcastThread(Peer peer, int ttl) {
        this.peer = peer;
        this.ttl = ttl;
        setName("BroadcastThread");
        this.nodeId = UUID.randomUUID().toString().substring(0, 8);
    }

    @Override
    public void run() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            socket.setBroadcast(true);

            while(isRunning) {
                String msg = "HELLO|" + nodeId + "|" + peer.getPort() + "|" + ttl;
                byte[] buffer = msg.getBytes();

                InetAddress address = InetAddress.getByName("192.168.1.255");
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
                socket.send(packet);

                Thread.sleep(5000);
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(socket != null) socket.close();
        }
    }

    public void stopThread() {
        isRunning = false;
    }
}
