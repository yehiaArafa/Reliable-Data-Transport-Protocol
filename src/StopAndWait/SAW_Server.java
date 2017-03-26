package StopAndWait;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Random;

import Main.template;

public class SAW_Server extends template {

	// private DatagramSocket serverSocket;

	private String fileName;
	private String ACK;
	private int sequence;

	ArrayList<byte[]> packets = new ArrayList();
	private DatagramSocket serverSocket;
	private double plp;
	InetAddress IPAdress;

	public SAW_Server(String fileName, DatagramSocket serverSocket, double plp, int sizeOfDatagram) throws IOException {

		this.fileName = fileName;
		this.serverSocket = serverSocket;
		packets = this.createPackets(sizeOfDatagram, this.fileName);
		this.plp = Math.ceil(plp * packets.size());

	}

	public void start() throws SocketException, UnknownHostException {

		this.sequence = 0;
		int currentPacket = 0;
		Random rand = new Random();

		byte[] sentData = new byte[1024];
		byte[] recievedData = new byte[1024];
		ByteBuffer b;

		IPAdress = InetAddress.getByName("localhost");

		Boolean[] choosenPackets = new Boolean[packets.size()];
		for (int j = 0; j < packets.size(); j++) {
			choosenPackets[j] = false;
		}

		Boolean[] flag = new Boolean[packets.size()];
		for (int j = 0; j < packets.size(); j++) {
			flag[j] = true;
		}

		for (int j = 0; j < (int) this.plp; j++) {
			int temp = rand.nextInt(packets.size() + 1);
			choosenPackets[temp] = true;
		}

		while (currentPacket <= packets.size()) {

			try {

				if (currentPacket == packets.size()) {

					String end = "End-";
					byte[] endConnection = end.getBytes();
					DatagramPacket sendPacket = new DatagramPacket(endConnection, endConnection.length, IPAdress, 4445);
					serverSocket.send(sendPacket);
					break;

				}

				else {

					Thread.sleep(1000);
					System.out.println("**Ready to send packet " + (currentPacket + 1) + "**");
					Thread.sleep(1000);

					/* if it a loss packet */
					if (choosenPackets[currentPacket] && flag[currentPacket]) {
						System.out.println("Packet " + (currentPacket + 1) + " is sent");// ........
																							// will
																							// not
																							// send
																							// nothing
						flag[currentPacket] = false;

					} else {

						/* if it is a non loss packet */

						// b = ByteBuffer.allocate(4);
						// b.putInt(sequence);
						// sentData=b.array();
						/*
						 * System.out.println("**"+sentData[0]);
						 * System.out.println("**"+sentData[1]);
						 * System.out.println("**"+sentData[2]);
						 * System.out.println("**"+sentData[3]);
						 * 
						 */

						// System.out.println("**"+sentData);

						byte[] packet = packets.get(currentPacket);

						DatagramPacket sendPacket = new DatagramPacket(packet, packet.length, IPAdress, 4445);
						serverSocket.send(sendPacket);

						sequence = ((sequence + 1) % 2);

						System.out.println("Packet " + (currentPacket + 1) + " is sent");

					}

					System.out.println("Waitng for ACK...");

					try {
						serverSocket.setSoTimeout(3000);
						DatagramPacket recievedPacket = new DatagramPacket(recievedData, recievedData.length);
						serverSocket.receive(recievedPacket);
						ACK = new String(recievedPacket.getData());
						System.out.println("ACK for Packet " + (currentPacket + 1) + " is here ");
						currentPacket++; /* next packet */

					}

					catch (SocketTimeoutException e) {
						System.out.println("Time-Out--> Re-Sending the same packet");
						continue;
					}

				}
			} catch (Exception e) {
			}
		}

		serverSocket.close();

	}

}
