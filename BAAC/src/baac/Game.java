package baac;

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
	String turn;
	String boardState;		// Same format as sent from server (see CheckersServerDocumentation)
	BAAC client;
	String tableID;			// Received from server
	Mediator mediator;
	
	//Thread safe buffers used to add/remove messages from this thread
	private final LinkedBlockingQueue<String> sendToServer = new LinkedBlockingQueue<String>();	
	private final LinkedBlockingQueue<String> receiveFromServer = new LinkedBlockingQueue<String>();
	
	/**
	 * Constructor for creating a new table
	 * @param tid is the table ID
	 * @param passedMediator is the Mediator used to pass messages between peers
	 */
	public Game(String tid, PeerMediator passedMediator)	{
		player1 = Player.getUsername();
		player2 = "";
		turn = "";
		boardState = "";
		tableID = tid;
		mediator = passedMediator;
		mediator.addPeerClass(this);
	}
	
	/**
	 * Constructor for joining a table
	 * @param baac is the client
	 * @param tid is the existing table id
	 * @param passedMediator is the Mediator used to pass messages between peers
	 */
	public Game(String tid, String opponent, PeerMediator passedMediator){
		player1 = Player.getUsername();
		player2 = opponent;
		turn = "";
		boardState = "";
		tableID = tid;
		mediator = passedMediator;
		mediator.addPeerClass(this);
	}

	/**
	 * Check the buffers for messages from the server and messages to be sent to the server 
	 */
	public void run() {
		while(true){
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
		String messageCode = message.substring(0, 3);
		//System.out.println(messageCode);
        switch (messageCode) {
            case ServerMessage.GAME_START:  messageCode = "August";
                     break;
            case ServerMessage.COLOR_BLACK:  messageCode = "September";
                     break;
            case ServerMessage.COLOR_RED: messageCode = "October";
                     break;
            case ServerMessage.OPP_MOVE: messageCode = "November";
                     break;
            case ServerMessage.BOARD_STATE: messageCode= "December";
                     break;
            case ServerMessage.GAME_WIN: messageCode = "October";
            		 break;
            case ServerMessage.GAME_LOSE: messageCode = "November";
            		 break;
            case ServerMessage.WHO_ON_TBL: messageCode= "December";
            		 break;
            case ServerMessage.OPP_LEFT_TABLE: messageCode= "December";
            		 break;
            case ServerMessage.YOUR_TURN: messageCode = "November";
            		 break;
            //note - there is no way to know who is observing table (if anyone)
        }
		
	}
	
	/******************************************************/
				/* Handle Events From Server */
	/******************************************************/
	
	
	
	
	
	
	
	
	
}
	
	