package p2p;

//Kagan Tek - 20210702027 - P2P File Sharing Project - Yeditepe University CSE471 Course

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.Random;

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
            peer.gui.log("Server thread " + connSocket.getInetAddress().getHostAddress() + " connected.");
            peer.gui.log(">>> Sending data...");
			File file = new File("fileToSend.txt");

			RandomAccessFile raf = new RandomAccessFile(file, "r");
			int length = (int) file.length();

			int chunkCount = (int) Math.ceil(length / 256000.0);
		
			int[] checkArray = new int[chunkCount];
			DataInputStream dIS = new DataInputStream(connSocket.getInputStream());
			DataOutputStream dOS = new DataOutputStream(connSocket.getOutputStream());

			dOS.writeInt(length);
			Random random = new Random();
			int loop = 0;
			while (loop < chunkCount) {
				int i = random.nextInt(chunkCount);
				if (checkArray[i] == 0) {

					raf.seek(i * 256000);

					byte[] toSend = new byte[256000];
					int read = raf.read(toSend);
					dOS.writeInt(i);
					dOS.writeInt(read);
					dOS.write(toSend, 0, read);
					dOS.flush();
					int ACK = dIS.readInt();
					if (i == ACK) {
						checkArray[i] = 1;
						loop++;
					}
				}
			}
			peer.gui.log(">>> Sent all chunks to " + connSocket.getInetAddress().getHostAddress() + "...");
			raf.close();
			dOS.writeInt(-1);
			dOS.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
