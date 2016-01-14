package json;
import java.util.ArrayList;
import java.util.Arrays;

import util.Logger;

public class Parser {
	/*
	 * 
	 */
	public static JsonObject parse(String data, String delimiter) {
		Logger l = new Logger();
		l.out(data);
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		data = data.trim();
		result = getPairs(data);
		return new JsonObject(result);
	}
	
	public static ArrayList<ArrayList<String>> getPairs(String data) {
		data = data.substring(1, data.length() -1).trim();
		ArrayList<String> all = new ArrayList<String>();
		String next = "\"";
		boolean isEnclosed = true;
		for(int i = 1 ; i < data.length(); i++) {
			if(data.charAt(i) == ':' && data.charAt(i - 1) != '\\' ||
			   data.charAt(i) == ',' && !isEnclosed) {
				all.add(next);
				next = "";
			} else if (data.charAt(i) == '\"' && data.charAt(i - 1) != '\\') {
				isEnclosed = !isEnclosed;
				next += data.charAt(i);
			} else {
				next += data.charAt(i);
			}
		}
		all.add(next);
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>(); 
		for(int i = 0; i < all.size() / 2; i++) {
		    ArrayList<String> tmp = new ArrayList<String>();
		    String first = all.get(2 * i);
		    String second = all.get(1 + 2 * i);
		    first = first.trim();
		    first = first.substring(1, first.length() - 1);
		    second = second.trim();
		    second = second.substring(1, second.length() - 1);
		    tmp.add(first);
		    tmp.add(second);
		    result.add(tmp);
		}
		return result;
	}
	
	public static JsonObject parse(String data) {
		return parse(data,"");
	}
	
	public static void main(String args[]) {
		String test = "{ \"h\\\"e\\:ll, \\\",o\":\"wor%^*)(%$*^%(TUGUYVOTURVRURCTUR*CRE^RECT&^ld\", \"test\": \"statement\" }";
		System.out.print(parse(test).toString("  "));
	}
}
