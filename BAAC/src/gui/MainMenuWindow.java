package gui;
import java.util.Observable;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import baac.BAAC;

/*********************************************************************************
 * The MainMenuWindow allows a player to select their actions from a GUI window
 * 
 * D. Sutherin, November 2016
 ********************************************************************************/

public class MainMenuWindow extends Observable {
	private MenuButtonStatus lastPressed = null;
	private JFrame frame;
	
	/**
	 * Instantiating a MainMenuWindow causes it to run immediately
	 */
	public MainMenuWindow(BAAC b)	{
		addObserver(b);
		setupGUI();
	}
	
	/**
	 * setupGUI defines the actions to be taken when a MainMenuWindow is created
	 * It also includes lambda functions for button event handlers
	 */
	public void setupGUI() {
		frame = new JFrame("Main Menu");
		
		JPanel panel = new JPanel();
		panel.setSize(300,400);
		panel.setLayout(new BoxLayout(panel, JFrame.EXIT_ON_CLOSE));
		frame.getContentPane().add(panel);
		
		// Add buttons to menu window
		
		// Add the 'Start Game' button
		JButton startButton = new JButton("Start Game");
		startButton.setSize(280,100);
		startButton.addActionListener(e -> {
			//TODO: Interact w/ BAAC
			System.out.println("Starting game...");
			lastPressed = MenuButtonStatus.START;
			setChanged();
			notifyObservers();
		});
		panel.add(startButton);
		
		// Add the 'Join Game' button
		JButton joinButton = new JButton("Join Game");
		joinButton.addActionListener(e -> {
			//TODO: Interact w/ BAAC
			System.out.println("Joining game...");
			lastPressed = MenuButtonStatus.JOIN;
			setChanged();
			notifyObservers();
		});
		panel.add(joinButton);
		
		// Add the 'Observe Game' button
		JButton observeButton = new JButton("Observe Game");
		observeButton.addActionListener(e -> {
			//TODO: Interact w/ BAAC
			System.out.println("Observing game...");
			lastPressed = MenuButtonStatus.OBSERVE;
			setChanged();
			notifyObservers();
		});
		panel.add(observeButton);

		// Add the 'Start Private Chat' button
		JButton chatButton = new JButton("Start Private Chat");
		chatButton.addActionListener(e -> {
			//TODO: Interact w/ BAAC
			System.out.println("Starting private chat...");
			lastPressed = MenuButtonStatus.PRIVATE_CHAT;
			setChanged();
			notifyObservers();
		});
		panel.add(chatButton);

		// Add the 'Exit' button
		JButton exitButton = new JButton("Exit");
		exitButton.addActionListener(e -> {
			//TODO: Interact w/ BAAC
			System.out.println("Exiting...");
			lastPressed = MenuButtonStatus.EXIT_BAAC;
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
	 * Returns the last button pressed.  Used when BAAC calls update()
	 * @return lastPressed is a MenuButtonStatus corresponding to the last pressed button
	 */
	public MenuButtonStatus getLastPressed()	{
		return lastPressed;
	}
	
	public void closeWindow(){
		frame.setVisible(false); 
		frame.dispose(); 
	}

}
