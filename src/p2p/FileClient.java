package p2p;

//Kagan Tek - 20210702027 - P2P File Sharing Project - Yeditepe University CSE471 Course

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.net.Socket;

public class FileClient extends Thread {

	private String ip;
	private int port;
    private Peer peer;
    private String fileName;

	public FileClient(Peer peer, String ip, int port, String fileName) {
		super("FileClientThread");
		this.peer = peer;
		this.ip = ip;
		this.port = port;
		this.fileName = fileName;
	}

	@Override
	public void run() {
		try {
			
			//Creating destination for file to get into while downloaded
			File destFolder = peer.getDownload();
			if (destFolder == null) {
				peer.gui.log("FileClient No download folder set, using current dir");
				destFolder = new File(".");
			}

			File outFile = new File(destFolder, fileName);
			peer.gui.log("FileClient Creating local file: " + outFile.getAbsolutePath());
			if (!outFile.exists()) {
				outFile.createNewFile();
			}

			RandomAccessFile raf = new RandomAccessFile(outFile, "rw");
			
			// Connect
			Socket socket = new Socket(ip, port);
			peer.gui.log("[FileClient] Connected to server " + ip + ":" + port);

			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			DataInputStream dis = new DataInputStream(socket.getInputStream());

			// Send download command
			String requestCommand = "REQUEST|" + fileName;
			dos.writeUTF(requestCommand);
			dos.flush();

			int length = dis.readInt();
			if (length == -1) {
				peer.gui.log("FileClient Server says file not found: " + fileName);
				socket.close();
				raf.close();
				return;
			}

			peer.gui.log("FileClient File size: " + length);
			raf.setLength(length);

			//Write file to download path
			int i;
			while ((i = dis.readInt()) != -1) {
				int chunkLength = dis.readInt();
				byte[] receive = new byte[chunkLength];
				dis.readFully(receive);

				raf.seek((long)i * 256000);
				raf.write(receive);

				// ACK
				dos.writeInt(i);
				dos.flush();
			}

			peer.gui.log("FileClient Download complete for file: " + fileName);
			raf.close();
			dis.close();
			dos.close();
			socket.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}