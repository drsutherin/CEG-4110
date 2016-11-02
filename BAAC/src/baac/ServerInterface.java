package baac;

import java.net.*;
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
public class ServerInterface implements Runnable {

	private Socket socket = null;
	private Thread thread = null;
	private InputStreamReader console = null;
	private BufferedReader consoleBuffer = null;
	private PrintWriter streamOut = null;
	private ServerInterfaceThread client = null;

	/***************************************************************************
	*METHOD:
	*DESCRIPTION:
	*PARAMETERS:
	*RETURNS:
	*DESCRIPTION:
	*
	*
	***************************************************************************/
	public ServerInterface(String serverName, int serverPort) {
		System.out.println("Establishing connection. Please wait ...");
		try {
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
				streamOut.println(consoleBuffer.readLine());
				streamOut.flush();
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
		if (msg.equals(".bye")) {
			System.out.println("Good bye. Press RETURN to exit ...");
			stop();
		} else {
			System.out.println(msg);
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
						client.handle(String.valueOf(cbuf).replaceAll("\n", "").replace("<EOM>", "<EOM>\n"));
				} catch (IOException ioe) {
					System.out.println("Listening error: " + ioe.getMessage());
					client.stop();
				}
			}
		}
	}
}
