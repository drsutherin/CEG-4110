package baac;

import java.util.Observer;

/**
 * This abstract class provides a contract as to what defines a peer class
 * 
 * 
 * Every peer must satisfy these conditions:
 * 	1) extend Peer in class definition
 *  2) implement the constructor with the parameter mediator and set this.mediator = mediator
 *  3) in the constructor, add the peer as an interface or a peer by using the mediator.addPeer(this) or mediator.addServerInterface(this) methods
 *  4) implement the abstract method: receiveFromMediator(message)
 * 
 * @author ulyz
 *
 */
public abstract class Peer implements Observer {
	
	private Mediator mediator;
	
	//*********************************
	//implement in each peer class
	//public className(Mediator passedMediator){
	//	this.mediator = passedMediator;
	//	mediator.addPeer(this); or mediator.addServerInterface(this);
	//}**************************************
	
	//get mediator
	public Mediator getMediator(){
		return mediator;
	}
	
	//send message to mediator
	public void sendToMediator(String message){
		mediator.receiveFromPeer(message);
	}
	
	/**
	 * Thread will sleep for a given number of cycles
	 */
	protected void sleepyThread(){
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//must be implemented by every class that extends peer
	public abstract void receiveFromMediator(String message);
	
}
