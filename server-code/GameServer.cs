using System;
using System.Collections.Generic;
using System.Collections;
using System.Text;
using System.Net;
using System.Net.Sockets;
using System.Threading;

namespace CheckersServer {
    /// <summary>
    /// This is the central class of the checkers server.
    /// 
    /// Here is where the server listens for new connections and manages all 
    /// the server client objects, game tables, and lobby activity. 
    /// 
    /// There is no real 'lobby' instance on the server, it is only logical. 
    /// If a client's state is "in lobby", it will be able to join tables,
    /// observe games, view profiles, and participate in the lobby chat.
    /// 
    /// Each client object has a thread that listens for incoming messages.
    /// When clients communicate to the server, messages 
    /// are routed from the client objects this central class where game or lobby states are 
    /// altered accordingly. This class also sends status messages to the clients.
    /// 
    /// derek 
    /// v1 8.13.08
    /// v2 11.09.08
    /// </summary>
    public class GameServer {

        public static int portNum = 45322;
        public static IPAddress serverIP;

        //maintain a thread-safe list of clients
        private Hashtable clients; //<string, GameClient> : username, game client instance
        //a thread that listens for incoming connections
        private Thread listenerThread;
        //reference to the front-end gui, to display messages
        public ServerGUI gui;
        //maintain a list of all running tables.
        private Dictionary<int, GameTable> tables;

        private List<GameClient> dcList;

        public GameServer(ServerGUI cs) {
            clients = new Hashtable();
            dcList = new List<GameClient>();
            tables = new Dictionary<int, GameTable>();
            gui = cs;
            listenerThread = new Thread(new ThreadStart(startListening));
            listenerThread.Name = "NewConnections";
            listenerThread.Start();
        }

        /* This method operates in its own thread. 
         * Here we listen for new tcp connections from clients.
         */
        public void startListening() {
            //if the server frontend is actually running...
            if (gui != null) {
                //Create an instance of TcpListener to listen for TCP connection.
                TcpListener tcpListener = TcpListener.Create(portNum);
                tcpListener.Start();
                while (true) {
                    try {
                        //Program blocks here until a client connects.
                        TcpClient client = tcpListener.AcceptTcpClient();
                        gui.output("connection request received");
                        string reply = "Send username:";
                        byte[] msg = new byte[512];
                        client.Client.Send(System.Text.Encoding.ASCII.GetBytes(reply + Message.endOfMsg), reply.Length + Message.endOfMsg.Length, 0);
                        //the first message received by a client should be the desired username. 
                        int bytesReceived = client.Client.Receive(msg);
                        string cname = System.Text.Encoding.ASCII.GetString(msg, 0, bytesReceived);
                        int nameOK = -1;
                        lock (clients.Keys.SyncRoot) {
                            if (cname.Contains(" ") || cname.Length <= 0 || cname.Contains("#")) {
                                nameOK = Message.BAD_NAME;
                                client.Client.Send(
                                    System.Text.Encoding.ASCII.GetBytes(nameOK.ToString() + Message.endOfMsg), nameOK.ToString().Length + Message.endOfMsg.Length, 0);
                                client.Client.Close();
                                client.Close();
                            }
                            if (clients.ContainsKey(cname)) {
                                nameOK = Message.NAME_IN_USE;
                                client.Client.Send(
                                    System.Text.Encoding.ASCII.GetBytes(nameOK.ToString() + Message.endOfMsg), nameOK.ToString().Length + Message.endOfMsg.Length, 0);
                                client.Client.Close();
                                client.Close();
                            } else nameOK = Message.CONN_OK;
                            if (nameOK == Message.CONN_OK) {
                                //create a new instance of a game client
                                clients.Add(cname, new GameClient(cname, client, this));
                                gui.output(cname + " has connected to the server.");
                                sendConnectionMsg((GameClient)clients[cname]);
                            } else {
                                try {
                                    if (client != null && client.Client != null) {
                                        //else tell the client the name selected is in use. 
                                        client.Client.Send(System.Text.Encoding.ASCII.GetBytes(nameOK.ToString() + Message.endOfMsg), nameOK.ToString().Length + Message.endOfMsg.Length, 0);
                                        //Disconnect them.
                                        try {
                                            client.Client.Close();
                                            client.Close();
                                        } catch (Exception) {
                                            FileLogger.writeToExceptionLog("exception closing a client connection when the name they selected is in use.");
                                        }
                                    }
                                } catch (NullReferenceException) { }
                            }
                        }
                    } catch (SocketException se) {
                        FileLogger.writeToExceptionLog("A Socket Exception has occurred while the client was connecting!" + se.ToString());
                        FileLogger.writeToExceptionLog("Server should still accepting incoming connections!");
                    } catch (Exception e) {
                        FileLogger.writeToExceptionLog("An unknown exception has occured while a client was connecting. Excepion: " + e.ToString());
                        FileLogger.writeToExceptionLog("Server should still accepting incoming connections!");
                    }
                }
            }
        }
            

        /// <summary>
        /// Send setup messages to the newly connected client.
        /// Tell the client they are in the lobby,
        /// who is in the lobby, 
        /// and what tables are up.
        /// Finally, start the listening thread for the client.
        /// </summary>
        /// <param name="client"></param>
        public void sendConnectionMsg(GameClient client) {
            /** We should be locked from start listening */
            //if the client is not in the client data structure yet, add it.
            if (!clients.Contains(client.getUserName())) {
                clients.Add(client.getUserName(), client);
            }
            //first join the lobby.
            client.joinLobby();
            //get everyone else in the lobby.
            string lobbyList = "";
            foreach (object o in clients.Values) {
                GameClient gc = (GameClient)o;
                if (gc.inLobby()) {
                    lobbyList += gc.getUserName() + " ";
                }
            }
            client.sendMessage(Message.WHO_IN_LOBBY + " " + lobbyList);
            //get all the tables.
            string tids = "";
            foreach (int i in tables.Keys) {
                tids += i.ToString() + " ";
            }
            client.sendMessage(Message.TBL_LIST + " " + tids.Trim());
            //tell everyone this client joined the lobby.
            this.alertJoinLobby(client);
            //finally, start the listening thread for the game client to hear incoming requests.
            client.Start();
        }

        /// <summary>
        /// Process a message receieved from a game client.
        /// </summary>
        /// <param name="client">The GameClient instance that sent the message</param>
        /// <param name="msg">The TCP message code</param>
        /// <param name="args">A list of the arguments for the message</param>
        public void processMessage(GameClient client, int msg, List<string> args) {
            string text = "";
            lock (this) {
                try {
                    switch (msg) {
                        case Message.MSG_ALL:        //client sends message <1> to everyone in lobby
                            if (client.inLobby())
                                //when we broadcast a message, each word is in the args.                            
                                foreach (string s in args)
                                    text += s + " ";
                            broadcastMsg(client.getUserName(), text);
                            break;
                        case Message.MSG_C:         //client sends message <3> to client <1>.
                            for (int i = 1; i < args.Count; i++)
                                text += args[i] + " ";
                            sendMsg(client.getUserName(), args[0], text);
                            break;
                        case Message.MAKE_TBL:      //client wants to make and sit at a table
                            if (client.inLobby())
                                createTable(client);
                            else client.sendMessage(Message.NOT_IN_LOBBY.ToString());
                            break;
                        case Message.JOIN_TBL:      //client wants to join table id <1>
                            if (!client.inLobby())
                                client.sendMessage(Message.NOT_IN_LOBBY.ToString());
                            else {
                                int p1 = -1;
                                try {
                                    p1 = int.Parse(args[0]);
                                }
                                catch (Exception) {
                                    client.sendMessage(Message.BAD_MESSAGE.ToString());
                                    return;
                                }
                                joinTable(client, p1);
                            }
                            break;
                        case Message.READY:         //client is ready for game to start
                            sendReady(client);
                            break;
                        case Message.MOVE:          //client moves from <1> to <2> 
                            sendMove(client,
                                args[0].Substring(0, 1) + "," + args[0].Substring(2, 1),
                                args[1].Substring(0,1) + "," + args[1].Substring(2, 1));
                            break;
                        case Message.LEAVE_TBL:     //client leaves the table
                            if (!client.inLobby())
                                leaveTable(client);
                            else client.sendMessage(Message.ERR_IN_LOBBY.ToString());
                            break;
                        case Message.ASK_TBL_STATUS:    //client is asking for the status of table <1>
                             int tid = -1;
                                try {
                                    tid = int.Parse(args[0]);
                                }
                                catch (Exception) {
                                    client.sendMessage(Message.BAD_MESSAGE.ToString());
                                    return;
                                }
                                sayWhoOnTbl(client, tid);
                            break;
                        /** part 4 new messages **/
                        case Message.OBSERVE_TBL:
                           // if (client.inLobby())
                                addObserver(client, args[0]);
                            //else client.sendMessage(Message.NOT_IN_LOBBY);
                            break;
                        case Message.STOP_OBSERVING:
                            if(client.isObserving())
                                stopObserving(client, args[0], false);
                            else client.sendMessage(Message.NOT_OBSERVING);
                            break;
                      /* case Message.REGISTER:
                            registerClient(client, args[0]);
                            break;
                        case Message.LOGIN:
                            loginClient(client, args[0]);
                            break;
                        case Message.UPDATE_PROFILE:
                            text = "";
                            foreach (string s in args)
                                text += s + " ";
                            updateProfile(client, text);
                            break;
                        case Message.GET_PROFILE:
                            getProfile(client, args[0]);
                            break;*/
                            
                        case Message.QUIT:          //client leaves the server
                            alertPlayerDisconnected(client);
                            break;
                    }
                }
                catch (Exception) {
                    //the message, or some parameter was bad.
                    client.sendMessage(Message.BAD_MESSAGE);
                }
            }
        }

        public void clearClosedClients() {
            if (dcList.Count > 0) {
                List<GameClient> closed = new List<GameClient>(dcList);
                dcList.Clear();
                foreach (GameClient gc in closed) {
                    clients.Remove(gc.getUserName());
                }
                foreach (GameClient gc in closed) {
                    alertPlayerDisconnected(gc);
                }
            }
        }

        public void clearClosedClient(GameClient client) {
            FileLogger.writeToExceptionLog("clearClosedClient removing client " + client.getUserName());
            this.alertPlayerDisconnected(client);
        }


        private void sayWhoOnTbl(GameClient client, int tid ) {
                GameTable tbl;
                try {
                    tbl = tables[tid];
                }
                catch (Exception) {
                    client.sendMessage(Message.TBL_NOT_EXIST);
                    return;
                }
                if (tbl != null) {
                    //the clients <2> <3> are on table with tid <1>. <2> is black. <3> is red. If either is -1, the seat is open.   
                    GameClient[] players = tbl.getPlayers();
                    //red = 0, black = 1.
                    string rp = players[0] == null ? "-1" : players[0].getUserName();
                    string bp = players[1] == null ? "-1" : players[1].getUserName();
                    client.sendMessage(Message.WHO_ON_TBL.ToString() + " " + tbl.getTid() + " " + bp + " " + rp);
                }
        }


        private void broadcastMsg(string fromUser, string msg) {
            foreach (object o in clients.Values) {
                GameClient gc = (GameClient)o;
                if (gc.inLobby()) {
                    if (gc.isConnected()) {
                        gc.sendMessage(Message.MSG + " " + fromUser + " 0 " + msg);
                    } else dcList.Add(gc);
                }
            }
            clearClosedClients();
        }

        private void sendMsg(string fromUser, string toUser, string msg) {
                foreach (object o in clients.Values) {
                    GameClient gc = (GameClient)o;
                    if (gc.getUserName().Equals(toUser)) {
                        gc.sendMessage(Message.MSG + " " + fromUser + " 1 " + msg);
                        return;
                    }
                }
        }

        private void createTable(GameClient client) {
            if (client.isLoggedIn()) {
                Random r = new Random();
                while (true) {
                    int tid = r.Next(1001, 5000);
                    bool exists = false;
                    if (tables.Count == 0) {
                        tables.Add(tid, new GameTable(tid));
                        alertNewTable(tid);
                        joinTable(client, tid);
                        return;
                    }
                    else {
                        foreach (int i in tables.Keys) {
                            if (i == tid) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            tables.Add(tid, new GameTable(tid));
                            alertNewTable(tid);
                            joinTable(client, tid);
                            return;
                        }
                    }
                }
            }
            else client.sendMessage(Message.LOGIN_FAIL);                
        }

        /**
         * All clients will be constantly updated about the status of the lobby, regardless of whether they are in 
         * the game or not.
         */
        ///<summary> ejects the game client passed from its table.</summary>
        private void leaveTable(GameClient c) {
            //find the table they are in and tell them the client is leaving.
            foreach (GameTable tbl in tables.Values) {
                GameClient[] players = tbl.getPlayers();
                
                if ((players[0]!=null && c.getUserName().Equals(players[0].getUserName()))
                    ||
                    (players[1]!=null && c.getUserName().Equals(players[1].getUserName())))
                {
                    putOffTable(c, tbl, false);
                    return;
                }
            }
            //else cant find the player in any table.
            c.sendMessage(Message.TBL_NOT_EXIST);
        }
        
        //a game client sent a move. We need to send this move to the chceckers game instance.
        private void sendMove(GameClient c, string from, string to) {
            try {
                int x = int.Parse(from.Substring(0, 1));
                x = int.Parse(from.Substring(2, 1));
                x = int.Parse(to.Substring(0, 1));
                x = int.Parse(to.Substring(2, 1));
            }
            catch (Exception) {
                c.sendMessage(Message.BAD_MESSAGE);
                return;
            }
            foreach (GameTable tbl in tables.Values) {
                GameClient[] players = tbl.getPlayers();
                if ((players[0]!=null && c.getUserName().Equals(players[0].getUserName()))
                    ||
                    (players[1]!=null && c.getUserName().Equals(players[1].getUserName())))
                {
                    tbl.sendMove(c, from, to);
                    return;
                }
            }
        }

        /// <summary>
        /// send rdy alert to checkers game
        /// </summary>
        /// <param name="c"></param>
        private void sendReady(GameClient c) {
            foreach (GameTable tbl in tables.Values) {
                GameClient[] players = tbl.getPlayers();
                if ((players[0] != null && c.getUserName().Equals(players[0].getUserName()))
                    ||
                    (players[1] != null && c.getUserName().Equals(players[1].getUserName()))) {
                    tbl.clientReady(c);
                    return;
                }
            }         
        }

        private void putOffTable(GameClient c, GameTable t, bool disconnecting) {
            t.nowLeaving(c);
            c.leaveTable(t.getTid());
            alertTableUpdate(t.getTid());
            c.joinLobby();
            if(!disconnecting)
                alertJoinLobby(c);
        }

        private void putOnTable(GameClient c, int tid) {
            c.leaveLobby();
            alertLeaveLobby(c);
            c.joinTable(tid);
            alertTableUpdate(tid);
        }

        private void addObserver(GameClient c, string tableId) {
            //if (c.isLoggedIn()) {
                int tid;
                try {
                    tid = int.Parse(tableId);
                }
                catch (Exception) {
                    c.sendMessage(Message.BAD_MESSAGE);
                    return;
                }
                if (tables.ContainsKey(tid)) {
                    GameTable t = tables[tid];
                    //c.leaveLobby();
                    //alertLeaveLobby(c);
                    c.observeTable(tid);
                    //give the player the latest update about the table its about to observe.
                    sayWhoOnTbl(c, tid);
                    t.addObserver(c);
                }
                else c.sendMessage(Message.TBL_NOT_EXIST);
            //} c.sendMessage(Message.LOGIN_FAIL);
        }

        private void stopObserving(GameClient c, string tableId, bool disconnecting) {
            int tid;
            try {
                tid = int.Parse(tableId);
            } catch (Exception) {
                c.sendMessage(Message.BAD_MESSAGE);
                return;
            }
            if (tables.ContainsKey(tid)) {
                GameTable t = tables[tid];
                t.removeObserver(c);
                c.stopObserving(tid);
                //c.joinLobby();
                //if (!disconnecting)
                //    alertJoinLobby(c);
            } else c.sendMessage(Message.TBL_NOT_EXIST);
        }
                

        //verify the client can join the table - see if tid has open seat 
        private void joinTable(GameClient c, int tid) {
            if (c.isLoggedIn()) {
                if (tables.Count > 0) {
                    foreach (int i in tables.Keys) {
                        if (i == tid) {
                            GameTable t = tables[i];
                            if (t.joinTable(c)) {
                                putOnTable(c, tid);
                                if (!t.hasOpenSeat()) {
                                    t.createGame();
                                }
                            }
                            else {
                                //we couldnt join the table. It must be full.
                                c.sendMessage(Message.TBL_FULL.ToString());
                            }
                            return;
                        }
                    }
                    //else we iterated over all the tables, and couldnt find the id.
                    c.sendMessage(Message.TBL_NOT_EXIST.ToString());
                }
                else
                    c.sendMessage(Message.TBL_NOT_EXIST.ToString());
            }
            else c.sendMessage(Message.LOGIN_FAIL);
        }

        /// <summary>
        /// Broadcast to everyone info about the table passed in.
        /// </summary>
        /// <param name="tid"></param>
        public void alertTableUpdate(int tid) {
            //we have to be locked from an above method
                if (tables.ContainsKey(tid)) {
                    //broadcast to everyone info about this table.
                    foreach (object o in clients.Values) {
                        GameClient gc = (GameClient)o;
                        if (gc.isConnected()) {
                            sayWhoOnTbl(gc, tid);
                        } else dcList.Add(gc);
                    }
                    clearClosedClients();
                }
        }

        /// <summary>
        /// Broadcast to everyone a new table was created.
        /// </summary>
        /// <param name="tid"></param>
        public void alertNewTable(int tid){
            //we have to be locked from an above method
                if (tables.ContainsKey(tid)) {
                    //tell everyone a new table was created.
                    foreach (object o in clients.Values) {
                        GameClient gc = (GameClient)o;
                        if (gc.isConnected()) {
                            gc.sendMessage(Message.NEW_TBL + " " + tid.ToString());
                        } else dcList.Add(gc);
                    }
                    clearClosedClients();
                }
            
        }

        /// <summary>
        /// Broadcasts to everyone that c joined the lobby.
        /// </summary>
        /// <param name="c"></param>
        public void alertJoinLobby(GameClient c) {
            //we have to be locked from an above method.
            foreach (object o in clients.Values) {
                GameClient gc = (GameClient)o;
                if (!gc.Equals(c)) {
                    if (gc.isConnected()) {
                        gc.sendMessage(Message.NOW_IN_LOBBY + " " + c.getUserName());
                    } else dcList.Add(gc);
                }
            }
            clearClosedClients();            
        }

        public void alertLeaveLobby(GameClient c) {
            //we have to be locked from an above method
            foreach (object o in clients.Values) {
                GameClient gc = (GameClient)o;
                if (!gc.Equals(c)) {
                    if (gc.isConnected()) {
                        gc.sendMessage(Message.NOW_LEFT_LOBBY + " " + c.getUserName());
                    } else dcList.Add(gc);
                }
            }
            clearClosedClients();
        }

        public void alertPlayerDisconnected(GameClient c) {
            //we have to locked from an above method
            string name = c.getUserName();
            //first check if the user is observing a table. If so, remove the user from observer list.
            if (c.isObserving()) {
                this.stopObservingAll(c);
            }
            //next check if the user is on a table. If so, tell the table the user is leaving.
            if (tables.Count > 0) {
                foreach (GameTable tbl in tables.Values) {
                    GameClient[] players = tbl.getPlayers();
                    if (
                        (players[0] != null &&
                        c.getUserName().Equals(players[0].getUserName()))
                                                ||
                        (players[1] != null &&
                        c.getUserName().Equals(players[1].getUserName()))
                    ) {
                        putOffTable(c, tbl, true);
                        break;
                    }
                }
            }
            //now see if the user is in the lobby. If so, tell everyone he is leaving.
            if (c.inLobby())
                alertLeaveLobby(c);
            //now free up the username and remove the client instance from memory.
            if (clients.Contains(name)) {
               clients.Remove(name);
               // c.setConnected(false);
            }
            this.gui.output("User " + name + " has left the server.");
        }

        private void stopObservingAll(GameClient c) {
            List<int> observing = c.getObserving();
            if (observing.Count > 0) {
                foreach (int tid in observing)
                    this.stopObserving(c, tid.ToString(), true);
            }
        }

        public void removeUser(string name) {
            lock (clients.SyncRoot) {
                if (clients.Contains(name)) {
                    clearClosedClient((GameClient)clients[name]);
                    this.gui.output("username " + name + " is now available.");
                } else this.gui.output("username " + name + " is now available.");
            }
        }

        /// <summary>
        /// Tear down all connections.
        /// </summary>
        public void tearDownAll() {
            lock (this) {
                IDictionaryEnumerator e = clients.GetEnumerator();
                GameClient gc;
                string keys = "";
                while (e.MoveNext()) {
                    DictionaryEntry de = (DictionaryEntry)e.Current;
                    gc = (GameClient)de.Value;
                    keys += gc.getUserName() + " ";
                }
                while (keys != "") {
                    alertPlayerDisconnected((GameClient)clients[keys.Substring(0, keys.IndexOf(" "))]);
                    keys = keys.Remove(0, keys.IndexOf(" ") + 1);
                }
                clients.Clear();
            }
        }

        /**part 4 methods 
         * These require the support of the DBConnection class
         * **/
      /*  private void registerClient(GameClient c, string pwd) {
            if (Program.CUR_PHASE == Program.Phase.FULL) {
                if (DBConnection.addUser(c.getUserName(), pwd) > 0)
                    c.sendMessage(Message.REGISTER_OK);
                else c.sendMessage(Message.ALREADY_REGISTERED);
            }
        }

        private void loginClient(GameClient c, string pwd) {
            if (Program.CUR_PHASE == Program.Phase.FULL) {
                if (DBConnection.login(c.getUserName(), pwd)) {
                    c.setLogin();
                    c.sendMessage(Message.LOGIN_OK);
                } else c.sendMessage(Message.LOGIN_FAIL);
            }
        }
        private void updateProfile(GameClient c, string profile) {
            if (Program.CUR_PHASE == Program.Phase.FULL) {
                if (c.isLoggedIn()) {
                    if (DBConnection.updateProfile(c.getUserName(), profile) > 0)
                        c.sendMessage(Message.PROFILE_UPDATED);
                } else c.sendMessage(Message.LOGIN_FAIL);
            }
        }
        private void getProfile(GameClient c, string user) {
            if (Program.CUR_PHASE == Program.Phase.FULL) {
                if (c.isLoggedIn()) {
                    string[] profile = DBConnection.getProfile(user);
                    if (profile != null) {
                        c.sendMessage(Message.USER_PROFILE.ToString() + " " + profile[0] + " " + profile[1] + " " + profile[2] + " " + profile[3]);
                    }
                } else c.sendMessage(Message.LOGIN_FAIL);
            }
        }*/
    }
}
