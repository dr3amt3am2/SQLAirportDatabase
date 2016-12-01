import java.sql.*;
import java.io.*;


public class SQLWorker {
	private String user;
	private String pass;
	private Connection con;
	private Statement command;

	public SQLWorker(String in_user, String in_pass) {
		user = in_user;
		pass = in_pass;
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
			con = DriverManager.getConnection(url, user, pass);
			command = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
											ResultSet.CONCUR_UPDATABLE);
		}
		catch (SQLException ex) {

			System.err.println("SQL Exception: " + ex.getMessage());
		}
	}
	public void disconnectSQL() throws SQLException {
		command.close();
		con.close();
		}

	public void updateSQL(String sql_code) {
		connectSQL();
		try {
			command.executeUpdate(sql_code);
		}
		catch (SQLException e) {
			System.err.println("SQL Exception: " + e.getMessage());
		}
	}
	public ResultSet querySQL(String sql_query) {
		try {
			return command.executeQuery(sql_query);
		}
		catch (SQLException e) {
			System.err.println("SQL Exception: " + e.getMessage());
		}
		return null;
	}

}