package interfaces;

import java.net.SocketAddress;

public interface MessageRead {
	public void forwardMessage(SocketAddress dest, String message, String recipient, String sender);
	public void messageReceived(String message, String sender);
	public String getClientId();
	public boolean isLaptop();
	public void addImage(byte[] image);
	public void forwardImage(String dest, byte[] image, String sender);
}
