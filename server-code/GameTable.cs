using System;
using System.Collections.Generic;
using System.Text;

namespace CheckersServer {
    /// <summary>
    /// derek
    /// 8.14.08
    /// </summary>
    public class GameTable {

        private GameClient black;
        private GameClient red;
        private int seatsOpen = 2;
        private int tid;
        private Random coin = new Random();
        private CheckersGame curGame = null;
        private Dictionary<int, CheckersGame> gameHistroy;
        private Dictionary<string, GameClient> observers;

        public GameTable(int tableId) {
            gameHistroy = new Dictionary<int, CheckersGame>();
            observers = new Dictionary<string, GameClient>();
            tid = tableId;
            seatsOpen = 2;
            black = null;
            red = null;
            curGame = null;         
        }

        public bool joinTable(GameClient user) {
            bool joinOK = false;
            lock(this){
                if (seatsOpen == 2) {
                    if (coin.Next(1, 3) == 1)
                        black = user;
                    else red = user;
                    --seatsOpen;
                    joinOK = true;
                }
                else if (seatsOpen == 1) {
                    if (black == null)
                        black = user;
                    else red = user;
                    --seatsOpen;
                    joinOK = true;
                }
                else {
                    joinOK = false;
                }
                return joinOK;
            }
        }
        
        public bool hasOpenSeat() { return seatsOpen > 0; }

        public void createGame() {
            if (seatsOpen == 0) {
                //then start a new game!
                curGame = new CheckersGame(black, red, this, getNewGameId());
            }
        }

        public GameClient[] getPlayers() {
            GameClient[] gcs = { red, black };
            return gcs;
        }

        private int getNewGameId() {
            while (true) {
                Random r = new Random();
                int gameID = (int)(r.Next(1000));
                if (!gameHistroy.ContainsKey(gameID))
                    return gameID;
            }

        }

        public void clientReady(GameClient c) {
            if (curGame != null) {
                curGame.tsAlertReady(c);
            } else c.sendMessage(Message.GAME_NOT_CREATED);
        }

        public void alertGameEnd() {
            /* check if there is a winner before saving this game. 
             * If a client left the table, not the game never started, there is no
             * winner, cause the game never even started.
             */
            if(curGame.getWinner() > 0)
                gameHistroy.Add(curGame.getGameId(), curGame);
            curGame = null;
        }

        /// <summary>
        /// Removes a player from the table
        /// </summary>
        /// <param name="c">the player instance to remove</param>
        public void nowLeaving(GameClient c) {
            if (black != null && black.Equals(c)) {
                black = null;
                if(red != null)
                    red.sendMessage(Message.OPP_LEFT_TABLE.ToString());
                seatsOpen++;
            }
            else if (red != null && red.Equals(c)) {
                red = null;
                if(black != null)
                    black.sendMessage(Message.OPP_LEFT_TABLE.ToString());
                seatsOpen++;
            }
            if (curGame != null) {
                //if a game was instanciated, tell the game a player quit.
                //This will make the game close properly.
                curGame.tsPlayerLeft(c);
            }
        }

        public void sendMove(GameClient c, string from, string to) {
            curGame.tsMakeMove(c, from, to);
        }

        public int getTid() { return tid; }

        //observers..
        public void addObserver(GameClient c) {
            if(!observers.ContainsKey(c.getUserName())){
                this.observers.Add(c.getUserName(), c);
            }
            if (curGame != null) {
                 c.sendMessage(Message.BOARD_STATE + " " +this.tid.ToString()+" "+ curGame.getBoardState());                              
            }
        }

        public void removeObserver(GameClient c) {
            if (observers.ContainsKey(c.getUserName()))
                observers.Remove(c.getUserName());
        }

        public void updateObservers(string boardState) {
            foreach (GameClient c in observers.Values) {
                c.sendMessage(Message.BOARD_STATE + " " +this.tid.ToString()+ " "+ boardState);
            }
        }

        public CheckersGame getGameInstance() {
            return curGame;
        }
    }
}

