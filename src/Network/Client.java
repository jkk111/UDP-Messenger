package Network;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
import util.extendedLog;

public class Client extends Node implements FinishedSending, MessageSend, MessageRead, ClientUpdater, PingClient {
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
	boolean amMarked = false;
	extendedLog el;
	String localSubnet = "192.168.0.255";
	boolean lsr = false;
	public Client(String id, boolean isLaptop, ArrayList<ClientNode> clients, String subnet, boolean useLSR) {
		this(DEFAULT_PORT, id, isLaptop, clients);
		this.localSubnet = subnet;
		l.out(subnet);
		l.out(clients.toString());
		l.out(id+isLaptop+clients.toString()+subnet);
		this.lsr = useLSR;
	}
	
	public Client(String id, boolean isLaptop, ArrayList<ClientNode> clients) {
		this(DEFAULT_PORT, id, isLaptop, clients);
	}
	
	public Client(int port, String id, boolean isLaptop, ArrayList<ClientNode> clients) {
		super();
		this.isLaptop = isLaptop;
		this.clients = clients;
		this.id = id;
		el = new extendedLog();
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
			if(highestId > 0)
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
	
	public void updateClientAdjacent(String client, ArrayList<ClientNode> adjacent) {
		for(int i = 0 ; i < clients.size(); i++) {
			if(clients.get(i).id.equals(client)) {
				clients.get(i).setAdjacent(adjacent);
			}
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
	
	public void checkAllMarked() {
		for(int i = 0 ; i < clients.size(); i++) {
			if(clients.get(i).id.startsWith("L") && (!clients.get(i).LSRReceived || !clients.get(i).DVRReceived)) {
				return;
			}
		}
		ArrayList<ClientNode> newOrder = new ArrayList<ClientNode>();
		if(lsr) {
			for(int i = 0 ; i < clients.size(); i++) {
				if(sameSubnet(localSubnet, clients.get(i).address +"")) {
					newOrder.add(clients.get(i));
				}
			}
			buildTable(newOrder);
		} else {
			buildTableDVR(newOrder);
		}
		clients = newOrder;
	}
	
	public void buildTable(ArrayList<ClientNode> newOrder) {
		boolean added = false;
		do {
			added = false;
			for(ClientNode node : newOrder) {
				for(ClientNode child : node.adjacent) {
					if(!inList(newOrder, child)) {
						newOrder.add(child);
						added = true;
					}
				}
			}
		} while (added);
	}
	
	public void buildTableDVR(ArrayList<ClientNode> newOrder) {
		
	}
	
	public boolean inList(ArrayList<ClientNode> list, ClientNode child) {
		for(int i = 0; i < list.size(); i++) {
			if(list.get(i).id.equals(child.id)) {
				return true;
			}
		}
		return false;
	}
	
	public ClientNode getClient(String id) {
		for(int i = 0 ; i < clients.size(); i++) {
			if(clients.get(i).id.equals(id)) {
				return clients.get(i);
			}
		}
		return null;
	}
	
	public synchronized void onReceipt(DatagramPacket packet) {
		
		/*
		 * if packet is a message, do whatever it already does,
		 *  else (when the packet is an lsr packet/should be the only other type of packet/so far anyway)
		 *  	send my list to all neighbours	
		 *  	if (sender already marked){
		 *  		do nothing
		 *  	} else {
		 *  		mark sender
		 *  		store received list
		 *  		forward packet to all neighbours
		 * 		}
		 */
		
		JsonObject o = Parser.parse(new String(packet.getData()));
		if(o.get("dvr") != null) {
			String sender = o.get("sender");
			if(this.id.equals(sender) && amMarked || !this.id.equals(sender) && getClient(sender).DVRReceived && getClient(sender).lastData.equals(o.get("dvr")))
				return;
			amMarked = true;
			markClient(sender);
			ArrayList<SocketAddress> directlyConnected = new ArrayList<SocketAddress>();
			for (int i = 0; i < clients.size(); i++) {
				if(clients.get(i).id.equals(this.id))
						continue;
				if(sameSubnet(clients.get(i).address + "", localSubnet)) {
					packet.setSocketAddress(clients.get(i).address);
					(new Echo(packet)).start();
				}		
			}
			String[] clients = o.get("dvr").split(",");
			ArrayList<ClientNode> adjacent = new ArrayList<ClientNode>();
			for(int i = 0 ; i < clients.length; i++) {
				String[] parts = clients[i].split(":");
				String id = parts[i];
				int pingTime = Integer.parseInt(parts[1]);
				ClientNode tmp = new ClientNode(lookupClient(id), id);
				tmp.pingTime = pingTime;
				adjacent.add(tmp);
			}
			getClient(sender).adjacent = adjacent;
			distanceVectorRouting();
			checkAllMarked();
		}
		if(o.get("lsr") != null) {
			String sender = o.get("sender");
			if(this.id.equals(sender) && amMarked || getClient(sender).LSRReceived && getClient(sender).lastData.equals(o.get("lsr")) )
				return;
			amMarked = true;
			markClient(sender);
			l.out(sender);
			ArrayList<SocketAddress> directlyConnected = new ArrayList<SocketAddress>();
			for (int i = 0; i < clients.size(); i++) {
				if(clients.get(i).id.equals(this.id))
						continue;
				if(sameSubnet(clients.get(i).address + "", localSubnet)) {
					packet.setSocketAddress(clients.get(i).address);
					(new Echo(packet)).start();
				}		
			}
			String[] clients = o.get("lsr").split(",");
			ArrayList<ClientNode> adjacent = new ArrayList<ClientNode>();
			for(int i = 0 ; i < clients.length; i++) {
				adjacent.add(new ClientNode(lookupClient(sender), clients[i]));
			}
			linkStateRouting();
			checkAllMarked();
			
		}
		
		if(o.get("request") != null) {
			l.out("addr: " + packet.getAddress());
			handleRequest(packet.getAddress(), packet.getPort(), o.get("request").equals("laptop"));
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
	
	public ArrayList<ClientNode> getAdjacent() {
		ArrayList<ClientNode> adjacent = new ArrayList<ClientNode>();
		for(int i = 0 ; i < clients.size(); i++) {
			if(sameSubnet(clients.get(i).address+"", localSubnet)) {
				adjacent.add(clients.get(i));
			}
		}
		return adjacent;
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

	public String getClientsAsString(ArrayList<ClientNode> clients) {
		String res = "";
		boolean hasFirst = false;
		for(int i = 0; i < clients.size(); i++) {
			if(clients.get(i).pingTime == 0)
				continue;
			if(hasFirst)
				res += ",";
			hasFirst = true;
			res += clients.get(i).id + ":"+clients.get(i).pingTime;
		}
		return res;
	}
	
	public void distanceVectorRouting() {
		//ping neybahs
		//store latency + ids
		//tell clients how long messages take to each other
		//curl up in corner and cry
		ArrayList<ClientNode> adjacent = getAdjacent();
		for(int i = 0 ; i < adjacent.size(); i++ ) {
			(new Pinger(adjacent.get(i).address, adjacent.get(i).id, this)).start();
		}
		checkAllMarked();
	}
	
	public void linkStateRouting() {
		/* 	Flooding
			When a new client (clientA) joins, client A sends a list of it's neighbours to its neighbours.
			When a client receives a list, it checks if this list has already been received.
			If the list has already been received, it does nothing, if it has not been received,
			it marks this list as received and forwards it to all other neighbours.
			once all of these lists have been exchanged, each client builds its routing table
		*/
		JsonObject o = new JsonObject();
		o.add("sender", this.id);
		ArrayList<SocketAddress> directlyConnected = new ArrayList<SocketAddress>();
		String clientList = "";
		for (int i = 0; i < clients.size(); i++) {
			if(clients.get(i).id.equals(this.id))
					continue;
			if(sameSubnet(clients.get(i).address + "", localSubnet)) {
				if(!clientList.equals("")) 
					clientList +=",";
				clientList += clients.get(i).id;
				directlyConnected.add(clients.get(i).address);
			}		
		}
		o.add("lsr", clientList);
		DatagramPacket p = new DatagramPacket(o.toString().getBytes(), o.toString().length());
		for (int i = 0; i < directlyConnected.size(); i++) {
			p.setSocketAddress(directlyConnected.get(i));
			(new Echo(p)).start();
		}
		checkAllMarked();
	}
	
	public boolean sameSubnet(String first, String second) {
		System.out.println(first +":"+ first);
		String[] firstArray = first.split("\\.");
		String[] secondArray = second.split("\\.");
		if(firstArray.length < 4 || secondArray.length < 4)
			return false;
		return firstArray[2].equals(secondArray[2]);
	}
	
	public void handleRequest(InetAddress addr, int port, boolean isLaptop) {
		l.out("adding new client");
		JsonObject o = new JsonObject();
		o.add("sender", this.id);
		if(isLaptop) {
			clients.add(new ClientNode(new InetSocketAddress(addr, 50000), "L" + laptops));
			o.add("id", "L"+ laptops++);
		} else {
			clients.add(new ClientNode(new InetSocketAddress(addr, 50000), "P" + phones));
			o.add("id", "P"+ phones++);
		}
		o.add("clients", getClientsAsString());
		String m = o.toString();
		// Send from host as opposed to sender so the client can get our socketaddress
		DatagramPacket p = new DatagramPacket(m.getBytes(), m.length(), new InetSocketAddress(addr, port));
		try {
			socket.send(p);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(lsr)
			linkStateRouting();
		else
			distanceVectorRouting();
	}
	
	public static void main(String args[]) {
		Client c = new Client("L0", true, new ArrayList<ClientNode>(), "192.168.0.255", false);
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
	
	public void markClient(String client) {
		for (int i = 0; i < clients.size(); i++) {
			if(clients.get(i).id.equals(client)) {
				clients.get(i).LSRReceived = true;
			}
		}
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

	@Override
	public void pingComplete(String id, int timeTaken) {
		getClient(id).pingTime = timeTaken;
		ArrayList<ClientNode> adjacent = getAdjacent();
		JsonObject o = new JsonObject();
		o.add("sender", this.id);
		o.add("dvr", getClientsAsString(adjacent));
		DatagramPacket p = new DatagramPacket(o.toString().getBytes(), o.toString().length());
		for(int i = 0 ; i < adjacent.size(); i++) {
			p.setSocketAddress(lookupClient(adjacent.get(i).id));
			(new Echo(p)).start();
		}
	}
}
