package util;
import java.net.SocketAddress;

public class ClientNode {
	SocketAddress address;
	String id;
	public ClientNode(SocketAddress address, String id) {
		this.address = address;
		this.id = id;
	}
}
