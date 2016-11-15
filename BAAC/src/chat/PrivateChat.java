package chat;

import java.util.Observable;
import java.util.concurrent.LinkedBlockingQueue;

import baac.Peer;
import baac.PeerMediator;
import baac.Player;
import baac.ServerMessage;


/***
 * The private chat handles parsing messages to and from the server. The chat thread remains active until the user closes the
 * private chat window.
 * @author ulyZ
 *
 */
public class PrivateChat extends Peer implements Runnable {
	//Internal blocking Queues
	private final LinkedBlockingQueue<String> receiveFromServer = new LinkedBlockingQueue<String>();
	private final LinkedBlockingQueue<String> sendToServer = new LinkedBlockingQueue<String>();
	
	private String username = Player.getUsername();
	private PeerMediator mediator;
	
	private String chatBuddy;
	
	/***
	 * Constructor for private chat.
	 * @param passedMediator, the Mediator that sends and receives messages
	 * @param passedChatBuddy, the client this user will be chatting with
	 */
	public PrivateChat(PeerMediator passedMediator, String passedChatBuddy)	{
		mediator = passedMediator;
		mediator.addPeerClass(this);
		chatBuddy = passedChatBuddy;
	}
	
	/**
	 * Add's the message to privateChat's queue. The run() method will periodically 
	 * check to see if this queue has a message to send
	 * @param outgoingMessage: 102<client><buddy><message><EOM>
	 */
	public void formatMessageFromUI(String outgoingMessage) {
	     String formattedString = "102" + " " + username + " " + chatBuddy + " " + outgoingMessage + "<EOM>";
	     try {
			sendToServer.put(formattedString);
		} catch (InterruptedException e) {
			// TODO Add to log
		}
	}
	
	/**
	 * receives messages from the Server and parses them to send to the GUI to be displayed
	 * @param incomingMessage in the form "201 chatBuddy\r 1 message from buddy<EOM>"
	 * 	 index = 0 - is the code 201 
	 *	 index = 1 - the sender's name
	 *	 index = 3 - the code 0 for public, 1 for private
	 *	 index = 4 - the sender's message
	 */
	public void formatMessageFromServer(String incomingMessage){
		//splits the message into three parts based on the first two spaces it sees,
		String senderName = "";
		String senderMessage = "";
		String code = incomingMessage.substring(0, 3);
		switch(code){
		case ServerMessage.MSG:
			try{
				incomingMessage = incomingMessage.replace("\r ", " ");//Incoming message looks like: 201 Name\r 0 this is a message <EOM>
				incomingMessage = incomingMessage.replace("<EOM>", ""); //don't pass EOM to the GUI
				String inMessage[] = incomingMessage.split(" ", 4);
				senderName = inMessage[1];
				if (inMessage[3].equals("1") && senderName.equals(chatBuddy)){
					senderMessage = inMessage[2];
					String messageToUI[] = {senderName, senderMessage};
				}
			} catch(ArrayIndexOutOfBoundsException e){

			}
			//only display the message if the user is in the lobby
				//TODO: displayInUI(messageToUI);
			break;	
		}
	}

	/**
	 *  The thread is constantly checking the in and outgoing buffer
	 */
	@Override
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
		}
	}

	/**
	 * Mediator calls this class to place messages from the server into the class's private queue for
	 * parsing and decoding.
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
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}
}
