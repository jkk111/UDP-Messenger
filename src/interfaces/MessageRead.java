package interfaces;

import java.net.SocketAddress;

public interface MessageRead {
	public void forwardMessage(SocketAddress dest, String message, String recipient, String sender);
	public void messageReceived(String message, String sender);
	public String getClientId();
	public boolean isLaptop();
}
