 /* Student Name: <Tatiana Flores>, Lab Section: <16160 #> */
// UT EID TH27979
package assignment6;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.HashMap;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
//import javax.xml.bind.DatatypeConverter;
import java.net.*;
import java.io.*;
import static java.security.KeyFactory.getInstance;
import java.util.ArrayList;

public class ClientMain implements Runnable { // dealing with stuff from network in run method // has 2 threads : main and one
	int mode = 0;                 // with client
	Socket socket = null;
	ObjectOutputStream out = null;
	ObjectInputStream in = null;
	String ip = "";
	int port = 0;
	boolean exit = false;
	int id = 0;
	static ClientMain connection = null;
	static HashMap<String, DirectoryServer.ServerInfo> servers = new HashMap<String, DirectoryServer.ServerInfo>();
	Cipher decrypt = null;
	Cipher encrypt = null;
	String user = null;

	public ClientMain(String ip, int port) throws UnknownHostException, IOException {
		socket = new Socket(ip, port);
		this.ip = ip;
		this.port = port;
		out = new ObjectOutputStream(socket.getOutputStream()); // my java objects
		in = new ObjectInputStream(socket.getInputStream());

	}

	public void sendMessage(String m) throws IOException, IllegalBlockSizeException, BadPaddingException {
		sendMessage(0, m);
	}

	public void sendMessage(int i, String m) throws IOException, IllegalBlockSizeException, BadPaddingException {
		Packet p = new Packet(Packet.SENDMESSAGE, id, i, m);
		if (user != null) {
			p.suser = user;
		}
		sendPacket(p);
	}

	public void sendMessage(String u, String m) throws IOException, IllegalBlockSizeException, BadPaddingException {
		Packet p = new Packet(Packet.SENDMESSAGE, id, 0, m);
		if (user != null) {
			p.suser = user;
		}
		p.duser = u;
		sendPacket(p);
	}

	public synchronized void sendPacket(Packet p) throws IllegalBlockSizeException, BadPaddingException, IOException {
		if (encrypt != null) {
			p = new Packet(p);
			p.crypt(encrypt);
		}
		out.writeObject(p); // Java object
	}

	public Packet receivePacket()
			throws ClassNotFoundException, IOException, IllegalBlockSizeException, BadPaddingException {
		Packet p = (Packet) in.readObject();
		if (decrypt != null) {
			p.crypt(decrypt);
		}
		return (p);
	}

	public void run() {
		try {
			PublicKey serverkey = null;
			String symkey = null;
			while (!exit) {
				Packet p = receivePacket();
				if (p.type == Packet.ASSIGNID) {
					id = Packet.DataToInt(p.data, 0);
					System.out.println("Client assigned ID by server of " + id);
				} else if (p.type == Packet.SENDMESSAGE) {
					System.out.println("Message From: ClientID" + p.source + "(" + p.suser + ")" + new String(p.data));
				} else if (p.type == Packet.PUBLICKEYSEND) {
					//Create the servers public key
					X509EncodedKeySpec ks = new X509EncodedKeySpec(p.data);
					KeyFactory kf = KeyFactory.getInstance("RSA");
					serverkey = kf.generatePublic(ks);
					System.out.println("Got Server Public Key.");
					symkey = Keys.generateSym(); //generate random string
					//encrypt with server public key
					Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
					cipher.init(Cipher.ENCRYPT_MODE, serverkey);
					cipher.update(symkey.getBytes());
					p = new Packet(Packet.SYMKEYSEND, id, 0, cipher.doFinal());
					sendPacket(p);
					SecretKeySpec KS = new SecretKeySpec(symkey.getBytes(), "Blowfish");
					// initialise cipher to with secret key
					encrypt = Cipher.getInstance("Blowfish");
					decrypt = Cipher.getInstance("Blowfish");
					decrypt.init(Cipher.DECRYPT_MODE, KS);
					encrypt.init(Cipher.ENCRYPT_MODE, KS);
					System.out.println("Created,Encrypted and sent serversecret key. Encryption Enabled:" + symkey);
				} else if (p.type == Packet.LOGINSTATUS) {
					if (p.data[0] == 0) {
						System.out.println("Login Failed");
						connection.user = null;
					} else {
						System.out.println("Login Completed");
					}
				} else if (p.type == Packet.SERVERMSG) {
					System.out.println(new String(p.data));
				} else if (p.type == Packet.SENDFILE) {
					File f = File.createTempFile("Client-", ".download");
					System.out.println("File (" + p.description + ") Sent. Trying to save to " + f.getCanonicalPath()); // full file name
					FileOutputStream fs = new FileOutputStream(f);
					fs.write(p.data);
					fs.close();
				} else {
					System.out.println("Got packet " + p.type);
				}
			}
		} catch (

		Exception e) {
			System.out.println("Got an Error");
			System.out.println(e);
		}
	}

	public static void processUserData(BufferedReader in) {
		try {
			System.setProperty("java.net.preferIPv4Stack" , "true");
			String input = in.readLine();
			if (input.length() == 0) {
			} else if (input.charAt(0) == '#') {
				String s = input.substring(1, input.length());
				String details[] = s.split("\\s+");
				if (details[0].equalsIgnoreCase("HELP")) {
					System.out.println("ID MSG\n\t\tSend a message to client id#ID");
					System.out.println("NAME MSG\n\t\tSend a message to  user NAME");
					System.out.println("#SENDFILE USERNAME filename\n\t\tSend a file to other clients");
					System.out.println("#SERVERSR\n\t\tLists Registered Servers");
					System.out.println("#SERVERSB\n\t\tListen for a local server");
					System.out.println("#CONNECT IP:PORT\n\t\tconnect to a server");
					System.out.println("#LOGIN USERNAME PASSWORD");
					System.out.println("#CREATEACCOUNT USERNAME PASSWORD");
					System.out.println("#HISTORY");
					System.out.println("#WHO");
					System.out.println("#EXIT");
					
				} else if (details[0].equalsIgnoreCase("SERVERSB")) {
					DatagramSocket socket = new DatagramSocket(DirectoryServer.METAPORT,InetAddress.getByName("0.0.0.0"));
					byte[] buf = new byte[256];
					DatagramPacket packet = new DatagramPacket(buf, buf.length);
					System.out.println("Listening to the network for Broadcast Servers. This might fail if the firewall is blocking port "+DirectoryServer.METAPORT);
					try {
					socket.setSoTimeout(2000); 
					socket.receive(packet);
					String server= new String(packet.getData(), 0, packet.getLength());
					details=server.split(":");
					System.out.println(details[0]+"\t"+packet.getAddress().getHostAddress()+":"+details[1]);
					socket.close();
					}catch(java.net.SocketTimeoutException e){
						socket.close();
						System.out.println("Can't find any servers on my network");
					}
				} else if (details[0].equalsIgnoreCase("CONNECT") && details.length == 2) {
					System.out.println("Connect to " + details[1]);
					String ip[] = details[1].split(":");
					connection = new ClientMain(ip[0], Integer.parseInt(ip[1]));
					new Thread(connection).start();
				} else if (details[0].equalsIgnoreCase("SERVERSR") && details.length == 1) {
					DirectoryServer m = new DirectoryServer();
					List<DirectoryServer.ServerInfo> si = m.getServers();
					si.forEach((temp) -> {
						System.out.println(temp);
					});
				} else if (details[0].equalsIgnoreCase("SENDFILE") && details.length == 3) {
					try {
						InputStream inputStream = new FileInputStream(details[2]);
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						int byteRead;
						while ((byteRead = inputStream.read()) != -1) {
							baos.write((byte) byteRead);
						}
						Packet p = new Packet(Packet.SENDFILE, connection.id, 0, baos.toByteArray());
						p.description = details[2];
						p.duser = details[1];
						connection.sendPacket(p);
						inputStream.close();
					} catch (Exception e) {
						System.out.println("Failed to read file" + details[2]);
					}
				} else if (details[0].equalsIgnoreCase("LOGIN") && details.length == 3) {
					Packet p = new Packet(Packet.LOGIN, connection.id, 0, (details[1] + ":" + details[2]).getBytes());
					// System.out.println("Sent Packet2");
					connection.user = details[1];
					ClientMain.connection.sendPacket(p);
				} else if (details[0].equalsIgnoreCase("CREATEACCOUNT") && details.length == 3) {
					Packet p = new Packet(Packet.CREATEACCOUNT, connection.id, 0,
							(details[1] + ":" + details[2]).getBytes());
					ClientMain.connection.sendPacket(p);
				} else if (details[0].equalsIgnoreCase("HISTORY")) {
					ClientMain.connection.sendPacket(new Packet(Packet.GETHISTORY, connection.id, 0, new byte[0]));
				} else if (details[0].equalsIgnoreCase("WHO")) {
					ClientMain.connection.sendPacket(new Packet(Packet.WHO, connection.id, 0, new byte[0]));

				}else if(details[0].equalsIgnoreCase("EXIT")) {
					System.exit(0);
				} else {
					System.out.println("Unknown Command " + s);
				}
			} else {
				// Sending messages
				if (ClientMain.connection == null) {
					System.out.println("Cannot send message with connecting to a ser ver");
				} else if (input.matches("^\\d+\\s.*")) {
					String sendid[] = input.split("\\s+");
					String message = input.replaceAll("^\\d+", "");
					ClientMain.connection.sendMessage(Integer.parseInt(sendid[0]), message);
				} else if (input.matches("^\\S+\\s+\\S+.*")) { // added .* to take multiple words at a time
					String sendid[] = input.split("\\s+");
					String message = input.replaceAll("^\\S+", "");
					ClientMain.connection.sendMessage(sendid[0], message);
				} else {
					System.out.println("You need both a dest and a message:" + input);
				}
			}

		} catch (

		Exception e) {
			System.out.println("Failed to process user input2");
			System.out.println(e);
		}
	}

	public static void main(String[] args) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Client started. Use #HELP for help");
			while (true) {
				processUserData(in);
			}
		} catch (Exception e) {
			System.out.println("Failed to process user input");
			System.out.println(e);
		}
	}

}
