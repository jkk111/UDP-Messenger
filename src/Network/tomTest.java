package Network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class tomTest {
	public static void main(String[] args) throws IOException {
		DatagramSocket socket = new DatagramSocket(5000);
		DatagramPacket p = new DatagramPacket("FUCK YOU TOM".getBytes(), 12, InetAddress.getByName("255.255.255.255"), 50000);
		socket.setBroadcast(true);
		while(true)
			socket.send(p);
	}
}
