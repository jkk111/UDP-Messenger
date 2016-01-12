package util;

import java.net.InetSocketAddress;
import java.util.ArrayList;

public interface MessageSend {
	public void sendMessage(String message, InetSocketAddress dest);
	public ArrayList<ClientNode> getClients();
}
