package Network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import json.JsonObject;
import json.Parser;
import util.ClientNode;

public class Phone {
	String id;
	Client client;
	public Phone(String id, JsonObject o, SocketAddress hostAddr, String subnet) {
		ArrayList<ClientNode> clients = new ArrayList<ClientNode>();
		clients.add(new ClientNode(hostAddr, o.get("sender")));
		String[] clientIds = o.get("clients").split(",");
		for(int i = 0 ; i < clientIds.length; i++) {
			clients.add(new ClientNode(hostAddr, clientIds[i]));
		}
		this.id = id;
		client = new Client(id, false, clients, subnet);
		client.start();
	}
	/*
	 * For a phone when we get a response, it should set all socket addresses to its parennt/ server
	 */
	public static void main(String[] args) {
		String subnet = JOptionPane.showInputDialog(null, "Please enter a valid subnet eg. 192.168.137.255", "192.168.0.255");
		try {
			DatagramSocket s = new DatagramSocket();
			s.setBroadcast(true);
			s.setSoTimeout(5000);
			String registerString = "{ \"request\": \"phone\" }";
			DatagramPacket p = new DatagramPacket(registerString.getBytes(), registerString.length(), InetAddress.getByName(subnet), 50000);
			s.send(p);
			DatagramPacket response = new DatagramPacket(new byte[65536], 65536);
			s.receive(response);
			s.send(new DatagramPacket("OK".getBytes(), 2, response.getSocketAddress()));
			s.close();
			String id = Parser.parse(new String(response.getData())).get("id");
			Phone ph = new Phone(id, Parser.parse(new String(response.getData())), response.getSocketAddress(), subnet);
		} catch(SocketTimeoutException e) {
			System.err.println("Could not find a server, please start a laptop then try again");
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
