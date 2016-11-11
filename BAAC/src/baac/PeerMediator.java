package baac;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class PeerMediator implements Mediator {

	//Lists of classes signed up for updates
	private final LinkedBlockingQueue<Peer> serverInterfaceList = new LinkedBlockingQueue<Peer>();
	private final LinkedBlockingQueue<Peer> peerList = new LinkedBlockingQueue<Peer>();
	
	
	/**
	 * Hands messages from the peer to the server interface
	 */
	@Override
	public synchronized void receiveFromPeer(String message) {
		for(Peer peer : serverInterfaceList){
			peer.receiveFromMediator(message);
		}
	}

	/**
	 * Add peer classes to the list of peer classes (non-server interfaces only)
	 * Thread safe method that times out and tries again if cannot access the blocking queue
	 */
	@Override
	public synchronized void addPeerClass(Peer thisClass) {
		boolean done = false;
			while(!done){
			try{
				//try to add, wait 5 millisec then release the block and try again
				peerList.offer(thisClass, (long) 5.0, TimeUnit.MILLISECONDS);
				done = true;
			} catch (InterruptedException e){
				e.printStackTrace();
				done = true;
			}
		}		
	}

	/**
	 * Remove the peer class from the blocking queue
	 * TODO: Check if threadSafe in case where an item is being added to the 
	 * 		list as an item is being removed
	 */
	@Override
	public synchronized void removePeerClass(Peer thisClass) {
		peerList.remove(thisClass);
	}

	/**
	 * Hands messages from peer to the server interface
	 */
	@Override
	public synchronized void receiveFromServer(String message) {
		for(Peer peer : peerList){
			peer.receiveFromMediator(message);
		}		
	}

	/** 
	 * Add server interfaces to list of server interfaces
	 * Thread safe method that times out and tries again if cannot access the blocking queue
	 */
	@Override
	public synchronized void addServerInterface(Peer thisClass) {
		boolean done = false;
		while(!done){
		try{
			//try to add, wait 5 millisec then release the block and try again
			serverInterfaceList.offer(thisClass, (long) 5.0, TimeUnit.MILLISECONDS);
			done = true;
		} catch (InterruptedException e){
			e.printStackTrace();
			done = true;
		}
	}	
		
	}

	/**
	 * Remove the server interface class from the blocking queue
	 * TODO: Check if threadSafe in case where an item is being added to the 
	 * 		list as an item is being removed
	 */
	@Override
	public synchronized void removeServerInterface(Peer thisClass) {
		serverInterfaceList.remove(thisClass);
	}

}
