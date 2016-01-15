package interfaces;

import java.net.InetSocketAddress;
import java.util.ArrayList;

import util.ClientNode;

public interface MessageSend {
	public void sendMessage(String message, InetSocketAddress addr, String dest);
	public ArrayList<ClientNode> getClients();
	public void sendImage(byte[] image, String dest);
}
