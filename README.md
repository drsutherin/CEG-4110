# (Baby Got) Blood and Ashes Checkers
The group project for CEG 4110: Intro to Software Engineering

## Team Members
* Zuli Rhodes
* Jon Rosen
* Michael Rutkowski
* David Sutherin


## Project Goal
Develop a checkers game playing client in Java. The software will communicate with a game server Dr. Doran has written and will run in his office. We will use the client to log-in, chat, play, and observe other games on the server.

# Packages

This document defines the packages/modules the BAAC client will implement. Large
headings are packages, while smaller headings are classes.  Packages without
any classes listed will just have one with the same name.

(I've also added some thoughts/notes about implementation in italics as things to
think about as we go forward -Dave)

## Package Interaction
Since Java is a pass-by-reference language, all classes will be passed the BAAC instance, allowing them to pass messages by calling functions of other classes.  For example, when a user types a message in a chat window, the 'Send' button event handler will be able add the message to the chat's message list (which can in turn call a ServerInterface method to compose and transmit the message).


## baac
* Contains ```Main.java```, and the BAAC, Player, ServerInterface, and Game classes.
* ```Main.java``` will create a BAAC instance which will, in turn, create all other necessary classes

#### BAAC
* There will be **only one** instance of the BAAC class, which will be created by Main.java when the client is launched.http://voce.sourceforge.net/
* The BAAC class will create:
  * a ServerInterface instance
  * a LobbyChat instance (only updated while user is ```in_lobby```)
  * a list of PrivateChat instances (if any)
  * a Game instance (if playing/observing)
  * a Voce SpeechInterface instance (for voice recognition)
  * a GUI instance
* BAAC will also contain several data structures:
  * ActiveUsersList
    * Will maintain a list of **all** users active on the server (i.e. any user than
    could receive a private message)
  * ActiveTablesList
    * Will maintain a list of all tables on the server
    * Table statuses will depend on how server responds to ```109: ASK_TBL_STATUS```, i.e. what ```207: BOARD_STATE``` returns

#### Player
* Simple class containing:
  * player info: ```username```
  * player status enum: ```in_lobby```, ```on_board```, ```playing_game```


#### Game
* Will contain information regarding the current game:
  * Players
  * Status (active, waiting_for_opponent, waiting_for_server)
  * Turn
  * Board state *(make sure it matches the server's board state for easy checking)*
* Handles players' moves
  * Select piece
  * Move to location
* Handles responses from server
  * Valid vs. invalid moves
* Sends updates to GUI (for inGameToolBarWindow)

#### ServerInterface
* Constantly active
* Communicates with server
* Parses messages from server
* Passes messages to appropriate recipient

## Chat
* The Chat module will contain both Lobby and Private Chat classes
* There will be an abstract Chat class implementing message passing functions
  * send(), receive(), etc.
* Any messages from server regarding chat will be channeled to the appropriate chat

#### LobbyChat
* There will only be **one** LobbyChat instance
* LobbyChat will only update while the player is ```in_lobby```

#### PrivateChat
* There can be multiple PrivateChat instances

## Voce
* The Voce module will contain the source code from Voce for voice recognition

## GUI
* The GUI module will handle the graphical state of the client
* The GUI module will contain the visual representations of all windows:
  * Stage/Background
  * Menus
  * Chats
  * Tables/Game boards
  * Toolbars
* GUI module will also contain the controller for the UI

#### MainMenuWindow
* Will contain buttons for:
  * Create Game
  * Join Game
  * Observe Game
  * Private Chat
* Will serve as the primary means of user interaction from lobby
  * Will create new game/chat instances
  * Will signal to GUI to update windows
* Event handlers will communicate with other packages to signal menu item selections

#### GameMenuWindow
* Buttons for:
  * Leave Table
  * Private Chat
* Event handlers will communicate w/ other packages to signal menu item selections

####InGameToolBarWindow
* Information will be received from Game class
* Viewable while on a table (waiting/playing/observing)
* Will show users playing and turn (if user is playing)

# Development Quotes
* "I don't use a git GUI, I'm not a trifling bitch"
* "Fuck yes, I pulled!"
* "No one cares about Mac, Michael!"
* "DID YOU TOUCH MY BANANA?!?"
* "Night sass"
* "Porn?"
* "Who wants to play a game with themselves so I can watch?"
* "Just to make sure I'm clear on the rules of checkers, you do take pieces by moving on top of the other right?"

