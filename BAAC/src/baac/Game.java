package baac;
/**
 * * Will contain information regarding the current game:
  * Players
  * Status (active, waiting_for_opponent, waiting_for_server)
  * Turn
  * Board state *(make sure it matches the server's board state for easy checking)*
* Handles players' moves
  * Select piece
  * Move to location
* Handles responses from server
  * Valid vs. invalid moves
* Sends updates to GUI (for inGameToolBarWindow)
* 
 * @author reuintern
 *
 */
public class Game implements Runnable {
	String player1, player2;
	GameStatus status;
	String turn;
	String boardState;		// Same format as sent from server (see CheckersServerDocumentation)
	BAAC client;
	String tableID;			// Received from server
	
	/**
	 * Constructor for creating a new table
	 * @param tid is the table ID
	 */
	public Game(String tid)	{
		player1 = Player.getUsername();
		player2 = "";
		turn = "";
		boardState = "";
		tableID = tid;
	}
	
	/**
	 * Constructor for joining a table
	 * @param baac is the client
	 * @param tid is the existing table id
	 * @param opponent is the opponent's username
	 */
	public Game(String tid, String opponent){
		player1 = Player.getUsername();
		player2 = opponent;
		turn = "";
		boardState = "";
		tableID = tid;
	}

	public void run() {
		// TODO Start the game, bro!
		
	}
}
