package baac;

import java.util.Scanner;
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
	Scanner in = new Scanner(System.in);
	
	Player you = Player.getInstance();//ensures there is a player
	PeerMediator mediator = new PeerMediator();
	LobbyChat lobby = new LobbyChat(mediator);
	
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
		in = new Scanner(System.in);
		mediator.addPeerClass(this);
		//buffer for passing messages within the client (from classes to this interface)
		serverInterface = new Thread(new ServerInterface(mediator)); //"mchlrtkwski.tk", 45322, this);
		activeUsers = new Vector<String>();
		activeTables = new Vector<Integer>();
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
			while (!receiveFromServer.isEmpty()){
				decodeMessageFromServer();			
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
	private void decodeMessageFromServer(){
		String message;
		String out;
		try {
			message = receiveFromServer.take();
			//this method will only use codes starting with 2 as those are ones that are received from the server
			//System.out.println(message);
			String code = message.substring(0, 3);
			switch(code){
			//2 codes start here
				case ServerMessage.ASK_USERNAME:
					System.out.println("Enter Username");
					//Scanner to halt works here because the server needs a username before we can do anything else
					//This will be replaced with gui elements in the future
					out = in.nextLine();
					out.replaceAll("\n", "");
					sendToServer.put(out);
					Player.setUsername(out);
				case ServerMessage.CONN_OK:
					//System.out.println("Connected to Server");
					break;
				case ServerMessage.IN_LOBBY:
					System.out.println("You are now in the lobby");
					lobbyChat = new Thread(lobby);	
					lobbyChat.start();
					break;
				case ServerMessage.OUT_LOBBY:
					//code for lobby handling
					break;
				case ServerMessage.NEW_TBL:
					//code for indicating a new table has been created
					break;
				case ServerMessage.GAME_START:
					break;
				case ServerMessage.TBL_JOINED:
					//tell the user that they joined a new table
					break;
				case ServerMessage.TBL_LEFT:
					//tell the user they have left the table
					break;
				case ServerMessage.WHO_IN_LOBBY:
					System.out.println("Users in lobby are: ");
					System.out.println(message.substring(4, message.length()-6));
					message = message.substring(4, message.length()-6);
					String[] users = message.split(" ");
					//System.out.println(users.toString());
					for (int i = 0; i < users.length; i++){
						activeUsers.add(users[i]);
					}
					//send gui info for displaying who is in the lobby
					break;
				case ServerMessage.NOW_IN_LOBBY:
					message = message.substring(4, message.length()-6);
					activeUsers.add(message);
					//indicate to the user that they are in the lobby
					break;
				case ServerMessage.WHO_ON_TBL:
					//indicate to the user who is on the table
					break;
				case ServerMessage.TBL_LIST:
					//if there are no tables this condition will not be fulfilled
					if (message.length()-6 > 4){
						//select table part of the message
						message = message.substring(4, message.length()-6);
						//if there are several tables split the string into an array of strings
						String[] tables = message.split(" ");
						//put the table ids into the active tables vector
						for (int i = 0; i < tables.length; i++){
							activeTables.add(Integer.parseInt(tables[i]));
						}
					}
					break;
				case ServerMessage.NOW_LEFT_LOBBY:
					message = message.substring(4, message.length()-6);
					activeUsers.remove(message);
					//indicate that a user has left the lobby
					break;
				case ServerMessage.OPP_LEFT_TABLE:
					//indicate that the opponent has left the table
					break;
				case ServerMessage.NOW_OBSERVING:
					//start observe game thread
					break;
				case ServerMessage.STOPPED_OBSERVING:
					//end observe game thread
					break;
				case ServerMessage.REGISTER_OK:
					break;
				case ServerMessage.LOGIN_OK:
					break;
				case ServerMessage.PROFILE_UPDATED:
					break;
				case ServerMessage.USER_PROFILE:
					break;
				//4 codes start here
				case ServerMessage.NET_EXCEPTION:
					break;
				case ServerMessage.NAME_IN_USE:
				case ServerMessage.BAD_NAME:
					//these will both result in the user having to choose a new name
					break;
				case ServerMessage.ILLEGAL:
					//inform user that the move they chose was illegal
					break;
				case ServerMessage.TBL_FULL:
					//inform user that the table they are trying to join is full
					break;
				case ServerMessage.NOT_IN_LOBBY:
					//inform the user that they are not in the lobby
					break;
				case ServerMessage.BAD_MESSAGE:
					break;
				case ServerMessage.ERR_IN_LOBBY:
					break;
				case ServerMessage.PLAYERS_NOT_READY:
					break;
				case ServerMessage.NOT_YOUR_TURN:
					//inform the user that they cannot move as it is not their turn
					break;
				case ServerMessage.TBL_NOT_EXIST:
					//inform the user that they cannot join the table they are trying to because it does not exist
					break;
				case ServerMessage.GAME_NOT_CREATED:
					break;
				case ServerMessage.ALREADY_REGISTERED:
					break;
				case ServerMessage.LOGIN_FAIL:
					//inform the user of a general login failure error
					break;
				case ServerMessage.NOT_OBSERVING:
					break;
				default:
					break;
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This method is accessed by PeerMediator and adds messages from the server to thusernamee local buffer
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
