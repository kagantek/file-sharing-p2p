package p2p;

//Kagan Tek - 20210702027 - P2P File Sharing Project - Yeditepe University CSE471 Course

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;


//FileServer class will be handling all the connections which will take place in FileServerThread class. 
public class FileServer {
    private int port;
    private boolean isRunning;
    private ServerSocket socket;
    private ExecutorService threads; //Will store the FileServerThreads

    public FileServer(int port) {
        this.port = port;
    }

    public void startServer() {
        isRunning = true;
        threads = Executors.newCachedThreadPool();
        new Thread(() -> runServer()).start();
    }

    public void stopServer() {
        return;
    }

    public void runServer() {
        return;
    }
}
