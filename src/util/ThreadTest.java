package util;

public class ThreadTest extends Thread {
	public static void main(String[] args) {
		ThreadTest test = new ThreadTest();
		test.start();
	}
	
	public ThreadTest() {
		
	}
	
	public void run() {
		int i = 0;
		while(++i < 100000) {
			System.out.println(i);
		}
	}
}
