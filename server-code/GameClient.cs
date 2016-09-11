using System;
using System.Collections.Generic;
using System.Collections;
using System.Text;
using System.Net;
using System.Net.Sockets;
using System.Threading;
using System.IO;

namespace CheckersServer
{
    /// <summary>
    /// This class represents a game client. Each instance is managed by a 
    /// thread that listens for messages from this client. The game clients
    /// communicate messages to the NetInterface, manipulating the server
    /// state accordingly. 
    /// 
    /// 8.14.08 
    /// -modified: 
    /// 9.15.08
    /// 10.7.08
    /// /***
    /// If there is no user authentication in place, set the private instance variable loggedIn = true.
    /// ***/
    /// </summary>
    public class GameClient
    {

        private TcpClient client;
        bool keepRunning = false;
        Thread cThread;
        GameServer server;
        NetworkStream networkStream;
        string[] msgDelim = { Message.endOfMsg };

        List<string> msgSent; //the record of tcp messages sent to this client

        /* system related information */
        private string userName = "";
        private bool lobby = false;
        private bool connected = true;

        private bool loggedIn = false;

        private int? curTable = null;
        private List<int> nowObserving = new List<int>();

        /*Constructor is just for initialization. This does not give the client state, 
         * only a user name and the network connection to the client computer.  */
        public GameClient(string name, TcpClient ClientSocket, GameServer netInt)
        {
            client = ClientSocket;
            server = netInt;
            userName = name;
            curTable = null;
            networkStream = null;
            connected = true;
            msgSent = new List<string>();
        }

        public NetworkStream getNetworkStream() { return this.networkStream; }

        public string getUserName() { return userName; }
        public bool inLobby() { return lobby; }
        public int getCurTable() { return curTable != null ? curTable.Value : -1; }
        public List<int> getObserving() { return nowObserving; }
        public bool isObserving() { return nowObserving.Count > 0; }
        public void setLogin() { loggedIn = true; }
        public bool isLoggedIn() { if (Program.CUR_PHASE == Program.Phase.FULL) return loggedIn; else return true; }
        /// <summary>
        /// Gets the list of all TCP messages sent to this client from the server.
        /// </summary>
        /// <returns>A list of the TCP messages in chronological order as strings.</returns>
        public List<string> getMessages() { return msgSent; }

        public void leaveTable(int tid)
        {
            curTable = null;
            sendMessage(Message.TBL_LEFT.ToString() + " " + tid.ToString());
        }
        public void joinTable(int tid)
        {
            curTable = tid;
            sendMessage(Message.TBL_JOINED.ToString() + " " + tid.ToString());
        }
        public void observeTable(int tid)
        {
            nowObserving.Add(tid);
            sendMessage(Message.NOW_OBSERVING.ToString() + " " + tid.ToString());
        }
        public void stopObserving(int tid)
        {
            if (nowObserving.Contains(tid))
            {
                nowObserving.Remove(tid);
                sendMessage(Message.STOPPED_OBSERVING.ToString() + " " + tid.ToString());
            }
            else sendMessage(Message.NOT_OBSERVING);
        }

        public void joinLobby()
        {
            lobby = true;
            curTable = null;
            this.sendMessage(Message.IN_LOBBY.ToString());
        }

        public void leaveLobby()
        {
            lobby = false;
            this.sendMessage(Message.OUT_LOBBY.ToString());
        }

        public void sendMessage(int x) { sendMessage(x.ToString()); }
        public void sendMessage(string s)
        {
            msgSent.Add(s);
            if (connected && client != null)
            {
                try
                {
                    Byte[] msg = System.Text.Encoding.ASCII.GetBytes(s + Message.endOfMsg);
                    client.Client.Send(msg, msg.Length, 0);
                }
                catch (Exception ex)
                {
                    if (ex is SocketException || ex is ObjectDisposedException)
                    {
                        //the connection might have been focibly closed by the user.
                        FileLogger.writeToExceptionLog("A socket exception occured on game client trying to send data to it " + this.getUserName());
                        FileLogger.writeToExceptionLog("Connection may have been forcefully closed.");
                        this.connected = false;
                        //tell the server to get rid of this client
                        server.clearClosedClient(this);
                        Kill();
                        //TODO : find the right way to kill the thread and this game client instance.
                    }
                }
            }
        }

        #region Thread Management
        /// <summary>
        /// "Starts" the client instance, that is, start listening for messages from the real client 
        /// on the network.
        /// </summary>
        public void Start()
        {
            keepRunning = true;
            //create a thread to listen for msgs from clients. messageListener is the method to be threaded.
            cThread = new Thread(new ThreadStart(messageListener));
            cThread.Start();
        }

        ///<summary> 
        /// This method operates in its own thread. The thread listens
        /// for incoming messages from the client whose connection is encapsulated
        /// in the TcpClient object client. When a message is received, the method passes
        /// it off to the NetInterface class. 
        ///</summary>
        private void messageListener()
        {

            // Incoming data from the client.
            string streamIn = null;
            // Data buffer for incoming data.
            byte[] bytes;
            // Some messages will have parameters. Store them in this list.
            List<string> mParams = new List<string>();
            char[] splitter = { ' ' };

            if (client != null)
            {
                networkStream = client.GetStream();
                //ClientSocket.ReceiveTimeout = 100; // 1000 miliseconds
                while (keepRunning && connected)
                {
                    bytes = new byte[client.ReceiveBufferSize];
                    try
                    {
                        int BytesRead = networkStream.Read(bytes, 0, (int)client.ReceiveBufferSize);
                        if (BytesRead > 0)
                        {
                            //we received a message from this client. 
                            Thread.Sleep(2000); //sleep just to make sure the entire message gets here.
                            streamIn = Encoding.ASCII.GetString(bytes, 0, BytesRead);
                            string[] msgs = streamIn.Split(msgDelim, StringSplitOptions.RemoveEmptyEntries);
                            foreach (string data in msgs)
                            {
                                string[] datSplit = data.Split(splitter);
                                string msgCode = datSplit[0];
                                mParams.Clear();
                                for (int i = 2; i < datSplit.Length; i++)
                                    mParams.Add(datSplit[i]);
                                int imsg = -1;
                                try
                                {
                                    imsg = int.Parse(msgCode);
                                    if (imsg == 9999)
                                    { //9999 is the kill switch for this client.
                                        Kill();
                                        break;
                                    }
                                    if (imsg == 108)
                                    {
                                        this.connected = false;
                                    }
                                    if (imsg > 0)
                                    { //process the message received on the server.
                                        //output message received to the server output window
                                        server.gui.output(userName + ": " + data);
                                        server.processMessage(this, int.Parse(msgCode), mParams);
                                    }
                                }
                                catch (Exception)
                                {
                                    this.sendMessage(Message.BAD_MESSAGE.ToString());
                                }
                            }
                        }
                        else Kill();
                    }
                    catch (IOException) { } // Timeout
                    catch (SocketException)
                    {
                        //the connection might have been focibly closed by the user.
                        FileLogger.writeToExceptionLog("A socket exception occured on game client reading data from it " + this.getUserName());
                        FileLogger.writeToExceptionLog("Connection may have been forcefully closed.");
                        this.connected = false;
                        //tell the server to get rid of this client
                        server.clearClosedClient(this);
                        Kill();
                        break;
                    }
                    catch (Exception)
                    {
                        this.sendMessage(Message.BAD_MESSAGE.ToString());
                    }
                    Thread.Sleep(2000);
                }
            } Kill();
        }

        public void Kill()
        {
            if (keepRunning)
            {
                keepRunning = false;
                try
                {
                    networkStream.Close(); //close the network stream used to get data from the client
                }
                catch (Exception)
                {
                    FileLogger.writeToExceptionLog("error in kill client " + this.getUserName() + ".: network connection already closed.");
                }
                try
                {
                    client.Client.Close(); //close the socket connection to the client.
                }
                catch (Exception)
                {
                    FileLogger.writeToExceptionLog("error in kill client " + this.getUserName() + ".: socket connection already closed.");
                }
                if (cThread != null)
                {
                    try
                    {
                        cThread.Abort();
                        cThread = null;
                    }
                    catch (ThreadAbortException) { cThread = null; }
                    catch (Exception e)
                    {
                        FileLogger.writeToExceptionLog("Exception in aborting cThread in client " + this.getUserName() + ". Exception: "
                          + e.Message);
                        FileLogger.writeToExceptionLog("cThread = null now.");
                        cThread = null;
                    }
                }
                connected = false;
            }
        }

        public bool Alive
        {
            get
            {
                return (cThread != null && cThread.IsAlive);
            }
        }
        #endregion

        public TcpClient getTCPCon() { return client; }

        public bool isConnected()
        {
            if (networkStream == null)
            {
                return true;
            }
            try
            {
                Byte[] msg = System.Text.Encoding.ASCII.GetBytes("ping" + Message.endOfMsg);
                client.Client.Send(msg, msg.Length, 0);
                return true;
            }
            catch (Exception)
            {
                connected = false;
                return false;
            }
        }

        /// <summary>
        /// Overload of the Equals method. GC's are equal if they have the same user name only.
        /// </summary>        
        public bool Equals(GameClient c)
        {
            return this.getUserName().Equals(c.getUserName());
        }

        internal void setConnected(bool p)
        {
            this.connected = p;
        }
    }
}