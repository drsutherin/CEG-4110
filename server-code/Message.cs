using System;
using System.Collections.Generic;
using System.Text;

namespace CheckersServer {
    /// <summary>
    /// Defines all TCP messages that can be sent to and from the client to the server.
    /// These are the raw messages that are sent across the network link between the client
    /// and server. They are just integer codes with parameters delimited by a single whitespace.
    /// Each message ends with the <EOM> tag.
    /// 
    /// Rely on this class to intepret the RSI and server consoles to see what messages are sent. 
    /// Otherwise, you only need to rely on the interfaces for sending and receiving messages. The
    /// interpretation of the codes and parameters are handled automatically by the RSI.
    /// </summary>
    public class Message {
        public const string endOfMsg="<EOM>";

        /**TCP messages from clients
         * to quit a game, you can just leave the table.
        */
        public const int MSG_ALL = 101;        //client <1> sends message <2> to everyone in lobby
        public const int MSG_C = 102;          //client <1> sends message <3> to client <2>
        public const int MAKE_TBL = 103;       //client <1> wants to make and sit at a table
        public const int JOIN_TBL = 104;       //client <1> wants to join table id <2>
        public const int READY = 105;          //client <1> is ready for game to start
        public const int MOVE = 106;           //client <1> moves from <2> to <3>. <2> and <3> are 0-based.
        public const int LEAVE_TBL = 107;      //client <1> leaves the table
        public const int QUIT = 108;           //client <1> leaves the server
        public const int ASK_TBL_STATUS = 109;  //client <1> is asking for the status of table <2>        
        public const int OBSERVE_TBL = 110;     //client <1> wants to observer table <2>
        public const int STOP_OBSERVING = 115;  //client <1> wants to stop observing table <2>;
        public const int REGISTER = 111;        //client <1> is registering, with password <2>
        public const int LOGIN = 112;           //client <1> is logging in, using password <2>
        public const int UPDATE_PROFILE = 113;  //client <1> is updating profile. 
        public const int GET_PROFILE = 114;     //client <1> wants to get the profile for client <2>

        /** TCP messages to clients
        */
        public const int CONN_OK = 200;        //connection to server establised
        public const int IN_LOBBY = 218;       //client has joined the lobby
        public const int OUT_LOBBY = 213;      //client has left the lobby
        public const int MSG = 201;            //client received message <3> from <1>. If <2> = 1 then it is private.
        public const int NEW_TBL = 202;        //a new table has been created with id <1> 
        public const int GAME_START = 203;     //The game at the table has begun
        public const int COLOR_BLACK = 204;    //client is playing as black 
        public const int COLOR_RED = 205;      //client is playing as red
        public const int OPP_MOVE = 206;       //opponent has moved from <1> to <2>
        public const int BOARD_STATE = 207;  //the board state on table <1> is <2>
        public const int GAME_WIN = 208;       //client has won their game
        public const int GAME_LOSE = 209;      //client has lost their game
        public const int TBL_JOINED = 210;     //client has joined table <1>
        public const int TBL_LEFT = 222;       //client has left table <1>.
        public const int WHO_IN_LOBBY = 212;   //the clients <1> <2> ... <n> are in the server
        public const int NOW_IN_LOBBY = 214;    //client <1> is now in the lobby
        public const int WHO_ON_TBL = 219;     //the clients <2> <3> are on table with tid <1>. <2> is black. <3> is red. If either is -1, the seat is open.   
        public const int TBL_LIST = 216;       //the current tables are <1> <2> ... <n>. Clients will have to request status.
        public const int NOW_LEFT_LOBBY = 217;	//the client <1> has left the lobby.
        public const int OPP_LEFT_TABLE = 220;   //your opponent has left the game.
        public const int YOUR_TURN = 221;       //it is now your turn.        
        public const int NOW_OBSERVING = 230;   //you are now observing table <1>. 
        public const int STOPPED_OBSERVING = 235;   //you stopped observing table <1>.
        public const int REGISTER_OK = 231;     //your registration is complete.
        public const int LOGIN_OK = 232;        //you have logged in successfully.
        public const int PROFILE_UPDATED = 233; //your profile has been updated.
        public const int USER_PROFILE = 234;    //the profile for <1>. <4> is its description, <2> is wins, <3> is losses.

        /** Error messages
         */
        public const int NET_EXCEPTION = 400;  //network exception
        public const int NAME_IN_USE = 401;    //client name already in use
        public const int BAD_NAME = 408;       //the user name requested is bad.
        public const int ILLEGAL = 402;        //illegal move
        public const int TBL_FULL = 403;       //table you tried to join is full
        public const int NOT_IN_LOBBY = 404;  //If you are not in the lobby, you can't join a table.
        public const int BAD_MESSAGE = 405;   //some part of a message the server got from a client is bad.
        public const int ERR_IN_LOBBY = 406;    //you cannot perform the requested operation because you are in the lobby.
        public const int PLAYERS_NOT_READY = 409;   //you made a move, but your opponent is not ready to play yet
        public const int NOT_YOUR_TURN = 410;   //you made a move, but its not your turn.
        public const int TBL_NOT_EXIST = 411;   //table queried does not exist.
        public const int GAME_NOT_CREATED = 412;    //called if you say you are ready on a table with no current game.
        public const int ALREADY_REGISTERED = 413;  //this name is already registered.
        public const int LOGIN_FAIL = 414;          //authentication failed.        
        public const int NOT_OBSERVING = 415;       //client is not observing a table.
        //public const int NOT_LOGGED_IN = 416;     //client is not logged in
        //^^ this message, forgot to implement. LOGIN_FAIL is sent in its place for now.
    }
}
