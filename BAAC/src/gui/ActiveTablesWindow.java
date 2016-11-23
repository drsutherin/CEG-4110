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
	JFrame frame;
	JPanel panel;
	/**
	 * Instantiating a ActiveTablesWindow causes it to run immediately
	 */
	public ActiveTablesWindow()	{
//		String currentTable;
//		for (int i = 0; i < tables.size(); i++)	{
//			currentTable = "Table " + tables.get(i)[0] + ": " + tables.get(i)[1]
//					+ " vs " + tables.get(i)[2];
//			tablesVector.add(currentTable);
//		}
		tablesList = new JList<String>();
		tablesVector = new Vector<String>();
		frame = new JFrame("Active Tables");
		panel = new JPanel();
		setupGUI();
	}
	
	/**
	 * setupGUI defines the initial setup of the ActiveTablesWindow
	 */	
	public void setupGUI() {
		// Initial window setup
		panel.setSize(300,400);
		panel.setLayout(new BoxLayout(panel, JFrame.EXIT_ON_CLOSE));
		frame.getContentPane().add(panel);
		
		// Add tables list the panel
		for (int i = 0; i < 5; i++)
			tablesVector.add("Testing");
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
	 * @param toAdd is a String array where [0]=table id, [1]=player1 username, [2]=player2 username
	 */
	public void add(String[] toAdd)	{
		// test whether table is already in the list before adding 
		boolean alreadyThere = false;
		int i = 0;
		String tbl;
		if (tablesVector.size() > 0) { // if the vector is empty, the table is obviously not there
			while (!alreadyThere && i < tablesVector.size()){
				tbl = tablesVector.get(i).substring(0,4);
				if (tbl.equals(toAdd[0]))	{
					alreadyThere = true;
				}
				i++;
			}
		}
		// add the table and update the window
		if (!alreadyThere){
			String currentTable = toAdd[0] + ": " + toAdd[1] + " vs " + toAdd[2];
			tablesVector.add(currentTable);
			tablesList.setListData(tablesVector);
			panel.removeAll();
			panel.add(tablesList);
			// Display the window.
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	// will need to remove close/minimize buttons
			frame.setSize(300, 400);
			frame.setTitle("Active Tables");
			frame.pack();
			frame.setVisible(true);
		}
	}
}