package interfaces;

import java.net.InetSocketAddress;
import java.util.ArrayList;

import util.ClientNode;
/*
 * An interface for a number of interfaces used in sending messages.
 */
public interface MessageSend {
	public void sendMessage(String message, String dest);
	public ArrayList<ClientNode> getClients();
	public void sendImage(byte[] image, String dest);
	public String getClientId();
}
