 /* Student Name: <Tatiana Flores>, Lab Section: <16160 #> */
// UT EID TH27979
package assignment6;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import java.io.*;

//Packet is just a way to communication
public class Packet implements Serializable {
	//Just fix this in case of minor changes // fragile
	private static final long serialVersionUID = 12312312312L; // if object ever changed 
//	final static int HELLO = 1;
//	final static int MYKEY = 2;
//	final static int MYNAME = 3;
	final static int SENDMESSAGE = 4;
	final static int SENDFILE = 5;
	final static int SENDBROADCAST = 6;
	final static int PUBLICKEYSEND = 8;
	final static int SYMKEYSEND = 9;
	final static int ASSIGNID = 10;
	final static int LOGIN=11;
	final static int LOGINSTATUS=12;
	final static int SERVERMSG=13;
	final static int GETHISTORY=14;
	final static int CREATEACCOUNT=16;
	final static int WHO=17;
	//Type,length of packet
	int type = 0;
	int length = 0;
	//Source and dest Client Ids for the packet
	int source = 0;
	int dest = 0;
	//source and dest users for the packet
	String suser="";
	String duser="";
	//description (free form)
	String description="";
	//Raw data
	byte data[];
	//Save an int to the data stream
	public static final void IntToData(byte[] a, int offset, int value) {
		a[offset] = (byte) (value >>> 24);
		a[offset + 1] = (byte) (value >>> 16);
		a[offset + 2] = (byte) (value >>> 8);
		a[offset + 3] = (byte) (value >>> 0);
	};
	//Read an int from the byte array
	public static final int DataToInt(byte[] a, int offset) {
		int value = 0;
		value = a[offset] << 24;
		value += a[offset + 1] << 16;
		value += a[offset + 2] << 8;
		value += a[offset + 3];
		return value;
	};
	
	//Not really used but eventually for sending raw packets across network
	public byte[] toBytes() {
		byte[] r = new byte[4 * 4 + data.length];
		IntToData(r, 0, type);
		IntToData(r, 4, length);
		IntToData(r, 8, source);
		IntToData(r, 12, dest);	
		System.arraycopy(data, 0, r, 16, length);
		return r;
	}
	//Clone a packet
	//We might need this to ensure that data is a deep copy, not shallow
	public Packet(Packet p) {
		this.type = p.type;
		this.source = p.source;
		this.dest = p.dest;
		this.length = p.data.length;
		this.data = p.data.clone();
		this.duser=p.duser;
		this.suser=p.suser;
		this.description=p.description;
	}
	//Create a new packet
	public Packet(int type, int source, int dest, byte[] data) {
		this.type = type;
		this.source = source;
		this.dest = dest;
		this.length = data.length;
		this.data = data.clone();
	}
//Another way to create a packet jus uses a string instead of bytes
	public Packet(int type, int source, int dest, String data) {
		this(type, source, dest, data.getBytes());
	}
	
	//Will decrypt or encrypt based on what key you use
	public void crypt(Cipher c) throws IllegalBlockSizeException, BadPaddingException {
		data=c.doFinal(data);
	}
}
