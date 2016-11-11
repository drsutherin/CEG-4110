package baac;

import chat.LobbyChat;

/**
 * The Main class starts the client
 * @author reuintern
 *
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Thread client = new Thread(new BAAC());
		client.start();
		
		
	}

}
