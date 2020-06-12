 /* Student Name: <Tatiana Flores>, Lab Section: <16160 #> */
// UT EID TH27979
package assignment6;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.io.*;

public class ClientService implements Runnable {
	Socket s = null;
	final ServerMain ms;
	public boolean exit = false; // to control thread
	ObjectOutputStream out = null;
	ObjectInputStream in = null;
	static int count = 10;
	final int id;
	String ip="";
//	String history;
	Cipher encrypt = null;
	Cipher decrypt = null;
	String user = null;

	public ClientService(Socket s, ServerMain ms) {
		this.s = s;
		this.ms = ms;
		this.id = count++;
	}

	public synchronized void sendPacket(Packet p) {
		try {
			if (encrypt != null) {
				p = new Packet(p);
				p.crypt(encrypt);
			}
			out.writeObject(p);
		} catch (IOException | IllegalBlockSizeException | BadPaddingException e) {
			System.out.println("Failed to write to client removingfrom list of clients ID:" + id); // if failed -remove this client from the list, set flag exit
			System.out.println(e);
			ms.clients.remove(this);
			this.exit = true;
		}
	}

	public Packet readPacket() // asynch avoid deadlocks
			throws ClassNotFoundException, IOException, IllegalBlockSizeException, BadPaddingException {
		Packet r = (Packet) in.readObject();
		if (decrypt != null) {
			r.crypt(decrypt);
		}
		return (r);

	}

	public void run() {
		try {
			out = new ObjectOutputStream(s.getOutputStream());
			in = new ObjectInputStream(s.getInputStream());
			System.out.println("Running client " + id);
			ip=s.getInetAddress().getHostAddress();
			Packet p = new Packet(Packet.ASSIGNID, 1, id, new byte[4]);
			Packet.IntToData(p.data, 0, id);
			sendPacket(p);
			p = new Packet(Packet.PUBLICKEYSEND, 1, id, ms.serverkeys.serialize());
			sendPacket(p);
			while (!exit) {
				final Packet r = readPacket();
				if (r.type == Packet.SENDMESSAGE) {
					// They sent a message for now forward to appropriateclients
					System.out.println(
							"Relaying message " + r.source + "(" + r.suser + ")=>" + r.dest + "(" + r.duser + ")");
					if (user != null) {
						ms.addHistory(user, new String(r.data));
					}
					ms.clients.forEach((temp) -> { // for each
						if (temp.id == r.dest
								|| (r.dest == 0 && r.duser != null && r.duser.equalsIgnoreCase(temp.user))) { // sending pac
							temp.sendPacket(r);
						}
					});
				} else if (r.type == Packet.SENDFILE) {
					// They sent a file  for now forward to appropriateclients
					System.out.println(
							"Relaying File " + r.source + "(" + r.suser + ")=>" + r.dest + "(" + r.duser + ")");
					if (user != null) {
						ms.addHistory(user, new String("File Sent:"+r.description)); //file name
					}
					ms.clients.forEach((temp) -> {
						if (temp.id == r.dest
								|| (r.dest == 0 && r.duser != null && r.duser.equalsIgnoreCase(temp.user))) {
							temp.sendPacket(r);
						}
					});

				} else if (r.type == Packet.SYMKEYSEND) { //Client is sending us his key
					Cipher c2 = Cipher.getInstance("RSA/ECB/PKCS1Padding"); // decryption with orivate key
					c2.init(Cipher.DECRYPT_MODE, ms.serverkeys.keys.getPrivate());
					c2.update(r.data);
					byte[] symkey = c2.doFinal();
					System.out.println(
							"Receieved Encrypted Sym Key. Encryption enabled " + r.source + " " + new String(symkey));
					// create a key
					SecretKeySpec KS = new SecretKeySpec(symkey, "Blowfish");
					// initialise cipher to with secret key
					encrypt = Cipher.getInstance("Blowfish");
					decrypt = Cipher.getInstance("Blowfish");
					decrypt.init(Cipher.DECRYPT_MODE, KS);
					encrypt.init(Cipher.ENCRYPT_MODE, KS);
				} else if (r.type == Packet.LOGIN) {
					String login = new String(r.data);
					String details[] = login.split(":");
					System.out.println("Login Attempted for :" + details[0]);
					byte result[] = new byte[1];
					if (ms.users.check_user(details[0], details[1])) {
						user = details[0];
						result[0] = 1; // success
						sendPacket(new Packet(Packet.LOGINSTATUS, 1, id, result));
					} else {
						result[0] = 0; // failed
						sendPacket(new Packet(Packet.LOGINSTATUS, 1, id, result));
					}
				} else if (r.type == Packet.CREATEACCOUNT) {
					String login = new String(r.data);
					String details[] = login.split(":");
					System.out.println("Account creation for :" + details[0]);
					if (ms.users.create_account(details[0], details[1])) {
						sendPacket(new Packet(Packet.SERVERMSG, 0, id, "Account Created"));
					} else {
						sendPacket(new Packet(Packet.SERVERMSG, 0, id, "Unable to Create Account"));
					}
				} else if (r.type == Packet.GETHISTORY && user != null) {
					sendPacket(new Packet(Packet.SERVERMSG, 0, id, ms.getHistory(user)));
				} else if (r.type == Packet.WHO) {
					String result="ID\tIP\t\tUSER\n";
					for (ClientService s : ms.clients) {
							result+=s.id+"\t"+s.ip+"\t";
							if(s.user!=null) {result+=s.user;}
							result+="\n";
					};
					sendPacket(new Packet(Packet.SERVERMSG, 0, id, result));
					
				} else {
					System.out.println("Got Unknown Packet " + r.type + " from " + r.source);
				}
			}
		} catch (

		Exception e) {
			System.out.println("Client ID " + id + " died");
			System.out.println(e);
			ms.clients.remove(this);
		}
	}
}
