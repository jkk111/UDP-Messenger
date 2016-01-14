package Network;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Hashtable;

import json.JsonObject;
import json.Parser;
import util.Logger;
import util.MessageRead;
import util.MessageSend;

public class Receiver {
	Hashtable<SocketAddress, ArrayList<String>> connections;
	MessageRead parent;
	Logger l;
	public Receiver(MessageRead s) {
		l = new Logger();
		connections = new Hashtable<SocketAddress, ArrayList<String>>();
		parent = s;
	}
	
	public void add(DatagramPacket p) {
		String isEnd = Parser.parse(new String(p.getData())).get("end");
		l.err(p.getSocketAddress().toString() + ":" + new String(p.getData()));
		if(isEnd != null && isEnd.equals("true")) {
			String message = "";
			ArrayList<String> data = connections.get(p.getSocketAddress());
			String sender = "";
			String dest = "";
			for(int i = 0 ; i < data.size(); i++) {
				JsonObject o = Parser.parse(data.get(i));
				message += o.get("message");
				sender = o.get("sender");
				dest = o.get("dest");
			}
			if(dest.equals(parent.getClientId()))
				parent.messageReceived(message, sender);
			else {
				InetSocketAddress addr = new InetSocketAddress(p.getAddress(), 50000);
				l.out("made: " + addr.toString());
				parent.forwardMessage(addr, message, dest, sender);
			}
			connections.remove(p.getSocketAddress());
			return;
		}
		ArrayList<String> data = connections.get(p.getSocketAddress());
		if(data == null) {
			data = new ArrayList<String>();
			data.add(new String(p.getData()));
			connections.put(p.getSocketAddress(), data);
		} else {
			data.add(new String(p.getData()));
		}
	}
	
}
