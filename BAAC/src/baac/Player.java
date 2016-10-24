package baac;

import java.util.Vector;
/**
 * * Simple class containing:
  * player info: ```username```
  * player status enum: ```in_lobby```, ```on_board```, ```playing_game```
  * ActiveUsersList
    * Will maintain a list of **all** users active on the server (i.e. any user than
    could receive a private message)
  * ActiveUsersList
    * Will maintain a list of all tables on the server
    * Table statuses will depend on how server responds to ```109: ASK_TBL_STATUS```, i.e. what ```207: BOARD_STATE``` returns
 * @author reuintern
 *
 */

public class Player {
	String username;
	Status user_status;
	Vector<String> activeUsersList;		// all active users on the server (anyone who can receive a private message)
	Vector<String[]> activeTablesList;	// all active tables on the server
										// Tables defined as arrays: table[0] = name, table[1] = player1, table[2] = player2, table[3] = tableStatus
	public Player()	{
		username = "";
		user_status = Status.in_lobby;
		activeUsersList = new Vector<String>();
		activeTablesList = new Vector<String[]>();
	}
	

}
