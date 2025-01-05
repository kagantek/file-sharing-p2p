package p2p;

//Kagan Tek - 20210702027 - P2P File Sharing Project - Yeditepe University CSE471 Course

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;


//FileServer class will be handling all the connections which will take place in FileServerThread class. 
public class FileServer {
    private Peer peer;
    private int port;
    private boolean isRunning;
    private ServerSocket socket;
    private ExecutorService threads; //Will store the FileServerThreads

    public FileServer(Peer peer, int port) {
        this.peer = peer;
        this.port = port;
    }

    public void startServer() {
        isRunning = true;
        threads = Executors.newCachedThreadPool();
        new Thread(() -> runServer()).start();
    }

    //When stop server is called it will check the socket and existing threads then close them all.
    public void stopServer() throws IOException {
        isRunning = false;
        if(socket != null) socket.close();
        if(threads != null) threads.shutdownNow();
    }

    //Starting to listen on the specified port and creating threads progressively.
    public void runServer() {
        try {
            socket = new ServerSocket();
            System.out.println("Server listening on port " + port);
            
            while(isRunning) {
                try {
                    Socket connSocket = socket.accept();
                    threads.execute(new FileServerThread(peer, connSocket));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Server stopped.");
        }
        return;
    }
}
