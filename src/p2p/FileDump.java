package p2p;

//Kagan Tek - 20210702027 - P2P File Sharing Project - Yeditepe University CSE471 Course

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Random;

public class FileDump {
	public void createFile() throws Exception {
		File file = new File("fileToSend.txt");
		BufferedWriter myFile = new BufferedWriter(new FileWriter(file));
		Random r = new Random();
		int length = 10000000;
		for (int i = 0; i < length; i++) {
			myFile.write(Integer.toString(r.nextInt(10)));
		}
		myFile.flush();
		myFile.close();
	}

}