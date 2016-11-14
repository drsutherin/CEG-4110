package chat;

import java.util.concurrent.LinkedBlockingQueue;

import baac.Peer;
import baac.PeerMediator;
import baac.Player;
import baac.ServerMessage;


/***
 * The chat handles parsing messages to and from the server. The chat thread remains active until the system is
 * disconnected
 * @author ulyZ
 *
 */
public class LobbyChat extends Peer implements Runnable {
	//Internal blocking Queues
	private final LinkedBlockingQueue<String> receiveFromServer = new LinkedBlockingQueue<String>();
	private final LinkedBlockingQueue<String> sendToServer = new LinkedBlockingQueue<String>();
	
	private String username = Player.getUsername();
	private PeerMediator mediator;
	
	/***
	 * constructor with passed consumer-producer buffer
	 * lobby chat is a producer of outgoingMessages, the server-interface is the consumer
	 * @param messageQueue, the shared buffer, messages to be to be sent to the server are directed 
	 * 		to the server-interface via this shared buffer
	 */
	public LobbyChat(PeerMediator passedMediator)	{
		mediator = passedMediator;
		mediator.addPeerClass(this);
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
		// index = 2 - the sender's message
		String senderName = "";
		String senderMessage = "";
		if (incomingMessage.startsWith(ServerMessage.MSG_ALL)){
			try{
				String inMessage[] = incomingMessage.split(" ", 3);//change back to 3
				senderName = inMessage[1];
				senderMessage = inMessage[2];	
			} catch(ArrayIndexOutOfBoundsException e){
				senderName = "error";
				senderMessage = "incorrect message format";
			}
		}
		String messageToUI[] = {senderName, senderMessage};
		//System.out.println("Server Recieved: " + senderName + ", " + senderMessage);
		//displayInUI(messageToUI);
	}

	/**
	 * Adds the message to the client's shared outgoing buffer
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

	@Override
	public void receiveFromMediator(String message) {
		
		try {
			receiveFromServer.put(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
		
	}
}
