

public class Mote{

	public static final int INIT_STATUS = 0;
	public static final int SYNC_STATUS = 1;
	public static final int READY_STATUS = 2;
	public static final int WAIT_STATUS = 3;

	private int ID;
	private int interval;
	private boolean ack;
	private int status;
	
	public Mote(int ID){
		this.ID = ID;
		this.status = INIT_STATUS;
	}

	public void setID(int ID){
		this.ID = ID;
	}
	
	public int getID(){
		return ID;
	}
	
	public void setInterval(int interval){
		this.interval = interval;
	}
	
	public int getInterval(){
		return interval;
	}
	
	public void setStatus(int newStatus){
		status = newStatus;
	}
	
	public void acknowledge(){
		ack = !ack;
	}

	public boolean getAck(){
		return ack;
	}
	
	public int getStatus(){
		return status;
	}
	

}
