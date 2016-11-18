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
	Vector<Integer> activeTables;
	Vector<Vector<String>> activeTableStatus;
	String message = "";
	Scanner in = new Scanner(System.in);

	Player you = Player.getInstance();//ensures there is a player
	PeerMediator mediator = new PeerMediator();
	LobbyChat lobbyChat;

	Game theGame = new Game(mediator);

	//Thread safe buffers used to add/remove messages from this thread
	private final LinkedBlockingQueue<String> sendToServer = new LinkedBlockingQueue<String>();
	private final LinkedBlockingQueue<String> receiveFromServer = new LinkedBlockingQueue<String>();

	// Voce speechInterface;
	// Stage gui_background;

	// GUI Windows
	MainMenuWindow mainMenu;


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
		activeTableStatus = new Vector<Vector<String>>();
		privateChatList = new Vector<PrivateChat>();
		setupLobby();
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
		sleepyThread();
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
	public void leaveGame(){
		//can only leave game if we are in or observing a game
		if (Player.getUserStatus() != Status.IN_LOBBY){
			message = "107 "+ Player.getUsername() + "<EOM>";
			queueUpToSendToServer(message);
			Player.setUserStatus(Status.IN_LOBBY);
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
					JFrame frame = new JFrame("Username Entry");
			        // prompt the user to enter their name
			        out = JOptionPane.showInputDialog(frame, "Enter username:").replaceAll("\n","");
					enterUsername(out);
				case ServerMessage.CONN_OK:
					//System.out.println("Connected to Server");
					break;
				case ServerMessage.IN_LOBBY:
					lobbyChatThread = new Thread(lobbyChat);
					lobbyChatThread.start();

					//Uncomment this to
					//requestCreateTable();

					break;
//				case ServerMessage.MSG:
//					String[] messageSplit = message.split(" ");
//					String sender, receiver, msg;
//					sender = messageSplit[1];
//					receiver = messageSplit[2];
//					msg = messageSplit[3];
//
//					if (receiver == "1")	{
//						boolean found = false;
//						for (int i = 0; i < privateChatList.size(); i++)	{
//							if (privateChatList.get(i).getBuddy() == sender)	{
//								privateChatList.get(i).formatMessageFromServer(message);
//								found = true;
//								break;
//							}
//						}
//						if (!found) {
//							PrivateChat newChat = new PrivateChat(mediator, sender);
//							newChat.formatMessageFromServer(message);
//							privateChatList.add(newChat);
//						}
//					}
//					else {
//						lobby.formatMessageFromServer(message);
//					}
//					break;
				case ServerMessage.OUT_LOBBY:
					lobbyChatThread.stop();
					break;
				case ServerMessage.NEW_TBL:
					message = message.replace(ServerMessage.NEW_TBL + " ", "");
					message = message.replace(" <EOM>", "");
					message = message.replace("<EOM>", "");
					sendToServer.put("109 " + Player.getUsername() + " " + message + " <EOM>");
					//update gui elements
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
					gameThread.start();
					break;
				case ServerMessage.TBL_LEFT:

					theGame.stop();
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
					break;
				case ServerMessage.NOW_IN_LOBBY:
					message = message.replace(ServerMessage.NOW_IN_LOBBY + " ", "");
					message = message.replace(" <EOM>", "");
					message = message.replace("<EOM>", "");
					activeUsers.add(message);
					//update gui elements for who is in the lobby
					break;
				case ServerMessage.WHO_ON_TBL:
					//remove code and <EOM> from message
					message = message.replace(ServerMessage.WHO_ON_TBL + " ", "");
					message = message.replace(" <EOM>", "");
					message = message.replace("<EOM>", "");
					//instantiate string to hold the players on the table
					//this will be added to the activeTablesStatus vector
					Vector<String> statusHold = new Vector<String>();
					//split the message into an array of strings by spaces
					//we will end up with an array of 3 strings
					String[] split = message.split(" ");

					//add the table id as an int to its vector
					activeTables.add(Integer.parseInt(split[0]));

					//run through the rest of the array aFnd add the name of the person in that spot or
					//vacant if there is nobody
					for (int i = 1; i < split.length; i++){
						if (split[i].equalsIgnoreCase("-1")){
							statusHold.add("free seat");
						}else{
							statusHold.add(split[i]);
						}
					}
					activeTableStatus.add(statusHold);
					break;
				case ServerMessage.TBL_LIST:
					Vector<Integer> tblHold = new Vector<Integer>();
					message = message.replace(ServerMessage.TBL_LIST + " ", "");
					message = message.replace(" <EOM>", "");
					message = message.replace("<EOM>", "");
					//if there are several tables split the string into an array of strings
					String[] tables = message.split(" ");
					//put the table ids into the active tables vector
					for (int i = 0; i < tables.length; i++){
						tblHold.add(Integer.parseInt(tables[i]));
					}
					for (int i = 0; i < tblHold.size(); i++){
						sendToServer.put("109 " + Player.getUsername() + " " + tblHold.get(i) + " <EOM>");
					}
					break;
				case ServerMessage.NOW_LEFT_LOBBY:
					message = message.replace(ServerMessage.NOW_LEFT_LOBBY + " ", "");
					message = message.replace(" <EOM>", "");
					message = message.replace("<EOM>", "");
					activeUsers.remove(message);
					//indicate that a user has left the lobby
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
					System.out.println("NET EXCEPTION");
					break;
				case ServerMessage.NAME_IN_USE:
				case ServerMessage.BAD_NAME:
					JFrame frame2 = new JFrame("Username Error");
			        // prompt the user to enter their name
			        out = JOptionPane.showInputDialog(frame2, "Username error. Re-enter username:").replaceAll("\n","");
					//Scanner to halt works here because the server needs a username before we can do anything else
					//This will be replaced with gui elements in the future
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
		//when gui elements established place prompt in the gui and obtain
		//username from gui elements
		//System.out.println(prompt);
		//String out = in.nextLine();
		out.replaceAll("\n", "");
		try {
			sendToServer.put(out);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
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
		if (Player.getUserStatus() == Status.IN_LOBBY)	{
			last = mainMenu.getLastPressed();
		}
		else {
			// TODO: Update this once inGameMenu is created
			//last = inGameMenu.getlastPressed();
			last = null;
		}
		switch (last)	{
		case START:
			// create a new game
			break;
		case JOIN:
			// prompt user for which game to join
			// attempt to join selected game
			break;
		case OBSERVE:
			// join a game as an observer
			break;
		case PRIVATE_CHAT:
			JFrame frame = new JFrame();
			System.out.println(activeUsers);
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
			// leave the current game
			break;
		case EXIT_BAAC:
			queueUpToSendToServer("108 " + Player.getUsername());
			break;
		}
	}
	
	/**
	 * Initializes all windows and classes necessary for the lobby
	 */
	private void setupLobby()	{
		lobbyChat = new LobbyChat(mediator);
		mainMenu = new MainMenuWindow(this);
		// TODO: make sure these are setup correctly and let 'em rip
		// activeUsersWindow = new ActiveUsersWindow(this);
		// activeTablesWindow = new ActiveTablesWindow(this);
	}


}
