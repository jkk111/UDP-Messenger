package Network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;

import interfaces.ClientUpdater;
import json.JsonObject;
import json.Parser;
import util.Logger;

public class Resolver extends Thread {
	// TODO (john): implement a threaded client resolver
	DatagramSocket socket;
	DatagramPacket nextPacket;
	String dest;
	boolean sent = false;
	static final int MAX_TIMEOUTS = 10;
	ClientUpdater parent;
	Logger l;
	public Resolver(SocketAddress addr, String dest, ClientUpdater p) {
		try {
			this.socket = new DatagramSocket();
			socket.setSoTimeout(1000);
		} catch (IOException e) {
			
		}
		System.out.println(((InetSocketAddress) addr).getHostString());
		this.dest = dest;
		JsonObject o = new JsonObject();
		o.add("resolve", dest);
		nextPacket = new DatagramPacket(o.toString().getBytes(), o.toString().length(), addr);
		parent = p;
		l = new Logger();
	}
	
	public void run() {
		int timeouts = 0;
		InetSocketAddress newAddr = null;
		
		while(timeouts < MAX_TIMEOUTS && !sent) {
			DatagramPacket p = new DatagramPacket(new byte[65536], 65536);
			try {
				socket.send(nextPacket);
				socket.receive(p);
				JsonObject o = Parser.parse(new String(p.getData()));
				newAddr = new InetSocketAddress(o.get("addr"), 50000);
			} catch (SocketTimeoutException e) {
				timeouts++;
				l.out("timeout on original host");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(sent) {
			timeouts = 0;
			JsonObject o = new JsonObject();
			o.add("PING", dest);
			nextPacket = new DatagramPacket(o.toString().getBytes(), o.toString().length(), newAddr);
			sent = false;
			while (timeouts < MAX_TIMEOUTS && !sent) {
				DatagramPacket p = new DatagramPacket(new byte[65536], 65536);
				try {
					socket.send(nextPacket);
					socket.receive(p);
					sent = true;
				} catch (SocketTimeoutException e) {
					timeouts++;
					l.out("timeout on new host");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(timeouts < MAX_TIMEOUTS) {
				parent.updateClient(newAddr, dest);
			}
		}
	}
	
	public static void main(String[] args) {
		(new Resolver(new InetSocketAddress("127.0.0.1", 50000), "L1", null)).start();
	}
}
