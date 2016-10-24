package chat;

import java.util.Vector;

public class LobbyChat {
	Vector<String[]> theChat;		// Contains all chat messages, each entry is a string array where
									//		string[0] = senderName, string[1] = message
	String message;
	
	public LobbyChat()	{
		theChat = new Vector<String[]>();
		message = "";
	}
	
	
	
	
}
