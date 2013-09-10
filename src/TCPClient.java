import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;
 
public class TCPClient {
	
	String hostname;
	int portnumber;

    Socket sock = null;
    PrintStream out = null;
    BufferedReader in = null;
    
    
	TCPClient(String host, int port){
		this.hostname = host;
		this.portnumber = port;	
	}
	
	public void getSocket(){
		
		try {
			ArrayList<String> ipAddress = new ArrayList<String>();
			ArrayList<String> portAddress = new ArrayList<String>();
			BufferedReader serverfile = new BufferedReader(new FileReader("Ports.txt"));
			
			String str;
			String [] strarray = new String [1];
			while((str = serverfile.readLine() )!= null){
				
				strarray = str.split(" ");
				
				ipAddress.add(strarray[0]);
				portAddress.add(strarray[1]);
			}
			serverfile.close();
			
			Random random = new Random();
			int pingHost;
			while(sock == null){
			
				pingHost = random.nextInt(ipAddress.size());
				sock = ConnectServer(ipAddress.get(pingHost), Integer.parseInt(portAddress.get(pingHost)));
			
			}
			
			out = new PrintStream(sock.getOutputStream());
	        in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
	    	
			BufferedReader read = new BufferedReader(new InputStreamReader(System.in));
			
			out.println(read.readLine());
			out.flush();
			
			System.out.println(in.readLine());
			
			
		} 
		catch (UnknownHostException e) {
            System.err.println("Don't know about host");
            System.exit(1);
		} 
		catch (IOException e) {
			 System.err.println("Couldn't get I/O for the connection");
			 System.exit(1);
		}
		
	
		
	}

	Socket ConnectServer(String hostname, int port){
		Socket sock = null;
		try {
			sock = new Socket(hostname, port);
			
		} 
		catch (UnknownHostException e) {
			return null;
		}
		catch (IOException e) {
			return null;
		}
		
		
		return sock;
	}
	
	
    public static void main(String[] args) throws IOException {
    	
    	
    	while(true){
    	TCPClient client = new TCPClient("localhost", 1234); 	
    	client.getSocket();
    	}
    
 
    }
}



class clientMaker implements Runnable{

	TCPClient client;

	clientMaker(TCPClient c){
		this.client = c;
	}
	public void run() {
		
		client.getSocket();
	
	}

	
}