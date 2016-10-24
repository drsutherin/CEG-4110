package chat;

import java.util.Vector;

public class PrivateChat {
	String chatBuddy;
	Vector<String[]> messages;		// a Vector containing string arrays where:
									// string[0] = senderName, string[1] = message
	
	public PrivateChat(String buddy)	{
		chatBuddy = buddy;
		messages = new Vector<String[]>();
	}
}
