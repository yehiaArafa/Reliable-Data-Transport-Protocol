package Main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class template {
	byte[] recievedBytes;
	ArrayList<byte[]> packets = new ArrayList();

	public void createFile(ArrayList<byte[]> packs, String filepath) throws IOException {
		byte[] filebytes = new byte[(packs.size() - 1) * packs.get(0).length + packs.get(packs.size() - 1).length];
		// Pointer to check where to stop for the next byte array.
		int packetIndex = 0;
		for (int i = 0; i < packs.size(); i++) {
			byte[] singlePacket = packs.get(i);
			for (int j = 0; j < singlePacket.length; j++) {
				filebytes[packetIndex] = singlePacket[j];
				packetIndex++;
			}
		}
		System.out.println(filebytes.length);
		// Writing bytes into file.
		File newFile = new File(filepath);
		FileOutputStream writerStreamer = new FileOutputStream(newFile);
		for(int i=0;i<filebytes.length && filebytes[i]!=0;i++) {
			writerStreamer.write(filebytes[i]);
		}
		writerStreamer.flush();
		writerStreamer.close();
	}

	public ArrayList createPackets(int sizeOfPackets, String filename) throws IOException {
		this.recievedBytes = new byte[sizeOfPackets];
		File sourceFile = new File(filename);
		FileInputStream createStreamer = new FileInputStream(sourceFile);
		int size;
		// This size = line checks and assigns value of the recievedBytes.
		// If the sent packet size is less that 4096 it will be assigned in the
		// size variable.
		// Else it will send a chunk of the rest of the array.
		while ((size = createStreamer.read(recievedBytes, 0, sizeOfPackets)) != -1) {
			packets.add(Arrays.copyOfRange(recievedBytes, 0, size));
		}
		return packets;
	}

}