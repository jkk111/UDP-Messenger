package util;
import java.net.SocketAddress;
import java.util.ArrayList;
/*
 * Node object used when calculating shortest path to destination.
 */
public class ClientNode {
	public SocketAddress address;
	public String id;
	public boolean LSRReceived = false;
	public boolean DVRReceived = false;
	public ArrayList<ClientNode> adjacent;
	public int pingTime = 0;
	public String lastData = "";
	public ClientNode(SocketAddress address, String id) {
		adjacent = new ArrayList<ClientNode>();
		this.address = address;
		this.id = id;
	}
	
	public void setAdjacent(ArrayList<ClientNode> adjacent) {
		this.adjacent = adjacent;
	}
	
	public ClientNode getClient(String id) {
		for(int i = 0 ; i < adjacent.size(); i++) {
			if(adjacent.get(i).id.equals(id)) {
				return adjacent.get(i);
			}
		}
		return null;
	}
}
