package p2p;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Gui extends JFrame {

    private JMenuBar menuBar;
    private JMenu menuFiles;
    private JMenu menuHelp;
    private JMenuItem menuConnect;
    private JMenuItem menuDisconnect;
    private JMenuItem menuExit;
    private JMenuItem menuAbout;

    private JPanel mainPanel;
    private JTextField txtSharedFolder;
    private JButton btnBrowseShared;
    private JTextField txtDownloadFolder;
    private JButton btnBrowseDownload;
    private JButton btnRefreshPeers;
    private JList<String> lstPeers;
    private DefaultListModel<String> peersModel;

    private JButton btnShowSharedFiles;
    private JButton btnManageExclusions; // for excluding local subfolders/files

    private JTextArea txtLog;
    private JFileChooser folderChooser;

    // The "brain" of the peer
    public Peer peer;

    public Gui() {
        super("P2P File Sharing Application");

        peer = new Peer(this);

        initUI();

        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void initUI() {
        // Menus 
        menuBar = new JMenuBar();
        menuFiles = new JMenu("Files");
        menuHelp = new JMenu("Help");

        menuConnect = new JMenuItem("Connect");
        menuDisconnect = new JMenuItem("Disconnect");
        menuExit = new JMenuItem("Exit");
        menuAbout = new JMenuItem("About");

        menuFiles.add(menuConnect);
        menuFiles.add(menuDisconnect);
        menuFiles.addSeparator();
        menuFiles.add(menuExit);
        menuHelp.add(menuAbout);

        menuBar.add(menuFiles);
        menuBar.add(menuHelp);
        setJMenuBar(menuBar);

        // Menu Actions 
        menuConnect.addActionListener(e -> onConnect());
        menuDisconnect.addActionListener(e -> {
            try {
                onDisconnect();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        menuExit.addActionListener(e -> {
            try {
                onExit();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        menuAbout.addActionListener(e -> onAbout());

        // Main Panel 
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new GridLayout(2, 1, 5, 5));

        // Shared Folder Row
        JPanel sharedRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sharedRow.add(new JLabel("Shared Folder:"));
        txtSharedFolder = new JTextField(30);
        btnBrowseShared = new JButton("Browse");
        sharedRow.add(txtSharedFolder);
        sharedRow.add(btnBrowseShared);

        btnBrowseShared.addActionListener(e -> {
            chooseFolder(txtSharedFolder);
            peer.setShared(new File(txtSharedFolder.getText()));
        });

        // Download Folder Row
        JPanel downloadRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        downloadRow.add(new JLabel("Download Folder:"));
        txtDownloadFolder = new JTextField(30);
        btnBrowseDownload = new JButton("Browse");
        downloadRow.add(txtDownloadFolder);
        downloadRow.add(btnBrowseDownload);

        btnBrowseDownload.addActionListener(e -> {
            chooseFolder(txtDownloadFolder);
            peer.setDownload(new File(txtDownloadFolder.getText()));
        });

        topPanel.add(sharedRow);
        topPanel.add(downloadRow);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Peer List + Buttons
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        peersModel = new DefaultListModel<>();
        lstPeers = new JList<>(peersModel);
        JScrollPane scrollPeers = new JScrollPane(lstPeers);
        centerPanel.add(scrollPeers, BorderLayout.CENTER);

        btnRefreshPeers = new JButton("Refresh Peers");
        btnShowSharedFiles = new JButton("Show Shared Files of Selected Peer");
        btnManageExclusions = new JButton("Manage Exclusions");

        JPanel centerButtons = new JPanel(new FlowLayout());
        centerButtons.add(btnRefreshPeers);
        centerButtons.add(btnShowSharedFiles);
        centerButtons.add(btnManageExclusions);

        centerPanel.add(centerButtons, BorderLayout.SOUTH);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Log area
        txtLog = new JTextArea();
        txtLog.setEditable(false);
        JScrollPane scrollLog = new JScrollPane(txtLog);
        scrollLog.setPreferredSize(new Dimension(400, 150));
        mainPanel.add(scrollLog, BorderLayout.SOUTH);

        // Listeners
        btnRefreshPeers.addActionListener(e -> refreshPeersList());
        btnShowSharedFiles.addActionListener(e -> showSharedFilesOfPeer());
        btnManageExclusions.addActionListener(e -> manageExclusions());

        add(mainPanel);
    }

    private void chooseFolder(JTextField textField) {
        if (folderChooser == null) {
            folderChooser = new JFileChooser();
            folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }
        int result = folderChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = folderChooser.getSelectedFile();
            textField.setText(selected.getAbsolutePath());
        }
    }

    //Menu Methods
    private void onConnect() {
        peer.connect();
    }

    private void onDisconnect() throws IOException {
        peer.disconnect();
    }

    private void onExit() throws IOException {
        peer.disconnect();
        System.exit(0);
    }

    private void onAbout() {
        JOptionPane.showMessageDialog(this,
            "Yeditepe University CSE471 P2P File Sharing Project\nKagan Tek\n20210702027",
            "About",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void refreshPeersList() {
        peersModel.clear();
        for (Map.Entry<String, PeerInfo> entry : peer.getPeer().entrySet()) {
            PeerInfo info = entry.getValue();
            String display = info.getId() + " (" + info.getIp() + ":" + info.getPort() + ")";
            peersModel.addElement(display);
        }
    }

    private void showSharedFilesOfPeer() {
        String selected = lstPeers.getSelectedValue();
        if (selected == null) {
            log("No peer selected.");
            return;
        }
        log("Requesting shared file list from: " + selected);

        // Find PeerInfo
        PeerInfo remote = findPeerInfoFromList(selected);
        if (remote == null) {
            log("Peer not found in map.");
            return;
        }

        // Request the file list
        java.util.List<Peer.FileInfo> files = peer.requestFileList(remote);
        if (files.isEmpty()) {
            log("Peer has no files or request failed.");
            return;
        }

        // Let user pick a file
        String[] fileOptions = new String[files.size()];
        for (int i = 0; i < files.size(); i++) {
            fileOptions[i] = files.get(i).toString();
        }

        String chosen = (String) JOptionPane.showInputDialog(
                this,
                "Select a file to download:",
                "Shared Files",
                JOptionPane.QUESTION_MESSAGE,
                null,
                fileOptions,
                fileOptions[0]
        );

        if (chosen == null) {
            log("No file selected.");
            return;
        }

        String actualFileName = chosen;
        int idx = actualFileName.indexOf(" (");
        if (idx > 0) {
            actualFileName = actualFileName.substring(0, idx);
        }

        log("User picked file: '" + chosen + "' => actualFileName='" + actualFileName + "'");

        downloadFileFromPeer(remote, actualFileName);
    }

    private void downloadFileFromPeer(PeerInfo remote, String fileName) {
        log("Downloading file '" + fileName + "' from " + remote.getIp() + ":" + remote.getPort());
        FileClient fc = new FileClient(peer, remote.getIp(), remote.getPort(), fileName);
        fc.start();
    }

    private PeerInfo findPeerInfoFromList(String display) {
        String nodeId = display.split(" ")[0];
        for (Map.Entry<String, PeerInfo> entry : peer.getPeer().entrySet()) {
            if (entry.getKey().equals(nodeId)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private void manageExclusions() {
        String[] options = {
            "Add Exclusion (Files/Folders)",
            "Remove Exclusion (Files/Folders)",
            "Cancel"
        };

        int choice = JOptionPane.showOptionDialog(
                this,
                "Manage which type of exclusion?",
                "Manage Exclusions",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        switch (choice) {
            case 0:
                addLocalExclusion();
                break;
            case 1:
                removeLocalExclusion();
                break;
            default:
                log("Canceled exclusion management.");
        }
    }

    private void addLocalExclusion() {
        File sharedFolder = peer.getShared();
        if (sharedFolder == null || !sharedFolder.isDirectory()) {
            log("No valid shared folder set. Cannot manage exclusions.");
            return;
        }

        List<File> allFiles = new ArrayList<>();
        listAllFiles(sharedFolder, allFiles);

        String[] fileArray = new String[allFiles.size()];
        for (int i = 0; i < allFiles.size(); i++) {
            fileArray[i] = allFiles.get(i).getAbsolutePath();
        }

        JList<String> list = new JList<>(fileArray);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        int result = JOptionPane.showConfirmDialog(
                this,
                new JScrollPane(list),
                "Select file(s)/folder(s) to exclude from share",
                JOptionPane.OK_CANCEL_OPTION
        );
        if (result == JOptionPane.OK_OPTION) {
            for (String sel : list.getSelectedValuesList()) {
                peer.addExcludedFolderOrFile(new File(sel));
            }
            log("Exclusions updated. The following paths have been excluded:");
            for (File f : peer.getExcludedPaths()) {
                log(" - " + f.getAbsolutePath());
            }
        }
    }

    private void removeLocalExclusion() {
        if (peer.getExcludedPaths().isEmpty()) {
            log("No currently excluded paths to remove.");
            return;
        }
        List<File> excludedList = new ArrayList<>(peer.getExcludedPaths());
        String[] arr = new String[excludedList.size()];
        for (int i = 0; i < excludedList.size(); i++) {
            arr[i] = excludedList.get(i).getAbsolutePath();
        }

        JList<String> list = new JList<>(arr);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        int result = JOptionPane.showConfirmDialog(
                this,
                new JScrollPane(list),
                "Select file(s)/folder(s) to UN-exclude from share",
                JOptionPane.OK_CANCEL_OPTION
        );
        if (result == JOptionPane.OK_OPTION) {
            for (String sel : list.getSelectedValuesList()) {
                peer.removeExcludedPath(new File(sel));
            }
            log("The following paths have been un-excluded:");
            for (String sel : list.getSelectedValuesList()) {
                log(" - " + sel);
            }
        }
    }

    private void listAllFiles(File folder, List<File> collector) {
        if (folder.isFile()) {
            collector.add(folder);
        } else if (folder.isDirectory()) {
            collector.add(folder);
            File[] sub = folder.listFiles();
            if (sub != null) {
                for (File f : sub) {
                    listAllFiles(f, collector);
                }
            }
        }
    }

    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            txtLog.append(message + "\n");
        });
    }
}
