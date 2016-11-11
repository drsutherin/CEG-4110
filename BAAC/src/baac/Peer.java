package baac;
/**
 * This abstract class provides a contract as to what defines a peer class
 * 
 * 
 * Every peer must satisfy these conditions:
 * 	1) extend Peer in class definition
 *  2) implement the constructor with the parameter mediator and set this.mediator = mediator
 *  3) implement the abstract method: receiveFromMediator(message)
 * 
 * @author ulyz
 *
 */
public abstract class Peer {
	
	private Mediator mediator;
	
	//public Peer(Mediator passedMediator){
	//	this.mediator = passedMediator;
	//}
	
	//get mediator
	public Mediator getMediator(){
		return mediator;
	}
	
	//send message to mediator
	public void sendToMediator(String message){
		mediator.receiveFromPeer(message);
	}
	
	//must be implemented by every class that extends peer
	public abstract void receiveFromMediator(String message);
	
	

}
