package interfaces;
/*
 * Interface to provide a message received callback.
 */
public interface MessageReceived {
	public void receivedMessage(String message, String sender);
}
