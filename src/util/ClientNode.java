package util;
import java.net.SocketAddress;

public class ClientNode {
	public SocketAddress address;
	public String id;
	public ClientNode(SocketAddress address, String id) {
		this.address = address;
		this.id = id;
	}
}
