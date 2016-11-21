package baac;

import java.awt.Color;
import java.util.Observable;
import java.util.concurrent.LinkedBlockingQueue;

import java.util.Observable;
import java.util.concurrent.LinkedBlockingQueue;

import gui.GameBoardWindow;
import gui.GameBoardWindow.Turn;
import gui.InGameMenuWindow;
import gui.MenuButtonStatus;

/**
 * * Will contain informatemptyion regarding the current game:
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
 * @author Dave, Zuli
 *
 */
public class Game extends Peer implements Runnable {
	
	String player1, player2;
	GameStatus status;
	Boolean isTurn;
	byte[][] boardState;		// Same format as sent from server (see CheckersServerDocumentation)
	BAAC client;
	String tableID;			// Received from server
	GameBoardWindow gameGUI;
	String username = Player.getUsername();
	
	Boolean activeThread = true;
	Mediator mediator;
	private Color myColor;	
	//Thread safe buffers used to add/remove messages from this thread
	private final LinkedBlockingQueue<String> sendToServer = new LinkedBlockingQueue<String>();	
	private final LinkedBlockingQueue<String> receiveFromServer = new LinkedBlockingQueue<String>();

	
	/**
	 * Constructor for creating a new table
	 * @param tid is the table ID
	 * @param passedMediator is the Mediator used to pass messages between peers
	 */
	public Game(PeerMediator passedMediator)	{
		player1 = "";
		player2 = "";
		isTurn = false;
		mediator = passedMediator;
		mediator.addPeerClass(this);
	}


     /*****************************/
	       /* Setup Game */
	/*****************************/
	
	/**
	 * Set table number 
	 * @param tableNumber
	 */
	public void setTableID(String tableNumber){
		tableID = tableNumber;
	}
	
	/**
	 * Check the buffers for messages from the server and messages to be sent to the server 
	 */
	public void run() {
		gameGUI = new GameBoardWindow(this);
		
		while(activeThread){
			//send message to server
			String outgoingMessage;
			while (!sendToServer.isEmpty()){
				try {
					//get the message(Received from the UI)
					outgoingMessage = sendToServer.take();
					//send message to mediator which then sends it to the server interface
					mediator.receiveFromPeer(outgoingMessage);
				} catch (InterruptedException e) {
					e.printStackTrace();
				};
			}				
			//process messages from server
			String incomingMessage;
			while (!receiveFromServer.isEmpty()){
				try{
					//get the message (placed into blocking queue by the mediator)
					incomingMessage =  receiveFromServer.take();
					//verify it is a pertinent message and pass it on for further processing 
					scanMessageFromServer(incomingMessage);
				}catch (InterruptedException e) {
					e.printStackTrace();
				};			
			}		
			sleepyThread();
		}		
	}

	/**
	 * Method that allows to the mediator to place messages into the messagesFromServer queue.
	 */
	@Override
	public void receiveFromMediator(String message) {
		try {
			receiveFromServer.put(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
	}
	
	/***
	 * Looks at all messages sent from the server and determines which ones pertain to Game Class
	 * Directs message to the proper class method for further processing.
	 */
	public void scanMessageFromServer(String message){
		message = message.replace(" <EOM>", "");
		message = message.replace("<EOM>", "");
		boolean noMatch = false;
		String inMessage[];
		String messageCode = message.substring(0, 3);
        switch (messageCode) {
            case ServerMessage.GAME_START:
            	status = GameStatus.active;
        		isTurn = false;
        		if(username == player1){
        			isTurn = true;
        			gameGUI.setTurn(Turn.YOURS);
        		}
                break;
            case ServerMessage.COLOR_BLACK:
        		myColor = Color.BLACK;
        		gameGUI.setPlayerColor(Color.BLACK);
                break;
            case ServerMessage.COLOR_RED:
        		myColor = Color.RED;
        		gameGUI.setPlayerColor(Color.RED);
                break;
            case ServerMessage.OPP_MOVE: 
            	gameGUI.setTurn(Turn.THEIRS);
        		isTurn = false;
                break;
            case ServerMessage.BOARD_STATE:
            	//<code><tableID><boardState>
            	//split the string into three parts based on first two spaces
            	inMessage = message.split(" ", 3);
            	if(inMessage[1] == tableID){
            		String boardString = inMessage[2];
            		sendBoardToGUI(boardString);
            	}
                break;
            case ServerMessage.GAME_WIN: 
            	status = GameStatus.player_win;
            	break;
            case ServerMessage.GAME_LOSE:
            	status = GameStatus.player_lose;
            	break;
            case ServerMessage.WHO_ON_TBL:
            	//<code><tableID><player1><players2> <-- p1 or p2 may be -1 if empty
            	//split  this string into four parts based on the first three spaces
            	//TODO: Player Username cannot have spaces
            	inMessage = message.split(" ", 4);
            	String tbl, p1, p2;
            	tbl = inMessage[1];
            	p1 = inMessage[2];
            	p2 = inMessage[3];
            	if (tableID.equals(tbl)){
            		// user is player 2 in server message
            		if (p2.equals(Player.getUsername())){
            			player2 = p2;
            			// other seat is empty
            			if (p1.equals("-1")) {
	            			status = GameStatus.waiting_opponent;
	            			gameGUI.setOpponent("-1");
            			}
            			// other seat is taken
            			else {
            				status = GameStatus.active;
            				player1 = p1;
            				gameGUI.setOpponent(p1);
            			}
            		}
            		// user is player 1 in server message
            		else if (p1.equals(Player.getUsername())){
            			player1 = p1;
            			// other seat is empty
            			if (p2.equals("-1")) {
	            			status = GameStatus.waiting_opponent;
	            			gameGUI.setOpponent("-1");
            			}
            			// other seat is taken
            			else {
            				status = GameStatus.active;
            				player2 = p2;
            				gameGUI.setOpponent(p2);
            			}
            		}
            	}
            	break;
            case ServerMessage.OPP_LEFT_TABLE:
            	status = GameStatus.waiting_opponent;
            	break;
            case ServerMessage.YOUR_TURN:
            	isTurn = true;
            	gameGUI.setTurn(Turn.YOURS);
            	break;
            case ServerMessage.ILLEGAL:
            	gameGUI.updateBoard(boardState);
            	gameGUI.setTurn(Turn.YOURS);
            default:
            	noMatch = true;
            	break;
        }
        //if the messages matched a code, update the GUI
        if (noMatch == false){
        	//TODO: Update GUI with game vars
        }
   }
		
	
	/******************************************************/
		/*Helper Methods for sending updates to GUI*/
	/******************************************************/
	
	
	public void sendBoardToGUI(String state){
		//state = stateof(i,j)isatindex(8*i)+jofstring
		boardState = new byte[8][8];
		for(int i=0; i<8; i++){
			for(int j=0; j<8; j++){
				boardState[i][j]=Byte.parseByte(String.valueOf(state.charAt((8*i)+j)));
			}
		}
		//if the player is black, need to flip the board before sending
		if(Player.getUsername() == player1){
			byte[][] tempBoard = boardState;
			int iFlip;
			int jFlip;
			for(int i=0; i<8; i++){
				iFlip = 7 - i;
				for(int j=0; j<8; j++){	
					jFlip = 7 - j;
					boardState[i][j]=tempBoard[iFlip][jFlip];
				}
			} 
		}
		gameGUI.updateBoard(boardState);
	}
	
	/***
	 * Gracefully end process
	 */
	public void stopGame(){
		//stop getting updates from server
		mediator.removePeerClass(this);
		
		//set the run flag end on next loop
		activeThread = false;
		
		//clear both queues, otherwise might end up in an error condition
		sendToServer.clear();
		receiveFromServer.clear();
		
		//close the GUI's
		gameGUI.closeWindow();
	}
	
	
	/******************************************************/
				/* Handle Events From GUI */
	/******************************************************/
	

	/**
	 * Processes the move information from the GUI.
	//Player Makes a Move
	/// 0 1 2 3 4 5 6 7/
	//-----------------/
	//0| |B| |B| |B| |B|/ The server expects format : (row,column) 
	//1|B| |B| |B| |B| |/					 example: (1,2) 
	//2| |B| |B| |B| |B|/
	//3| | | | | | | | |/
	//4| | | | | | | | |/
	//5|R| |R| |R| |R| |/
	//6| |R| |R| |R| |R|/
	//7|R| |R| |R| |R| |/
	//-----------------
	 * @param moveArray: enters as an array {string1, string2} where string = "A1" , Row = A, Column = 1 
	 * places a string in mediator's queue:<106><clientName><currentPosition><newPosition><EOM>
	 */
	public void clientMoveRequest(String[] moveArray){
		//example input array{A1, B2}
		String currentPositionString = moveArray[0];
		String newPositionString = moveArray[1];
		
		//Rows are characters, columns are ints
		int currentRow = (int)currentPositionString.charAt(0) - 65; //will return 65(A) - 72(H) need 0-7
		int currentColumn = (int)currentPositionString.charAt(1) - 1; //will return 1-8 need 0-7
		int newRow = (int)newPositionString.charAt(0) - 65; //will return 65(A) - 72(H) need 0-7
		int newColumn = (int)newPositionString.charAt(1) - 1; //will return 1-8 need 0-7

		//check for client player color is black (is player 1) in which case the values are inverted
		if(myColor == Color.BLACK){
			//invert everything
			currentRow = Math.abs(currentRow - 7);
			currentColumn = Math.abs(currentColumn - 7);
			newRow = Math.abs(newRow - 7);
			newColumn = Math.abs(newColumn - 7);
		}
		
		//format the request to the server
		String currentPosition = String.valueOf(currentRow) + String.valueOf(currentColumn);
		String newPosition = String.valueOf(newRow) + String.valueOf(newColumn);
		String move = "106 " + username + " " + currentPosition + " " + newPosition + " <EOM>"; 
		placeMessageInIntermediateQueue(move);
	}
	
	/**
	 * Processes the GUI status change for player leaves the table.
	 * places a string in mediator's queue: <107><client><EOM>
	 */
	public void clientLeaveTableRequest(){
		//format the request to the server
		String leaveGame =  "107 " + username + "<EOM>";
		placeMessageInIntermediateQueue(leaveGame);
	}
	
	//Player Starts Game
	/**
	 * Processes the GUI status change for player is Ready
	 * places a string in the mediator's queue: <105><client><EOM>
	 */
	public void clientStartGameRequest(){
		//format the request to the server
		String startGame = "105 " + username + "<EOM>";
		placeMessageInIntermediateQueue(startGame);
	}
	
	public void placeMessageInIntermediateQueue(String message){
		try {
			sendToServer.put(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	

	/***
	 * Responds to updates from the GUI
	 * Updates can be arriving from either the GameWindow or the GameMenuWindow
	 * If it is the game window, it will check for the GUI's flags and change the flags 
	 * once the request is processed
	 * If it is the game menu window, it will check for the GUI's enum and respond appropriatly
	 * 
	 */
	@Override
	public void update(Observable o, Object arg) {
		//check flag		
		if (o.equals(gameGUI)){
			if(gameGUI.getReadyFlag()){
				gameGUI.setReadyFlag(false);
				clientStartGameRequest();
			}
			
			if(gameGUI.getMoveFlag()){
				gameGUI.setMoveFlag(false);
				String[] unParsedMove = gameGUI.getMove();//enters as an array {string1, string2} where string = "A1" , Row = A, Column = 1 
				clientMoveRequest(unParsedMove);
			}
		}
	}		
}	

		
	