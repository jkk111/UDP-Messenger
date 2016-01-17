package Network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import util.Logger;

public class Echo extends Thread {
	public static final int MAX_TIMEOUTS = 10;
	Logger l;
	DatagramSocket socket;
	DatagramPacket echoPacket;
	boolean sent = false;
	boolean arq = false;
	public Echo(DatagramPacket packet) {
		echoPacket = packet;
		l = new Logger();
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		int timeouts = 0;
		while(timeouts < MAX_TIMEOUTS && !sent) {
			try {
				l.out("sending echo packet");
				socket.send(echoPacket);
				if(arq) {
					DatagramPacket p = new DatagramPacket(new byte[65536], 65536);
					socket.receive(p);
				}
				sent = true;
				l.out("sent echo packet");
			} catch (SocketTimeoutException e) {
				timeouts++;			
		    } catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
