/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication1;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 *
 * @author michaelrutkowski
 */
//public class JavaApplication1 {


// public static void main(String argv[]) throws Exception
// {
//  String sentence;
//  String modifiedSentence;
//  BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));
//  Socket clientSocket = new Socket("24.166.20.116", 45322);
//  DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
//  BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//  sentence = inFromUser.readLine();
//  outToServer.writeBytes(sentence);
//  modifiedSentence = inFromServer.readLine();
//  System.out.println("FROM SERVER: " + modifiedSentence);
//  clientSocket.close();
// }
//}
    import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
public class JavaApplication1 {


	public static void main(String[] args) throws IOException {
         
        
        String serverHostname = new String ("24.166.20.116");

        if (args.length > 0)
           serverHostname = args[0];
        System.out.println ("Attemping to connect to host " +
		serverHostname + " on port 45322.");

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
while ((userInput = stdIn.readLine()) != null) {
    out.println(userInput);
    System.out.println("echo: " + in.readLine());
}
    }
}