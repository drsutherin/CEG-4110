package baac;

import java.util.Observable;
import java.util.concurrent.LinkedBlockingQueue;

import gui.GameBoardWindow;
import gui.InGameMenuWindow;
import gui.MenuButtonStatus;

/**
 * ObservableGame controls logic for observing a game and owns the GUI
 * windows
 * @author Zuli
 */
public class ObservableGame extends Peer implements Runnable {

	String player1, player2;
	GameStatus status;
	byte[][] boardState;
	String tableID;			// Received from server
	GameBoardWindow gameGUI;
	InGameMenuWindow gameMenu;
	
	Boolean activeThread = true; //the thread will be active until this is switched off
	Mediator mediator;
	
	//Thread safe buffers used to add/remove messages from this thread
	private final LinkedBlockingQueue<String> sendToServer = new LinkedBlockingQueue<String>();	
	private final LinkedBlockingQueue<String> receiveFromServer = new LinkedBlockingQueue<String>();
	
	/**
	 * Constructor for creating a new table
	 * @param tid is the table ID
	 * @param passedMediator is the Mediator used to pass messages between peers
	 */
	public ObservableGame(String tid, PeerMediator passedMediator)	{
		player1 = "";
		player2 = "";
		tableID = tid;
		mediator = passedMediator;
		mediator.addPeerClass(this);
	}
	
	/***
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
		gameMenu = new InGameMenuWindow(this);		
	
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
	
	
	
	/**
	 * Shutdown thread
	 */
	public void shutdownObservableGame(){
		activeThread = false;
	}
	
	/***
	 * Looks at all messages sent from the server and determines which ones pertain to ObsevrableGame Class
	 * Directs message to the proper class method for further processing.
	 */
	public void scanMessageFromServer(String message){
		boolean noMatch = false;
		String inMessage[];
		String messageCode = message.substring(0, 3);
        switch (messageCode) {
        	case ServerMessage.BOARD_STATE:
            	//<code><tableID><boardState>
            	//split the string into three parts based on first two spaces
            	inMessage = message.split(" ", 3);
            	if(inMessage[1] == tableID){
            		String boardString = inMessage[2];
            		sendBoardToGUI(boardString);
            	}
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
            default:
            	//do nothing
            	break;
        }
  
   }
	
	/******************************************************/
	/*Helper Methods for sending updates to GUI*/
/******************************************************/


	public void sendBoardToGUI(String state){
		//state = stateof(i,j)isatindex(8*i)+jofstring
		byte[][]boardState = new byte[8][8];
		for(int i=0; i<8; i++){
			for(int j=0; j<8; j++){
				boardState[i][j]=Byte.parseByte(String.valueOf(state.charAt((8*i)+j)));
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
		gameMenu.closeWindow();
		gameGUI.closeWindow();
	}
		
	
	
	/*************************/
	/** Handle GUI Functions**/
	/*************************/
	/**
	 * Processes the GUI leave observation change for observer leaves table.
	 * places a string in mediator's queue: <107><client><EOM>
	 * TODO: verify a 107 message works for an observer leaving a table
	 */
	public void clientLeaveTableRequest(){
		//format the request to the server
		String leaveGame =  "107 " + player1 + "<EOM>";
		try {
			sendToServer.put(leaveGame);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o.equals(gameMenu)){
		 	MenuButtonStatus last;
			if (Player.getUserStatus() == Status.PLAYING)	{
				last = gameMenu.getLastPressed();
				
				switch(last){
				
				case EXIT_GAME:
					clientLeaveTableRequest();
					break;
				case PRIVATE_CHAT:
					break;
				default:
					break;
					
				}
							
			}		
		}
		
	}
}
