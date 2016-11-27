package gui;

import java.awt.event.ActionEvent;
import java.util.Observable;
import java.util.Vector;
import javax.swing.*;

import com.sun.glass.events.KeyEvent;

import chat.LobbyChat;

/*********************************************************************************
 * The LobbyUsersWindow displays a list messages and supplies a text area and 
 * send button for users to participate in a chat
 * 
 * D. Sutherin, November 2016
 ********************************************************************************/

public class LobbyChatWindow extends Observable {

	private Vector<String> viewableMessages;
	private Vector<String> rawMessages;
	private JList<String>messagesList;	
	private JFrame frame;
	JButton sendButton = new JButton("Send");
	private KeyStroke keyStroke;
	
	private Action enterClick = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            sendButton.doClick();
        }
    };
	
	
	
	public LobbyChatWindow(LobbyChat l){
		addObserver(l);
		viewableMessages = new Vector<String>();
		rawMessages = new Vector<String>();
		setupGUI();
	}

	/**
	 * Initiates the LobbyChatWindow
	 */
	private void setupGUI()	{
		// Initial window setup
		frame = new JFrame("Lobby Chat");
		
		
		
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
		
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		Object actionKey = outbox.getInputMap(
                JComponent.WHEN_FOCUSED).get(keyStroke);
		
		outbox.getActionMap().put(actionKey, enterClick);
		// Add 'Send' button w/ listener that calls setChanged and notifyObservers
		
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
		panel.add(sendButton);
		
		
		
		// Display the window.
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	// will need to remove close/minimize buttons
		frame.setSize(300, 400);
		frame.setTitle("Lobby Chat");
		frame.pack();
		frame.setVisible(true);
	}
	
	/**
	 * Adds a message to the Vector of messages and updates the GUI
	 * @param incoming is a 2D String array where [0]=username, [1]=message
	 */
	public void addMessage(String[] incoming){
		rawMessages.add(incoming[1]);
		String newText = incoming[0]+":    "+incoming[1];
		viewableMessages.add(newText);
		messagesList.setListData(viewableMessages);
	}
	
	/**
	 * Returns the list of messages
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
	
	public void closeWindow(){
		frame.setVisible(false); 
		frame.dispose(); 
	}
}
