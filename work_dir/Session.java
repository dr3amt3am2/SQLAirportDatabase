import java.util.Scanner;
import java.sql.*;
import java.io.*;

public class Session {
	protected String user;
	protected String pass;
	protected String sqlUser;
	protected String sqlPass;
	Scanner scnr = new Scanner(System.in);
	Console co = System.console();
	SQLWorker sql;
	Query que;

	public Session(boolean round) {
		promptSQL();
		sql = new SQLWorker(sqlUser, sqlPass);
		promptLogin();
		que = new Query(sql, user, round);
		try {
			sql.disconnectSQL();
		}
		catch (SQLException e) {
			System.out.println("Could not close connection to SQL...");
		}
	}

	protected void promptSQL() {
		// user and pass
		System.out.print("Please enter your SQL username: ");
		sqlUser = scnr.nextLine();
		char [] PassArray = co.readPassword("Please enter your SQL password: ");
		sqlPass = new String(PassArray);

	}
	protected void promptLogin() {
		System.out.print("Please enter your username: ");
		user = scnr.nextLine();
		// if user is in database, ask for password.
		char [] PassArray = co.readPassword("Please enter your password: ");
		pass = new String(PassArray);
		login();
	}
	protected void login() {
	    boolean test = sql.checkUser(user, pass);
	    if (!test) {
	        System.out.println("Wrong password... Try new login.");
	        promptLogin();
	    }
   }
}

