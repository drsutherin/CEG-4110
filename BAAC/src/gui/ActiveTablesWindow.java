package gui;

import java.awt.EventQueue;

import javax.swing.JFrame;

public class ActiveTablesWindow {

	private JFrame frmActiveTables;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ActiveTablesWindow window = new ActiveTablesWindow();
					window.frmActiveTables.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ActiveTablesWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmActiveTables = new JFrame();
		frmActiveTables.setOpacity(0.8f);
		frmActiveTables.setTitle("Active Tables");
		frmActiveTables.setBounds(100, 100, 450, 300);
		frmActiveTables.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
