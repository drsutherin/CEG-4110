package chat;

import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

import baac.Player;

public class LobbyChat {
	Vector<String[]> theChat;		// Contains all chat messages, each entry is a string array where
									//		string[0] = senderName, string[1] = message
	String message;
	LinkedBlockingQueue<String> clientMessageQueue;
	String username = Player.getUsername();
	
	
	public LobbyChat(LinkedBlockingQueue<String> messageQueue)	{
		theChat = new Vector<String[]>();
		message = "";
		//messages to be to be sent to the server are directed to the server-interface via this shared buffer
		clientMessageQueue = messageQueue;
	}
	
	//lobby chat is a producer of outgoingMessages, the server interface is the consumer
	public void outGoingMessage(String outgoingMessage){
	     String formattedString = "101" + " " + username + " " + outgoingMessage + "<EOM>";
	     //String outMessage[] = {"101", username, outgoingMessage};
		 try {
			clientMessageQueue.put(formattedString);
		} catch (InterruptedException e) {
			//ToDo Log in error log
		}
	}
	
	public void incomingMessage(String incomingMessage){
		//splits the message into three parts,
		// index = 0 - is the number 101 -ignored
		// index = 1 - the sender's name
		// index = 2 - the sender's message
		String inMessage[] = incomingMessage.split(" ", 3);
		String senderName = inMessage[1];
		String senderMessage = inMessage[2];		
		//ToDo pass message to the gui
	}
	
	
}
