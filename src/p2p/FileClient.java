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

	public FileClient(String ip, int port, String name) {
		super(name); //assigns a name to the thread
		this.ip = ip; //servers IP
		this.port = port; //servers port
	}

	@Override
	public void run() {
		try {
			File file = new File(getName());
			if (!file.exists()) {
				file.createNewFile();
			}
			RandomAccessFile raf = new RandomAccessFile(file, "rw");
			Socket socket = new Socket(ip, port);
			System.out.println(getName() + " has connected to server...");
			DataInputStream dis = new DataInputStream(socket.getInputStream());
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			int length = dis.readInt();
	
			System.out.println(getName() + " has read " + length + " for fileLength...");
			raf.setLength(length);

			int i;
			while ((i = dis.readInt()) != -1) {
				System.out.println(getName() + " has read " + i + " for chunkID...");
				raf.seek(i * 256000);
				int chunkLength = dis.readInt();
				System.out.println(getName() + " has read " + chunkLength + " for chunkSize...");
				byte[] toReceive = new byte[chunkLength];
				dis.readFully(toReceive);
				System.out.println(getName() + " has read " + chunkLength + " bytes for chunkID " + i + "...");
				raf.write(toReceive);
				dos.writeInt(i);
				System.out.println(getName() + " has sent " + i + " for ACK...");
			}
			System.out.println(getName() + " has read " + i + " for chunkID...");
			raf.close();
			socket.close();
		} catch (Exception e) {
			System.out.println("java -jar FileClient.jar <IP> <PORT> <number>\r\n"
					+ "Where <IP> is a string, <PORT> is a number and <number> represents concurrent file downloads.");
		}
	}

}