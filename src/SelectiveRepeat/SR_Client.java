package SelectiveRepeat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;

import Main.template;

public class SR_Client extends template {

	private DatagramSocket clientSocket;
	byte[] recievedData;

	// Array of final bytes
	ArrayList<byte[]> packets = new ArrayList();

	// DatagramSize
	int sizeOfDatagram;

	public SR_Client(DatagramSocket clientSocket, int sizeOfDatagram) throws IOException {
		this.clientSocket = clientSocket;
		this.sizeOfDatagram = sizeOfDatagram;
		this.recievedData = new byte[this.sizeOfDatagram];
		System.out.println("This is the reliable UDP reciever.");
	}

	public void start() throws IOException {
		while (!clientSocket.isClosed()) {
			DatagramPacket recievedPacket = new DatagramPacket(recievedData, recievedData.length);
			clientSocket.receive(recievedPacket);
			recievedData = recievedPacket.getData();
			
			if (recievedPacket.getLength() == 0) {
				clientSocket.close();
			} else if (recievedPacket.getLength() < this.sizeOfDatagram) {
				byte[] finalByte = new byte[recievedPacket.getLength()];
				finalByte = Arrays.copyOfRange(recievedData, 0, recievedPacket.getLength()-1);
				Byte seqNumber = recievedData[recievedPacket.getLength()];
				System.out.println("Packet " + seqNumber.intValue() + " received.");
				packets.add(seqNumber.intValue(),finalByte);
				this.ack(recievedPacket.getPort(), recievedPacket.getAddress());
			} else if (recievedPacket.getLength() == this.sizeOfDatagram) {
				Byte seqNumber = recievedData[recievedPacket.getLength()-1];
				System.out.println("Packet " + seqNumber.intValue() + " received.");
				packets.add(Arrays.copyOfRange(recievedData, 0, recievedPacket.getLength()-2));
				this.ack(recievedPacket.getPort(), recievedPacket.getAddress());
			}
			// this.updateWindow((int)recievedData[0]);
		}
		this.createFile(packets, "textfile-R.txt");
		System.out.println("FINAL FILE SUCCESSFULLY CREATED !");
	}

	public void ack(int port, InetAddress IP) throws IOException {
		String ack = "t-";
		byte[] sendAck = ack.getBytes();
		DatagramPacket sentPacket = new DatagramPacket(sendAck, sendAck.length, IP, port);
		clientSocket.send(sentPacket);
	}
}