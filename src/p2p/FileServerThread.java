package p2p;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.Random;

//Kagan Tek - 20210702027 - P2P File Sharing Project - Yeditepe University CSE471 Course

public class FileServerThread implements Runnable {
    private Peer peer;
    private Socket connSocket;

    public FileServerThread(Peer peer, Socket connSocket) {
        this.peer = peer;
        this.connSocket = connSocket;
    }

    @Override
    public void run() {
        try {
            peer.gui.log("[ServerThread] " + connSocket.getInetAddress().getHostAddress() + " connected.");

            DataInputStream dis = new DataInputStream(connSocket.getInputStream());
            DataOutputStream dos = new DataOutputStream(connSocket.getOutputStream());

            // Read command and list files or transfer them accordingly
            String command = dis.readUTF(); 
            if (command.equals("LIST")) {
                sendFileList(dos);
            }
            else if (command.startsWith("REQUEST|")) {
                String[] parts = command.split("\\|");
                if (parts.length == 2) {
                    String requestedFileName = parts[1];
                    sendRequestedFile(requestedFileName, dis, dos);
                } else {
                    peer.gui.log("[ServerThread] Invalid REQUEST command: " + command);
                }
            }
            else {
                peer.gui.log("[ServerThread] Unknown command from client: " + command);
            }

            dis.close();
            dos.close();
            connSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendFileList(DataOutputStream dOS) {
        try {
            File folder = peer.getShared();
            if (folder == null || !folder.isDirectory()) {
                dOS.writeInt(0);
                dOS.flush();
                return;
            }
            File[] files = folder.listFiles();
            if (files == null) {
                dOS.writeInt(0);
                dOS.flush();
                return;
            }

            // We skip files/folders that are excluded
            java.util.List<File> includedFiles = new java.util.ArrayList<>();
            for (File f : files) {
                if (!peer.shouldExclude(f)) {
                    includedFiles.add(f);
                }
            }

            dOS.writeInt(includedFiles.size());
            for (File f : includedFiles) {
                dOS.writeUTF(f.getName());
                dOS.writeLong(f.length());
            }
            dOS.flush();
            peer.gui.log("[ServerThread] Sent file list of size " + includedFiles.size() + " to client.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendRequestedFile(String requestedFile, DataInputStream dis, DataOutputStream dos) {
        try {
            peer.gui.log("[ServerThread] Client requested file: " + requestedFile);

            File file = new File(peer.getShared(), requestedFile);
            // Also skip if parent or itself is excluded
            if (peer.shouldExclude(file) || !file.exists() || !file.isFile()) {
                dos.writeInt(-1);
                dos.flush();
                peer.gui.log("[ServerThread] File not found or excluded: " + file.getAbsolutePath());
                return;
            }

            RandomAccessFile raf = new RandomAccessFile(file, "r");
            int length = (int) file.length();
            dos.writeInt(length);

            int chunkCount = (int) Math.ceil(length / 256000.0);
            int[] checkArray = new int[chunkCount];
            Random random = new Random();
            int loop = 0;

            while (loop < chunkCount) {
                int i = random.nextInt(chunkCount);
                if (checkArray[i] == 0) {
                    raf.seek((long)i * 256000);
                    byte[] toSend = new byte[256000];
                    int read = raf.read(toSend);

                    dos.writeInt(i);
                    dos.writeInt(read);
                    dos.write(toSend, 0, read);
                    dos.flush();

                    int ACK = dis.readInt();
                    if (i == ACK) {
                        checkArray[i] = 1;
                        loop++;
                    }
                }
            }

            peer.gui.log("[ServerThread] Sent all chunks of file: " + requestedFile);
            raf.close();

            dos.writeInt(-1);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
