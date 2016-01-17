package util;
import java.net.SocketAddress;
import java.util.ArrayList;

public class ClientNode {
	public SocketAddress address;
	public String id;
	public boolean LSRReceived = false;
	public boolean DVRReceived = false;
	public ArrayList<ClientNode> adjacent;
	public int pingTime = 0;
	public String lastData = "";
	public ClientNode(SocketAddress address, String id) {
		this.address = address;
		this.id = id;
	}
	
	public void setAdjacent(ArrayList<ClientNode> adjacent) {
		this.adjacent = adjacent;
	}
}
