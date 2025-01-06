package test;

//Kagan Tek - 20210702027 - P2P File Sharing Project - Yeditepe University CSE471 Course

import java.io.IOException;
import java.net.InetSocketAddress;
import p2p.FileDump;
import p2p.FileServer;
import p2p.Gui;
import p2p.Peer;

public class FileServerTest {
    public static void main(String[] args) {
        Gui gui = new Gui(); //Dummy class at the moment
        
        int port = 6789;

        FileDump dump = new FileDump();
        try {
            System.out.println("Generating fileToSend.txt ...");
            dump.createFile();
            System.out.println("File generated successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("File generation failed (but continuing).");
        }

        Peer peer = new Peer(gui); 

        FileServer server = new FileServer(peer, port);

        server.startServer();

        System.out.println("Server started. Waiting for connections on port " + port + "...");
    }
}
