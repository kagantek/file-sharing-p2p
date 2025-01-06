package p2p;

//Kagan Tek - 20210702027 - P2P File Sharing Project - Yeditepe University CSE471 Course

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
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
    private JButton btnDownloadFile;
    private JTextArea txtLog;

    private JFileChooser folderChooser;

    //Peer object in order to call functipns
    private Peer peer;

    public Gui() {
        super("P2P File Sharing Application");

        peer = new Peer(this);  //Log peer into gui

        initUI();

        // Main frame
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void initUI() {
        // Menu
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

        // Menu actions
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

        // Shared folder row
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

        // Download folder row
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

        // List of peers + controls
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        peersModel = new DefaultListModel<>();
        lstPeers = new JList<>(peersModel);
        JScrollPane scrollPeers = new JScrollPane(lstPeers);
        centerPanel.add(scrollPeers, BorderLayout.CENTER);

        btnRefreshPeers = new JButton("Refresh Peers");
        btnShowSharedFiles = new JButton("Show Shared Files of Selected Peer");
        btnDownloadFile = new JButton("Download Selected File");

        JPanel centerButtons = new JPanel(new FlowLayout());
        centerButtons.add(btnRefreshPeers);
        centerButtons.add(btnShowSharedFiles);
        centerButtons.add(btnDownloadFile);

        centerPanel.add(centerButtons, BorderLayout.SOUTH);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Log area
        txtLog = new JTextArea();
        txtLog.setEditable(false);
        JScrollPane scrollLog = new JScrollPane(txtLog);
        scrollLog.setPreferredSize(new Dimension(400, 150));
        mainPanel.add(scrollLog, BorderLayout.SOUTH);

        // Button actions
        btnRefreshPeers.addActionListener(e -> refreshPeersList());
        btnShowSharedFiles.addActionListener(e -> showSharedFilesOfPeer());
        btnDownloadFile.addActionListener(e -> downloadFileFromPeer());

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

    private void onConnect() {
        peer.connect();
    }

    private void onDisconnect() throws IOException {
        peer.disconnect();
    }

    private void onExit() throws IOException {
        // Optionally do node.disconnect() if needed
        peer.disconnect();
        System.exit(0);
    }

    private void onAbout() {
        JOptionPane.showMessageDialog(this,
                "Yeditepe University CSE471 P2P File Sharing Project\nKagan Tek\n20210702027",
                "About", JOptionPane.INFORMATION_MESSAGE);
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

        log("Requesting shared file list from " + selected + " ...");
  
    }

    private void downloadFileFromPeer() {
        log("Download triggered. (Placeholder) - you need to implement actual selection and chunk requests.");
    }

    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            txtLog.append(message + "\n");
        });
    }
}