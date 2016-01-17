package Network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Date;

import interfaces.PingClient;
import json.JsonObject;

public class Pinger extends Thread {
	DatagramSocket socket;
	DatagramPacket nextPacket;
	SocketAddress addr;
	String dest;
	static final int MAX_TIMEOUTS = 10;
	boolean gotResponse = false;
	PingClient parent;
	public Pinger(SocketAddress addr, String dest, PingClient p) {
		try {
			socket = new DatagramSocket();
			socket.setSoTimeout(10000);
			this.dest = dest;
			this.addr = addr;
			parent = p;
			JsonObject o = new JsonObject();
			o.add("PING", dest);
			nextPacket = new DatagramPacket(o.toString().getBytes(), o.toString().length(), addr);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		int timeouts = 0;
		Date start = new Date();
		while(timeouts < MAX_TIMEOUTS && !gotResponse) {
			try {
				socket.send(nextPacket);
				DatagramPacket p = new DatagramPacket(new byte[65536], 65536);
				socket.receive(p);
				gotResponse = true;
			} catch(SocketTimeoutException e) {
				timeouts++;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(timeouts < MAX_TIMEOUTS) {
			int timeElapsed = (int) (new Date().getTime() - start.getTime());
			parent.pingComplete(dest, timeElapsed);
		}
	}
}
