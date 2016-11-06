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
	ServerInterface serverInterface;
	LobbyChat lobbyChat;
	Vector<PrivateChat> privateChatList;	// Contains all active private chats
	Vector<String> activeUsers;
	Vector<Integer> activeTables;
	Game theGame;
	Player you = Player.getInstance();
	private final LinkedBlockingQueue<String> clientMessageBuffer = new LinkedBlockingQueue<String>();
	// Voce speechInterface;
	// Stage gui_background;
	// BAAC_GUI gui;
	

	public BAAC()	{
		//buffer for passing messages within the client (from classes to this interface)
		ServerInterface serverInterface = null;
		serverInterface = new ServerInterface(clientMessageBuffer); //"mchlrtkwski.tk", 45322, this);	
	}

	//instantiate the other classes, send the clientMessageBuffer to classes that need to send messages back
	public void run() {
		// TODO Auto-generated method stub

	}
	public enum GameMode{
		OBSERVE, PLAY
	}
	/**
	 * This method will either join or observe a game depending on user input
	 * 
	 * @param gameMode indicates whether or not the user wishes to play or observe the game
	 * @param gameID the game that the user wishes to join/observe
	 * @return whether or not the message succeeded
	 */
	public boolean joinGame(GameMode gameMode, int gameID){
		boolean returnBool = true;
		
		if (gameMode == GameMode.PLAY){
			//send the server the appropriate join table message
			serverInterface.pushSendMessage("<104> <"+Player.getUsername()+"> <"+gameID+"> <EOM>");
		}else if (gameMode == GameMode.OBSERVE){
			//send the server the appropriate observe table message
			serverInterface.pushSendMessage("<110> <"+Player.getUsername()+"> <"+gameID+"> <EOM>");
		}else{
			//if you are here something went wrong and the message will not be sent
			returnBool =  false;
		}
		
		return returnBool;
	}
	/**
	 * This method sends the "leave table" message to the server for this user
	 */
	public void leaveGame(){
		serverInterface.pushSendMessage("<107> <"+Player.getUsername()+"> <EOM>");
	}
}
