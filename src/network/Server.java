package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import util.Logger;

public class Server extends Thread {
	DatagramSocket socket;
	Logger log;
	public void run() {
		log = new Logger();
		try {
			socket = new DatagramSocket(1337, InetAddress.getByName("0.0.0.0"));
			socket.setBroadcast(true);
			while(true) {
				byte buffer[] = new byte[60000];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				socket.receive(packet);
				log.out("Connection from: " + packet.getAddress().getHostAddress());
				String message = new String(packet.getData()).trim();
				log.out("Message was: " + message);
				if(message.equals("CONNECTION_REQUEST")) {
					byte[] res = ("ACK{'you':'"+ packet.getSocketAddress() +"'}").getBytes();
					DatagramPacket response = new DatagramPacket(res,res.length);
					response.setSocketAddress(packet.getSocketAddress());
					socket.send(response);
				}
			}
		} catch(Exception e) {
			log.err(e.getMessage());
		}
	}
	
	public static void main(String[] args) {
		new Server().start();
	}
}
