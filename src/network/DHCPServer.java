package network;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import util.Logger;

public class DHCPServer {
	DatagramSocket socket;
	Listener listener;
	Logger log;
	public static final int DHCP_PORT = 50000; // Plays nicely with unix host without superuser
	public DHCPServer() {
		try {
			socket = new DatagramSocket(DHCP_PORT);
		} catch (SocketException e) {
			System.out.println("something happened");
		}
		listener = new Listener();
		try {
			log = new Logger();
		} catch (IOException e) {
			e.printStackTrace();
		}
		listener.start();
	}
	
	class Listener extends Thread {
		public void run() {
			int i = 0;
			while(i < 20) {
				try {
					log.out("hello world");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				i++;
			}
			try {
				log.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(Thread.currentThread().getStackTrace()[1].getClassName());
		}
	}
	//http://stackoverflow.com/questions/18691054/why-the-isreachable-method-always-return-false
	public static void main(String[] args) {
		new DHCPServer();
	}
}
