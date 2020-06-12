 /* Student Name: <Tatiana Flores>, Lab Section: <16160 #> */
// UT EID TH27979
package assignment6;

import java.util.HashMap;

//This is the User Database
//Right now it's all memory based and allows creations of users and checking if a user has valid credentials.
//Eventually it coul do more

public class UserBase {
	HashMap<String, String> users = new HashMap<String, String>();//users to passwords

	//Create an account
	synchronized boolean create_account(String username, String password) {
		username=username.toLowerCase();
		if (users.containsKey(username)) {//Can't create a usaer if it already exists
			return false;
		}
		//Create user
		users.put(username, password);
		return (true);
	}
	//Check if the account is using the correct password
	synchronized boolean check_user(String username, String password) {
		username=username.toLowerCase();
		if (users.containsKey(username) && users.get(username).equalsIgnoreCase(password)) {
			return true;
		}
		return false;
	}
}
