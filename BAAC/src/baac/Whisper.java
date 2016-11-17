package baac;

public class Whisper{
	
	Thread myThread;
	int threadID;
	
	public Whisper(Thread myThread, int threadID){
		this.myThread = myThread;
		this.threadID = threadID;
	}
	
	public void setThreadID(int threadID){
		this.threadID = threadID;
	}
	
	public void setThread(Thread myThread){
		this.myThread = myThread;
	}
	
	public int getThreadID(){
		return threadID;
	}
	
	public Thread getThread(){
		return myThread;
	}
	
	
}