package Network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Base64;

import javax.xml.bind.DatatypeConverter;

import interfaces.FinishedSending;
import json.JsonObject;
import util.Logger;

public class Sender extends Thread {
	boolean isLarge = false;
	public static final int MAX_MESSAGE_SIZE = 65000;
	public static final int MAX_TIMEOUTS = 10;
	DatagramSocket socket;
	String message = "";
	DatagramPacket nextPacket;
	String recipient, sender;
	SocketAddress dest;
	FinishedSending onFinish;
	Logger l;
	boolean isImage = false;
	public Sender(SocketAddress dest, DatagramPacket p, FinishedSending f) {
		l = new Logger();
		onFinish = f;
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.dest = dest;
		isLarge = false;
		nextPacket = p;
	}
	
	public Sender(SocketAddress dest, String message, String recipient, String sender, FinishedSending f) {
		l = new Logger();
		l.out("sending message to: " + dest.toString());
		onFinish = f;
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.dest = dest;
		this.recipient = recipient;
		this.sender = sender;
		if(message.length() > MAX_MESSAGE_SIZE) {
			isLarge = true;
			this.message = message;
		} else {
			isLarge = false;
			JsonObject o = new JsonObject();
			o.add("dest", recipient);
			o.add("sender", sender);
			o.add("message", message);
			nextPacket = new DatagramPacket(o.toString().getBytes(), o.toString().length(), dest);
		}
	}
	
	public Sender(SocketAddress dest, byte[] image, String recipient, String sender, FinishedSending f) {
		message = DatatypeConverter.printBase64Binary(image);
		l = new Logger();
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		l.out(""+message.length());
		this.dest = dest;
		this.recipient = recipient;
		this.onFinish = f;
		this.sender = sender;
		isLarge = true;
		isImage = true;
	}
	
	public void run() {
		try {
			socket.setSoTimeout(5000);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		int index = 0;
		int timeouts = 0;
		boolean isFinal = false;
		boolean resend = false;
		boolean smallPending = !isLarge;
		if (isLarge) {
			JsonObject o = new JsonObject();
			o.add("dest", recipient);
	        o.add("sender", sender);
	        int end = Math.min((index + MAX_MESSAGE_SIZE), message.length());
	        String messageFragment = message.substring(index, end);
	        if(!isImage)
	        	o.add("message", messageFragment);
	        else
	        	o.add("image", messageFragment);
	        l.out(messageFragment.length()+"");
	        nextPacket = new DatagramPacket(o.toString().getBytes(), o.toString().length(), dest);
		} 
		while ((index < message.length() || resend || isFinal || smallPending ) && timeouts < MAX_TIMEOUTS) {
			try {
				l.out("sending packet");
				socket.send(nextPacket);
				DatagramPacket res = new DatagramPacket(new byte[65536], 65536);
				socket.receive(res);
				l.out("got response");
				index += MAX_MESSAGE_SIZE;
				l.out(index+":"+message.length());
				timeouts = 0;
				resend = false;
				if (isLarge && index < message.length()) {
					int end = Math.min(index + MAX_MESSAGE_SIZE, message.length());
					String fragment = message.substring(index, end);
					JsonObject o = new JsonObject();
					if(!isImage)
						o.add("message", fragment);
					else
						o.add("image", fragment);
					o.add("dest", recipient);
					o.add("sender", sender);
				    nextPacket = new DatagramPacket(o.toString().getBytes(), o.toString().length(), dest);
				} else if (!isFinal) {
					isFinal = true;
					smallPending = false;
					JsonObject o = new JsonObject();
					o.add("end", "true");
					o.add("dest", recipient);
					o.add("sender", sender);
				    nextPacket = new DatagramPacket(o.toString().getBytes(), o.toString().length(), dest);
				} else if (isFinal) {
					isFinal = false;
					l.out("Should finish now");
				}
			} catch (SocketTimeoutException e) {
				l.out("timeout");
				timeouts++;
				resend = true;
		    } catch (IOException e) {
				e.printStackTrace();
			}
		}
		l.out(dest.toString());
		sendingDone(index, timeouts);
	}
	
	public void sendingDone(int index, int timeouts) {
		if(timeouts >= MAX_TIMEOUTS) {
			if(index > 0 && isLarge) {
				l.errout("Sending failed");
			}
			else {
				l.errout("Could not communicate with the remote host");
			}
			if(onFinish != null) {
				onFinish.sendingFinished(false);
			}
		} else {
			l.out("Message sent successfully");
			if(onFinish != null) {
				onFinish.sendingFinished(true);
			}
		}
	}
}
