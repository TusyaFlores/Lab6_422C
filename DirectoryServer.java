 /* Student Name: <Tatiana Flores>, Lab Section: <16160 #> */
// UT EID TH27979
package assignment6;

import java.io.IOException;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DirectoryServer {
	static class ServerInfo {
		String name;
		int port;
		String ip;
		int time;

		ServerInfo(String name, String ip, int port, int time) {
			this.name = name;
			this.port = port;
			this.ip = ip;
			this.time = time;
		}

		public String toString() {
			return (name + "\t" + ip + ":" + port);
		}
	}

	public static final String METASERVER = "geodb.geo.utexas.edu";
	public static final int METAPORT = 3030;
	final int port;
	String server;

	public DirectoryServer() {
		this.server = METASERVER;
		this.port = METAPORT;
	}

	public DirectoryServer(String server, int port) {
		this.server = server;
		this.port = port;
	}

	// Run the meta server which allows clients to register
	public static void main(String[] args) throws IOException {
		ServerSocket ss = new ServerSocket(METAPORT);
		System.out.println("Listening for MetaClients on " + METAPORT);
		Pattern r = Pattern.compile("^REGISTER kittycat (\\d+) (\\S+).*"); // port and name
		HashMap<String,ServerInfo> servers=new HashMap<String,ServerInfo>();
		while (true) {
			try {				
				Socket socket = ss.accept();
				PrintWriter out = new PrintWriter(new BufferedOutputStream(socket.getOutputStream()));
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String line = in.readLine();
				Matcher m = r.matcher(line);
				String ip=socket.getInetAddress().getHostAddress();
				if (line.matches("^GET kittycat.*")) {
					System.out.println("GET from "+ip);
					servers.forEach((s,si)->{
						System.out.println("Sending "+s);
						out.println(si.name+" "+si.ip+" "+si.port+" "+si.time);						
					});
					out.close();
					//Else if line is REGISTER kittycat PORT SERVERNAME
				} else if (m.find()) {
					String port=m.group(1);
					String name=m.group(2);
					System.out.println("REGISTER "+m.group(1)+" "+m.group(2)+" "+ip);
					servers.remove(name); // if already there - delete and put back in
					servers.put(name,new ServerInfo(name,ip,Integer.parseInt(port),0));	// pasre string
				} else {
					System.out.println("Got unknown line" + line);
				}
				socket.close();
			} catch (Exception e) {
				System.out.println("Failed processing connection");
				System.out.println(e);
			}
		}
	}

	void register(int serverport, String servername) {
		try {
			Socket clientSocket = new Socket(server, port);
			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			outToServer.writeBytes("REGISTER kittycat " + serverport + " " + servername + "\n");
			System.out.println("Registered with DirectoryServer Server");
			inFromServer.readLine();
			clientSocket.close();
		} catch (Exception e) {
			System.out.println("Failed to register server");
			System.out.println(e);
		}
	}

	ArrayList<ServerInfo> getServers() {
		try {
			Socket clientSocket = new Socket(server, port);
			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			outToServer.writeBytes("GET kittycat\n");
			System.out.println("Getter server list from DirectoryServer Server");
			String line = null;
			ArrayList<ServerInfo> si = new ArrayList<ServerInfo>();
			do {
				line = inFromServer.readLine();
				if (line != null) {
					String details[] = line.split("\\s+");
					si.add(new ServerInfo(details[0], details[1], Integer.parseInt(details[2]),
							Integer.parseInt(details[3])));
				}
			} while (line != null);
			clientSocket.close();
			return (si);
		} catch (Exception e) {
			System.out.println("Failed to get meta server list");
			System.out.println(e);
		}
		return null;

	}
}
