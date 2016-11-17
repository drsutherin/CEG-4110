package baac;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

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
				System.out.println("Error from the input stream " + ioe);
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
				//String message = "";
				//String messageToConcat =  "";
				//char currentChar = ' ';
				
				try {
					String message ="";
					while (!message.contains("<EOM>")){
						//System.out.println("Before");
						message = message + (char)streamIn.read();
						//System.out.println("After");
					}
					//System.out.println("Double after");
					message = message.replaceAll("\n", "");
					//client.pushReceiveMessage(message);
					client.handle(message);
					if (message.contains("108")){
						System.out.println("108 received.");
						client.stop();
					}

				} catch (Exception ioe) {
					System.out.println("Error: " + ioe.getMessage());
					client.stop();
				}
			}
		}
	}
