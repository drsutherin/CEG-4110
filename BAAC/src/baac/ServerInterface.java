package baac;

import java.net.*;
import static baac.ServerMessage.*;
import java.util.Arrays;
import java.util.Observable;
import java.util.Vector;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import java.io.*;



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
public class ServerInterface extends Peer implements Runnable {

	private Socket socket = null;
	private Thread thread = null;
	private InputStreamReader console = null;
	private BufferedReader consoleBuffer = null;
	private PrintWriter streamOut = null;
	//private Vector<String> receiveVector = new Vector<String>();
	private ServerInterfaceThread client = null;
	private PeerMediator mediator;
	
	//buffer for passing messages within the client (generally from other classes to this interface)
	//private BlockingQueue<String> clientMessageQueue;
	
	private BlockingQueue<String> messagesFromClient = new LinkedBlockingQueue<String>();

	/***************************************************************************
	*METHOD: Constructor()
	*DESCRIPTION:
	*PARAMETERS:messageQueue, a reference to the internal buffer that the client classes pass messages to
	*RETURNS:
	*DESCRIPTION:
	*
	*
	***************************************************************************/
	public ServerInterface(PeerMediator passedMediator){ //String serverName, int serverPort, BAAC b) {
		
		mediator = passedMediator;
		//mediator.addServerInterface(this);
		
		 // a jframe here isn't strictly necessary, but it makes the example a little more real
        JFrame frame = new JFrame("InputDialog Example #1");

        // prompt the user to enter their name
        String serverName = JOptionPane.showInputDialog(frame, "Server Name or IP Adress", "mchlrtkwski.tk");
        String serverPort = JOptionPane.showInputDialog(frame, "Port Number", "45322");

        // Don't allow people to not select a server
        if (serverName == null || serverPort == null || serverName.equals("") || serverPort.equals(""))	{
        	System.out.println("No server/port selected. Exiting...");
        	System.exit(1);
        }

        // get the user's input. no this.pushSendMessage(name);//("The user's name is '%s'.\n", name);
       // System.exit(0);
        //serverName = "mchlrtkwski.tk";
        //serverPort = "45322";
		System.out.println("Establishing connection. Please wait ...");
		try {
			mediator.addServerInterface(this);
			socket = new Socket(serverName, Integer.parseInt(serverPort));
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
				//String message = (consoleBuffer.readLine()).replace("\n", "");
				//this.pushSendMessage(message);
				while (!messagesFromClient.isEmpty()){
					String messageToSend = this.popSendMessage();
					this.streamOut.print(messageToSend);
					streamOut.flush();
					if (messageToSend.startsWith("108")){
						System.out.println("goodbye now you");
						stop();						
					}
				}
			} catch (Exception ioe) {
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

		this.pushReceiveMessage(msg);
		System.out.println(msg);
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
	 *DESCRIPTION: Removes a message from the client's consumer-producer buffer.
	 *The buffer generally holds messages in the form: ### CLIENT_ID MESSAGE_FROM_CLIENT <EOM>.
	 *Most of these messages were produced by client classes (ie. PrivateChat, Game, etc)
	 *One exception to the form is the initial send message which consists only of the 
	 *username and is sent directly from the Server-Interface.
	 *The server-interface consumes the messages and sends them to the server
	 * ************************************************************************/
	private String popSendMessage() {
		String message = null;
		try {
			Boolean success = false;
			while (!success){
				message = messagesFromClient.poll(100, TimeUnit.MILLISECONDS);
				success = true;
			}
			
		} catch (InterruptedException e) {
			// TODO print to log
		}
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
	
	/*public String popRecieveMessage() {
		String message = this.receiveVector.get(0);
		this.receiveVector.remove(0);
		return message;
	}
	*/

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
		try {
			Boolean success = false;
			while (!success){
				messagesFromClient.offer(message, 100, TimeUnit.MICROSECONDS );
				success = true;
			}
		} catch (InterruptedException e) {
			// TODO print to log
		}
		
	}
	
	
	/**
	 * Passes method to the threadsafe pushSendMessages
	 * "receiveFromMediator" must be implemented to be a peer
	 */
	@Override
	public void receiveFromMediator(String message) {
		pushSendMessage(message);
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
		//TODO: Server will only process add request for username, should we move this functionality to BAAC?? 
		//this.receiveVector.add(message);		
		//this is the new way
		mediator.receiveFromServer(message);
	}

	//////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	

	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}


}
