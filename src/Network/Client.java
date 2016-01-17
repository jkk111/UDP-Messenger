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

public class Client extends Node implements FinishedSending, MessageSend, MessageRead, ClientUpdater {
	public static final int DEFAULT_PORT = 50000;
	String toSend;
	boolean sending = false;
	ArrayList<Message> pendingMessages;
	ArrayList<ClientNode> clients;
	GUI gui;
	boolean isLaptop = false;
	Receiver receiver;
	String id;
	int laptops, phones;
	public Client(String id, boolean isLaptop, ArrayList<ClientNode> clients) {
		this(DEFAULT_PORT, id, isLaptop, clients);
	}
	
	public Client(int port, String id, boolean isLaptop, ArrayList<ClientNode> clients) {
		super();
		this.isLaptop = isLaptop;
		this.clients = clients;
		this.id = id;
		pendingMessages = new ArrayList<Message>();
		try {
			socket = new DatagramSocket(port);
			if(isLaptop) {
				socket.setBroadcast(true);
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			gui = new GUI(this);
			l.out("start ADDRESS: " + socket.getLocalSocketAddress());
			l.out("Starting listener on port: " + port);
			listener.go();
			receiver = new Receiver(this);
		} catch(Exception e) {
			l.err(e.getClass() + "");
			l.err(e.getMessage() + "");
		}
		if(isLaptop) {
			laptops = Integer.parseInt(id.substring(1)) + 1;
			int highestId = 0;
			for(int i = 0 ; i < clients.size(); i++) {
				ClientNode client = clients.get(i);
				if(client.id.startsWith("P")) {
					int clientId = Integer.parseInt(client.id.substring(1));
					highestId = Math.max(clientId, highestId);
				}
			}
			phones = highestId + 1;
			resolveServers();
		} else {

			int highestId = 0;
			for(int i = 0 ; i < clients.size(); i++) {
				ClientNode client = clients.get(i);
				if(client.id.startsWith("L")) {
					int clientId = Integer.parseInt(client.id.substring(1));
					highestId = Math.max(clientId, highestId);
				}
			}
			laptops = highestId + 1;
			phones = Integer.parseInt(id.substring(1));
		}
	}
	
	public void resolveServers() {
		// we need to send a resolve packet to get source of server,
		// attempt to connect to it
		// if works update value
		// else route through server
		// if phone route though server regardless
		for(int i = 1 ; i < clients.size(); i++) {
			ClientNode client = clients.get(i);
			(new Resolver(client.address, client.id, this)).start();
		}
	}
	
	public synchronized void onReceipt(DatagramPacket packet) {
		JsonObject o = Parser.parse(new String(packet.getData()));
		
		if(o.get("request") != null) {
			handleRequest(packet.getSocketAddress() , o.get("request").equals("laptop"));
		}
		
		if(o.get("resolve") != null) {
			String req = o.get("resolve");
			for(int i = 0 ; i < clients.size(); i++) {
				ClientNode client = clients.get(i);
				if(client.id.equals(req)) {
					o = new JsonObject();
					l.out("found client" + client.id);
					o.add("addr", ((InetSocketAddress)client.address).getHostString());
					try {
						socket.send(new DatagramPacket(o.toString().getBytes(), o.toString().length(), packet.getSocketAddress()));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		if(o.get("PING") != null) {
			try {
				// This should probably be changed so it doesn't respond until it gets a reply from the source, so if ( id == source id) respond
				// will implement saturday evening
				socket.send(new DatagramPacket("PONG".getBytes(), 4, packet.getSocketAddress()));
				// here we should forward on the ping and the sender and dest and return a pong when it reaches the end
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
			
		if(o.get("message") != null || o.get("end") != null || o.get("image") != null) {
				receiver.add(packet);
			try {
				socket.send(new DatagramPacket("OK".getBytes(), 2, packet.getSocketAddress()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		latch.countDown();
		this.notify();
	}
	
	// Gives a list of clients as "L1,L2,L3,L4,C1,C2,C3,L5"
	public String getClientsAsString() {
		String res = "";
		for(int i = 0; i < clients.size(); i++) {
			if(i > 0)
				res += ",";
			res += clients.get(i).id;
		}
		return res;
	}
	
	public void distanceVectorRouting() {
		// eric plz
	}
	
	public void linkStateRouting() {
		// tom do the thing -- or at least describe it in heavy detail
	}
	
	public void handleRequest(SocketAddress addr, boolean isLaptop) {
		l.out("adding new client");
		JsonObject o = new JsonObject();
		o.add("sender", this.id);
		if(isLaptop) {
			clients.add(new ClientNode(addr, "L" + laptops));
			o.add("id", "L"+ laptops++);
		} else {
			clients.add(new ClientNode(addr, "P" + phones));
			o.add("id", "P"+ phones++);
		}
		o.add("clients", getClientsAsString());
		String m = o.toString();
		// Send from host as opposed to sender so the client can get our socketaddress
		DatagramPacket p = new DatagramPacket(m.getBytes(), m.length(), addr);
		try {
			socket.send(p);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]) {
		Client c = new Client("L0", true, new ArrayList<ClientNode>());
		c.start();
	}

	@Override
	public void sendingFinished(boolean success) {
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
		SocketAddress addr;
		if(dest.equals(this.id))
			addr = new InetSocketAddress("localhost", 50000);
		else
			addr = lookupClient(dest);
		if(addr == null)
			return;
		
		(new Sender(addr, image, dest, this.id, this)).start();
	}
	
	public ArrayList<ClientNode> getClients() {
		ArrayList<ClientNode> clients = new ArrayList<ClientNode>();
		clients.add(new ClientNode(new InetSocketAddress("localhost", 50000), this.id));
		clients.addAll(this.clients);
		return clients;
	}

	@Override
	public void sendMessage(String message, InetSocketAddress addr, String dest) {
		if (!sending) {
			sending = true;
			l.out("Attempting to send message: " + message);
			Sender sender;
			sender = new Sender(addr, message,dest, id, this);
			sender.start();
		} else {
			Message m = new Message(message, addr, dest, id);
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

	@Override
	public void updateClient(InetSocketAddress addr, String dest) {
		for(int i = 0 ; i < clients.size(); i++) {
			ClientNode client = clients.get(i);
			if(client.id.equals(dest)) {
				client.address = addr;
			}
		}
	}
}
