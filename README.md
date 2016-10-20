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

This document defines the packages/modules the BAAC client will implement.

**Note:** All classes will need to implement an inbox for messages from the ServerInterface

*Question:* Should all classes be subclasses of a GameClient? Would that make message passing easier?

## GameClient
* Serves as the primary class
* Will contain:
  * player info (username)
  * player status (enum: ```in_lobby```, ```on_board```, ```playing_game```)
  * a ServerInterface instance
  * a LobbyChat instance (only updated while user is ```in_lobby```)
  * a list of PrivateChat instances (if any)
  * a Menu instance (different types of menus depending on player status)
  * an LobbyUsersList
  * an ActiveTablesList
  * a Game instance (if playing/observing)
  * a Voce SpeechInterface instance (for voice recognition)
  * a GUI instance

## ServerInterface
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

## Menu
* The Menu module will contain all menus:
  * MainMenu
  * BoardMenu
  * GameMenu *should this be separate from BoardMenu?*
* There will be an abstract Chat class

#### MainMenu
* Will contain buttons for:
  * Create Game
  * Join Game
  * Observe Game
  * Private Chat
* Will serve as the primary means of user interaction
  * Will create new game/chat instances
  * Will signal to GUI to update windows

#### GameMenu
* Buttons for:
  * Leave Table
  * Private Chat

## Toolbars
* The Toolbars module will be the base for all toolbars:
  * ActiveTablesList
  * LobbyUsersList
  * InGameToolbar
* Toolbars will display information, but will not initiate actions
* Abstract Toolbars class (?)

#### ActiveTablesList
* Will maintain a list of all tables on the server
* Tables will be classified by a tableStatus:
  * ```empty```: No players are on the table *(maybe don't display these in the list?)*
  * ```waiting```: There is one player on a table waiting for another to join
  * ```full```: There are two players on the board, but the game hasn't started
  * ```in_progress```: There are two players on the board and they are playing
  * *These will depend on how server responds to ```109: ASK_TBL_STATUS```, i.e. what ```207: BOARD_STATE``` returns*

#### LobbyUsersList
* Will maintain a list of all users in the lobby

#### InGameToolbar
* Viewable while on a table (waiting/playing/observing)
* Will show users playing and turn (if user is playing)

## Game
* Will contain information regarding the current game:
  * Players
  * Status (active, waiting_for_opponent, waiting_for_server)
  * Turn
  * Board state *(make sure it matches the server's board state for easy checking)*
* Handles player's moves
  * Select piece
  * Move to location
* Handles responses from server
  * Valid vs. invalid moves

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
