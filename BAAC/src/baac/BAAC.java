package baac;

import java.util.Observable;
import java.util.Scanner;
import java.util.Vector;
import chat.*;
import gui.*;

import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

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
	Thread lobbyChatThread;
	Vector<PrivateChat> privateChatList;	// Contains all active private chats
	Vector<String> activeUsers;
	Vector<String[]> activeTables;		// [0]=table id, [1]=player1, [2]=player2
	Vector<Vector<String>> activeTableStatus;
	String message = "";
	Scanner in = new Scanner(System.in);
	Boolean shutdown = false;
	Vector<String> statusHold;
	Vector<String> tblHold;

	Player you = Player.getInstance();//ensures there is a player
	PeerMediator mediator = new PeerMediator();
	LobbyChat lobbyChat = null;

	Game theGame = new Game(mediator);
	InGameMenuWindow inGameMenu;

	//Thread safe buffers used to add/remove messages from this thread
	private final LinkedBlockingQueue<String> sendToServer = new LinkedBlockingQueue<String>();
	private final LinkedBlockingQueue<String> receiveFromServer = new LinkedBlockingQueue<String>();
	
	// Voce speechInterface;
	// Stage gui_background;

	// GUI Windows
	private MainMenuWindow mainMenu;
	private ActiveTablesWindow activeTablesWindow;
	private LobbyUsersWindow lobbyUsersWindow;
	private InGameToolbarWindow gameToolbarWindow;

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
		activeTables = new Vector<String[]>();
		activeTableStatus = new Vector<Vector<String>>();
		privateChatList = new Vector<PrivateChat>();
		//instantiate string to hold the players on the table
		//this will be added to the activeTablesStatus vector
		statusHold = new Vector<String>();
		tblHold = new Vector<String>();
		//voce.SpeechInterface.init("src/voce", true, true, "src/voce/gram", "every");
		enterLobby();
	}

	/**
	 * Check the buffers for messages from the server and messages to be sent to the server
	 */
	public void run() {
		while(!shutdown){
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
		sleepyThread();
		}

	}

	/**
	 * Type of Game
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
	public boolean requestJoinGame(GameMode gameMode, String gameID){
		boolean returnBool = true;

		if (gameMode == GameMode.PLAY){
			//send the server the appropriate join table message
			message = "104 "+ Player.getUsername() +" " + gameID + "<EOM>";
			queueUpToSendToServer(message);
			you.setUserStatus(Status.PLAYING);
		}else if (gameMode == GameMode.OBSERVE){
			//send the server the appropriate observe table message
			message = "110 "+ Player.getUsername() +" " + gameID + "<EOM>";
			queueUpToSendToServer(message);
			you.setUserStatus(Status.OBSERVING);
		}else{
			//if you are here something went wrong and the message will not be sent
			returnBool =  false;
		}
		return returnBool;
	}

	/***
	 * Asks the server to create a new table and place the user in a seat at the new table
	 */
	public void requestCreateTable(){
		String message = "103 " + Player.getUsername() + "<EOM>";
		queueUpToSendToServer(message);
	}

	/**
	 * This method sends the "leave table" message to the server for this user
	 */
	public void requestLeaveGame(){
		//can only leave game if we are in or observing a game
		if (Player.getUserStatus() != Status.IN_LOBBY){
			message = "107 "+ Player.getUsername() + "<EOM>";
			queueUpToSendToServer(message);
		}
	}
	
	/**
	 * A gracefull way to shutdown 
	 */
	public void shutDownBaac(){
		
		//Stop the mediator from dispatching messages
		mediator.setShutdown();
		
		//Shutdown the Server Interface
		//TODO: Fix server interface, why two threads??
		//serverInterface.shutdown();
		
		//TODO: Shutdown any private chats, this doesn't work bc Baac doesn't have a complete list of pchats
		//for (PrivateChat pchat : privateChatList){
			//pchat.shutdown();
		//}
		
		//TODO: verify games are shut down before getting here
		
		//Empty BAAC's message queues
		sendToServer.clear();
		receiveFromServer.clear();
		
		//Shutdown Lobby Chat
		lobbyChat.shutdown();
		
		//Shutdown Baac's thread
		shutdown = true;
		System.out.println("Shutdown complete");
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
			String user;
			switch(code){
			//2 codes start here
				case ServerMessage.ASK_USERNAME:
					JFrame frame = new JFrame("Username Entry");
			        // prompt the user to enter their name
			        out = JOptionPane.showInputDialog(frame, "Enter username:").replaceAll("\n","");
					enterUsername(out);
				case ServerMessage.CONN_OK:
					//System.out.println("Connected to Server");
					break;
				case ServerMessage.IN_LOBBY:
					if(lobbyChatThread == null){
						lobbyChatThread = new Thread(lobbyChat);
						lobbyChatThread.start();
					}
					Player.setUserStatus(Status.IN_LOBBY);
					break;
				//This checks for private messages sent to the client	
				case ServerMessage.MSG:
					 String[] messageSplit = message.split(" ");
					 String sender, receiver, msg;
					 sender = messageSplit[1];
					 receiver = messageSplit[2];
					 msg = messageSplit[3];
					 
					 if (receiver == "1"){
						 //checks the list of private chats. If the sender is
						 //not on the list, then it will open a new instance of private chat
						 boolean found = false;
						 for (int i = 0; i < privateChatList.size(); i++)	{
					 		if (privateChatList.get(i).getBuddy() == sender)	{
					 			privateChatList.get(i).formatMessageFromServer(message);
					 			found = true;
					 			break;
					 		}
						 }
					 if (!found) {
					 	PrivateChat newChat = new PrivateChat(mediator, sender);
					 	newChat.formatMessageFromServer(message);
					 	privateChatList.add(newChat);
					 	}
					 }
					 else {
						 //lobby.formatMessageFromServer(message);
					 }
					 break;	
				
				case ServerMessage.OUT_LOBBY:
					//do nothing, don't try to shut down the lobbyThread from here, it leads to intermittent exceptions 
					//and thread may not restart properly. Instead, the lobbyThread will not print or send messages if 
					//user status is not "in lobby"
					break;
				case ServerMessage.NEW_TBL:
					//Do nothing, the server will send a 219 message to all client automatically
					//Next message: 219 WHO_ON_TABLE
					message = message.replace(ServerMessage.NEW_TBL + " ", "");
					message = message.replace(" <EOM>", "");
					message = message.replace("<EOM>", "");
					sendToServer.put("109 " + Player.getUsername() + " " + message + " <EOM>");
					break;
				case ServerMessage.TBL_JOINED:
					//TODO: tell the user that they joined a new table

					//parse the string
					message = message.replace("<EOM>", "");
					String[] messageArray = message.split(" ", 2);
					String tableNum = messageArray[1];
					theGame.setTableID(tableNum);
					//create the thread
					Thread gameThread = new Thread(theGame);
					Player.setUserStatus(Status.PLAYING);
					gameThread.start();
					//Create an ingame menu
					inGameMenu = new InGameMenuWindow(this);
					exitLobby();
					break;
				case ServerMessage.TBL_LEFT:
					//leave the in game menu
					
					theGame.stopGame();
					inGameMenu.closeWindow();
					break;
				case ServerMessage.WHO_IN_LOBBY:
					//System.out.println("Users in lobby are:");
					//System.out.println(message.substring(4, message.length()-6));
					message = message.replace(ServerMessage.WHO_IN_LOBBY + " ", "");
					message = message.replace(" <EOM>", "");
					message = message.replace("<EOM>", "");
					String[] users = message.split(" ");
					//System.out.println(users.toStringString());
					for (int i = 0; i < users.length; i++){
						activeUsers.add(users[i]);
					}
					//send gui info for displaying who is in the lobby
					lobbyUsersWindow.updateList(activeUsers);
					break;
				case ServerMessage.NOW_IN_LOBBY:
					user = message.replace(ServerMessage.NOW_IN_LOBBY + " ", "");
					user = user.replace(" <EOM>", "");
					user = user.replace("<EOM>", "");
					activeUsers.add(user);
					//update gui elements for who is in the lobby
					lobbyUsersWindow.updateList(activeUsers);
					break;
				case ServerMessage.WHO_ON_TBL:
					//remove code and <EOM> from message
					message = message.replace(ServerMessage.WHO_ON_TBL + " ", "");
					message = message.replace(" <EOM>", "");
					message = message.replace("<EOM>", "");
					//split the message into an array of strings by spaces
					//we will end up with an array of 3 strings
					String[] split = message.split(" ");
					//add the table id to its vector
					String[] thisTable = new String[3];
					thisTable[0] = split[0];

					//run through the rest of the array aFnd add the name of the person in that spot or
					//vacant if there is nobody
					for (int i = 1; i < split.length; i++){
						if (split[i].equalsIgnoreCase("-1")){
							thisTable[i] = "free seat";
						}else{
							thisTable[i] = split[i];
						}
					}
					//activeTableStatus.add(statusHold);
					activeTables.add(thisTable);
					activeTablesWindow.add(thisTable);
					break;
				case ServerMessage.TBL_LIST:
					message = message.replace(ServerMessage.TBL_LIST + " ", "");
					message = message.replace(" <EOM>", "");
					message = message.replace("<EOM>", "");
					//if there are several tables split the string into an array of strings
					String[] tables = message.split(" ");
					//put the table ids into the active tables vector
					for (int i = 0; i < tables.length; i++){
						//make sure tables include something other than the empty string
						if (!tables[i].equals("")){
							tblHold.add(tables[i]);
						}
					}
					for (int i = 0; i < tblHold.size(); i++){
						String requestWhoOnTbl = "109 " + Player.getUsername() + " " + tblHold.get(i) + " <EOM>";
						queueUpToSendToServer(requestWhoOnTbl);//need to use indirect method due to blocking issues
					}
					break;
				case ServerMessage.NOW_LEFT_LOBBY:
					user = message.replace(ServerMessage.NOW_LEFT_LOBBY + " ", "");
					user = user.replace(" <EOM>", "");
					user = user.replace("<EOM>", "");
					activeUsers.remove(user);
					//indicate that a user has left the lobby
					lobbyUsersWindow.updateList(activeUsers);
					break;
				case ServerMessage.NOW_OBSERVING:
					//get the table number from the message
					message = message.replace("<EOM>", "");
					String[] obsMessageArray = message.split(" ", 2);
					String obsTableNum = obsMessageArray[1];
					theGame.setTableID(obsTableNum);
					//leave the game if you were in one (which you shouldn't be because the observe game
					//button is not accessible from in-game menu
					theGame.stopGame();
					Thread obsThread = new Thread(theGame);
					//create the thread
					Player.setUserStatus(Status.OBSERVING);
					obsThread.start();
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
					System.out.println("NET EXCEPTION");
					break;
				case ServerMessage.NAME_IN_USE:
				case ServerMessage.BAD_NAME:
					JFrame frame2 = new JFrame("Username Error");
			        // prompt the user to enter their name
			        out = JOptionPane.showInputDialog(frame2, "Username error. Re-enter username:").replaceAll("\n","");
					enterUsername(out);
				case ServerMessage.TBL_FULL:
					System.out.println("Cannot join, table is full");
					break;
				case ServerMessage.NOT_IN_LOBBY:
					System.out.println("You are not in the lobby");
					break;
				case ServerMessage.BAD_MESSAGE:
					System.out.println("Bad message to the server");
					break;
				case ServerMessage.ERR_IN_LOBBY:
					System.out.println("Error in lobby");
					break;
				case ServerMessage.TBL_NOT_EXIST:
					System.out.println("That table does not exist");
					break;
				case ServerMessage.ALREADY_REGISTERED:
					System.out.println("That username is already registered");
					break;
				case ServerMessage.LOGIN_FAIL:
					System.out.println("Unable to log in (did you enter your password correctly?)");
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
	 * This method will take a username from the user after
	 * prompting them to enter it
	 * The desired username will then be sent to the server
	 *
	 * @param prompt Enter username prompt
	 * @return the desired username
	 */
	private void enterUsername(String out){
		out.replaceAll("\n", "");
		try {
			sendToServer.put(out);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Player.setUsername(out);
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

	@Override
	public void update(Observable arg0, Object arg1) {
		MenuButtonStatus last;
		if (Player.getUserStatus().equals(Status.IN_LOBBY))	{
			last = mainMenu.getLastPressed();
		}else {
			last = inGameMenu.getLastPressed();
		}
		switch (last)	{
		case START:
			requestCreateTable();
			break;
		case JOIN:
		// Change this so it displays in a nicer format:
			// prompt user for which game to join
			Vector<String> joinableTables = new Vector<String>();
			String[] currentTable = new String[3];
			for (int i = 0; i < activeTables.size(); i++){
				currentTable = activeTables.get(i);
				if (currentTable[1].equals("free seat") || currentTable[2].equals("free seat"))	{
					joinableTables.add(currentTable[0]);
				}
			}
			JFrame jframe = new JFrame();
	        String tableToJoin  = (String) JOptionPane.showInputDialog(jframe, "Choose a Table to Join:",
	        		"Join Table Dialog", 3, null, joinableTables.toArray(), 0);	    
			if (tableToJoin != null)	{
		        requestJoinGame(GameMode.PLAY, tableToJoin);
			}
			// attempt to join selected game
				//requestJoinGame(GameMode.PLAY, table);
			break;
		case OBSERVE:
			// Change this so it displays in a nicer format:
			// prompt user for which game to join
			Vector<String> observableTables = new Vector<String>();
			String[] thisTable = new String[3];
			for (int i = 0; i < activeTables.size(); i++){
				thisTable = activeTables.get(i);
				if (thisTable[1].equals("free seat") || thisTable[2].equals("free seat"))	{
					observableTables.add(thisTable[0]);
				}
			}
			JFrame myjframe = new JFrame();
	        String tableToObserve  = (String) JOptionPane.showInputDialog(myjframe, "Choose a Table to Observe:",
	        		"Observe Table Dialog", 3, null, observableTables.toArray(), 0);	    
			if (tableToObserve != null)	{
		        requestJoinGame(GameMode.OBSERVE, tableToObserve);
			}			

			break;
		case PRIVATE_CHAT:
			JFrame frame = new JFrame();
	        String chatBuddy  = (String) JOptionPane.showInputDialog(frame, "Choose a chat buddy:", "Private Chat Setup",
	        		3, null, activeUsers.toArray(), activeUsers.get(0));	    
			if (chatBuddy != null)	{
		        PrivateChat p = new PrivateChat(mediator, chatBuddy);
				privateChatList.add(p);
				Thread privateChatThread = new Thread(p);
				privateChatThread.start();
			}
			break;
		case EXIT_GAME:
			// ask the server to leave the current game
			theGame.clientLeaveTableRequest();
			break;
		case EXIT_BAAC:
			queueUpToSendToServer("108 " + Player.getUsername());
			
			break;
		}
	}
	
	/**
	 * Initializes all windows and classes necessary for the lobby
	 */
	public void enterLobby()	{
		lobbyChat = new LobbyChat(mediator);
		mainMenu = new MainMenuWindow(this);
		Player.setUserStatus(Status.IN_LOBBY);
		// TODO: make sure these are setup correctly and let 'em rip
		lobbyUsersWindow = new LobbyUsersWindow();
		activeTablesWindow = new ActiveTablesWindow();
	}
	
	/**
	 * Exits the lobby by closing all open windows and dereferencing containing classes
	 * @param newStatus
	 */
	private void exitLobby()	{
		lobbyChat.shutdown();
		lobbyChat = null;
		mainMenu.closeWindow();
		mainMenu = null;
		lobbyUsersWindow.closeWindow();
		activeTablesWindow.closeWindow();
	}
	

	


}
