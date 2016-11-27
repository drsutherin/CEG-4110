package gui;
import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Observable;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
		panel.setSize(800,800);
		panel.setLayout(new BoxLayout(panel, JFrame.EXIT_ON_CLOSE));
		frame.getContentPane().add(panel);
		
		//adding title and logo to main menu
		JLabel logoLabel = new JLabel();
		JLabel sparkles = new JLabel();
		JLabel titleLabel = new JLabel();
		titleLabel.setIcon(new ImageIcon(getClass().getResource("title.png")));
		logoLabel.setIcon(new ImageIcon(getClass().getResource("logo.png")));
		sparkles.setIcon(new ImageIcon(getClass().getResource("sparkle.gif")));
		titleLabel.setAlignmentX(titleLabel.CENTER_ALIGNMENT);
		panel.add(titleLabel);
		logoLabel.setAlignmentX(logoLabel.CENTER_ALIGNMENT);
		
		panel.add(logoLabel);
		logoLabel.add(sparkles);
		
		
		// Add buttons to menu window
		// Add the 'Start Game' button
		JButton startButton = new JButton("Start Game");
		//startButton.setSize(280,100);
		startButton.addActionListener(e -> {
			//TODO: Interact w/ BAAC
			System.out.println("Starting game...");
			lastPressed = MenuButtonStatus.START;
			setChanged();
			notifyObservers();
		});
		startButton.setAlignmentX(startButton.CENTER_ALIGNMENT);

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
		joinButton.setAlignmentX(joinButton.CENTER_ALIGNMENT);
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
		observeButton.setAlignmentX(observeButton.CENTER_ALIGNMENT);

		panel.add(observeButton);

		// Add the 'Start Private Chat' button
		JButton chatButton = new JButton("Start Private Chat");
		chatButton.addActionListener(e -> {
			//TODO: Interact w/ BAAC
			//chatButton.setSize(300,400);
			System.out.println("Starting private chat...");
			lastPressed = MenuButtonStatus.PRIVATE_CHAT;
			setChanged();
			notifyObservers();
		});
		chatButton.setAlignmentX(chatButton.CENTER_ALIGNMENT);

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
		exitButton.setAlignmentX(exitButton.CENTER_ALIGNMENT);

		
		
		// Display the window.
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	// will need to remove close/minimize buttons
		frame.setSize(300, 800);
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
