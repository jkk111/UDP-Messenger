package json;
/*
 * Pair object used for key, value pairs.
 */
public class Pair<T extends Comparable<T>> {
	String key;
	T value;
	Pair(String k, T v) {
		key = k;
		value = v;
	}
	
	public String toString() {
		return "\""+ key + "\": " 
				+ (value instanceof String ? "\"" + value + "\"": value);
	}
	
	public static void main(String args[]) {
		Pair test = new Pair("data", 7);
		System.out.println(test.toString());
		System.out.println("a123b".matches("^[0-9]{1,45}$"));
	}
}
