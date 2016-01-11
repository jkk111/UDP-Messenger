package Network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Queue;

import Network.*;
import util.sendingFinished;

public class Client extends Node implements sendingFinished {
	public static final int DEFAULT_PORT = 50000;
	String toSend;
	boolean sending = false;
	
	public Client() {
		this(DEFAULT_PORT);
	}
	
	public Client(int port) {
		super();
		try {
			socket = new DatagramSocket(port);
			socket.setSoTimeout(1000);
			l.out("Starting listener on port: " + DEFAULT_PORT);
			listener.go();
		} catch(Exception e) {}
	}	
	
	public void send(String message) throws SocketException {
		l.out("sending some shit to the server");
		InetSocketAddress dest = new InetSocketAddress("localhost", 50001);
		Sender sender = new Sender(dest, message, socket, this);
		sender.start();
	}
	
	public synchronized void onReceipt(DatagramPacket packet) {
		if(packet != null) {
			String message = new String(packet.getData());
			l.out("Message Received: " + message);
		} else {
			System.out.println("heil");
		}
		this.notify();
	}
	
	public synchronized void run() {
		while(true) {
			try {
				latch.await();
				if(toSend != null) {
					send(toSend);
					toSend = null;
				}
			} catch (InterruptedException | SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String args[]) {
		Client c = new Client();
		c.toSend = "hey nigger";
		c.start();
	}

	@Override
	public void finishedSending(boolean success) {
		// So this will be used to signify that a sender has finished
		System.out.println("hello world" + success +":" + DEFAULT_PORT);
	}
}
