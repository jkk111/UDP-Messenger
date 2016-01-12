package util;

import java.net.InetSocketAddress;

public class ClientNode {
	InetSocketAddress address;
	String id;
	public ClientNode(InetSocketAddress address, String id) {
		this.address = address;
		this.id = id;
	}
}
