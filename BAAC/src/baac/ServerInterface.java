package baac;

import java.net.*;
import static baac.Message.*;
import java.util.Arrays;
import java.util.Vector;
import java.io.*;
import baac.BAAC;

/***************************************************************************
 * The ServerInterface class was designed to be implemented within the
 * GameClient class for the CEG 4410 project (Baby Got) Blood and Ashes Checkers
 * client. It handles all communications with the game server.
 *
 * It implements Runnable so it can run as an independent thread.
 *
 * @author Z. Rhodes
 * @author J. Rosen
 * @author M. Rutkowski
 * @author D. Sutherin
 *
 *Source of Multithreading Material found at:
 *http://pirate.shu.edu/~wachsmut/Teaching/CSAS2214/Virtual/Lectures/chat-client-server.html
 *Needed Largely edited due to deprecated methods
 *Things to do:
 *	Store Messages in at thread-safe Vector in order for use. *class attribute*
 *	Create push and pop methods to implement a stack on the Vector
 *	Move catch statement prints to Error Log
 ***************************************************************************/
public class ServerInterface implements Runnable {

	private Socket socket = null;
	private Thread thread = null;
	private InputStreamReader console = null;
	private BufferedReader consoleBuffer = null;
	private PrintWriter streamOut = null;
	private Vector<String> sendVector = new Vector<String>();
	private Vector<String> receiveVector = new Vector<String>();
	private ServerInterfaceThread client = null;
	private BAAC baac;

	/***************************************************************************
	*METHOD: Constructor()
	*DESCRIPTION:
	*PARAMETERS:
	*RETURNS:
	*DESCRIPTION:
	*
	*
	***************************************************************************/
	public ServerInterface(String serverName, int serverPort, BAAC b) {
		System.out.println("Establishing connection. Please wait ...");
		try {
			baac = b;
			socket = new Socket(serverName, serverPort);
			System.out.println("Connected: " + socket);
			start();
		} catch (UnknownHostException uhe) {
			System.out.println("Host unknown: " + uhe.getMessage());
		} catch (IOException ioe) {
			System.out.println("Unexpected exception: " + ioe.getMessage());
		}
	}

	/***************************************************************************
	*METHOD: run()
	*PARAMETERS: --
	*RETURNS: void
	*DESCRIPTION: 
	*
	*
	***************************************************************************/
	public void run() {
		while (thread != null) {
			try {
				String message = consoleBuffer.readLine();
				//streamOut.println(message);
				//streamOut.flush();
				this.pushSendMessage(message);
				while (!this.sendVector.isEmpty()){
					String messageToSend = this.popSendMessage();
					this.streamOut.println(messageToSend);
					streamOut.flush();
				}
			} catch (IOException ioe) {
				System.out.println("Sending error: " + ioe.getMessage());
				stop();
			}
		}
	}

	/***************************************************************************
	 *METHOD: handle()
	 *PARAMETERS: String
	 *RETURNS: void
	 *DESCRIPTION:
	 *
	 *
	 * ************************************************************************/

	public void handle(String msg) {
		
		String[] allMessages = msg.split("#");
		Vector<String> messagesToAdd = new Vector<String>(Arrays.asList(allMessages));
		for (int i = 0; i < messagesToAdd.size(); i++){
			if ((messagesToAdd.get(i).length() > 3)){
				this.pushReceiveMessage((messagesToAdd.get(i)));
			}

		}
		while (!this.receiveVector.isEmpty()){
			String messageToPerform = this.popRecieveMessage();
			System.out.println(messageToPerform);
			String messageCode = messageToPerform.substring(0, 3);
			//System.out.println(messageCode);
	        String monthString;
	        switch (messageCode) {
	            case Message.ASK_USERNAME: System.out.println("they asked your name stupid");;
	                     break;
	            case Message.CONN_OK:  messageCode= "March";
	                     break;
	            case Message.IN_LOBBY:  messageCode = "April";
	                     break;
	            case Message.OUT_LOBBY:  messageCode = "May";
	                     break;
	            case Message.MSG:  messageCode = "June";
	                     break;
	            case Message.NEW_TBL:  messageCode = "July";
	                     break;
	            case Message.GAME_START:  messageCode = "August";
	                     break;
	            case Message.COLOR_BLACK:  messageCode = "September";
	                     break;
	            case Message.COLOR_RED: messageCode = "October";
	                     break;
	            case Message.OPP_MOVE: messageCode = "November";
	                     break;
	            case Message.BOARD_STATE: messageCode= "December";
	                     break;
	            case Message.GAME_WIN: messageCode = "October";
                		 break;
	            case Message.GAME_LOSE: messageCode = "November";
	            		 break;
	            case Message.TBL_JOINED: messageCode= "December";
                		 break;
	            case Message.TBL_LEFT: messageCode = "October";
                		 break;
	            case Message.WHO_IN_LOBBY: messageCode = "November";
                		 break;
	            case Message.WHO_ON_TBL: messageCode= "December";
                		 break;
	            case Message.TBL_LIST: messageCode = "October";
	            		 break;
	            case Message.NOW_LEFT_LOBBY: messageCode = "November";
       					 break;
	            case Message.OPP_LEFT_TABLE: messageCode= "December";
	            		 break;
	            case Message.YOUR_TURN: messageCode = "November";
	            		 break;
	            case Message.NOW_OBSERVING: messageCode= "December";
	            		 break;
	            case Message.STOP_OBSERVING: messageCode = "October";
	            		 break;
	            case Message.REGISTER_OK: messageCode = "November";
	            		 break;
	            case Message.LOGIN_OK: messageCode= "December";
	            		 break;
	            case Message.PROFILE_UPDATED: messageCode = "October";
	            		 break;
	            case Message.USER_PROFILE: messageCode = "November";
	            		 break;
	            default: monthString = "Invalid month";
	                     break;

	        }
		}
		//if (msg.equals(".bye")) {
		//	System.out.println("Good bye. Press RETURN to exit ...");
		//	stop();
		//} else {
		//	System.out.println(msg);
		//}
	}

	/***************************************************************************
	 *METHOD: start()
	 *PARAMETERS: --
	 *RETURNS: void
	 *DESCRIPTION:
	 *
	 *
	 * ************************************************************************/
	public void start() throws IOException {
		console = new InputStreamReader(System.in);
		consoleBuffer = new BufferedReader(console);
		streamOut = new PrintWriter(socket.getOutputStream());
		if (thread == null) {
			client = new ServerInterfaceThread(this, socket);
			thread = new Thread(this);
			thread.start();
		}
	}

	/***************************************************************************
	 *METHOD: stop()
	 *PARAMETERS: --
	 *RETURNS: void
	 *DESCRIPTION:
	 *
	 *
	 * ************************************************************************/
	public void stop() {
		if (thread != null) {
			thread.stop();
			thread = null;
		}
		try {
			if (console != null) {
				console.close();
			}
			if (streamOut != null) {
				streamOut.close();
			}
			if (socket != null) {
				socket.close();
			}
		} catch (IOException ioe) {
			System.out.println("Error closing ...");
		}
		client.close();
		client.stop();
	}

	/***************************************************************************
	 *METHOD: popSendMessage()
	 *PARAMETERS: --
	 *RETURNS: String
	 *DESCRIPTION:
	 *
	 *
	 * ************************************************************************/
	private String popSendMessage() {
		String message = this.sendVector.get(0);
		this.sendVector.remove(0);
		return message;

	}

	/***************************************************************************
	 *METHOD: popRecieveMessage()
	 *PARAMETERS: --
	 *RETURNS: String
	 *DESCRIPTION:
	 *
	 *
	 *
	 * ************************************************************************/
	public String popRecieveMessage() {
		String message = this.receiveVector.get(0);
		this.receiveVector.remove(0);
		return message;
	}

	/***************************************************************************
	 *METHOD: pushSendMessage()
	 *PARAMETERS: String
	 *RETURNS: void
	 *DESCRIPTION:
	 *
	 *
	 *
	 * ************************************************************************/
	public void pushSendMessage(String message) {
		this.sendVector.add(message);
	}

	/***************************************************************************
	 *METHOD: pushReceiveMessage()
	 *PARAMETERS: String
	 *RETURNS: void
	 *DESCRIPTION:
	 *
	 *
	 *
	 * ************************************************************************/
	private void pushReceiveMessage(String message) {
		this.receiveVector.add(message);

	}

	//////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	
	public class ServerInterfaceThread extends Thread {
		
		private Socket socket = null;
		private ServerInterface client = null;
		private BufferedReader streamIn = null;

		/***************************************************************************
		 *METHOD: Constructor
		 *DESCRIPTION:
		 *PARAMETERS:
		 *RETURNS:
		 *DESCRIPTION:
		 *
		 *
		 * ************************************************************************/
		public ServerInterfaceThread(ServerInterface _client, Socket _socket) {
			client = _client;
			socket = _socket;
			open();
			start();
		}

		/***************************************************************************
		 *METHOD: open()
		 *PARAMETERS: --
		 *RETURNS: void
		 *DESCRIPTION:
		 *
		 *
		 * ************************************************************************/
		public void open() {
			try {
				streamIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				;
			} catch (IOException ioe) {
				System.out.println("Error getting input stream: " + ioe);
				client.stop();
			}
		}

		/***************************************************************************
		 *METHOD: close()
		 *PARAMETERS: --
		 *RETURNS: void
		 *DESCRIPTION:
		 *
		 *
		 * ************************************************************************/
		public void close() {
			try {
				if (streamIn != null)
					streamIn.close();
			} catch (IOException ioe) {
				System.out.println("Error closing input stream: " + ioe);
			}
		}

		/***************************************************************************
		 *METHOD: run()
		 *PARAMETERS: --
		 *RETURNS: void
		 *DESCRIPTION:
		 *
		 *
		 * ************************************************************************/
		public void run() {
			while (true) {
				try {
					char[] cbuf = new char[100];
					streamIn.read(cbuf);
						client.handle(String.valueOf(cbuf).replaceAll("\n", "").replace("<EOM>", "<EOM>#"));

				} catch (IOException ioe) {
					System.out.println("Listening error: " + ioe.getMessage());
					client.stop();
				}
			}
		}
	}
}
