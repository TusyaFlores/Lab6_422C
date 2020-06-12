 /* Student Name: <Tatiana Flores>, Lab Section: <16160 #> */
// UT EID TH27979
package assignment6;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/*
 * 
 * Broadcaster is responsible for registering this server with the DirectoryServer and then seninging broadcasts every 1/2 second so clients
 * can find us if directory server down 
 */
public class Broadcaster implements Runnable {
	final int serverport;
	final String servername;

	public Broadcaster(String name, int port) {
		this.servername = name;
		this.serverport = port;
	}

//Register with directory and then sound out broadcasts
	public void run() {
		try {
			System.out.println("Registering my port("+serverport+") on the meta server "+DirectoryServer.METASERVER );
			DirectoryServer m = new DirectoryServer();
			m.register(serverport, servername);
		}catch(Exception e){System.out.println("Unable to register with server "+e);}
		try {
			DatagramSocket broadcaster = new DatagramSocket();
			broadcaster.setBroadcast(true);
			InetAddress address = InetAddress.getByName("255.255.255.255");
			String s=servername+":"+serverport;
			byte[] message = s.getBytes();
			System.out.println("Broadcasting my info("+s+") every 500 miliseconds" );

			while(true) {
				DatagramPacket packet = new DatagramPacket(message, message.length, address, DirectoryServer.METAPORT);
				broadcaster.send(packet);
				Thread.sleep(500);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
