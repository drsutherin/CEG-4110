package baac;

public interface Mediator {
	//Peer Methods: All messages sent from Peer(non-server interface) classes
	public void receiveFromPeer(String message);
	
	public void addPeerClass(Peer thisClass);
	
	public void removePeerClass(Peer thisClass);
	
	//Server interface methods: All messages sent from Server interface are broadcast to all peer (non-interface) classes
	public void receiveFromServer(String message);
	
	public void addServerInterface(Peer thisClass);
	
	public void removeServerInterface(Peer thisClass);

}

