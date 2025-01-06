package p2p;

//Kagan Tek - 20210702027 - P2P File Sharing Project - Yeditepe University CSE471 Course

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.UUID;

public class DiscoveryThread extends Thread {
    
    private Peer peer;
    private boolean isRunning = true;
    private DatagramSocket socket;
    private int port = 9000; 

    public DiscoveryThread(Peer peer) {
        this.peer = peer;
        setName("DiscoveryThread");
    }

    @Override
    public void run() {
        try {
            socket = new DatagramSocket(port);
            socket.setSoTimeout(0);
            while(isRunning) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String received = new String(packet.getData(), 0, packet.getLength());
                processMessage(received, packet.getAddress().getHostAddress());
            }
        } catch (Exception e) {
            if(isRunning) {
                e.printStackTrace();
            }
        } finally {
            if(socket != null) {
                socket.close();
            }
        }
    }

    //Processes broadcast message in order to store peer info
    private void processMessage(String msg, String ip) {
        try {
            String[] parts = msg.split("\\|");
            if(parts.length < 4 ) return;

            String cmd = parts [0];
            String nodeId = parts[1];
            int port = Integer.parseInt(parts[2]);
            int ttl = Integer.parseInt(parts[3]);
            
            if("HELLO".equals(cmd)) {
                PeerInfo info = new PeerInfo(nodeId, ip, port);
                if(peer.getPeer().containsKey(nodeId)) {
                    peer.updatePeer(nodeId, info);
                } else {
                    peer.addPeer(nodeId, info);
                }

                if(ttl > 1) {
                    ttl--;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void stopThread() {
        isRunning = false;
        if(socket != null) {
            socket.close();
        }
    }
    
}
