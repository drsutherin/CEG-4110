package gui;

import java.util.Observable;
import java.util.Vector;
import javax.swing.*;
import chat.PrivateChat;

/*********************************************************************************
 * The LobbyUsersWindow displays a list messages and supplies a text area and 
 * send button for users to participate in a chat
 * 
 * D. Sutherin, November 2016
 ********************************************************************************/

public class PrivateChatWindow extends Observable {

	private Vector<String> viewableMessages;
	private Vector<String> rawMessages;
	private JList<String>messagesList;
	String chatBuddy;
	Thread myThread;
	PrivateChat myPChat;
	
	public PrivateChatWindow(PrivateChat p, String buddy){
		addObserver(p);
		myPChat = p;
		viewableMessages = new Vector<String>();
		rawMessages = new Vector<String>();
		chatBuddy = buddy;
		setupGUI();
	}

	/**
	 * Initiates the PrivateChatWindow
	 */
	private void setupGUI()	{
		// Initial window setup
		JFrame frame = new JFrame("Private Chat");
		
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
			rawMessages.add(msg);
			msg = "Me:    " + msg;
			viewableMessages.add(msg);
			messagesList.setListData(viewableMessages);
			setChanged();
			notifyObservers();
		});
		
		
		
		
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(e -> {
			stopChat();
			frame.setVisible(false);
			frame.dispose();
		});
		panel.add(sendButton);
		panel.add(closeButton);
		
		// Display the window.
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	// will need to remove close/minimize buttons
		frame.setSize(300, 400);
		frame.setTitle("Private Chat with " + chatBuddy);
		frame.pack();
		frame.setVisible(true);
	}
	
	/**
	 * Adds a message to the Vector of messages and updates the GUI
	 * @param incoming is a String containing the new message from user's chat partner
	 */
	public void addMessage(String[] incoming){
		rawMessages.add(incoming[1]);
		String newText = chatBuddy+":    "+incoming;
		viewableMessages.add(newText);
		messagesList.setListData(viewableMessages);
	}
	
	/**
	 * Returns all messages
	 */
	public Vector<String> getMessages()	{
		return rawMessages;
	}
	
	/**
	 * Returns the most recent message
	 */
	public String getLastMessage()	{
		return rawMessages.lastElement();
	}
	/*
	 * Stops the thread for the private chat that this window is running
	 */
	private void stopChat(){
		myPChat.stop();
	}
}
