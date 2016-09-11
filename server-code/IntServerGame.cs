using System;
using System.Collections.Generic;
using System.Text;

namespace CheckersServer {
    /// <summary>
    /// This interface defines methods that -must- be implemented for any game to be played on this server.
    /// includes methods to allow clients to alert they are ready to play, allow clients to tell the game 
    /// their moves, allow game tables to tell the game someone left, and provide a public way to get the current
    /// state of the game. 
    /// 
    /// The game state must be encodable to a string.
    /// </summary>
    interface IntServerGame {
        /// <summary>
        /// Tells the game a client is ready. If both are ready, this
        /// method also starts the game and tells black to move.
        /// </summary>
        /// <param name="c">The GameClient instance that is ready</param>
        void tsAlertReady(GameClient c);
        /// <summary>
        /// Sends the move to the game, where it is validated
        /// and committed. The players are directly notified by the 
        /// game the results.
        /// </summary>
        /// <param name="c">The client making the move</param>
        /// <param name="from">from position "i,j"</param>
        /// <param name="to">to position "i,j"</param>
        void tsMakeMove(GameClient c, string from, string to);
        /// <summary>
        /// The table says this client has left the table.
        /// </summary>
        /// <param name="c">The client quitting the game.</param>
        void tsPlayerLeft(GameClient c);
        /// <summary>
        /// Provide a public way to get the state of the game.
        /// </summary>
        /// <returns>The board state encoded in a string.</returns>
        string getBoardState();
    }
}
