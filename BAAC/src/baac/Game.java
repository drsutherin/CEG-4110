package baac;

import java.util.Observable;
import java.util.concurrent.LinkedBlockingQueue;

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
public class Game extends Peer implements Runnable {
	
	String player1, player2;
	GameStatus status;
	Boolean isTurn;
	String boardState;		// Same format as sent from server (see CheckersServerDocumentation)
	BAAC client;
	String tableID;			// Received from server
	
	Boolean activeThread = true;
	Mediator mediator;
	
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
		boardState = "";
		mediator = passedMediator;
		mediator.addPeerClass(this);
	}

	/***
	 * Override the stop function
	 */
	public void stop(){
		shutdownPlayableGame();
	}
	
	/**
	 * Shutdown thread
	 */
	public void shutdownPlayableGame(){
		activeThread = false;
	}

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
		boolean noMatch = false;
		String inMessage[];
		String messageCode = message.substring(0, 3);
        switch (messageCode) {
            case ServerMessage.GAME_START:
            	status = GameStatus.active;
        		isTurn = false;
        		if(Player.getUsername() == player1){
        			isTurn = true;
        		}
                break;
            case ServerMessage.COLOR_BLACK:
        		player1 = Player.getUsername();		
                break;
            case ServerMessage.COLOR_RED:
        		player1 = Player.getUsername();
                break;
            case ServerMessage.OPP_MOVE: 
        		isTurn = false;
                break;
            case ServerMessage.BOARD_STATE:
            	//<code><tableID><boardState>
            	//split the string into three parts based on first two spaces
            	inMessage = message.split(" ", 3);
            	if(inMessage[1] == tableID){
            		boardState = inMessage[2];
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
            	if (tableID == inMessage[1]){
            		//set player 1
            		if (inMessage[2] == "-1"){
            			status = GameStatus.waiting_opponent;
            		} else {
            			player1 = inMessage[2];
            		}
            		//set player 2
            		if (inMessage[3] == "-1"){
            			status = GameStatus.waiting_opponent;
            		} else {
            			player2 = inMessage[3];
            		}
            	}
            	break;
            case ServerMessage.OPP_LEFT_TABLE:
            	status = GameStatus.waiting_opponent;
            	break;
            case ServerMessage.YOUR_TURN:
            	isTurn = true;
            	break;
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
	 * @param currentRow
	 * @param currentColumn
	 * @param newRow
	 * @param newColumn
	 * places a string in mediator's queue:<106><clientName><currentPosition><newPosition><EOM>
	 */
	public void clientMoveRequest(String currentRow, String currentColumn, String newRow, String newColumn ){
		//format the request to the server
		String currentPosition = "(" + currentRow + "," + currentColumn + ")";
		String newPosition = "(" + currentRow + "," + currentColumn + ")";
		String move = "106 " + player1 + " " + currentPosition + " " + newPosition + "<EOM>"; 
		placeMessageInIntermediateQueue(move);
	}
	
	/**
	 * Processes the GUI status change for player leaves the table.
	 * places a string in mediator's queue: <107><client><EOM>
	 */
	public void clientLeaveTableRequest(){
		//format the request to the server
		String leaveGame =  "107 " + player1 + "<EOM>";
		placeMessageInIntermediateQueue(leaveGame);
	}
	
	//Player Starts Game
	/**
	 * Processes the GUI status change for player is Ready
	 * places a string in the mediator's queue: <105><client><EOM>
	 */
	public void clientStartGameRequest(){
		//format the request to the server
		String startGame = "105 " + player1 + "<EOM>";
		placeMessageInIntermediateQueue(startGame);
	}
	
	public void placeMessageInIntermediateQueue(String message){
		try {
			sendToServer.put(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	

	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}	
}
	
	