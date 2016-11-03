package baac;

import java.net.*;
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
	*METHOD:
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
	*METHOD:
	*DESCRIPTION:
	*PARAMETERS:
	*RETURNS:
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
	 *METHOD:
	*DESCRIPTION:
	*PARAMETERS:
	*RETURNS:
	*DESCRIPTION:
	 *
	 *
	 * ************************************************************************/

	public void handle(String msg) {
		//System.out.println(msg);
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
			String messageCode = messageToPerform.substring(0, 2);
	        String monthString;
	        switch (messageCode) {
	            case "101":  System.out.println("hello");;
	                     break;
	            case "102":  messageCode = "February";
	                     break;
	            case "103":  messageCode= "March";
	                     break;
	            case "104":  messageCode = "April";
	                     break;
	            case "105":  messageCode = "May";
	                     break;
	            case "106":  messageCode = "June";
	                     break;
	            case "107":  messageCode = "July";
	                     break;
	            case" 108":  messageCode = "August";
	                     break;
	            case "109":  messageCode = "September";
	                     break;
	            case "1012": messageCode = "October";
	                     break;
	            case "1013": messageCode = "November";
	                     break;
	            case "1015": messageCode= "December";
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
		*METHOD:
	*DESCRIPTION:
	*PARAMETERS:
	*RETURNS:
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
	 	*METHOD:
	*DESCRIPTION:
	*PARAMETERS:
	*RETURNS:
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
	 *
	 *
	 *
	 * ************************************************************************/
	private String popSendMessage() {
		String message = this.sendVector.get(0);
		this.sendVector.remove(0);
		return message;

	}

	/***************************************************************************
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
	 *
	 *
	 *
	 * ************************************************************************/
	public void pushSendMessage(String message) {
		this.sendVector.add(message);
	}

	/***************************************************************************
	 *
	 *
	 *
	 * ************************************************************************/
	private void pushReceiveMessage(String message) {
		this.receiveVector.add(message);

	}
	/***************************************************************************
	 	*METHOD:
	*DESCRIPTION:
	*PARAMETERS:
	*RETURNS:
	*DESCRIPTION:
	 *
	 *
	 * ************************************************************************/
	public class ServerInterfaceThread extends Thread {
		private Socket socket = null;
		private ServerInterface client = null;
		private BufferedReader streamIn = null;



		/***************************************************************************
		 	*METHOD:
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
		 	*METHOD:
	*DESCRIPTION:
	*PARAMETERS:
	*RETURNS:
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
		 	*METHOD:
	*DESCRIPTION:
	*PARAMETERS:
	*RETURNS:
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
