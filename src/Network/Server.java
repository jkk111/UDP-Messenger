package Network;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

import util.FinishedSending;

public class Server extends Node implements FinishedSending {
	Hashtable<String, ArrayList<Sender>> connections;
	Hashtable<String, InetSocketAddress> clients;
	public static final int DEFAULT_PORT = 50001;
	String id;
	public Server(String id) throws SocketException {
		super();
		socket = new DatagramSocket(DEFAULT_PORT);
		l.out("Starting listener on port: " + DEFAULT_PORT);
		listener.go();
		this.id = id;
	}
	
	public Server() {
		super();
	}
	
	public void sendingFinished(boolean success) {
		
	}
	
	public synchronized void onReceipt(DatagramPacket packet) {
		if(packet!=null && !packet.equals("timeout") && !packet.equals("Hello")) {
			String response = new String(packet.getData()).trim();
			l.out("Received: " + response +" :" + new Date().toString());
			if(response.equals("Hello"))
				return;
			DatagramPacket res = new DatagramPacket("OK".getBytes(), 2, packet.getSocketAddress());
			try {
				socket.send(res);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) throws SocketException {
		Server test = new Server("S1");
		test.start();
		test.onReceipt(new DatagramPacket("Hello".getBytes(), 5));
	}
}
