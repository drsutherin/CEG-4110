package gui;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/*********************************************************************************
 * The MainMenuWindow allows a player to select their actions from a GUI window
 * 
 * D. Sutherin, November 2016
 ********************************************************************************/

public class MainMenuWindow {

	/**
	 * Instantiating a MainMenuWindow causes it to run immediately
	 */
	public MainMenuWindow()	{
		setupGUI();
	}
	
	/**
	 * setupGUI defines the actions to be taken when a MainMenuWindow is created
	 * It also includes lambda functions for button event handlers
	 */
	public void setupGUI() {
		JFrame frame = new JFrame("Main Menu");
		
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
		});
		panel.add(startButton);
		
		// Add the 'Join Game' button
		JButton joinButton = new JButton("Join Game");
		joinButton.addActionListener(e -> {
			//TODO: Interact w/ BAAC
			System.out.println("Joining game...");
		});
		panel.add(joinButton);
		
		// Add the 'Observe Game' button
		JButton observeButton = new JButton("Observe Game");
		observeButton.addActionListener(e -> {
			//TODO: Interact w/ BAAC
			System.out.println("Observing game...");
		});
		panel.add(observeButton);

		// Add the 'Start Private Chat' button
		JButton chatButton = new JButton("Start Private Chat");
		chatButton.addActionListener(e -> {
			//TODO: Interact w/ BAAC
			System.out.println("Starting private chat...");
		});
		panel.add(chatButton);

		// Add the 'Exit' button
		JButton exitButton = new JButton("Exit");
		exitButton.addActionListener(e -> {
			//TODO: Interact w/ BAAC
			System.out.println("Exiting...");
			// Send exit command to ServerInterface
			// Close all windows
			// System.exit(0);
		});
		panel.add(exitButton);
		
		
		// Display the window.
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	// will need to remove close/minimize buttons
		frame.setSize(300, 400);
		frame.setTitle("Main Menu");
		frame.pack();
		frame.setVisible(true);
	}

}