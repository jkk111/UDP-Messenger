package json;
import java.util.ArrayList;
import java.util.Arrays;

public class Parser {
	public static JsonObject parse(String data, String delimiter) {
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		data = data.trim().replace("\"", "").replace(delimiter, "");
		data = data.substring(1, data.length() -1).trim();
		String[] kvs = data.split(",");
		for (int i = 0; i<kvs.length;i++) {
			String pair[] = kvs[i].split(":", 2);
			ArrayList<String> tmp = new ArrayList<String>();
			tmp.add(pair[0].trim());
			tmp.add(pair[1].trim());
			result.add(tmp);
		}
		return new JsonObject(result);
	}
	
	public static JsonObject parse(String data) {
		return parse(data,"");
	}
	
	public static void main(String args[]) {
		String test = "{\"hello\":\"world\"}";
		System.out.println(test);
		System.out.println(parse(test).toString("  "));
	}
}
