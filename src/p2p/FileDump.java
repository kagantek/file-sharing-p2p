package p2p;

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