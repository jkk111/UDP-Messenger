package Network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import util.Logger;
import util.sendingFinished;

public class Sender extends Thread {
	public static final int MAX_PACKET_SIZE = 128;
	public static final int MAX_TIMEOUTS = 10;
	DatagramSocket socket;
	DatagramPacket packet;
	CountDownLatch latch;
	AtomicBoolean receivedResponse;
	String content;
	boolean resend;
	boolean isFinal;
	sendingFinished onFinish;
	Logger l;
	int index = 0;
	int packetsSent = 0;
	int timeouts = 0;
	InetSocketAddress dest;
	public Sender(InetSocketAddress destination, String content) throws SocketException {
		this(destination, content, new DatagramSocket());
	}
	
	public Sender(InetSocketAddress destination, String content, DatagramSocket socket) throws SocketException {
		latch = new CountDownLatch(1);
		resend = false;
		isFinal = false;
		this.socket = socket;
		socket.setSoTimeout(1000);
		this.content = content;
		l = new Logger();
		this.dest = destination;
	}
	
	public Sender(InetSocketAddress destination, String content, DatagramSocket socket, sendingFinished f) throws SocketException {
		this(destination, content, socket);
		onFinish = f;
	}
	
	public void run() {
		l.out("Attempting to send string: \"" + content +"\" to: " + dest.toString());
		while ((index < content.length() || resend || isFinal ) && timeouts < MAX_TIMEOUTS) {
			if(!resend && !isFinal) {
				int end = index + MAX_PACKET_SIZE;
				if(end > content.length())
					end = content.length();
				byte[] tmp = content.substring(index, end).getBytes();
				packet = new DatagramPacket(tmp, tmp.length, dest);
			}
			try {
				l.out("Sending packet: " + new String(packet.getData()));
				socket.send(packet);
				DatagramPacket response = new DatagramPacket(new byte[MAX_PACKET_SIZE], MAX_PACKET_SIZE);
				socket.receive(response);
				packetsSent++;
				l.out("Successfully sent " + packetsSent +" packets");
				timeouts = 0;
				resend = false;
				index += MAX_PACKET_SIZE;
				if(index > content.length() && !isFinal) {
					isFinal = true;
					byte[] finalMessage = "END".getBytes();
					packet = new DatagramPacket(finalMessage, finalMessage.length, dest); 
				} else if (isFinal)
					isFinal = false;
			} catch (IOException e) {
				if(e instanceof SocketTimeoutException) {
					l.out("Timeout number " + ++timeouts +" " + new Date().toString());
					resend = true;
					if(timeouts > 10) {
						Thread.currentThread().interrupt();
					}
				} else {
					l.err(e.getMessage());
					e.printStackTrace();
				}
			}
		}
		sendingDone();
	}
	
	public void sendingDone() {
		if(timeouts >= MAX_TIMEOUTS) {
			if(index > 0) {
				l.errout("Sending failed");
			}
			else {
				l.errout("Could not communicate with the remote host");
			}
			if(onFinish != null) {
				onFinish.finishedSending(false);
			}
		} else {
			l.out("Message sent successfully");
			if(onFinish != null) {
				onFinish.finishedSending(true);
			}
		}
		l.close();
	}
	
	public static void main(String[] args) throws SocketException {
		InetSocketAddress tst = new InetSocketAddress("127.0.0.1", 8080);
		new Sender(tst, "Hello cucks of the world").start();
	}
}