package Network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import json.JsonObject;
import json.Parser;

public class Laptop extends Thread {
	String id;
	Client client;
	DatagramSocket listener;
	int lId = 0;
	int pId = 0;
	public Laptop(String id) {
		this.id = id;
		client = new Client(id, true);
		client.start();
	}
	
	public static void main(String[] args) {
		DatagramSocket s = null;
		try {
			s = new DatagramSocket();
			s.setBroadcast(true);
			s.setSoTimeout(1000);
			String registerString = "{ \"type\": \"request\", \"request\": \"laptop\" }";
			DatagramPacket p = new DatagramPacket(registerString.getBytes(), registerString.length(), InetAddress.getByName("255.255.255.255"), 50000);
			s.send(p);
			DatagramPacket response = new DatagramPacket(new byte[65536], 65536);
			s.receive(response);
			s.close();
			String id = Parser.parse(new String(response.getData())).get("id");
			Laptop l = new Laptop(id);
			l.start();
		} catch(SocketTimeoutException e) {
			System.out.println("Could not find a server, Registering as first server");
			s.close();
			Laptop l = new Laptop("L0");
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
