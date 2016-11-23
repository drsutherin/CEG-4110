package chat;

import java.util.Observable;
import java.util.concurrent.LinkedBlockingQueue;

import baac.*;
import gui.LobbyChatWindow;


/***
 * The chat handles parsing messages to and from the server. The chat thread remains active until the system is
 * disconnected
 * @author Z. Rhodes, D. Sutherin
 *
 */
public class LobbyChat extends Peer implements Runnable {
	//Internal blocking Queues
	private final LinkedBlockingQueue<String> receiveFromServer = new LinkedBlockingQueue<String>();
	private final LinkedBlockingQueue<String> sendToServer = new LinkedBlockingQueue<String>();
	
	private String username = Player.getUsername();
	private PeerMediator mediator;
	private boolean shutdown = false;
	
	private Status playerStatus = Player.getUserStatus();
	
	LobbyChatWindow chatWindow;
	
	/***
	 * constructor with passed consumer-producer buffer
	 * lobby chat is a producer of outgoingMessages, the server-interface is the consumer
	 * @param messageQueue, the shared buffer, messages to be to be sent to the server are directed 
	 * 		to the server-interface via this shared buffer
	 */
	public LobbyChat(PeerMediator passedMediator)	{
		mediator = passedMediator;
		mediator.addPeerClass(this);
		chatWindow = new LobbyChatWindow(this);
	}
	
	/**
	 * Add's the message to the lobbyChat queue. Holding the internal (intermediate) queue will prevent 
	 * the chat thread from stopping when the  shared buffer is full or unavailable
	 * @param outgoingMessage 
	 */
	public void formatMessageFromUI(String outgoingMessage) {
	     String formattedString = "101" + " " + username + " " + outgoingMessage + "<EOM>";
	     try {
			sendToServer.put(formattedString);
		} catch (InterruptedException e) {
			// TODO Add to log
		}
	}
	
	/**
	 * receives messages from the Server and parses them to send to the GUI to be displayed
	 * @param incomingMessage
	 */
	public void formatMessageFromServer(String incomingMessage){
		//splits the message into three parts based on the first two spaces it sees,
		// index = 0 - is the number 101 - not used
		// index = 1 - the sender's name
		// index = 3 - the code 0 for public, 1 for private
		// index = 4 - the sender's message
		String senderName = "";
		String senderMessage = "";
		String code = incomingMessage.substring(0, 3);
		
		switch(code){
		case ServerMessage.MSG:
			try{
				incomingMessage = incomingMessage.replace("\r ", " ");//Incoming message looks like: 201 Name\r 0 this is a message <EOM>
				incomingMessage = incomingMessage.replace("<EOM>", ""); //don't pass EOM to the GUI
				String inMessage[] = incomingMessage.split(" ");
				senderName = inMessage[1];
				String username = Player.getUsername();
				if (!senderName.equals(username))	{
					for (int i = 3; i < inMessage.length; i++)	{
						senderMessage = senderMessage + " " + inMessage[i];
					}
					String messageToUI[] = new String[]{senderName, senderMessage};
					//only display the message if the user is in the lobby
					if (Player.getUserStatus() == Status.IN_LOBBY){
						displayInUI(messageToUI);
					}
				}
				break;
			} catch(ArrayIndexOutOfBoundsException e){
				
			}	
		}
	}

	/**
	 * Sends a message to the LobbyChatWindow
	 * @param messageToUI is a 2D String array where [0]=username, [1]=message
	 */
	private void displayInUI(String[] messageToUI) {
		chatWindow.addMessage(messageToUI);
	}

	/**
	 * Adds the message to the client's shared outgoing buffer
	 */
	@Override
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
					//TODO Log in error log
				};
			}
			
			//get message from server
			String incomingMessage;
			while (!receiveFromServer.isEmpty()){
				try{
					//get the message (received from the mediator)
					incomingMessage =  receiveFromServer.take();
					//verify it is a pertinent message and format to prepare to send to server 
					formatMessageFromServer(incomingMessage);
				}catch (InterruptedException e) {
					//TODO Log in error log
				};			
			}
			
			sleepyThread();
			
		}
	}

	@Override
	public void receiveFromMediator(String message) {
		
		try {
			receiveFromServer.put(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
		
	}

	/**
	 * Gets the most recent message from the GUI (presumably one just 
	 * typed by the user) and adds it to the outgoing queue of messages
	 */
	@Override
	public void update(Observable o, Object arg) {
		try {
			String outFinalDraft = "101 " + Player.getUsername() + " " + chatWindow.getLastMessage() + "<EOM>";
			sendToServer.put(outFinalDraft);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
		}
	}
	
	public void shutdown(){
		shutdown = true;
		chatWindow.closeWindow();
	}
	
	
	
}
