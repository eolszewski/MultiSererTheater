import java.net.Socket;

public class LamportClock implements Comparable<Object> {
     int c;
    private String message;
    private String description;
    String data;
    private int port, extport;
	public int getExtport() {
		return extport;
	}
	public void setExtport(int extport) {
		this.extport = extport;
	}
	Socket pending;

	public LamportClock() {
        c = 1;
    }
    public int getValue() {
        return c;
    }
    
    public String getDes(){
    	return description;
    }
    
    public void tick() { // on internal actions
        c = c + 1;
    }
    public void sendAction() {
       // include c in message
        c = c + 1;
    }
    public void receiveAction(int src, int sentValue) {
        c = Math.max(c, sentValue) + 1;
    }
    public void setMessage(String message) {
    	this.message = message;
    }
    public void setDes(String des){
    	description = des;
    }
    public String getMessage() {
    	return this.message;
    }
    public int getPort() {
		return port;
	}
	public LamportClock setPort(int port) {
		this.port = port;
		return this;
	}
	@Override
	public int compareTo(Object obj) {
		
		LamportClock clock = (LamportClock) obj;
		
		if(this.c < clock.c){
			return -1;
		}
		else if (this.c > clock.c){
			return 1;
		}
		else {
			if(this.port < clock.port){
				return -1;
			}
			else if (this.port > clock.port){
				return 1;
			}	
		}
		
		
		return 0;
	}
}