package Network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import json.Parser;

public class Phone {
	String id;
	Client client;
	public Phone(String id) {
		this.id = id;
		client = new Client(id, false);
		client.start();
	}
	
	public static void main(String[] args) {
		try {
			new Phone("a2");
			DatagramSocket s = new DatagramSocket();
			s.setBroadcast(true);
			s.setSoTimeout(5000);
			String registerString = "{ \"request\": \"phone\" }";
			DatagramPacket p = new DatagramPacket(registerString.getBytes(), registerString.length(), InetAddress.getByName("255.255.255.255"), 8888);
			s.send(p);
			DatagramPacket response = new DatagramPacket(new byte[65536], 65536);
			s.receive(response);
			String id = Parser.parse(new String(response.getData())).get("id");
			Phone ph = new Phone(id);
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
