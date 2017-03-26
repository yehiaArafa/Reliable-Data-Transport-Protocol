package Main;

import java.net.DatagramSocket;
import java.util.Scanner;

import SelectiveRepeat.SR_Client;
import SelectiveRepeat.SR_Server;
import StopAndWait.SAW_Client;
import StopAndWait.SAW_Server;


public class main {

	private int ARQ;
	private int clientServer;
	private Scanner scan;
	private int seqSize = 1;
	
	public static void main(String[]args) throws Exception{
		
		main m = new main();
		m.function();
		
	}
	
	private void function() throws Exception{
		
		scan=new Scanner(System.in);
		
		//InetSocketAddress address = new InetSocketAddress("localhost", 4444);
		//Socket.bind(address);
		
		System.out.println("Please choose Stop-And-Wait(0) or Selective Repeat(1)");
		this.ARQ = scan.nextInt();

		System.out.println("Please choose CLient(0) or Server(1)");
		this.clientServer = scan.nextInt();

		
		if (ARQ == 0 && clientServer == 1)// SAW + server
		{
			DatagramSocket Socket = new DatagramSocket(4444);
			SAW_Server ss = new SAW_Server("textfile.txt",Socket,0.1,4096);
			ss.start();		
			
		}

		else if (ARQ == 0 && clientServer == 0)// SAW + client
		{
			DatagramSocket Socket = new DatagramSocket(4445);
	   	    SAW_Client sc = new SAW_Client(Socket,4096);
		    sc.start();

		}

		else if (ARQ == 1 && clientServer == 1)// SR + server
		{
			DatagramSocket Socket = new DatagramSocket(4444);
			SR_Server sr = new SR_Server("textfile.txt",5120,Socket,0.2);
			sr.start();
			

		} else if (ARQ == 1 && clientServer == 0)// SR + client
		{
			DatagramSocket Socket = new DatagramSocket(4445);
			SR_Client src = new SR_Client(Socket,5120+1); //+1 is the byte fore the sequence number
			src.start();	
       			
		}
		
		
	}
	
	
}
