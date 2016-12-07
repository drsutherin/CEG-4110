package baac;

    public final class ServerMessage {
        public  static final String endOfMsg="<EOM>";
        public  static final String ASK_USERNAME="Sen";

        /**TCP messages from clients
         * to quit a game, you can just leave the table.
        */
        public static final String MSG_ALL = "101";        //client <1> sends message <2> to everyone in lobby
        public  static final String MSG_C = "102";          //client <1> sends message <3> to client <2>
        public static final String MAKE_TBL = "103";       //client <1> wants to make and sit at a table
        public  static final String JOIN_TBL = "104";       //client <1> wants to join table id <2>
        public  static final String READY = "105";          //client <1> is ready for game to start
        public  static final String MOVE = "106";           //client <1> moves from <2> to <3>. <2> and <3> are 0-based.
        public  static final String LEAVE_TBL = "107";      //client <1> leaves the table
        public  static final String QUIT = "108";           //client <1> leaves the server
        public  static final String ASK_TBL_STATUS = "109";  //client <1> is asking for the status of table <2>
        public  static final String OBSERVE_TBL = "110";     //client <1> wants to observer table <2>
        public  static final String STOP_OBSERVING = "115";  //client <1> wants to stop observing table <2>;
        public  static final String REGISTER = "111";        //client <1> is registering, with password <2>
        public  static final String LOGIN = "112";           //client <1> is logging in, using password <2>
        public  static final String UPDATE_PROFILE = "113";  //client <1> is updating profile.
        public  static final String GET_PROFILE = "114";     //client <1> wants to get the profile for client <2>

        /** TCP messages to clients
        */
        public  static final String CONN_OK = "200";        //connection to server establised
        public   static final String IN_LOBBY = "218";       //client has joined the lobby
        public  static final String OUT_LOBBY = "213";      //client has left the lobby
        public  static final String MSG = "201";            //client received message <3> from <1>. If <2> = 1 then it is private.
        public  static final String NEW_TBL = "202";        //a new table has been created with id <1>
        public  static final String GAME_START = "203";     //The game at the table has begun
        public  static final String COLOR_BLACK = "204";    //client is playing as black
        public  static final String COLOR_RED = "205";      //client is playing as red
        public  static final String OPP_MOVE = "206";       //opponent has moved from <1> to <2>
        public  static final String BOARD_STATE = "207";  //the board state on table <1> is <2>
        public  static final String GAME_WIN = "208";       //client has won their game
        public  static final String GAME_LOSE = "209";      //client has lost their game
        public  static final String TBL_JOINED = "210";     //client has joined table <1>
        public  static final String TBL_LEFT = "222";       //client has left table <1>.
        public  static final String WHO_IN_LOBBY = "212";   //the clients <1> <2> ... <n> are in the server
        public  static final String NOW_IN_LOBBY = "214";    //client <1> is now in the lobby
        public  static final String WHO_ON_TBL = "219";     //the clients <2> <3> are on table with tid <1>. <2> is black. <3> is red. If either is -1, the seat is open.
        public  static final String TBL_LIST = "216";       //the current tables are <1> <2> ... <n>. Clients will have to request status.
        public  static final String NOW_LEFT_LOBBY = "217";	//the client <1> has left the lobby.
        public  static final String OPP_LEFT_TABLE = "220";   //your opponent has left the game.
        public  static final String YOUR_TURN = "221";       //it is now your turn.
        public  static final String NOW_OBSERVING = "230";   //you are now observing table <1>.
        public  static final String STOPPED_OBSERVING = "235";   //you stopped observing table <1>.
        public  static final String REGISTER_OK = "231";     //your registration is complete.
        public  static final String LOGIN_OK = "232";        //you have logged in successfully.
        public  static final String PROFILE_UPDATED = "233"; //your profile has been updated.
        public  static final String USER_PROFILE = "234";    //the profile for <1>. <4> is its description, <2> is wins, <3> is losses.

        /** Error messages
         */
        public  static final String NET_EXCEPTION = "400";  //network exception
        public  static final String NAME_IN_USE = "401";    //client name already in use
        public  static final String BAD_NAME = "408";       //the user name requested is bad.
        public  static final String ILLEGAL = "402";        //illegal move
        public  static final String TBL_FULL = "403";       //table you tried to join is full
        public  static final String NOT_IN_LOBBY = "404";  //If you are not in the lobby, you can't join a table.
        public  static final String BAD_MESSAGE = "405";   //some part of a message the server got from a client is bad.
        public  static final String ERR_IN_LOBBY = "406";    //you cannot perform the requested operation because you are in the lobby.
        public  static final String PLAYERS_NOT_READY = "409";   //you made a move, but your opponent is not ready to play yet
        public  static final String NOT_YOUR_TURN = "410";   //you made a move, but its not your turn.
        public  static final String TBL_NOT_EXIST = "411";   //table queried does not exist.
        public static final String GAME_NOT_CREATED = "412";    //called if you say you are ready on a table with no current game.
        public  static final String ALREADY_REGISTERED = "413";  //this name is already registered.
        public static final  String LOGIN_FAIL = "414";          //authentication failed.
        public static final  String NOT_OBSERVING = "415";       //client is not observing a table.
        //public  String NOT_LOGGED_IN = 416;     //client is not logged in
        //^^ this message, forgot to implement. LOGIN_FAIL is sent in its place for now.
       
        
    }

