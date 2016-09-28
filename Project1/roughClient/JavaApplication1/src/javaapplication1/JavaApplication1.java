//Documentation was found on the Oracle Website for Socket help
//http://docs.oracle.com/javase/tutorial/networking/sockets/readingWriting.html
package javaapplication1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class JavaApplication1 {

    public static void main(String[] args) throws IOException {

        //Server Name input
        String server = new String("localhost");

        System.out.println("Connecting to host "+ server + " on port 45322.");

        //Declare Variables
        Socket socketToTry = null;
        PrintWriter socketOut = null;
        BufferedReader socketIn = null;

        try {
            //Create socket
            socketToTry = new Socket(server, 45322);
            //Create Writer to write socker output
            socketOut = new PrintWriter(socketToTry.getOutputStream(), true);
            //Create Reader to read socket input
            socketIn = new BufferedReader(new InputStreamReader(socketToTry.getInputStream()));

            //Catch Exceptions relating to failed Connection
        } catch (Exception e) {
            System.err.println("Error");
        }

        //Read in user input
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        
        //Read in user input continuously. Once the user has typed something they recieve
        //feeback from the server. This creates a problem, as the server should relay
        //messages regardless of if user enters input or not. This is a multithreading 
        //issue that will be worked on.
        String userInput;
        while ((userInput = stdIn.readLine()) != null) {
            socketOut.println(userInput);
            System.out.println("echo: " + socketIn.readLine());
        }
    }
}
