package interfaces;

import java.net.InetSocketAddress;
import java.util.ArrayList;

import util.ClientNode;

public interface MessageSend {
	public void sendMessage(String message, InetSocketAddress dest);
	public ArrayList<ClientNode> getClients();
}
