package util;

import java.net.InetSocketAddress;

public class Message {
	public String message;
	public InetSocketAddress dest;
	public String recipient;
	public Message(String message, InetSocketAddress dest, String recipient) {
		this.message = message;
		this.dest = dest;
		this.recipient = recipient;
	}
}
