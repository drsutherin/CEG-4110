package baac;

import java.util.Vector;
import chat.*;
import gui.*;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * will create:
 * a mediator instance
 * a ServerInterface instance
 * a LobbyChat instance (only updated while user is ```in_lobby```)
 * a list of PrivateChat instances (if any)
 * a Game instance (if playing/observing)
 * a Voce SpeechInterface instance (for voice recognition)
 * a GUI instance
 *
 * @author Dave, Jon, Zuli
 *
 */
public class BAAC extends Peer implements Runnable {
	Thread serverInterface = null;
	Thread lobbyChat;
	Vector<PrivateChat> privateChatList;	// Contains all active private chats
	Vector<String> activeUsers;
	Vector<Integer> activeTables;
	String message = "";
	Game theGame;
	Player you = Player.getInstance();//ensures there is a player
	PeerMediator mediator = new PeerMediator();
	
	//Thread safe buffers used to add/remove messages from this thread
	private final LinkedBlockingQueue<String> sendToServer = new LinkedBlockingQueue<String>();	
	private final LinkedBlockingQueue<String> receiveFromServer = new LinkedBlockingQueue<String>();	
		
	// Voce speechInterface;
	// Stage gui_background;
	// BAAC_GUI gui;
	
	/**
	 * Constructor for BAAC
	 * 	a) add self to mediator peer list so it will recieve messages from server
	 *  b) Instantiate the serverInterface
	 *  c) instantiate and start the lobbyChat
	 */
	public BAAC()	{
		mediator.addPeerClass(this);
		//buffer for passing messages within the client (from classes to this interface)
		serverInterface = new Thread(new ServerInterface(mediator)); //"mchlrtkwski.tk", 45322, this);
		lobbyChat = new Thread(new LobbyChat(mediator));	
		lobbyChat.start();
	}
	
	/**
	 * Check the buffers for messages from the server and messages to be sent to the server 
	 */
	public void run() {
		while(true){
			//send message to server
			String outgoingMessage;
			while (!sendToServer.isEmpty()){
				try {
					//get the message(Received from the UI)
					outgoingMessage = sendToServer.take();
					//send message to mediator
					mediator.receiveFromPeer(outgoingMessage);
				} catch (InterruptedException e) {

				};
			}
				
			//get message from server
			String incomingMessage;
			while (!receiveFromServer.isEmpty()){
				try{
					//get the message (received from the mediator)
					incomingMessage =  receiveFromServer.take();
					//verify it is a pertinent message and format to prepare to send to server 
					decodeMessageFromServer(incomingMessage);
				}catch (InterruptedException e) {

				};			
			}
		}		
	}
	
	/**
	 * 
	 */
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
	public boolean requestJoinGame(GameMode gameMode, int gameID){
		boolean returnBool = true;
		
		if (gameMode == GameMode.PLAY){
			//send the server the appropriate join table message
			message = "<104> <"+Player.getUsername()+"> <"+gameID+"> <EOM>";
			queueUpToSendToServer(message);
			you.setUserStatus(Status.playing);
		}else if (gameMode == GameMode.OBSERVE){
			//send the server the appropriate observe table message
			message = "<110> <"+Player.getUsername()+"> <"+gameID+"> <EOM>";
			queueUpToSendToServer(message);
			you.setUserStatus(Status.observing);
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
		//can only leave game if we are in or observing a game
		if (Player.getUserStatus() != Status.in_lobby){
			message = "<107> <"+Player.getUsername()+"> <EOM>";
			queueUpToSendToServer(message);
			Player.setUserStatus(Status.in_lobby);
		}
	}
	
	/**
	 * Receives a message and places it in Baac's out going message buffer (threadsafe)
	 * @param message, a formatted string that will be sent to the server interface
	 */
	private void queueUpToSendToServer(String message){
		if (message != ""){
			try {
				sendToServer.put(message);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/***
	 * Looks at all the messages from server and determines which ones to process
	 */
	private void decodeMessageFromServer(String incomingMessage){
		//Jon- this is some switch statement, I don't really know what should go in here...
		// I was thinking. . .
		// Does the server interface need to receive/process any messages from the sever? Currently it is responsible for getting the username
		// and looking for a 108 message but perhaps that is better suited here since for Server interface to receive anything from the
		// server has to bypass the mediator (which is what is currently happening, it just copies the messages to an array and check them
		// in its own switch statement. It's janky. I want to get rid of it but I don't know if there are any implications. I think this 
		// switch is going to involve you looking at almost every incoming message of so I was hoping you could keep that in the back of your 
		// mind and come to a conclusion either way.
	}

	/**
	 * This method is accessed by PeerMediator and adds messages from the server to the local buffer
	 * the thread in the run() method will consume the messages.
	 * Because PeerMediator accesses this method from another thread, a thread safe blocking queue is used
	 */
	@Override
	public void receiveFromMediator(String message) {
		try {
			receiveFromServer.put(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}
	
	
}
