package gui;
import java.awt.Dimension;
import java.util.Observable;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/*********************************************************************************
 * The InGameToolbarWindow simply shows who is playing on the board
 * 
 * D. Sutherin, November 2016
 ********************************************************************************/

public class InGameToolbarWindow {
	private MenuButtonStatus lastPressed = null;
	JFrame frame;
	JPanel panel;
	JButton redPlayer, blackPlayer;
	JLabel vs;
	
	/**
	 * Instantiating a InGameMenuWindow causes it to run immediately
	 */
	public InGameToolbarWindow()	{;
		setupGUI();
	}
	
	/**
	 * setupGUI defines the actions to be taken when a InGameMenuWindow is created
	 * It also includes lambda functions for button event handlers
	 */
	public void setupGUI() {
		frame = new JFrame("Main Menu");
		
		panel = new JPanel();
		panel.setSize(300,400);
		//panel.setLayout(new BoxLayout(panel, JFrame.EXIT_ON_CLOSE));
		frame.getContentPane().add(panel);

		Dimension size = new Dimension(50,100);
		redPlayer = new JButton();
		redPlayer.setMinimumSize(size);
		redPlayer.setBackground(java.awt.Color.RED);
		redPlayer.setOpaque(true);
		redPlayer.setBorderPainted(false);
		panel.add(redPlayer);
		vs = new JLabel(" vs ");
		panel.add(vs);
		blackPlayer = new JButton();
		blackPlayer.setMinimumSize(size);
		blackPlayer.setBackground(java.awt.Color.GRAY);
		blackPlayer.setOpaque(true);
		blackPlayer.setBorderPainted(false);
		panel.add(blackPlayer);
		
		// Display the window.
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	// will need to remove close/minimize buttons
		frame.setSize(300, 400);
		frame.setTitle("Main Menu");
		frame.pack();
		frame.setVisible(true);
	}
	
	public void setPlayerColors(String red, String black){
		redPlayer.setText(red);
		blackPlayer.setText(black);
		panel.removeAll();
		panel.add(redPlayer);
		panel.add(vs);
		panel.add(blackPlayer);
		// Display the window.
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	// will need to remove close/minimize buttons
		frame.setSize(300, 400);
		frame.setTitle("Users in Lobby");
		frame.pack();
		frame.setVisible(true);
	}
	
	/**
	 * Called by Game class to shutdown windows
	 */
	public void closeWindow(){
		frame.setVisible(false); 
		frame.dispose(); 
	}

}
