package Network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import json.JsonObject;
import json.Parser;
import util.ClientNode;
/*
 * Laptop & router module.
 */
public class Laptop extends Thread {
	String id;
	Client client;
	DatagramSocket listener;
	int lId = 0;
	int pId = 0;
	public Laptop(String id, ArrayList<ClientNode> clients, String subnet, boolean useLSR) {
		this.id = id;
		client = new Client(id, true, clients, subnet, useLSR);
		client.start();
	}
	
	public static void main(String[] args) {
		String subnet = JOptionPane.showInputDialog(null, "Please enter a valid subnet eg. 192.168.137.255", "192.168.0.255");
		if(subnet == null)
			System.exit(0);
		String[] options = { "Link State Routing", "Distance Vector Routing" };
		int mode = JOptionPane.showOptionDialog(null, 
		        "Select a routing mode", 
		        "routing mode select", 
		        JOptionPane.OK_CANCEL_OPTION, 
		        JOptionPane.INFORMATION_MESSAGE, 
		        null, 
		        options, // this is the array
		        "default");
		if(mode == -1)
			System.exit(0);
		DatagramSocket s = null;
		try {
			s = new DatagramSocket();
			s.setBroadcast(true);
			s.setSoTimeout(1000);
			String registerString = "{ \"type\": \"request\", \"request\": \"laptop\" }";
			DatagramPacket p = new DatagramPacket(registerString.getBytes(), registerString.length(), InetAddress.getByName(subnet), 50000);
			s.send(p);
			DatagramPacket response = new DatagramPacket(new byte[65536], 65536);
			s.receive(response);
			s.send(new DatagramPacket("OK".getBytes(), 2, response.getSocketAddress()));
			JsonObject o = Parser.parse(new String(response.getData()));
			String id = o.get("id");
			String[] clientIds = o.get("clients").split(",");
			ArrayList<ClientNode> clients = new ArrayList<ClientNode>();
			clients.add(new ClientNode(response.getSocketAddress(), o.get("sender")));
			for(int i = 0 ; i < clientIds.length; i++) {
				clients.add(new ClientNode(response.getSocketAddress(), clientIds[i]));
			}
			s.setBroadcast(false);
			s.close();
			Laptop l = new Laptop(id, clients, subnet, mode == 0);
			l.start();
		} catch(SocketTimeoutException e) {
			System.out.println("Could not find a server, Registering as first server");
			try {
				s.setBroadcast(false);
			} catch (SocketException e1) {
				e1.printStackTrace();
			}
			s.close();
			Laptop l = new Laptop("L0", new ArrayList<ClientNode>(), subnet, mode == 0);
			l.start();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
