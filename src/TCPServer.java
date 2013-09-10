import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.StringTokenizer;

public class TCPServer {
	static int totalseats = 30;
	static int acknowledgements = 1;
	final static int port = 1234;
	final static int theaterseats = 31;
	final static int servers = 2; 
	static boolean enterCS = false;
	static ArrayList <Integer>Seats = new ArrayList<Integer>();
	static ArrayList <String>Ports = new ArrayList<String>();
	static Map<String, ArrayList <Integer>> booking = new HashMap<String, ArrayList<Integer>>();
	static PriorityQueue <LamportClock>queue = new PriorityQueue<LamportClock>();
	static LamportClock lc = new LamportClock().setPort(port);
	
    public static void main(String[] args) throws IOException {
    	
    	BufferedReader br = new BufferedReader(new FileReader("Ports.txt"));
        try {
            String line = br.readLine();
            String [] strarray = new String [2];
            while (line != null) {
            	strarray = line.split(" ");	
				Ports.add(strarray[1]);
                line = br.readLine();
            }
        } finally {
            br.close();
        }
    	ServerSocket serverSocket = null;
    	try{
    		System.out.println("Starting Server...");
    		serverSocket = new ServerSocket(port);	
    	} 
    	catch (IOException e) {
            System.err.println("Could not listen on port." + port);
            System.exit(1);}
    	
    	
    	

    	while(true){
	    	Socket client = serverSocket.accept();
			br = new BufferedReader(new InputStreamReader(client.getInputStream()));
	    	String request = br.readLine();
	    	
	    	taskHandler tasker = new taskHandler(request, client);
	    	Thread startTask = new Thread(tasker);
	    	startTask.start();
	    	System.out.println("Task started...");
	    	
			
	    	
	    	
    	}

    }
 
	static synchronized void replyMessage(LamportClock lc, int port) {
		try {
			lc.sendAction();
			PrintStream out;
			Socket replyServer = new Socket("localhost", port);
			
			System.out.println("Reply  " + lc.getPort() + lc.getMessage());
			out = new PrintStream(replyServer.getOutputStream());
			out.println("` " + lc.c + " " + lc.getPort() + " "
					+ lc.getMessage());
			out.flush();
		} catch (Exception e) {
			String temp = lc.getMessage();
			lc.setMessage("REBOOT");
			lc.setDes(null);
			lc.setExtport(port);
			queue.add(lc);
			sendAllMessage();
			lc.setMessage(temp);
			Ports.remove(port);
		}
	}

	public static synchronized void sendAllMessage()
	{
		for (String i : Ports)
		{
			if (Integer.parseInt(i) == lc.getPort()) 
				continue; 
			PrintStream out;
			Socket toServers;

			try {
				lc.sendAction();
				toServers = new Socket("localhost", Integer.parseInt(i));
				out = new PrintStream(toServers.getOutputStream());
				if (lc.getMessage() == "FINISHED")
				{
					out.println("` " + lc.c + " " + lc.getPort() + " "
							+ lc.getMessage() + " " + lc.getDes());
				}
				else if (lc.getMessage() == "REBOOT")
				{
					out.println("` " + lc.c + " " + lc.getPort() + " "
							+ lc.getMessage() + " " + lc.getExtport());
				}
				else
					out.println("` " + lc.c + " " + lc.getPort() + " "
							+ lc.getMessage());
				out.flush();
			} catch (Exception e) {
				String temp = lc.getMessage();
				lc.setMessage("REBOOT");
				lc.setDes(null);
				lc.setExtport(Integer.parseInt(i));
				queue.add(lc);
				sendAllMessage();
				lc.setMessage(temp);
				Ports.remove(i);
			}
		}
	}


    
    public static synchronized String reserve(String name, int num) {

		if (num > totalseats) {
			return "Not enough seats currently available";
		}

		if (booking.containsKey(name)) {
			return "Seats already booked against the name provided";
		}
			
		ArrayList <Integer>Current = new ArrayList();
		for(int i = 1; i < theaterseats; i++)
			if(!Seats.contains(i))
			{
				Current.add(i);
				Seats.add(i);
				--totalseats;
				if(--num == 0)
					break;
			}
		
		booking.put(name, Current);
		System.out.println(booking.toString());
		return "Seats assigned to you are " + Current;
	}

	public static String search(String name) {
		if (booking.containsKey(name)) {
			return "Seats reserved for " + name + " " + booking.get(name);
		} else
			return "No reservation found for " + name;
	}

	public static synchronized String delete(String name) {
		System.out.println("error in delete method");

		if (!booking.containsKey(name)) {
			return "No reservation found for " + name;
		}

		ArrayList <Integer>Current = booking.get(name);
		
		for(int i = 0; i < Current.size(); i++)
		{
			Seats.remove(Current.get(i));
			++totalseats;
		}
		booking.remove(name);
		System.out.println("success in delete method");

		return "Reservation for " + name + " deleted.";
	}

}


class taskHandler implements Runnable{
	
	boolean enterCS = false;
	String request;
	Socket client;
	
	taskHandler(String input, Socket cl){
		request = input;
		client = cl;
	}
	
	
	public void run(){
		System.out.println("Initliazing tokenizer");
		StringTokenizer token = new StringTokenizer(request);
		String search = token.nextToken();
		if(request!=null && request.charAt(0) == '`')
		{		
			System.out.println("Server Request");
			try{
				if(TCPServer.queue.peek().getPort() != TCPServer.port)
					TCPServer.acknowledgements = 1;
			}catch(NullPointerException e){
			}
			System.out.println("Instantiating Lamport clock");

			LamportClock extern = new LamportClock();
			extern.c = Integer.parseInt(token.nextToken().trim());
			extern.setPort(Integer.parseInt(token.nextToken().trim()));
			extern.setMessage(token.nextToken().trim());
			String des = "";

			while(token.hasMoreTokens()){
				des += token.nextToken();
				des += " ";
			}
			System.out.println("extern initialized with\nc =" + extern.c + "\nport =" + extern.getPort() + "\nmessage =" +extern.getMessage() + "\ndes =" +des);

			TCPServer.lc.receiveAction(TCPServer.lc.c, extern.c);
			if(extern.getMessage().equals("REQUEST"))
			{
				if(!TCPServer.queue.contains(extern))
					TCPServer.queue.add(extern);
				if(TCPServer.queue.peek() == extern)
				{
					//Give Acknowledgment
					TCPServer.lc.setMessage("ACKNOWLEDGE");
					TCPServer.replyMessage(TCPServer.lc,extern.getPort());
				}
				
			}
			else if(extern.getMessage().equals("ACKNOWLEDGE"))
			{
				TCPServer.acknowledgements--;
				if(TCPServer.acknowledgements == 0)
					enterCS = true;
			}
			else if(extern.getMessage().equals("FINISHED"))
			{
				System.out.println("error in extern Finished");

				String testing = des;
				
				StringTokenizer updateToken = new StringTokenizer(testing);
				
				String check = updateToken.nextToken();
				String name = updateToken.nextToken();
				
				if(check.compareToIgnoreCase("reserve") == 0){
					int seats = Integer.parseInt(updateToken.nextToken());
					ArrayList <Integer>Current = new ArrayList();
					for(int i = 1; i < TCPServer.theaterseats; i++)
						if(!TCPServer.Seats.contains(i))
						{
							Current.add(i);
							TCPServer.Seats.add(i);
							--TCPServer.totalseats;
							--seats;
							if(seats == 0)
								break;
						}
					
					TCPServer.booking.put(name, Current);
				}
				else if(check.compareToIgnoreCase("delete") == 0){
						TCPServer.booking.remove(name);
				}
				
				
				TCPServer.queue.poll();
				
				if(!TCPServer.queue.isEmpty())
					
				
					if(TCPServer.queue.peek().getPort() == TCPServer.port)
				
					{
					
						TCPServer.lc.setMessage("REQUEST");
						TCPServer.sendAllMessage();
				
					}
			}
			System.out.println("success in extern finished at delete");

			
		}
		
		else 
		{
			String searchResult;
			
			if(search.equalsIgnoreCase("search")){
				PrintWriter out = null;
				try {
					out = new PrintWriter(client.getOutputStream());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				try{
					 searchResult = TCPServer.search(token.nextToken().trim());
					out.println(searchResult);
					out.flush();

				}
				catch (Exception e){
					searchResult = "Improperly formatted request.";
					out.println(searchResult);
					out.flush();

					
				}
			}
			
			else
			{
				LamportClock temp = new LamportClock().setPort(TCPServer.port);
				temp.c = TCPServer.lc.c;
				temp.pending = client;
				temp.tick();
				temp.setDes(request);
				TCPServer.queue.add(temp);
				
				if(TCPServer.queue.peek().getPort() == TCPServer.port){
					TCPServer.lc.setMessage("REQUEST");
					if(TCPServer.Ports.size() > 1)
						TCPServer.sendAllMessage();
					else
						enterCS = true;
				}
			}
	    	
		
		}
    	if(enterCS && search.compareToIgnoreCase("search") != 0)
    	{
			System.out.println("success in extern finished at delete plus search");
    		LamportClock task = TCPServer.queue.poll();
    		
	    	clientHandler handle = new clientHandler(task.pending,task.getDes());
	    	Thread t = new Thread(handle);
	    	t.start();
	    	
	    	TCPServer.lc.setMessage("FINISHED");
	    	TCPServer.lc.setDes(task.getDes());
	    	if(TCPServer.Ports.size() > 1)
	    	TCPServer.sendAllMessage();
	    	
	    	enterCS = false;
	    	
	    	
    	}
		


	}

	
}

class clientHandler implements Runnable {

	static String sentence;
	String description;
	Socket client;
	
	clientHandler(Socket sock, String des){
		this.client = sock;
		description = des;
	}
	
	public void run() {
		try{
			
			//BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			PrintWriter  out = new PrintWriter(client.getOutputStream());
			String command = description;
			StringTokenizer token = new StringTokenizer(command);
			String tag = null;
			try{
				tag = token.nextToken().trim();
			}
			catch (Exception e){
				sentence = "Improperly formatted request.";
			}
		   
			if (tag.compareToIgnoreCase("reserve") == 0) {
				try{
					sentence = TCPServer.reserve(token.nextToken().trim(),Integer.parseInt(token.nextToken().trim()));
					out.println(sentence);

				}
				catch (Exception e){
					sentence = "Improperly formatted request.";
					out.println(sentence);

				}
			}
			else if (tag.compareToIgnoreCase("delete") == 0) {
				try{
					System.out.println("error in clienthandler at delete");
					sentence = TCPServer.delete(token.nextToken().trim());
					out.println(sentence);
				}
				catch (Exception e){
					sentence = "Improperly formatted request.";
					out.println(sentence);
				}
				System.out.println("success in clienthandler at delete");
			}
			
			else if (tag.compareToIgnoreCase("quit") == 0) {
				sentence = "Client quit";
				out.println(sentence);
			}
			else {
				sentence = "Improperly formatted request.";
				out.println(sentence);
			}

			out.flush();
System.out.println("success in run");
		}catch(Exception e){
			
		}
	}
}

