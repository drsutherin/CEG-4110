package chat;

import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

import baac.Player;

public class LobbyChat implements Runnable {
	//the client's shared buffer for outgoing messages
	private LinkedBlockingQueue<String> clientMessageQueue;
	
	//a class-specific intermediate buffer
	private Queue<String> outgoingLobbyChatQueue;
	private String username = Player.getUsername();
	
	/***
	 * constructor with passed consumer-producer buffer
	 * lobby chat is a producer of outgoingMessages, the server-interface is the consumer
	 * @param messageQueue, the shared buffer, messages to be to be sent to the server are directed 
	 * 		to the server-interface via this shared buffer
	 */
	public LobbyChat(LinkedBlockingQueue<String> messageQueue)	{
		clientMessageQueue = messageQueue;
	}
	
	/**
	 * Add's the message to the lobbyChat queue. Holding the internal (intermediate) queue will prevent 
	 * the chat thread from stopping when the  shared buffer is full or unavailable
	 * @param outgoingMessage
	 */
	public void outgoingMessage(String outgoingMessage){
	     String formattedString = "101" + " " + username + " " + outgoingMessage + "<EOM>";
	     outgoingLobbyChatQueue.add(formattedString);
	     run();
	}
	
	/**
	 * receives messages from the Server and parses them to send to the GUI to be displayed
	 * @param incomingMessage
	 */
	public void incomingMessage(String incomingMessage){
		//splits the message into three parts based on the first two spaces it sees,
		// index = 0 - is the number 101 - not used
		// index = 1 - the sender's name
		// index = 2 - the sender's message
		String inMessage[] = incomingMessage.split(" ", 3);
		String senderName = inMessage[1];
		String senderMessage = inMessage[2];		
		//TODO pass message and username to the gui
	}

	/**
	 * Adds the message to the client's shared outgoing buffer
	 */
	@Override
	public void run() {
		//get the message from the internal queue
		 String outgoingMessage = outgoingLobbyChatQueue.poll();
		 while (outgoingMessage != null){
			try {
				//place the message in the shared queue
				clientMessageQueue.put(outgoingMessage);
			} catch (InterruptedException e) {
				//TODO Log in error log
			}
			outgoingMessage = outgoingLobbyChatQueue.poll();
		}
	}
}
