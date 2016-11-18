package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Observable;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;

//import baac.Game;

public class GameBoardWindow extends Observable {
	
	/**
	 * Instantiating a GameBoardWindow creates the GUI immediately
	 * @param g is the Game to be displayed
	 */
	public GameBoardWindow(){//Game g)	{
		setupGUI();
	}
	
	/**
	 * setupGUI defines the initial setup of the LobbyUsersWindow
	 */	
	public void setupGUI() {
		// Initial window setup
		JFrame frame = new JFrame("Lobby Users List");
		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		Dimension buttonSize = new Dimension(80,80);
		
//		JPanel rowA = new JPanel();
//		rowA.setSize(300,400);;
//		frame.getContentPane().add(rowA);
//		
//		JButton a1 = buttonFactory(Color.RED, buttonSize);
//		a1.addActionListener(e -> {
//			
//		});
//		rowA.add(a1);
		
		String position;
		char row;
		JPanel thisRow;
		JButton thisButton;
		Color c;
		for (int i = 65; i < 73; i++)	{
			thisRow = new JPanel();
			container.add(thisRow);
			row = (char) i;
			for (int j = 1; j < 9; j++)	{
				if ((i % 2 == 1 && j % 2 == 1) || (i % 2 == 0 && j % 2 == 0))	{
					c = Color.RED;
				}
				else	{
					c = Color.BLACK;
				}
				thisButton = buttonFactory(c, buttonSize);
				position = row + String.valueOf(j);
				thisButton.setName(position);
				thisRow.add(thisButton);
			}
		}
		frame.getContentPane().add(container);

		// Display the window.
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	// will need to remove close/minimize buttons
		frame.setSize(300, 400);
		frame.setTitle("Users in Lobby");
		frame.pack();
		frame.setVisible(true);
	}
	
	public JButton buttonFactory(Color c, Dimension d)	{
		JButton newButton = new JButton();
		newButton.setBackground(c);
		newButton.setMinimumSize(d);
		newButton.setPreferredSize(d);
		newButton.addActionListener(e -> {
			handleClick(newButton);
		});
		return newButton;
	}
	
	public void handleClick(JButton b){
		
		setChanged();
		notifyObservers();
	}
}
