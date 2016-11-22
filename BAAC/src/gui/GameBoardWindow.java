package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import baac.Game;
import baac.ObservableGame;

/*********************************************************************************
 * The GameBoardWindow is the GUI which the user will interact with to control
 * their moves during the game
 * 
 * D. Sutherin, November 2016
 ********************************************************************************/

public class GameBoardWindow extends Observable {

	public enum Turn { YOURS, THEIRS };

	int clicks;
	String[] move;
	Hashtable<String,JButton> boardSpacesDict;
	Vector<JButton> boardSpacesVector;
	boolean ready, moving;
	Turn turn;
	JFrame frame;
	String opponent;
	
	/**
	 * Instantiating a GameBoardWindow creates the GUI immediately
	 * @param g is the Game to be displayed
	 */
	public GameBoardWindow(Game g)	{
		clicks = 0;
		move = new String[2];
		boardSpacesDict = new Hashtable<String,JButton>();
		boardSpacesVector = new Vector<JButton>();
		ready = moving = false;
		turn = null;
		addObserver(g);
		setupGUI();
	}
	
	/**
	 * Instantiating a GameBoardWindow creates the GUI immediately
	 * @param g is the Game to be displayed
	 */
	public GameBoardWindow(ObservableGame g)	{
		clicks = 0;
		move = new String[2];
		boardSpacesDict = new Hashtable<String,JButton>();
		boardSpacesVector = new Vector<JButton>();
		ready = moving = false;
		turn = null;
		addObserver(g);
		setupGUI();
	}
	
	
	/**
	 * setupGUI defines the initial setup of the LobbyUsersWindow
	 */	
	public void setupGUI() {
		// Initial window setup
		frame = new JFrame("Lobby Users List");
		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		Dimension buttonSize = new Dimension(80,80);
		
		// Create the board
		String position;
		char col;
		JPanel thisRow;
		JButton thisButton;
		Color c;
		for (int i = 1; i < 9; i++)	{
			thisRow = new JPanel();
			container.add(thisRow);
			for (int j = 65; j < 73; j++) {
				col = (char) j;
				if ((i % 2 == 1 && j % 2 == 1) || (i % 2 == 0 && j % 2 == 0))	{
					c = java.awt.Color.RED;
				}
				else	{
					c = java.awt.Color.BLACK;
				}
				thisButton = buttonFactory(c, buttonSize);
				position = col + String.valueOf(i);
				thisButton.setName(position);
				thisRow.add(thisButton);
				boardSpacesDict.put(position,thisButton);
				boardSpacesVector.add(thisButton);
			}
		}
		frame.getContentPane().add(container);

		// Display the window.
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	// will need to remove close/minimize buttons
		frame.setSize(300, 400);
		frame.setTitle("Game Board");
		frame.pack();
		frame.setVisible(true);
	}
	
	/**
	 * Creates buttons to correspond to game board spaces
	 * @param c is the color of the button/space
	 * @param d is the size of the button/space
	 * @return the new JButton that was created
	 */
	public JButton buttonFactory(Color c, Dimension d)	{
		JButton newButton = new JButton();
		newButton.setBackground(c);
		newButton.setOpaque(true);
		newButton.setBorderPainted(false);
		newButton.setMinimumSize(d);
		newButton.setPreferredSize(d);
		return newButton;
	}
	
	/**
	 * Handles button clicks during play
	 * First click is piece to move, second click is where to move it
	 * @param b is the button that was clicked
	 */
	public void handleClick(JButton b){
		String name = b.getName();
		
		// Initial code for testing clicks
//		System.out.println("You clicked " + name);
//		if (b.getBackground() == Color.RED)	{
//			b.setBackground(Color.PINK);
//		}
//		else if (b.getBackground() == Color.BLACK)	{
//			b.setBackground(Color.GRAY);
//		}
//		else if (b.getBackground() == Color.PINK)	{
//			b.setBackground(Color.RED);
//		}
//		else	{
//			b.setBackground(Color.BLACK);
//		}
		
		// Create the string array corresponding to the move based on user clicks
		// Clicks should always be 0 or 1
		if (clicks == 0){
			move[0] = name;
			clicks++;
		}
		else	{
			// If they clicked the same space again, negate the first click
			if (move[0] == name)	{
				move[0] = "";
				clicks = 0;
			}
			else if (move[1] == name)	{
				move[1] = "";
				clicks = 1;
			}
			// If this is the second click, it's a different space, and original selection has a piece on it
			else if (boardSpacesDict.get(move[0]).getIcon() != null)	{
				move[1] = name;
				clicks = 0;  // reset clicks for next turn
				moving = true;
				// Set icon on space moving to
				boardSpacesDict.get(move[1]).setIcon(boardSpacesDict.get(move[0]).getIcon());
				// Set icon on space moving from
				boardSpacesDict.get(move[0]).setIcon(null);
				setTurn(Turn.THEIRS);
				setChanged();
				notifyObservers();
			}
			else	{
				move[0] = move[1] = "";
				clicks = 0;
			}
		}
	}
	
	/**
	 * Populates the game board with pieces with the Player's color at the bottom
	 * @param c is the color that the user has been assigned by the server
	 */
	public void setPlayerColor(Color c)	{
		// TODO: Set up game pieces on the board
		int row, col;
		if (c == Color.RED){
			// Set up red pieces on bottom of board
			for (JButton b : boardSpacesVector){
				col = (int) b.getName().charAt(0);
				row = Character.getNumericValue(b.getName().charAt(1));
				
				// if in row 1 or 3 & even column, add black piece
				if ((row == 1 || row == 3) && (col % 2 == 0))	{
					b.setIcon(new ImageIcon(getClass().getResource("black.png")));
				}
				// if in row 2 & odd column, add black piece
				else if ((row == 2) && (col % 2 == 1))	{
					b.setIcon(new ImageIcon(getClass().getResource("black.png")));
				}
				// if in row 6 or 8 & odd column, add red piece
				else if ((row == 6 || row == 8) && (col % 2 == 1))	{
					b.setIcon(new ImageIcon(getClass().getResource("red.png")));
				}
				// if in row 7 & even column, add red piece
				else if ((row == 7) && (col % 2 == 0))	{
					b.setIcon(new ImageIcon(getClass().getResource("red.png")));
				}
			}
		}
		else	{
			// Set up black pieces on bottom of board
			for (JButton b : boardSpacesVector){
				col = (int) b.getName().charAt(0);
				row = Character.getNumericValue(b.getName().charAt(1));
				
				// if in row 1 or 3 & odd column, add black piece
				if ((row == 1 || row == 3) && (col % 2 == 0))	{
					b.setIcon(new ImageIcon(getClass().getResource("red.png")));
				}
				// if in row 2 & even column, add black piece
				else if ((row == 2) && (col % 2 == 1))	{
					b.setIcon(new ImageIcon(getClass().getResource("red.png")));
				}
				// if in row 6 or 8 & odd column, add red piece
				else if ((row == 6 || row == 8) && (col % 2 == 1))	{
					b.setIcon(new ImageIcon(getClass().getResource("black.png")));
				}
				// if in row 7 & even column, add red piece
				else if ((row == 7) && (col % 2 == 0))	{
					b.setIcon(new ImageIcon(getClass().getResource("black.png")));
				}
			}
		}
		
	}
	
	/**
	 * Sets the event handlers for all of the game board buttons based on
	 * whether it's the user's turn or not
	 * @param t is whose turn it is
	 */
	public void setTurn(Turn t)	{
		turn = t;
		
		if (turn == Turn.THEIRS){
			for (JButton b : boardSpacesVector) {
				// Remove all event listeners on the buttons
				ActionListener[] a = b.getActionListeners();
				for (ActionListener act : a){
					b.removeActionListener(act);
				}
				// Make all buttons unclickable
				b.addActionListener(e -> {
					JFrame frame = new JFrame("Hold your horses");
			        JOptionPane.showMessageDialog(frame, "It's not your turn", "Hold your horses", JOptionPane.ERROR_MESSAGE);
				});
			}
		}
		else {
			for (JButton b : boardSpacesVector) {
				// Remove all event listeners on the buttons
				ActionListener[] a = b.getActionListeners();
				for (ActionListener act : a){
					b.removeActionListener(act);
				}
				// Make the buttons clickable
				b.addActionListener(e -> {
					handleClick(b);
				});
			}
			JFrame frame = new JFrame("Your turn");
	        JOptionPane.showMessageDialog(frame, "It's your turn");
		}
	}
	
	/**
	 * Allows the Game class to check whether the user is ready
	 * @return ready corresponds to whether the user is ready to play
	 */
	public boolean getReadyFlag(){
		return ready;
	}
	
	/**
	 * Allows the Game class to set the user's ready status
	 * @param b is the user's new status
	 */
	public void setReadyFlag(boolean b)	{
		ready = b;
	}
	
	/**
	 * Allows the Game class to check whether the user is trying to send a move
	 * @return moving corresponds to whether the user is ready to move
	 */
	public boolean getMoveFlag(){
		return moving;
	}
	
	/**
	 * Allows the Game class to set the user's moving status
	 * @param b is the user's new status
	 */
	public void setMoveFlag(boolean b){
		moving = b;
	}
	
	
	/**
	 * Allows the Game class to get the last move
	 * @return move is a string array where [0]=original position [1]=new position
	 */
	public String[] getMove()	{
		return move;
	}
	
	/**
	 * Updates the positions of all pieces on the board
	 * @param b is a byte array corresponding to board locations as represented by the server 
	 */
	public void updateBoard(byte[][] b)	{
		int k = 0;
		JButton current;
		for (int i = 0; i < 8; i++){
			for (int j = 0; j < 8; j++){
				current = boardSpacesVector.get(i);
				switch (b[i][j])	{
				case 0: // nothing
					current.setIcon(new ImageIcon(getClass().getResource("")));
					break;
				case 1: // black
					current.setIcon(new ImageIcon(getClass().getResource("black.png")));
					break;
				case 2: // red
					current.setIcon(new ImageIcon(getClass().getResource("red.png")));
					break;
				case 3: // black king
					current.setIcon(new ImageIcon(getClass().getResource("black-king.png")));
					break;
				case 4: // red king
					current.setIcon(new ImageIcon(getClass().getResource("red-king.png")));
					break;
				}
			}
			
		}
	}
	
	/**
	 * Closes the GameBoardWindow
	 */
	public void closeWindow(){
		frame.setVisible(false); 
		frame.dispose(); 
	}
	
	/**
	 * Sets the opponent for the game when Game receives WHO_ON_TBL
	 * @param s is the name of the opponent, "-1" means waiting for opponent, anything else is assumed to be a valid username
	 */
	public void setOpponent(String s)	{
		JFrame frame = new JFrame();
		if (s.equals("-1"))	{
			JOptionPane.showMessageDialog(frame, "Waiting for opponent...");
		}
		else	{
			opponent = s;
			if (!ready) {
				JOptionPane.showMessageDialog(frame, "Ready to play?");
				ready = true;
				setChanged();
				notifyObservers();
			}
		}
	}
	
}
