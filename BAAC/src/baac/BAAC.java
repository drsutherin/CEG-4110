package baac;

import java.util.Vector;
import chat.*;
import gui.*;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * will create:
  * a ServerInterface instance
  * a LobbyChat instance (only updated while user is ```in_lobby```)
  * a list of PrivateChat instances (if any)
  * a Game instance (if playing/observing)
  * a Voce SpeechInterface instance (for voice recognition)
  * a GUI instance
  *
 * @author reuintern
 *
 */
public class BAAC implements Runnable {
	Thread serverInterface = null;
	Thread lobbyChat;
	Vector<PrivateChat> privateChatList;	// Contains all active private chats
	Game theGame;
	Player you = Player.getInstance();
	private final LinkedBlockingQueue<String> clientMessageBuffer = new LinkedBlockingQueue<String>();
	// Voce speechInterface;
	// Stage gui_background;
	//a random comment

	public BAAC()	{
		//buffer for passing messages within the client (from classes to this interface)
		serverInterface = new Thread(new ServerInterface(clientMessageBuffer)); //"mchlrtkwski.tk", 45322, this);
		lobbyChat = new Thread(new LobbyChat(clientMessageBuffer));		
	}

	//instantiate the other classes, send the clientMessageBuffer to classes that need to send messages back
	public void run() {
		// TODO Auto-generated method stub

	}
}
