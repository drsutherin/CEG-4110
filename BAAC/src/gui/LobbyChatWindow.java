package gui;

import java.util.Observable;
import java.util.Vector;
import javax.swing.*;
import chat.LobbyChat;

/*********************************************************************************
 * The LobbyUsersWindow displays a list messages and supplies a text area and 
 * send button for users to participate in a chat
 * 
 * D. Sutherin, November 2016
 ********************************************************************************/

public class LobbyChatWindow extends Observable {

	Vector<String> messages;
	JList<String>messagesList;
	
	public LobbyChatWindow(LobbyChat l){
		addObserver(l);
		messages = new Vector<String>();
		setupGUI();
	}

	/**
	 * Initiates the LobbyChatWindow
	 */
	private void setupGUI()	{
		// Initial window setup
		JFrame frame = new JFrame("Lobby Chat");
		
		JPanel panel = new JPanel();
		panel.setSize(300,400);
		panel.setLayout(new BoxLayout(panel, JFrame.EXIT_ON_CLOSE));
		frame.getContentPane().add(panel);
		
		// Add list for chat text
		messagesList = new JList<String>();
		JScrollPane messagePanel = new JScrollPane(messagesList);
		panel.add(messagePanel);
		
		// Add text box for user input
		JTextArea outbox = new JTextArea(5,50);
		outbox.setLineWrap(true);
		panel.add(outbox);
		
		// Add 'Send' button w/ listener that calls setChanged and notifyObservers
		JButton sendButton = new JButton("Send");
		sendButton.addActionListener(e -> {
			//TODO: Interact w/ BAAC
			String msg = outbox.getText();
			System.out.println(msg);
			outbox.setText("");
			msg = "Me:    " + msg;
			messages.add(msg);
			messagesList.setListData(messages);
			setChanged();
			notifyObservers();
		});
		panel.add(sendButton);
		
		// Display the window.
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	// will need to remove close/minimize buttons
		frame.setSize(300, 400);
		frame.setTitle("Main Menu");
		frame.pack();
		frame.setVisible(true);
	}
	
	/**
	 * Adds a message to the Vector of messages and updates the GUI
	 * @param incoming is a 2D String array where [0]=username, [1]=message
	 */
	public void addMessage(String[] incoming){
		String newText = incoming[0]+":    "+incoming[1];
		messages.add(newText);
		messagesList.setListData(messages);
	}
	
	/**
	 * Returns the list of messages
	 */
	public Vector<String> getMessages()	{
		return messages;
	}
	
	/**
	 * Returns the most recent message
	 */
	public String getLastMessage()	{
		return messages.lastElement();
	}
}
