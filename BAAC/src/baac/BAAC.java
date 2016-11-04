package baac;

import java.util.Vector;
import chat.*;
import gui.*;

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
	ServerInterface serverInterface;
	LobbyChat lobbyChat;
	Vector<PrivateChat> privateChatList;	// Contains all active private chats
	Game theGame;
	Player you;
	// Voce speechInterface;
	// Stage gui_background;

	public BAAC()	{
		ServerInterface serverInterface = null;
		serverInterface = new ServerInterface(this); //"mchlrtkwski.tk", 45322, this);
		
		
	}

	public void run() {
		// TODO Auto-generated method stub

	}
}
