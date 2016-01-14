package Network;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Queue;

import javax.imageio.ImageIO;

import GUI.GUI;
import Network.*;
import interfaces.*;
import json.JsonObject;
import json.Parser;
import util.ClientNode;
import util.ImageMessage;
import util.Message;

public class Client extends Node implements FinishedSending, MessageSend, MessageRead {
	public static final int DEFAULT_PORT = 50000;
	String toSend;
	boolean sending = false;
	ArrayList<Message> pendingMessages;
	ArrayList<ClientNode> clients;
	GUI gui;
	boolean isLaptop = false;
	Receiver receiver;
	String id;
	public Client(String id, boolean isLaptop) {
		this(DEFAULT_PORT, id);
		this.isLaptop = isLaptop;
	}
	
	public Client(String id, int port, boolean isLaptop) {
		this(port, id);
		this.isLaptop = isLaptop;
		if(isLaptop)
			try {
				socket.setBroadcast(true);
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public Client(int port, String id) {
		super();
		try {
			this.id = id;
			pendingMessages = new ArrayList<Message>();
			clients = new ArrayList<ClientNode>();
			gui = new GUI(this);
			socket = new DatagramSocket(port);
			l.out("start ADDRESS: " + socket.getLocalSocketAddress());
			l.out("Starting listener on port: " + port);
			listener.go();
			receiver = new Receiver(this);
		} catch(Exception e) {
			l.err(e.getClass() + "");
			l.err(e.getMessage() + "");
		}
	}
	
	public synchronized void onReceipt(DatagramPacket packet) {
		JsonObject o = Parser.parse(new String(packet.getData()));
		
		if(o.get("request") != null) {
			handleRequest(packet.getSocketAddress());
		}
			
		if(o.get("message") != null || o.get("end") != null || o.get("image") != null) {
				receiver.add(packet);
			try {
				socket.send(new DatagramPacket("OK".getBytes(), 2, packet.getSocketAddress()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		latch.countDown();
		this.notify();
	}
	
	public void handleRequest(SocketAddress addr) {
		l.out("REMOTE ADDRESS: " + socket.getRemoteSocketAddress());
	}
	
	public static void main(String args[]) {
		Client c = new Client("L1", true);
		c.clients.add(new ClientNode(new InetSocketAddress("localhost", 50000), "L1"));
		c.start();
	}

	@Override
	public void sendingFinished(boolean success) {
		l.out("REMOTE ADDRESS: " + socket.getRemoteSocketAddress());
		// So this will be used to signify that a sender has finished
		l.out("Sending message " + (success == true ? "completed successfully" : "failed"));
		sending = false;
		if(pendingMessages.size() > 0) {
			System.out.println(pendingMessages.size());
			Message m = pendingMessages.get(0);
			pendingMessages.remove(0);
			if(m instanceof ImageMessage) {
				sendImage((ImageMessage) m);
				return;
			}
			Sender sender = new Sender(m.dest, m.message, m.recipient, this.id, this);
			sender.start();
		}
	}
	
	public void sendImage(ImageMessage m) {
		try {
			FileOutputStream fos = new FileOutputStream("pathname.jpg");
			fos.write(m.image);
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		(new Sender(m.dest,m.image, m.recipient, m.sender, this)).start();
	}
	
	
	public void sendImage(byte[] image, String dest) {
		SocketAddress addr = lookupClient(dest);
		if(addr == null)
			return;
		
		(new Sender(addr, image, dest, this.id, this)).start();
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
		if(message.equals("image")) {
			byte[] testImage = null;
			String imagePath = "kawaii.jpg";
			try {
				BufferedImage originalImage = ImageIO.read(new File(imagePath));
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(originalImage, "jpg", baos);
				baos.flush();
				testImage = baos.toByteArray();
				baos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			(new Sender(dest, testImage, "L1", "L1", this)).start();
			return;
		}
		if (!sending) {
			sending = true;
			l.out("Attempting to send message: " + message);
			Sender sender;
			sender = new Sender(dest, message,"L1", id, this);
			sender.start();
		} else {
			Message m = new Message(message, dest, "L1", id);
			pendingMessages.add(m);
		}
	}

	@Override
	public void messageReceived(String message, String sender) {
		gui.receivedMessage(message, sender);
	}

	@Override
	public String getClientId() {
		return id;
	}

	@Override
	public void forwardMessage(SocketAddress dest, String message, String recipient, String sender) {
		l.out("in client its: " + dest.toString());
		(new Sender(dest, message, recipient, sender, this)).start();
	}
	
	public SocketAddress lookupClient(String client) {
		for(int i = 0 ; i < clients.size();i++) {
			if(clients.get(i).id.equals(client))
				return clients.get(i).address;
		}
		return null;
	}
	
	public void forwardImage(String dest, byte[] message, String sender) {
		l.out("forwarding message");
		SocketAddress client = lookupClient(dest);
		if(client == null)
			return;
		(new Sender(client, message, dest, sender, this)).start();
	}
	
	public void addImage(byte[] image) {
		l.out("adding image");
		gui.addImage(image);
	}

	@Override
	public boolean isLaptop() {
		return isLaptop;
	}
}
