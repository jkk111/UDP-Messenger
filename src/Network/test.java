package Network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class test extends Thread {
	public static final int MAX_PACKET_SIZE = 128;
	public static final int MAX_TIMEOUTS = 10;
	DatagramSocket socket;
	DatagramPacket packet;
	CountDownLatch latch;
	AtomicBoolean receivedResponse;
	String content;
	boolean resend;
	int index = 0;
	int timeouts = 0;
	InetSocketAddress dest;
	public test(InetSocketAddress destination, String content) throws SocketException {
		latch = new CountDownLatch(1);
		socket = new DatagramSocket(8080);
		resend = false;
		socket.setSoTimeout(100000);
		this.content = content;
		this.dest = destination;
	}
	
	public void run() {
		while (true) {
			DatagramPacket res = new DatagramPacket(new byte[MAX_PACKET_SIZE], MAX_PACKET_SIZE);
			try {
				socket.receive(res);
				System.out.println(new String(res.getData()).trim());
				packet = new DatagramPacket("OK".getBytes(), 2, res.getSocketAddress());
				socket.send(packet);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) throws SocketException {
		InetSocketAddress tst = new InetSocketAddress("localhost", 8080);
		new test(tst, "Hello cucks of the world").start();
	}
}
