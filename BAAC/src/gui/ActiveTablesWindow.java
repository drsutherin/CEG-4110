package gui;
import javax.swing.*;
import java.util.Vector;

/*********************************************************************************
 * The ActiveTablesWindow displays a list of the table IDs of all tables currently
 * active on the server.  No interactions are defined for the window at this time,
 * it is merely an informative tool.
 * 
 * D. Sutherin, November 2016
 ********************************************************************************/

public class ActiveTablesWindow {
	JList<String> tablesList;
	Vector<String> tablesVector;

	/**
	 * Instantiating a ActiveTablesWindow causes it to run immediately
	 * @param tables contains a list of the IDs of all active tables
	 */
	public ActiveTablesWindow(Vector<String> tables)	{
		tablesVector = tables;
		setupGUI();
	}
	
	/**
	 * setupGUI defines the initial setup of the ActiveTablesWindow
	 */	
	public void setupGUI() {
		// Initial window setup
		JFrame frame = new JFrame("Active Tables");
		
		JPanel panel = new JPanel();
		panel.setSize(300,400);
		panel.setLayout(new BoxLayout(panel, JFrame.EXIT_ON_CLOSE));
		frame.getContentPane().add(panel);
		
		// Add tables list the panel
		tablesList = new JList<String>();
		tablesList.setListData(tablesVector);
		
		/* ScrollPane was causing window to be tiny, TODO: fix LobbyUserWindow ScrollPane size */
		//JScrollPane usersPanel = new JScrollPane(lobbyUsersList);
		//usersPanel.setSize(new Dimension(50,100));
		//usersPanel.add(lobbyUsersList);		
		//panel.add(usersPanel);
		
		panel.add(tablesList);
		
		// Display the window.
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	// will need to remove close/minimize buttons
		frame.setSize(300, 400);
		frame.setTitle("Active Tables");
		frame.pack();
		frame.setVisible(true);
	}
	
	/**
	 * updateList is used to update the entire list of currently active tables
	 * @param users is a Vector<String> containing all active tables
	 */
	public void updateList(Vector<String> tables)	{
		tablesVector = tables;
		// Change this so it displays in a nicer format
		tablesList.setListData(tablesVector);
	}
	
	/**
	 * remove is used when a single table is destroyed
	 * @param toRemove is the ID number of the destroyed table
	 */
	public void remove(String toRemove)	{
		if (tablesVector.contains(toRemove))	{
			tablesVector.remove(toRemove);
			tablesList.setListData(tablesVector);
		}
	}
	
	/**
	 * add is used when a new table is created
	 * @param toAdd is the new table's ID number
	 */
	public void add(String toAdd)	{
		tablesVector.add(toAdd);
		// Change this so it displays in a nicer format
		tablesList.setListData(tablesVector);
	}
}