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
			RandomAccessFile rAF = new RandomAccessFile(file, "rw");
			Socket socket = new Socket(ip, port);
			System.out.println(getName() + " has connected to server...");
			DataInputStream dIS = new DataInputStream(socket.getInputStream());
			DataOutputStream dOS = new DataOutputStream(socket.getOutputStream());
			int length = dIS.readInt();
			//700 byte
			System.out.println(getName() + " has read " + length + " for fileLength...");
			rAF.setLength(length);
			//file length = 700 byte
			int i;
			while ((i = dIS.readInt()) != -1) {
				System.out.println(getName() + " has read " + i + " for chunkID...");
				rAF.seek(i * 256000);
				int chunkLength = dIS.readInt();
				System.out.println(getName() + " has read " + chunkLength + " for chunkSize...");
				byte[] toReceive = new byte[chunkLength];
				dIS.readFully(toReceive);
				System.out.println(getName() + " has read " + chunkLength + " bytes for chunkID " + i + "...");
				rAF.write(toReceive);
				dOS.writeInt(i);
				System.out.println(getName() + " has sent " + i + " for ACK...");
			}
			System.out.println(getName() + " has read " + i + " for chunkID...");
			rAF.close();
			socket.close();
		} catch (Exception e) {
			System.out.println("java -jar FileClient.jar <IP> <PORT> <number>\r\n"
					+ "Where <IP> is a string, <PORT> is a number and <number> represents concurrent file downloads.");
		}
	}

}