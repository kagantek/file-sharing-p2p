package p2p;

import java.net.Socket;

public class FileServerThread implements Runnable {
    private Peer peer;
    private Socket connSocket;

    public FileServerThread(Peer peer, Socket connSocket) {
        this.peer = peer;
        this.connSocket = connSocket;
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Unimplemented method 'run'");
    }
    
}
