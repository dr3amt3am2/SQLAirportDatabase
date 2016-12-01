import java.sql.*;
import java.io.*;

public class SQLWorker {
	private String user;
	private String pass;
	private Connection con;
	private Statement command = null;
	// current date/time
	// from: http://stackoverflow.com/questions/15534775/
	// how-to-insert-current-time-in-mysql-using-java
	java.util.Date date = new java.util.Date();
	Timestamp timestamp = new Timestamp(date.getTime()); 
	java.sql.Date sDate = new java.sql.Date(timestamp.getTime());

	public SQLWorker(String in_user, String in_pass) {
		user = in_user;
		pass = in_pass;
		int status = 0;
		while(true){
			status = connectSQL();
			if(status == 1) { break; }
			else { System.exit(0); }
		}
		System.out.println("Connection successful.");
		createViews();
	}
	private int connectSQL() {
		// url and driver setup for SQL
		String url = "jdbc:oracle:thin:@gwynne.cs.ualberta.ca:1521:CRS";
		String driverName = "oracle.jdbc.driver.OracleDriver";
		try {
			Class drvClass = Class.forName(driverName);
		}
		catch (Exception e) {
			System.err.println("ClassNotFoundException: " + e.getMessage());
			return 0;
		}
		try {
			// establish connection
			con = DriverManager.getConnection(url, user, pass);
			command = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
											ResultSet.CONCUR_UPDATABLE);
		}
		catch (SQLException ex) {
			System.err.println("SQL Exception: " + ex.getMessage());
			return 0;
		}
		return 1;
	}
	public void disconnectSQL() throws SQLException {
		try{
			command.close();
			con.close();
		}
		catch (Exception e) {}
	}

	public void updateSQL(String sql_code) {
		try {
			command.executeUpdate(sql_code);
		}
		catch (SQLException e) {
			System.err.println("SQL Exception: " + e.getMessage());
		}
	}
	public ResultSet sQuery(String query) {
		// connectSQL();
		ResultSet rs = null;
		try {
			rs = command.executeQuery(query);
		}
		catch (SQLException e) {
			System.err.println("SQL Exception: " + e.getMessage());
		}

		return rs;
	}
	public boolean checkUser(String fUser, String fPass) {
		// connectSQL();
	    String query = "SELECT email, pass FROM USERS";
	    ResultSet rs = null;
		try {
			rs = command.executeQuery(query);
		}
		catch (SQLException e) {
			System.err.println("SQL Exception: " + e.getMessage());
		}
		// check credentials
        try {
			while (rs.next()) {
			    String temp_u = rs.getString("email").trim();
			    String temp_p = rs.getString("pass").trim();
				if(fUser.equals(temp_u)) {
				    // username is in the DB
				    if (fPass.equals(temp_p)) {
				 		System.out.println("Login successful.");
				        return true;
				    }
				    else {
				        // password is wrong
				        return false;
				    }
				}
		    }
		}
		catch (SQLException e) {
			System.err.println("SQL Exception: " + e.getMessage());
		}
		
		// if we got this far, then we need to add this information to 
		// the database
		try {
		    rs.moveToInsertRow();
		    rs.updateString(1, fUser);
		    rs.updateString(2, fPass);
		    rs.insertRow();
		    System.out.println("User does not exist, signed up new user.");
		}
		catch (SQLException e) {}
	    return true;
	}
	public void createViews() {
		String drop_connections = "drop view good_connections";
		String drop_available = "drop view available_flights";
		String connections = "create view good_connections (src,dst,dep_time,"+
			"arr_time, flightno1,flightno2,layover,price, dep_date, seats) as "+
  			"select a1.src, a2.dst, a1.dep_time, a2.arr_time, a1.flightno, "+
  			"a2.flightno, (a2.dep_time-a1.arr_time)/24," +
			"a1.price+a2.price, a1.dep_date, "+ // next trick from:http://forums.devshed.com/ms-sql-development-95/select-565567.html
			"(0.5*((a1.seats+a2.seats)-ABS(a1.seats-a2.seats))) from "+
			"available_flights a1, "+
			"available_flights a2 where a1.dst=a2.src and a1.arr_time "+
			"+1.5/24 <=a2.dep_time and a1.arr_time +5/24 >=a2.dep_time";

  		String available = "create view available_flights(flightno,dep_date, "+
  			"src,dst,dep_time,arr_time,fare,seats,price) as "+ 
  			"select f.flightno, sf.dep_date, f.src, f.dst, f.dep_time+"+
  			"(trunc(sf.dep_date)-trunc(f.dep_time)),f.dep_time+(trunc(sf.dep_d"+
  			"ate)-trunc(f.dep_time))+(f.est_dur/60+a2.tzone-a1.tzone)/24, "+
         	"fa.fare, fa.limit-count(tno), fa.price from flights f, flight_fa"+
         	"res fa, sch_flights sf, bookings b, airports a1, airports a2 "+
  			"where f.flightno=sf.flightno and f.flightno=fa.flightno and "+
  			"f.src=a1.acode and f.dst=a2.acode and fa.flightno=b.flightno(+)"+
  			" and fa.fare=b.fare(+) and sf.dep_date=b.dep_date(+) "+
  			"group by f.flightno, sf.dep_date, f.src, f.dst, f.dep_time, "+
  			"f.est_dur,a2.tzone,a1.tzone, fa.fare, fa.limit, fa.price "+
 			" having fa.limit-count(tno) > 0";

 		try {
 			updateSQL(drop_connections);
 			updateSQL(drop_available);
 		}
 		catch (Exception e) {}
	 	updateSQL(available);
	 	updateSQL(connections);

	}


}

