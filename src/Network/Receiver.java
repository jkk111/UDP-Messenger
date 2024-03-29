package Network;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.xml.bind.DatatypeConverter;

import interfaces.MessageRead;
import json.JsonObject;
import json.Parser;
import util.Logger;

/*
 * Receiver, handles receiving messages.
 */
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
		l.out("got packet: " + p.getData().length +" bytes long");
		String isEnd = Parser.parse(new String(p.getData())).get("end");
		if(isEnd != null && isEnd.equals("true")) {
			String message = "";
			ArrayList<String> data = connections.get(p.getSocketAddress());
			String sender = "";
			String dest = "";
			if(data == null)
				return;
			for(int i = 0 ; i < data.size(); i++) {
				JsonObject o = Parser.parse(data.get(i));
				if(o.get("image") != null) {
					parseImage(p.getSocketAddress(), data);
					return;
				}
				message += o.get("message");
				sender = o.get("sender");
				dest = o.get("dest");
			}
			if(dest.equals(parent.getClientId()))
				parent.messageReceived(message, sender);
			else if(parent.isLaptop()){
				SocketAddress addr = parent.lookupClient(dest);
				if(addr == null)
					return;
				l.out("made: " + addr);
				parent.forwardMessage(addr, message, dest, sender);
			} else {
				l.err("packet not for this phone, dropping");
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
	
	public void parseImage(SocketAddress addr, ArrayList<String> pieces) {
		l.out(pieces.size() +" packets in image");
		String imageString = "";
		String dest = "";
		String sender = "";
		for(int i = 0 ; i < pieces.size();i++) {
			JsonObject o = Parser.parse(pieces.get(i));
			imageString += o.get("image");
			dest = o.get("dest");
		}
		byte[] image = DatatypeConverter.parseBase64Binary(imageString);
		connections.remove(addr);
		if(dest.equals(parent.getClientId())) {
			parent.addImage(image);
			parent.messageReceived("Sent an image!", sender);
		} else {
			parent.forwardImage(dest, image, sender);
		}
	}
	
}
