package p2p;

//Kagan Tek - 20210702027 - P2P File Sharing Project - Yeditepe University CSE471 Course

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//Class needed to manage threads, store states such as connected/disconnected, shared folders etc.
//Will also act as an API for the upcoming GUI, keep the project safe and modularized.
//"Brain" of a peer
public class Peer {
    //For passing data and executing functions in the future after gui is developed.
    Gui gui;

    //Storing shared and dowload files
    private File shared;
    private File download;

    //Store other detected peers for file sharing
    private ConcurrentHashMap<String, PeerInfo> peers = new ConcurrentHashMap<>();

    //Since this class will work as the brain of each peer, the threads will be started in this class' methods.
    private DiscoveryThread discovery;
    private BroadcastThread broadcast;
    private FileServer server;

    private int port = 6789;

    private boolean connected = false;

    public Peer(Gui gui) {
        this.gui = gui;
    }

    public void setShared(File shared) {
        this.shared = shared;
        gui.log("Shared folder set to" + shared.getAbsolutePath());
    }

    public void setDownload(File download) {
        this.download = download;
        gui.log("Download folder set to" + download.getAbsolutePath());
    }

    public Map<String, PeerInfo> getPeer() {
        return peers;
    }


    //When the connect button in gui gets clicked this function will run
    public void connect() {
        if(connected) {
            gui.log("Already connected to network at port: " + port);
            return;
        }
        gui.log("Connecting to network at port: " + port + "...");

        //Start server
        server = new FileServer(this, port);
        server.startServer();

        //Start udp discovery listener (will be implemented)
        discovery = new DiscoveryThread(this);
        discovery.start();

        //Start broadcaster
        broadcast = new BroadcastThread(this, 1);
        broadcast.start();

        connected = true;
        gui.log("Connected. Listening for peers on port: " + port);
    }

    public void disconnect() throws IOException {
        if(!connected) {
            gui.log("Not connected.");
            return;
        }

        gui.log("Disconnecting from network...");
        if(server != null) {
            server.stopServer();
        }

        if(discovery != null) {
            discovery.stopThread();
        }

        if(broadcast != null) {
            broadcast.stopThread();
        }

        connected = false;
        gui.log("Disconnected from network.");
    }

    //When a peer gets discovered they get added to the map
    public void addPeer(String nodeId, PeerInfo info) {
        if(!peers.containsKey(nodeId)) {
            peers.put(nodeId, info);
            gui.log("Discovered new peer: " + nodeId + " => " + info);
        }
    }

    public void updatePeer(String nodeId, PeerInfo info) {
        peers.put(nodeId, info);
    }

    public File getShared() {
        return shared;
    }

    public File getDownload() {
        return download;
    }

    public int getPort() {
        return port;
    }
}
