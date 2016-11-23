package gui;
import javax.swing.*;
import java.util.Vector;

/*********************************************************************************
 * The LobbyUsersWindow displays a list of the usernames of all users currently
 * active in the lobby.  No interactions are defined for the window at this time,
 * it is merely an informative tool.
 * 
 * D. Sutherin, November 2016
 ********************************************************************************/

public class LobbyUsersWindow {
	JList<String> lobbyUsersList;
	Vector<String> lobbyUsersVector;
	JFrame frame;
	JPanel panel;

	/**
	 * Instantiating a LobbyUsersWindow causes it to run immediately
	 * @param users contains the usernames of all users active in the lobby upon creation
	 */
	public LobbyUsersWindow()	{
		lobbyUsersVector = new Vector<String>();
		lobbyUsersList = new JList<String>();
		frame = new JFrame("Lobby Users List");
		panel = new JPanel();
		setupGUI();
	}
	
	/**
	 * setupGUI defines the initial setup of the LobbyUsersWindow
	 */	
	public void setupGUI() {
		// Initial window setup
		panel.setSize(300,400);
		panel.setLayout(new BoxLayout(panel, JFrame.EXIT_ON_CLOSE));
		frame.getContentPane().add(panel);
		
		// Add users list the panel
		lobbyUsersList.setListData(lobbyUsersVector);
		
		//TODO: frame/panel setPreferredSize and setMinumumSize and make scrollable
		/* ScrollPane was causing window to be tiny, TODO: fix LobbyUserWindow ScrollPane size */
		//JScrollPane usersPanel = new JScrollPane(lobbyUsersList);
		//usersPanel.setSize(new Dimension(50,100));
		//usersPanel.add(lobbyUsersList);		
		//panel.add(usersPanel);
		
		panel.add(lobbyUsersList);
		
		// Display the window.
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	// will need to remove close/minimize buttons
		frame.setSize(300, 400);
		frame.setTitle("Users in Lobby");
		frame.pack();
		frame.setVisible(true);
	}
	
	/**
	 * updateList is used to update the entire list of users in the lobby
	 * @param users is a Vector<String> containing all users currently in the lobby
	 */
	public void updateList(Vector<String> users)	{
		lobbyUsersVector.removeAllElements();
		for (int i = 0; i < users.size(); i ++){
			lobbyUsersVector.add(users.get(i));
		}
		lobbyUsersList.setListData(lobbyUsersVector);
		panel.removeAll();
		panel.add(lobbyUsersList);
		// Display the window.
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	// will need to remove close/minimize buttons
		frame.setSize(300, 400);
		frame.setTitle("Users in Lobby");
		frame.pack();
		frame.setVisible(true);
	}
	
	/**
	 * remove is used when a single users leaves the lobby
	 * @param toRemove is the username of the user who has left the lobby
	 */
	public void remove(String toRemove)	{
		if (lobbyUsersVector.contains(toRemove))	{
			lobbyUsersVector.remove(toRemove);
			lobbyUsersList.setListData(lobbyUsersVector);
			panel.removeAll();
			panel.add(lobbyUsersList);
			// Display the window.
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	// will need to remove close/minimize buttons
			frame.setSize(300, 400);
			frame.setTitle("Users in Lobby");
			frame.pack();
			frame.setVisible(true);
		}
	}
	
	/**
	 * add is used when a new user enters the lobby
	 * @param toAdd is the username of the user who has entered the lobby
	 */
	public void add(String toAdd)	{
		lobbyUsersVector.add(toAdd);
		lobbyUsersList.setListData(lobbyUsersVector);
		panel.removeAll();
		panel.add(lobbyUsersList);
		// Display the window.
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	// will need to remove close/minimize buttons
		frame.setSize(300, 400);
		frame.setTitle("Users in Lobby");
		frame.pack();
		frame.setVisible(true);
	}
	

	/**
	 * Closes the current window
	 */
	public void closeWindow(){
		frame.setVisible(false); 
		frame.dispose(); 
	}
}