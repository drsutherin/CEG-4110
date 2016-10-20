package serverInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * The ServerInterface class was designed to be implemented within the 
 * GameClient class for the CEG 4410 project (Baby Got) Blood and Ashes
 * Checkers client. It handles all communications with the game server.
 * 
 * It implements Runnable so it can run as an independent thread.
 * 
 * @author Z. Rhodes
 * @author J. Rosen 
 * @author M. Rutkowski
 * @author D. Sutherin
 *
 */
public class ServerInterface implements Runnable {

	/**
	 * Default constructor
	 */
	public ServerInterface()	{
		
	}

	/**
	 * The run method defines the functions of the ServerInterface
	 * while the client is active  
	 */
	public void run() {
		  String serverHostname = new String("24.166.20.116");

//	        if (args.length > 0) {
//	            serverHostname = args[0];
//	        }
	        System.out.println("Attemping to connect to host "
	                + serverHostname + " on port 45322.");

	        Socket socketTest = null;
	        PrintWriter out = null;
	        BufferedReader in = null;

	        try {
	            // echoSocket = new Socket("taranis", 7);
	            socketTest = new Socket(serverHostname, 45322);
	            out = new PrintWriter(socketTest.getOutputStream(), true);
	            in = new BufferedReader(new InputStreamReader(
	                    socketTest.getInputStream()));
	        } catch (UnknownHostException e) {
	            System.err.println("Don't know about host: " + serverHostname);
	            System.exit(1);
	        } catch (IOException e) {
	            System.err.println("Couldn't get I/O for "
	                    + "the connection to: " + serverHostname);
	            System.exit(1);
	        }

	        BufferedReader stdIn = new BufferedReader(
	                new InputStreamReader(System.in));
	        String userInput;
	        try {
		        while ((userInput = stdIn.readLine()) != null) {
		            out.println(userInput);
		            System.out.println("echo: " + in.readLine());
		        }
	        } catch (IOException e) {
	        	System.out.println("IOException: " + e);
	        }
	}
}
