package userstuff;
import java.util.Scanner;
import java.sql.*;
import java.io.*;



public class Session {
	private String user;
	private String pass;
	private String SQLUser;
	private String SQLPass;
	private Connection con;
	private Statement command;
	Scanner scnr = new Scanner(System.in);
	Console co = System.console();

	public Session() {
		promptSQL();
		promptLogin();
		try {
			disconnectSQL();
		}
		catch (SQLException e) {
			System.out.println("Could not close connection to SQL...");
		}
	}
	public void main() {
		promptSQL();
		promptLogin();
		try {
			disconnectSQL();
		}
		catch (Exception e) {
			// do nothing
		}
	}
	private void promptSQL() {
		// user and pass
		System.out.print("Please enter your SQL username: ");
		SQLUser = scnr.nextLine();
		System.out.println(" ");
		char [] PassArray = co.readPassword("Please enter your SQL password: ");
		SQLPass = new String(PassArray);
		connectSQL();

	}
	private void promptLogin() {
		System.out.println("Please enter your username: ");
		user = scnr.nextLine();
		System.out.println(user); // just to see
		// if user is in database, ask for password.
		System.out.println(" ");
		char [] PassArray = co.readPassword("Please enter your SQL password: ");
		pass = new String(PassArray);
		System.out.println(pass); // just to see
	}
	private void connectSQL() {
		// url and driver setup for SQL
		String url = "jdbc:oracle:thin:@gwynne.cs.ualberta.ca:1521:CRS";
		String driverName = "oracle.jdbc.driver.OracleDriver";
		try {
			Class drvClass = Class.forName(driverName);
		}
		catch (Exception e) {
			System.err.println("ClassNotFoundException: " + e.getMessage());
		}
		try {
			// establish connection
			con = DriverManager.getConnection(url, SQLUser, SQLPass);
			command = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
											ResultSet.CONCUR_UPDATABLE);
		}
		catch (SQLException ex) {
			System.err.println("SQL Exception: " + ex.getMessage());
		}
	}
	private void updateSQL(String sql_code) {
		try {
			command.executeUpdate(sql_code);
		}
		catch (SQLException e) {
			System.err.println("SQL Exception: " + e.getMessage());
		}
	}
	private void querySQL(String sql_query) {
		try {
			command.executeUpdate(sql_query);
		}
		catch (SQLException e) {
			System.err.println("SQL Exception: " + e.getMessage());
		}
	}
	private void disconnectSQL() throws SQLException {
		command.close();
		con.close();
	}
}

