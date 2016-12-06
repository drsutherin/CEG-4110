package baac;

/**
 * * Threadsafe Singleton Class for player:
  * player info: ```username```
  * player status enum: ```in_lobby```, ```on_board```, ```playing_game```
  * ActiveUsersList
    * Will maintain a list of **all** users active on the server (i.e. any user than
    could receive a private message)
  * ActiveUsersList
    * Will maintain a list of all tables on the server
    * Table statuses will depend on how server responds to ```109: ASK_TBL_STATUS```, i.e. what ```207: BOARD_STATE``` returns
 * @author reuintern, rhodes
 *
 */

public class Player {
	
	//private so only this class can access the instance directly
	private volatile static Player uniqueInstance;
	
	private static String username;
	private static Status user_status;
	
	private Player(){
		username = "";
		user_status = Status.IN_LOBBY;
	};
	
	
	// Threadsafe method that restricts creation of new instances. 
	// Prevents multiple threads from creating their own instance.
	// Reference: Head First Design Patterns, Page 184, Singleton
	public static Player getInstance(){
		//check for instance, if there is none, enter the synchronized block
		if (uniqueInstance == null);
			synchronized (Player.class) {
				//check again, since it's possible for another thread to have made
				// an instance before this thread got to the synchronized block
				if (uniqueInstance == null){
					uniqueInstance = new Player();
				}
			}
		return uniqueInstance;
	}
	
	public static void setUsername(String name){
		username = name;
	}
	
	public static void setUserStatus(Status status){
		user_status = status;
	}
	
	public static String getUsername(){
		return username;
	}
	
	public static Status getUserStatus(){
		return user_status;
	}

}
