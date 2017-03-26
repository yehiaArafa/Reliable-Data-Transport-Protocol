package StopAndWait;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;

import Main.template;

public class SAW_Client extends template {

	private DatagramSocket clientSocket;
	private int sequence, ACK;

	byte[] sentData = new byte[1024];
	byte[] recievedData;
	int sizeOfDatagram;
	ArrayList<byte[]> packets = new ArrayList();

	public SAW_Client(DatagramSocket clientSocket, int sizeOfDatagram) {
		this.clientSocket = clientSocket;
		this.sizeOfDatagram = sizeOfDatagram;
		recievedData = new byte[sizeOfDatagram];
	}

	public void start() throws IOException {

		sequence = 0;
		int counter = 0;

		while (!clientSocket.isClosed()) {

			DatagramPacket recievedPacket = new DatagramPacket(recievedData, recievedData.length);
			clientSocket.receive(recievedPacket);

			String response = new String(recievedPacket.getData());
			String[] checkEnd = response.split("-");
			if (checkEnd[0].equalsIgnoreCase("end")) {
				clientSocket.close();
				break;
			} else {
				if (recievedPacket.getLength() < this.sizeOfDatagram) {
					byte[] finalByte = new byte[recievedPacket.getLength()];
					finalByte = Arrays.copyOfRange(recievedData, 0, recievedPacket.getLength());
					packets.add(finalByte);
				} else {
					packets.add(recievedData);
				}

				sequence = ((sequence + 1) % 2);
				ACK = sequence;
				System.out.println("packet " + (counter + 1) + " is here ");
				System.out.println("Sendig ACk for this packet...");

				InetAddress IPAddress = recievedPacket.getAddress();
				int port = recievedPacket.getPort();
				sentData = Integer.toString(ACK).getBytes();
				DatagramPacket sendPacket = new DatagramPacket(sentData, sentData.length, IPAddress, port);
				this.clientSocket.send(sendPacket);
				System.out.println("ACk is sent");
				counter++;
			}
		}
		this.createFile(packets, "textfile-R.txt");
		System.out.println("FINAL FILE SUCCESSFULLY CREATED !");
	}
}
