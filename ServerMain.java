 /* Student Name: <Tatiana Flores>, Lab Section: <16160 #> */
// UT EID TH27979
package assignment6;

import java.io.*;
import java.net.*;
import java.lang.Thread;
import java.util.*;
import java.util.concurrent.*;


//The main server class
public class ServerMain {
	//public static final int PORT = 6666;
	public Keys serverkeys;
	public CopyOnWriteArrayList<ClientService> clients = new CopyOnWriteArrayList<ClientService>(); // not to corrupt during iteration
	final String name;
	final int listenport;
	final int broadcastport;
	ConcurrentSkipListMap<String, Integer> channels = new ConcurrentSkipListMap<String, Integer>();
	int channelid = 1000;
	UserBase users=new UserBase();

	private HashMap<String,String> msghistory=new HashMap<String,String>();
	
	public synchronized void addHistory(String user,String msg) {
		if(msghistory.containsKey(user)) {
			msghistory.replace(user,msghistory.get(user)+msg+"\n");		
		}else {
			msghistory.put(user, msg+"\n");
		}
	}
	public synchronized String getHistory(String user) {
		if(msghistory.containsKey(user)) {
			return(msghistory.get(user));
		}
		return("");
	}	
	public ServerMain(int listenport, int broadcastport, String name) {
		this.listenport = listenport;
		this.name = name;
		this.broadcastport = broadcastport;
		serverkeys = new Keys();
	}

	public static void main(String[] args) {
		try {
			String servername="ServerMain";
			int port=5000;			
			if(args.length>=1) {
				servername=args[0];
			}
			if(args.length>=2) {
				port=Integer.parseInt(args[1]);
			}
			
			ServerMain server = new ServerMain(port, 5001, servername);
			ServerSocket ss = new ServerSocket(server.listenport);
			System.out.println("Listening for clients on " + server.listenport);
			Broadcaster b = new Broadcaster(server.name, port);
			new Thread(b).start();

			while (true) {
				Socket s = ss.accept();
				ClientService cs = new ClientService(s, server);
				server.clients.add(cs);
				new Thread(cs).start();
			}

		} catch (Exception e) {
			System.out.println("Failed to accept client thread");
			System.out.println(e);
		}
	}
}
