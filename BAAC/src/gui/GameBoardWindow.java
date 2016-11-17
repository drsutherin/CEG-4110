package gui;

import java.awt.Color;
import java.util.Observable;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;

import baac.Game;

public class GameBoardWindow extends Observable {
	
	/**
	 * Instantiating a GameBoardWindow creates the GUI immediately
	 * @param g is the Game to be displayed
	 */
	public GameBoardWindow(Game g)	{
		setupGUI();
	}
	
	/**
	 * setupGUI defines the initial setup of the LobbyUsersWindow
	 */	
	public void setupGUI() {
		// Initial window setup
		JFrame frame = new JFrame("Lobby Users List");
		
		JPanel rowA = new JPanel();
		rowA.setSize(300,400);;
		frame.getContentPane().add(rowA);
		
		JButton a1 = new JButton();
		a1.setBackground(Color.RED);
		
		
		rowA.add(a1);
		
		// Display the window.
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	// will need to remove close/minimize buttons
		frame.setSize(300, 400);
		frame.setTitle("Users in Lobby");
		frame.pack();
		frame.setVisible(true);
	}
}
