package util;

import java.net.InetSocketAddress;
/*
 * Extended version of Message more tailored to images.
 */
public class ImageMessage extends Message {
	public byte[] image;
	public ImageMessage(String message, InetSocketAddress dest, String recipient, String sender, byte[] image) {
		super(message, dest, recipient, sender);
		this.image = image;
	}

}
