package test;

import p2p.FileClient;

public class FileClientTest {
    public static void main(String[] args) {
		try {
			if (args.length != 3) {
				System.out.println("USAGE: java -jar FileClient.jar <IP> <PORT> <number>\r\n\r\n"
						+ "Where <IP> is a string, <PORT> is a number and <number> represents concurrent file downloads.");
				//java -jar FileClient.jar 127.0.0.1 6789 3 -> 3 threads downloading the file from server
			} else {
				int a = Integer.parseInt(args[2]);
				System.out.println("Creating " + a + " thread(s)...");
				for (int i = 0; i < a; i++) {
					new FileClient(args[0], Integer.parseInt(args[1]), "fileToReceive" + (i + 1)).start();
				}
			}
		} catch (Exception e) {
			System.out.println("USAGE: java -jar FileClient.jar <IP> <PORT> <number>\r\n\r\n"
					+ "Where <IP> is a string, <PORT> is a number and <number> represents concurrent file downloads.");
		}
	}
}
