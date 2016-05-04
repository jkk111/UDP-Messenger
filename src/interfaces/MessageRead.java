package interfaces;

import java.net.SocketAddress;
/*
 * An interface that provides methods for ensuring messages reach the destination client.
 */
public interface MessageRead {
	public void forwardMessage(SocketAddress dest, String message, String recipient, String sender);
	public void messageReceived(String message, String sender);
	public String getClientId();
	public boolean isLaptop();
	public void addImage(byte[] image);
	public void forwardImage(String dest, byte[] image, String sender);
	public SocketAddress lookupClient(String client);
}
