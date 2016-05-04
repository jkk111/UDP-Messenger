package json;
import java.util.ArrayList;
/*
 * Really basic json "like" class that resembles json and can be used to serialize messages.
 */
public class JsonObject {
	ArrayList<Pair> pairs;
	public JsonObject(ArrayList<ArrayList<String>> data) {
		pairs = new ArrayList<Pair>();
		for(int i = 0 ; i < data.size(); i++) {
			String key = data.get(i).get(0);
			String value = data.get(i).get(1);
			if(value.matches("^[0-9]{1,45}$"))
				pairs.add(new Pair(key, Double.parseDouble(value)));
			else
				pairs.add(new Pair(key, value));
		}
	}
	
	public JsonObject() {
		pairs = new ArrayList<Pair>();
	}
	
	public String toString() {
		String result = "{ ";
		for(int i = 0 ; i < pairs.size();i++) {
			if(i>0)
				result += ", ";
			result += pairs.get(i).toString();
		}
		result += " }";
		return result;
	}
	
	public String toString(String delimiter) {
		String result = "{\n" + delimiter;
		for(int i = 0 ; i < pairs.size();i++) {
			if(i>0)
				result += ",\n"+delimiter;
			result += pairs.get(i).toString();
		}
		result += "\n}";
		return result;
	}
	
	public String get(String key) {
		for(int i = 0 ; i < pairs.size(); i++) {
			if(pairs.get(i).key.equals(key)) {
				return (String) pairs.get(i).value;
			}
		}
		return null;
	}
	
	public void add(String key, String value) {
		Pair tmp = new Pair(key, value);
		pairs.add(tmp);
	}
	
	public static void main(String args[]) {
		String[] vals = { "test", "1337", "hello", "world", "pop", "rocks" };
		ArrayList<ArrayList<String>> test = new ArrayList<ArrayList<String>>();
		for(int i = 0 ; i < 3 ; i++) {
			System.out.println(vals[(i*2) + 1]);
			ArrayList<String> input = new ArrayList<String>();
			input.add(vals[(i*2) + 0]);
			input.add(vals[(i*2) + 1]);
			test.add(input);
		}
		System.out.println(test.get(0).get(0));
		System.out.println(test.get(1).get(0));
		System.out.println(test.get(2).get(0));
		JsonObject json = new JsonObject(test);
		System.out.println(json.toString());
	}
}
