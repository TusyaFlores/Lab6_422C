 /* Student Name: <Tatiana Flores>, Lab Section: <16160 #> */
// UT EID TH27979
package assignment6;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Arrays;
import java.util.Random;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.interfaces.*;
import java.io.*;
import javax.crypto.*;
import java.security.*;

// System.out.println(new String(cipherText, "UTF8"));

public class Keys implements Serializable {
	private static final long serialVersionUID = 1L;
	public KeyPair keys;

	public Keys() {
		try {
			// Create KeyPair generator object
			KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
			keyPairGen.initialize(1024, new SecureRandom());
			// Initializing the key pair generator
			keyPairGen.initialize(2048);
			// Generating the pair of keys
			keys = keyPairGen.generateKeyPair();
		} catch (Exception e) {
			System.out.println("Can't generate key defaulting to no cipher");
		}
		// encrypting the data
		// byte[] cipherText = cipher.doFinal();
	}
	//Cand send a public key
	public byte[] serialize() {
		return (((RSAPublicKey) keys.getPublic()).getEncoded());
	}
	//helper function to generate a symetric key which is used for speed
	public static String generateSym() {
		int leftLimit = 97; // letter 'a'
		int rightLimit = 122; // letter 'z'
		int targetStringLength = 20;
		Random random = new Random();
		StringBuilder buffer = new StringBuilder(targetStringLength);
		for (int i = 0; i < targetStringLength; i++) {
			int randomLimitedInt = leftLimit + (int) (random.nextFloat() * (rightLimit - leftLimit + 1));
			buffer.append((char) randomLimitedInt);
		}
		return buffer.toString();
	}
}
