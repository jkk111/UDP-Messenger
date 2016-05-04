package interfaces;
/*
 * Interface to allow a callback when ping is sent successfully.
 */
public interface PingClient {
	public void pingComplete(String id, int timeTaken);
}
