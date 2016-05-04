package interfaces;

import java.net.InetSocketAddress;
/*
 * Interface to update client lists.
 */
public interface ClientUpdater {
	public void updateClient(InetSocketAddress addr, String dest);
}
