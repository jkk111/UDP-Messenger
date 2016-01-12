package Network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Queue;

import GUI.UI;
import Network.*;
import json.Parser;
import util.ClientNode;
import util.FinishedSending;
import util.Message;
import util.MessageSend;

public class Client extends Node implements FinishedSending, MessageSend {
	public static final int DEFAULT_PORT = 50000;
	String toSend;
	boolean sending = false;
	ArrayList<Message> pendingMessages;
	UI gui;
	public Client() {
		this(DEFAULT_PORT);
	}
	
	public Client(int port) {
		super();
		try {
			pendingMessages = new ArrayList<Message>();
			gui = new UI(this);
			socket = new DatagramSocket(port);
			l.out("Starting listener on port: " + DEFAULT_PORT);
			listener.go();
		} catch(Exception e) {}
	}	
	
	public void send(String message) throws SocketException {
		sending = true;
		l.out("sending some shit to the server");
		InetSocketAddress dest = new InetSocketAddress("localhost", 50001);
		Sender sender = new Sender(dest, message, this);
		sender.start();
	}
	
	public synchronized void onReceipt(DatagramPacket packet) {
		if(packet != null && !(new String(packet.getData())).trim().equals("END")) {
			l.err("got here");
			String message = Parser.parse(new String(packet.getData())).get("message");
			gui.receivedMessage(message);
			l.out("Message Received: " + message);
			l.out(packet.getSocketAddress().toString());
			try {
				socket.send(new DatagramPacket("OK".getBytes(), 2, packet.getSocketAddress()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (packet != null) {
			try {
				socket.send(new DatagramPacket("OK".getBytes(), 2, packet.getSocketAddress()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		l.out("unblocking receive loop");
		latch.countDown();
		this.notify();
	}
	
	public synchronized void run() {
		while(true) {
			try {
				if(toSend != null) {
					send(toSend);
					toSend = null;
				}
				this.wait();
			} catch (InterruptedException | SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	public static void main(String args[]) {
		Client c = new Client();
		c.start();
	}

	@Override
	public void sendingFinished(boolean success/* Maybe a message id?*/) {
		// So this will be used to signify that a sender has finished
		l.out("Sending message " + (success == true ? "completed successfully" : "failed"));
		sending = false;
		if(pendingMessages.size() > 0) {
			System.out.println(pendingMessages.size());
			Message m = pendingMessages.get(0);
			pendingMessages.remove(0);
			try {
				Sender sender = new Sender(m.dest, m.message, m.recipient, this);
				sender.start();
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public ArrayList<ClientNode> getClients() {
		ArrayList<ClientNode> clients = new ArrayList<ClientNode>();
		
		return clients;
	}

	@Override
	public void sendMessage(String message, InetSocketAddress dest) {
		// By this point message should be a JSON formatted string { "DEST": "C4", "MESSAGE": "HEY JIM" }
		// The client Doesn't necessarily know where its message must go
		// EG. C1 -> S1 -> S3 -> S4 -> C2
		// But to the client it will look like sending it to its host in this case "S1"
		if (!sending) {
			sending = true;
			l.out("Attempting to send message: " + message);
			Sender sender;
			try {
				sender = new Sender(dest, message, "C2", this);
				sender.start();
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				l.err(e.getMessage());
			}
		} else {
			Message m = new Message(message, dest, "C2");
			pendingMessages.add(m);
		}
	}
}
