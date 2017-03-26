package SelectiveRepeat;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import Main.template;

public class SR_Server extends template {

	// WINDOW INFO
	int ws = 4;
	int currentPacket = 0;

	// FILE DATA AND PACKETS INFO
	File sourcefile;
	public ArrayList packets;
	byte[] recievedData = new byte[1024];

	// CONNECTION INFO
	private DatagramSocket serverSocket;

	// ACK BOOLEANS
	boolean[] acks;
	
	// PACKETS BOOLEANS
	boolean[] toSend;
	
	//Probability
	double probability;
	int tlp;

	public SR_Server(String sourcefile, int sizeOfDatagram, DatagramSocket serverSocket,double lp) throws IOException {
		this.serverSocket = serverSocket;
		this.packets = this.createPackets(sizeOfDatagram, sourcefile); // 4KB
																	   // CHUNKS
																	   // FILESIZE
		this.acks = new boolean[this.packets.size() + 1];
		this.toSend = new boolean[this.packets.size() + 1];
		Arrays.fill(acks, Boolean.FALSE);
		Arrays.fill(toSend, Boolean.TRUE);
		this.probability = lp;
		tlp = (int)Math.ceil(lp*packets.size());
		Random rn = new Random();
		for(int i=0;i<tlp;i++) {
			int temp = rn.nextInt(packets.size()+1); //To ensure always first one sent
			toSend[temp] = false;
		}
	}

	public void start() throws IOException, InterruptedException {
		System.out.println("Selective repeated protocol chosen and server started.\n");
		System.out.println("Please make sure that the client is ready and listening.\n");

		// Initially sent with app startup
		while (currentPacket <= packets.size()) {
			if (currentPacket == packets.size()) {
				byte[] endByte = new byte[0];
				InetAddress IPAdress = InetAddress.getByName("localhost");
				DatagramPacket sendPacket = new DatagramPacket(endByte, endByte.length, IPAdress, 4445);
				serverSocket.send(sendPacket);
				break;
			} else {
				Thread.sleep(1000);
				for (int i = currentPacket; i < currentPacket + ws; i++) {
					if (i >= packets.size())
						break;
					else if (!acks[i] && toSend[i]) {
						sendPacket(i);
					} else if(!toSend[i]) {
						System.out.println("Package " + i + " is lost. Packet will be resent.");
						Thread.sleep(200);
						toSend[i]=true;
					}
				}
			}
			updateCurrentPacket();
		}
	}

	public void sendPacket(final int packnumber) throws IOException {
		new Thread() {
			public void run() {
				try {
					int inc;
					// START OF PACK SENT
					InetAddress IPAdress = InetAddress.getByName("localhost");
					byte[] data = (byte [])packets.get(packnumber);
					BigInteger pckno = BigInteger.valueOf(packnumber);
					byte sequenceNumber = pckno.byteValueExact();
					byte[] packet = new byte[data.length+1];
					for(inc=0;inc<data.length;inc++) {
						packet[inc] = data[inc];
					}
					packet[inc]=sequenceNumber;
					DatagramPacket sendPacket = new DatagramPacket(packet, packet.length, IPAdress, 4445);
					serverSocket.send(sendPacket);
					// END OF PACK SENT

					// NOW TIME FOR ACK RECIEVE
					serverSocket.setSoTimeout(2000);
					DatagramPacket recievedPacket = new DatagramPacket(recievedData, recievedData.length);
					serverSocket.receive(recievedPacket);
					String response = new String(recievedPacket.getData());
					acks[packnumber] = true;
					System.out.println("Package " + packnumber + " is sent.");
					// END OF ACK RECIEVE
				} catch (IOException e) {
					System.out.println(
							"Packet '" + packnumber + "' acknowldge has not been recieved. Package will be resent.");
				}
			}
		}.start();
	}

	public void updateCurrentPacket() {
		while (acks[currentPacket])
			currentPacket++;
	}

}
