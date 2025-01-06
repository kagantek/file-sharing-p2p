package p2p;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

//Kagan Tek - 20210702027 - P2P File Sharing Project - Yeditepe University CSE471 Course

public class Peer {
    Gui gui;

    private File shared;
    private File download;

    // Storing single file/folder exclusions for local share
    private Set<File> excludedPaths = new HashSet<>();

    private ConcurrentHashMap<String, PeerInfo> peers = new ConcurrentHashMap<>();

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

    public File getShared() {
        return shared;
    }

    public File getDownload() {
        return download;
    }

    public Map<String, PeerInfo> getPeer() {
        return peers;
    }

    public int getPort() {
        return port;
    }

    public boolean isConnected() {
        return connected;
    }

    public void connect() {
        if (connected) {
            gui.log("Already connected to network at port: " + port);
            return;
        }
        gui.log("Connecting to network at port: " + port + "...");

        server = new FileServer(this, port);
        server.startServer();

        discovery = new DiscoveryThread(this);
        discovery.start();

        broadcast = new BroadcastThread(this, 1);
        broadcast.start();

        connected = true;
        gui.log("Connected. Listening for peers on port: " + port);
    }

    public void disconnect() throws IOException {
        if (!connected) {
            gui.log("Not connected.");
            return;
        }
        gui.log("Disconnecting from network...");

        if (server != null) server.stopServer();
        if (discovery != null) discovery.stopThread();
        if (broadcast != null) broadcast.stopThread();

        connected = false;
        gui.log("Disconnected from network.");
    }

    public void addPeer(String nodeId, PeerInfo info) {
        if (!peers.containsKey(nodeId)) {
            peers.put(nodeId, info);
            gui.log("Discovered new peer: " + nodeId + " => " + info);
        }
    }

    public void updatePeer(String nodeId, PeerInfo info) {
        peers.put(nodeId, info);
    }

    // Exclusions
    public Set<File> getExcludedPaths() {
        return excludedPaths;
    }

    public void addExcludedFolderOrFile(File f) {
        excludedPaths.add(f);
    }

    public void removeExcludedPath(File f) {
        excludedPaths.remove(f);
    }

    public boolean shouldExclude(File file) {
        if (excludedPaths.contains(file)) {
            return true;
        }
        // Check any parent
        File parent = file.getParentFile();
        while (parent != null) {
            if (excludedPaths.contains(parent)) {
                return true;
            }
            parent = parent.getParentFile();
        }
        return false;
    }

    // Request file list
    public List<FileInfo> requestFileList(PeerInfo peerInfo) {
        List<FileInfo> result = new ArrayList<>();
        try {
            gui.log("[Peer] Connecting to " + peerInfo.getIp() + ":" + peerInfo.getPort() + " for LIST command...");
            java.net.Socket socket = new java.net.Socket(peerInfo.getIp(), peerInfo.getPort());
            java.io.DataOutputStream dOS = new java.io.DataOutputStream(socket.getOutputStream());
            java.io.DataInputStream dIS = new java.io.DataInputStream(socket.getInputStream());

            dOS.writeUTF("LIST");
            dOS.flush();

            int fileCount = dIS.readInt();
            if (fileCount == 0) {
                gui.log("[Peer] Received empty file list from " + peerInfo.getIp());
                socket.close();
                return result;
            }

            List<FileInfo> rawList = new ArrayList<>();
            for (int i = 0; i < fileCount; i++) {
                String fName = dIS.readUTF();
                long fSize = dIS.readLong();
                rawList.add(new FileInfo(fName, fSize));
            }
            socket.close();

            // detect duplicates by fileSize
            Map<Long, String> seenSameSize = new HashMap<>();
            for (FileInfo fi : rawList) {
                if (seenSameSize.containsKey(fi.getFileSize())) {
                    String dupName = seenSameSize.get(fi.getFileSize());
                    fi.setDuplicateOf(dupName);
                } else {
                    seenSameSize.put(fi.getFileSize(), fi.getFileName());
                }
            }

            // annotate the original
            Map<String, FileInfo> nameToInfo = new HashMap<>();
            for (FileInfo fi : rawList) {
                nameToInfo.put(fi.getFileName(), fi);
            }
            for (FileInfo fi : rawList) {
                if (fi.getDuplicateOf() != null) {
                    FileInfo orig = nameToInfo.get(fi.getDuplicateOf());
                    if (orig != null) {
                        orig.setDuplicateOf(fi.getFileName());
                    }
                }
            }

            result.addAll(rawList);
            gui.log("[Peer] Received file list of size " + fileCount + " from " + peerInfo.getIp());
        } catch (IOException e) {
            e.printStackTrace();
            gui.log("[Peer] Could not retrieve file list from peer: " + peerInfo.getIp());
        }
        return result;
    }

    public static class FileInfo {
        private String fileName;
        private long fileSize;
        private String duplicateOf;

        public FileInfo(String fileName, long fileSize) {
            this.fileName = fileName;
            this.fileSize = fileSize;
        }

        public String getFileName() { return fileName; }
        public long getFileSize() { return fileSize; }

        public void setDuplicateOf(String otherFileName) {
            duplicateOf = otherFileName;
        }
        public String getDuplicateOf() { return duplicateOf; }

        @Override
        public String toString() {
            if (duplicateOf != null) {
                return fileName + " (same file as " + duplicateOf + ")";
            }
            return fileName + " (" + fileSize + " bytes)";
        }
    }
}
