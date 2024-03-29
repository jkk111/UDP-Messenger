package util;

import java.net.InetSocketAddress;
/*
 * Basic message data type used when queuing messages.
 */
public class Message {
	public String message;
	public InetSocketAddress dest;
	public String recipient;
	public String sender;
	public Message(String message, InetSocketAddress dest, String recipient, String sender) {
		this.message = message;
		this.dest = dest;
		this.recipient = recipient;
		this.sender = sender;
	}
}
