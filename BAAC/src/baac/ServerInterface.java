package baac;

import java.net.*;
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
	private ServerInterfaceThread client = null;
	private BAAC baac;

	/***************************************************************************
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
	*
	*
	***************************************************************************/
	public void run() {
		while (thread != null) {
			try {
				streamOut.println(consoleBuffer.readLine());
				streamOut.flush();
			} catch (IOException ioe) {
				System.out.println("Sending error: " + ioe.getMessage());
				stop();
			}
		}
	}

	/***************************************************************************
	 *
	 *
	 *
	 * ************************************************************************/

	public void handle(String msg) {
		if (msg.equals(".bye")) {
			System.out.println("Good bye. Press RETURN to exit ...");
			stop();
		} else {
			System.out.println(msg);
			String[] split = msg.split("<EOM>");
			for (int i = 0; i < split.length; i++){
				System.out.println(i + ": " + split[i]);
			}
			if (split[0] == "101")	{
				System.out.println("I hear you");
			}
		}
	}

	/***************************************************************************
	 *
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
	 *
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
	public class ServerInterfaceThread extends Thread {
		private Socket socket = null;
		private ServerInterface client = null;
		private BufferedReader streamIn = null;

		/***************************************************************************
		 *
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
		 *
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
		 *
		 *
		 *
		 * ************************************************************************/
		public void run() {
			while (true) {
				try {
					client.handle(streamIn.readLine());
				} catch (IOException ioe) {
					System.out.println("Listening error: " + ioe.getMessage());
					client.stop();
				}
			}
		}
	}
}
