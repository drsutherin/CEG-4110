using System;
using System.Collections.Generic;
using System.Text;

namespace CheckersServer {
    /// <summary>
    /// This class describes a checkers game. This is the physical game in memory, 
    /// with references to players, and checking if moves are valid, when the game ends,
    /// sending game-state related messages to clients, etc.
    /// 
    /// * In this variant of checkers, you can only jump one piece at a time.
    /// 
    /// derk 8.30.08
    /// </summary>
    public class CheckersGame : IntServerGame {

        private enum side {red, black, blank}; 
        private GameClient black, red;
        private bool blackRdy, redRdy;
        /* this is the current board state. <state> is a matrix
	     * where 0 = no piece is at that position, 
	     * 		 1 = black is at that position
	     * 		 2 = red is at that position
	     * 	     3 = black King is at that position
	     * 		 4 = red King is at that position.
	     * The first index specifies the row (0-based) from top to bottom,
	     * and the second index specifies the column (0-based) from left to right.
	     * so state[0][0] = 3 means a black King is at the top left corner of the board
	     * and state[7][7] = 2 means a red piece is at the bottom right corner of the board.	         
         *  
         */
        private int[,] state;
        private side curTurn;
        private side winner;
        private GameTable table;
        private int gid;

        public CheckersGame(GameClient b, GameClient r, GameTable t, int gameId) {

            blackRdy = false;
            redRdy = false;
            state = new int[8, 8];
            resetBoard();
            curTurn = side.black;
            table = t;
            black = b;
            red = r;
            gid = gameId;
            winner = side.blank;
        }

        public int getGameId() { return gid; }

        /// <summary>
        /// resets the board to the initial state for a game: 
        ///    0 1 2 3 4 5 6 7
        ///   -----------------
        /// 0 | |B| |B| |B| |B|
        /// 1 |B| |B| |B| |B| |
        /// 2 | |B| |B| |B| |B|
        /// 3 | | | | | | | | |
        /// 4 | | | | | | | | | 
        /// 5 |R| |R| |R| |R| |
        /// 6 | |R| |R| |R| |R|
        /// 7 |R| |R| |R| |R| |
        ///   -----------------
        /// </summary>
        private void resetBoard() {
            for (int i = 0; i < 8; i++)
                for (int j = 0; j < 8; j++)
                    state[i, j] = 0;
            state[0, 1] = 1; state[0, 3] = 1;
            state[0, 5] = 1; state[0, 7] = 1;
            state[1, 0] = 1; state[1, 2] = 1;
            state[1, 4] = 1; state[1, 6] = 1;
            state[2, 1] = 1; state[2, 3] = 1;
            state[2, 5] = 1; state[2, 7] = 1;

            state[5, 0] = 2; state[5, 2] = 2;
            state[5, 4] = 2; state[5, 6] = 2;
            state[6, 1] = 2; state[6, 3] = 2;
            state[6, 5] = 2; state[6, 7] = 2;
            state[7, 0] = 2; state[7, 2] = 2;
            state[7, 4] = 2; state[7, 6] = 2;

        }

        /// <summary>
        /// Encodes the current board state into a string. Used to 
        /// send board status to players.
        /// </summary>
        /// <returns></returns>
        private string encodeState() {
            string sstate = "";
            for (int i = 0; i < 8; i++)
                for (int j = 0; j < 8; j++)
                    sstate += state[i, j].ToString();
            return sstate;
        }

        /// <summary>
        /// The game has started if red is ready and black is ready.
        /// </summary>
        /// <returns>true if the game has started.</returns>
        private bool gameStarted() { return redRdy & blackRdy; }

        /// <summary>
        /// Check if a move is legal
        /// </summary>
        /// <param name="from">a string of the form "x,y", where x and y are row and column positions.</param>
        /// <param name="to">a string of the form "x,y", where x and y are row and column positions.</param>
        /// <returns>true if the move is legal.</returns>
        private bool moveLegal(string from, string to) {
            int fr, fc, tr, tc;
            try {
                fr = int.Parse(from.Substring(0, 1));
                fc = int.Parse(from.Substring(2, 1));
                tr = int.Parse(to.Substring(0, 1));
                tc = int.Parse(to.Substring(2, 1));
            }
            catch (Exception) {
                //message was in a bad format.
                return false;
            }
            //first see if a piece is there
            if (state[fr, fc] == 0) return false;
            //now see if the piece to move is the right color
            int p = state[fr, fc];
            if ((p == 1 || p == 3) && curTurn == side.red) return false;
            if ((p == 2 || p == 4) && curTurn == side.black) return false;
            bool isKing = (p == 3) || (p == 4);
            //next see what the destination is and if it is empty (it must be).
            if (state[tr, tc] != 0) return false;
            //see that black is going down and red is going up, if the piece is not a king.
            if (fr < tr && curTurn == side.red && !isKing) return false;
            if (fr > tr && curTurn == side.black && !isKing) return false;            
            //if the distance is only 1, and we know there is no piece there, so the move is valid
            if (Math.Abs(fr - tr) == 1 && Math.Abs(fc - tc) == 1) return true;
            
            //else. we can only jump one piece, so make sure the difference is exactly 2.
            else if (Math.Abs(fr - tr) > 2 || Math.Abs(fc - tc) > 2) return false;
            else {
                //the distance is 2. make sure there is a piece in between.
                //j is the state of the 'jump' square
                int j = state[((fr + tr) / 2), ((fc + tc) / 2)];
                if (j == 0) return false;
                //now make sure the piece is the right color. We can only jump our opponent.
                else if ((j == 1 || j == 3) && curTurn == side.black) return false;
                else if ((j == 2 || j == 4) && curTurn == side.red) return false;
                //now we know jump square is fine. So the move is valid.
                else return true;
            }
        }

        /// <summary>
        /// Applies the move to the game state. 
        /// This method assumes the move is already valid.
        /// </summary>
        /// <param name="from">"x,y"</param>
        /// <param name="to">"x,y"</param>
        private void applyMove(string from, string to) {
            int fr, fc, tr, tc;
            try {
                fr = int.Parse(from.Substring(0, 1));
                fc = int.Parse(from.Substring(2, 1));
                tr = int.Parse(to.Substring(0, 1));
                tc = int.Parse(to.Substring(2, 1));
            }
            catch (Exception) {
                //message was in a bad format. 
                return;
            }
            //if were just doing a single space move,
            if (Math.Abs(fr - tr) == 1 && Math.Abs(fc - tc) == 1) {
                int piece = state[fr, fc];
                state[fr, fc] = 0;
                state[tr, tc] = piece;
                if(tr == 0 || tr == 7){
                    if(curTurn == side.black)
                        state[tr,tc] = 3;
                    else state[tr,tc]= 4;
                }
            }
            //else we are jumping a piece.
            else {
                int piece = state[fr, fc];
                state[fr, fc] = 0;
                state[tr, tc] = piece;
                state[(int)((fr + tr) / 2), (int)((fc + tc) / 2)] = 0;
                if (tr == 0 || tr == 7) {
                    if (curTurn == side.black)
                        state[tr,tc] = 3;
                    else state[tr,tc] = 4;
                }
            }
            /**
             * Here implementation for jumping multiple pieces could be added. 
             * You'd have to validate the move is valid for each step in the jump,
             * and recognize that after a jump in the chain there is another jump 
             * that must be done. 
             */
        }

        /// <summary>
        /// Checks if the game is over, i.e., if both black and white 
        /// do not have a piece still left on the board.
        /// </summary>
        /// <returns>true if the game is over.</returns>
        private bool isGameOver() {
            bool bp = false;
            bool rp = false;
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if (state[i, j] == 1) bp = true;
                    else if (state[i, j] == 2) rp = true;
                    else if (state[i, j] == 3) bp = true;
                    else if (state[i, j] == 4) rp = true;
                }
            }
            return !(bp & rp);
        }
        /// <summary>
        /// Checks which side still has a piece on the board.
        /// This method just scans the board for the first piece it 
        /// finds - the method assume the game is over.
        /// </summary>
        /// <returns>the winner.</returns>
        private side checkWinner() {
            bool bp = false;
            bool rp = false;
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if (state[i, j] == 1) bp = true;
                    else if (state[i, j] == 2) rp = true;
                    else if (state[i, j] == 3) bp = true;
                    else if (state[i, j] == 4) rp = true;
                }
            }
            if (bp && rp) winner = side.blank;
            if (bp) winner = side.black;
            if (rp) winner = side.red;
            return winner;
        }

        public int getWinner() {
            if (winner == side.blank) return 0;
            if (winner == side.red) return 2;
            if (winner == side.black) return 1;
            return 0;
        }
        
        #region Messages that come from the game table module (TableSays)

        /// <summary>
        /// Tells the game a client is ready. If both are ready, this
        /// method also starts the game and tells black to move.
        /// </summary>
        /// <param name="c">The GameClient instance that is ready</param>
        public void tsAlertReady(GameClient c) {
            if(red.Equals(c))
                redRdy = true;
            if(black.Equals(c))
                blackRdy = true;
            if(redRdy && blackRdy){
                resetBoard();   //reset the board
                red.sendMessage(Message.COLOR_RED);
                red.sendMessage(Message.GAME_START.ToString());
                black.sendMessage(Message.COLOR_BLACK);
                black.sendMessage(Message.GAME_START.ToString());
                black.sendMessage(Message.YOUR_TURN.ToString());
            }
        }

        /// <summary>
        /// Sends the move to the checkers game, where it is validated
        /// and committed. The players are directly notified by the 
        /// game the results.
        /// </summary>
        /// <param name="c">The client making the move</param>
        /// <param name="from">from position "i,j"</param>
        /// <param name="to">to position "i,j"</param>
        public void tsMakeMove(GameClient c, string from, string to) {
            if (gameStarted()) {
                if (curTurn == side.black && black.Equals(c) ||
                    curTurn == side.red && red.Equals(c)) {
                    if (moveLegal(from, to)) {
                        applyMove(from, to);
                        //tell opponent what the move was
                        if (curTurn == side.black)
                            red.sendMessage(Message.OPP_MOVE + " " + from + " " + to);
                        else black.sendMessage(Message.OPP_MOVE + " " + from + " " + to);
                        //send the new board state
                        red.sendMessage(Message.BOARD_STATE + " " + this.table.getTid()+ " "+encodeState());
                        black.sendMessage(Message.BOARD_STATE + " " + this.table.getTid()+ " "+ encodeState());
                        table.updateObservers(encodeState());
                        //now check if game is over
                        if (!isGameOver()) {
                            //tell the other player it is their turn
                            if (curTurn == side.black) {
                                red.sendMessage(Message.YOUR_TURN.ToString());
                                curTurn = side.red;
                            }
                            else {
                                black.sendMessage(Message.YOUR_TURN.ToString());
                                curTurn = side.black;
                            }
                        }
                        else {  //else the game is over.                           
                            side winner = checkWinner();
                            if (winner == side.black) {
                                setWinner(black);
                            }
                            else if (winner == side.red) {
                                setWinner(red);
                            }
                            else {
                                //something happened, the game is over, but we couldnt find a winner...
                                //todo...
                            }
                            //make both players not ready.
                            redRdy = false;
                            blackRdy = false;
                            //finally, tell the table the game is over.
                            table.alertGameEnd();
                        }
                    }
                    //else the move is not legal.
                    else c.sendMessage(Message.ILLEGAL);
                }
                //else its not this players turn.
                else c.sendMessage(Message.NOT_YOUR_TURN);
            }
            //else the game hasnt started yet: some players are not ready.
            else c.sendMessage(Message.PLAYERS_NOT_READY);
        }

        /// <summary>
        /// The table says this client has left the table.
        /// </summary>
        /// <param name="c">The client quitting the game.</param>
        public void tsPlayerLeft(GameClient c) {
            //we just have to check if the game started. If so, send winning messages.
            if (gameStarted()) {
                if (c.Equals(red)) {
                    setWinner(black);
                } else if (c.Equals(black)) {
                    setWinner(red);
                } else {
                    throw new Exception("Client " + c.getUserName() + " is not playing game " + gid + "!");
                }
            }
            table.alertGameEnd();
        }

        
        private void setWinner(GameClient c) {
            if (c.Equals(black)) {
                black.sendMessage(Message.GAME_WIN);
                red.sendMessage(Message.GAME_LOSE);
              /*  if (Program.CUR_PHASE == Program.Phase.FULL) {
                    DBConnection.updateWin(black.getUserName());
                    DBConnection.updateLoss(red.getUserName());
                } */
            } else {
                red.sendMessage(Message.GAME_WIN);
                black.sendMessage(Message.GAME_LOSE);
                /*if (Program.CUR_PHASE == Program.Phase.FULL) {
                    DBConnection.updateWin(red.getUserName());
                    DBConnection.updateLoss(black.getUserName());
                }*/
            }
        }

        public string getBoardState(){
            return encodeState();
        }

        #endregion

    }
}
