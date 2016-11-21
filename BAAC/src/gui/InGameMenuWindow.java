package gui;
import java.util.Observable;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import baac.Game;
import baac.ObservableGame;

/*********************************************************************************
 * The InGameMenuWindow allows a player to select their actions from a GUI window
 * 
 * D. Sutherin, November 2016
 ********************************************************************************/

public class InGameMenuWindow extends Observable {
	private MenuButtonStatus lastPressed = null;
	JFrame frame;
	JPanel panel;
	
	/**
	 * Instantiating a InGameMenuWindow causes it to run immediately
	 */
	public InGameMenuWindow(Game g)	{
		addObserver(g);
		setupGUI();
	}
	
	public InGameMenuWindow(ObservableGame g)	{
		addObserver(g);
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
		panel.setLayout(new BoxLayout(panel, JFrame.EXIT_ON_CLOSE));
		frame.getContentPane().add(panel);
		
		// Add the 'Start Private Chat' button
		JButton chatButton = new JButton("Start Private Chat");
		chatButton.addActionListener(e -> {
			System.out.println("Starting private chat...");
			lastPressed = MenuButtonStatus.PRIVATE_CHAT;
			setChanged();
			notifyObservers();
		});
		panel.add(chatButton);

		// Add the 'Exit Game' button
		JButton exitButton = new JButton("Exit Game");
		exitButton.addActionListener(e -> {
			System.out.println("Exiting Game...");
			lastPressed = MenuButtonStatus.EXIT_GAME;
			setChanged();
			notifyObservers();
		});
		panel.add(exitButton);
		
		// Display the window.
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	// will need to remove close/minimize buttons
		frame.setSize(300, 400);
		frame.setTitle("Main Menu");
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
	
	/**
	 * Returns the last button pressed.  Used when Game calls update()
	 * @return lastPressed is a MenuButtonStatus corresponding to the last pressed button
	 */
	public MenuButtonStatus getLastPressed()	{
		return lastPressed;
	}

}
