package Network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.CountDownLatch;

import util.Logger;

public abstract class Node extends Thread {
	CountDownLatch latch;
	DatagramSocket socket;
	boolean awaitingResponse = false;
	Logger l;
	Listener listener;
	public Node() {
		l = new Logger();
		latch = new CountDownLatch(1);
		listener = new Listener();
		listener.start();
	}
	
	public abstract void onReceipt(DatagramPacket packet);
	
	class Listener extends Thread {
		public void go() {
			latch.countDown();
		}
		
		public void run() {
			while(true) {
				DatagramPacket packet = new DatagramPacket(new byte[65536], 65536);
				try {
					latch.await();
					l.out("waiting for message from client");
					socket.receive(packet);
					l.out("received message from client");
					l.out("Packet contents: " + new String(packet.getData()).trim());
					onReceipt(packet);
				} catch (Exception e) {
					e.printStackTrace();
					l.err(e.getMessage());
					if(!awaitingResponse) {
						l.out("nothing");
						onReceipt(null);
					} else {
						l.out("timeout");
						packet = new DatagramPacket("TIMEOUT".getBytes(), 7);
						onReceipt(packet);
					}
				}
			}
		}
	}
	
}
