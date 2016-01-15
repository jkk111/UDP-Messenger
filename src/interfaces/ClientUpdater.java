package interfaces;

import java.net.InetSocketAddress;

public interface ClientUpdater {
	public void updateClient(InetSocketAddress addr, String dest);
}
