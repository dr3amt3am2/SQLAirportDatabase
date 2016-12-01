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

	// sql table objects
	ResultSet airports;
	ResultSet flights;
	ResultSet sch_flights;
	ResultSet fares;
	ResultSet flight_fares;
	ResultSet users;
	ResultSet passengers;
	ResultSet tickets;
	ResultSet bookings;
	ResultSet airline_agents;

	// current date/time
	// from: http://stackoverflow.com/questions/15534775/
	// how-to-insert-current-time-in-mysql-using-java
	/*java.util.Date date = new Date();
	Timestamp timestamp = new Timestamp(date.getTime()); */


	public Session() {
		promptSQL();
		sql = new SQLWorker(sqlUser, sqlPass);
		promptLogin();
	}

	protected void promptSQL() {
		// user and pass
		System.out.print("Please enter your SQL username: ");
		sqlUser = scnr.nextLine();
		System.out.println(" ");
		char [] PassArray = co.readPassword("Please enter your SQL password: ");
		sqlPass = new String(PassArray);
		try {
			sql.disconnectSQL();
		}
		catch (SQLException e) {
			System.out.println("Could not close connection to SQL...");
		}

	}
	protected void promptLogin() {
		System.out.println("Please enter your username: ");
		user = scnr.nextLine();
		// if user is in database, ask for password.
		char [] PassArray = co.readPassword("Please enter your password: ");
		pass = new String(PassArray);
		login();
	}
	protected void login() {
		String query = "SELECT email, pass, last_login FROM USERS";
		users = sql.querySQL(query);
		// check if user is in database
		if (users == null) {
			System.out.println("Adding user to database...");
			// TODO: actually do that
		}
		try {
			while (users.next()) {
				if (user.equals(users.getString("email"))) {
					// user is in database
					if (pass.equals(users.getString("pass"))) {
						// password is correct
						// to do: change last login time
						break;
					}
					else {
						System.out.println("User is system, but pass is wrong.");
						System.out.println("Please try again...");
						promptLogin();
					}
				}
			}

		}
		catch (Exception e) {
			System.out.println("FATAL ERROR. YOU SHOULD NEVER SEE THIS MESSAGE.");
			System.out.println("No, really it should be impossible to see this.");
			System.out.println("If you're seeing this that sucks. Exiting.");
			System.exit(-1);
		}
	}
}

