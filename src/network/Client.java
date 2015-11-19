package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Enumeration;

import util.Logger;

public class Client extends Thread {
	DatagramSocket socket;
	Logger log;
	boolean connected = false;
	SocketAddress server;
	
	public void broadcast() {
		try {
			byte[] message = "CONNECTION_REQUEST".getBytes();
			Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
			while(interfaces.hasMoreElements()) {
			    NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();
				if(networkInterface.isLoopback() || !networkInterface.isUp())
					continue;
				for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
					InetAddress broadcast = interfaceAddress.getBroadcast();
					if(broadcast == null)
						continue;
					DatagramPacket packet = new DatagramPacket(message, message.length, broadcast, 1337);
					try {
						socket.send(packet);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (SocketException e) {
			log.err(e.getMessage());
		}
	}
	
	public void onReceipt(DatagramPacket packet) {
		if(packet != null) {
			String message = new String(packet.getData()).trim();
			log.out("received: " + message);
		}
	}
	
	public void receive() {
		int attempts = 0;
		while(attempts < 5) {
			log.out("attempting to receive packet from the server");
			byte[] buffer = new byte[60000];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			try {
				attempts++;
				socket.receive(packet);
				onReceipt(packet);
				server = packet.getSocketAddress();
				attempts = 5;
				connected = true;
			} catch (IOException e){}
		}
	}
	
	
	
	public void run() {
		// Find the server using UDP broadcast
		  //Open a random port to send the package
		try {
		  socket = new DatagramSocket();
		  socket.setBroadcast(true);
		  socket.setSoTimeout(1000);
		  log = new Logger();
		} catch(Exception e) {
			e.printStackTrace();
		}int i =0;
		while( i<10) {
			connected = false;
		while(!connected) {
			broadcast();
			receive();
		}
		byte[] message = "hello world".getBytes();
		DatagramPacket test = new DatagramPacket(message, message.length, server);
		try {
			socket.send(test);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			Thread.sleep(1000L);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			break;
		}
		i++;
	}
		log.close();
	}
	
	public static void main(String[] args) {
		Client c = new Client();
		c.start();
	}
}
