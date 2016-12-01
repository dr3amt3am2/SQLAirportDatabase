import java.util.*;
import java.sql.*;
import java.io.*;
import java.text.*;

public class Query {

	private String username;
	Scanner scnr = new Scanner(System.in);
	SQLWorker sql;
	private Flight cFlight;
	private Flight rCFlight; // return flight if applicable
	private boolean isBooking = false;
	private String available_flights;
	private boolean round = false;
	private int flight_count = 0;
	private String rSrc = "";
	private String rDest = "";

	public Query(SQLWorker sql, String user, boolean round) {
		this.sql = sql;
		username = user.toLowerCase();
		this.round = round; // round trip mode!
		prompt();
	}

	private boolean checkAvailable(Flight flight) {
		if (flight == null) return false;
		if (flight_count == 2) {
			updateAvailable(rDest, rSrc, flight.getDateString());
		}
		boolean available = false;
		ResultSet rs = sql.sQuery(available_flights);
		try {
			while (rs.next()) {
				if (flight.getFlightNo().equals(rs.getString(1).trim())) {
					available = true;
					break;
				}
			}
		}
		catch (Exception e) {}
		return available;
	}

	private void updateAvailable(String dest, String src, String dep) {
		// query
		available_flights = "SELECT flightno1, flightno2, src, dst, dep_time,"+
			" arr_time, layover, price, stops, seats, dep_date FROM((SELECT flightno flightno1,"+
			" null flightno2, src, dst, dep_time, arr_time, null layover, "+
			"price, null stops, seats, dep_date FROM available_flights WHERE src = '"+src+"' AND "+
			"dst = '"+dest+"' AND dep_date = TO_DATE('"+dep+"', 'dd/mm/yyyy')"+
				") UNION"+
				"(SELECT flightno1, flightno2, src, dst, dep_time,"+
						" arr_time, layover, price, 1 stops, seats, dep_date "+  
					"FROM good_connections "+
					"WHERE src = '"+src+"' AND dst = '"+dest+"' "+ 
					"AND dep_date = TO_DATE('"+dep+"', 'dd/mm/yyyy')) " +
				"ORDER BY PRICE ASC"+
				")";
	}

	private int chooseTicketNo(ResultSet rs) {
		int tno = 1;
		ArrayList <Integer> l = new ArrayList<Integer>();
		try {
			// populate list
			while (rs.next()) {
				l.add(rs.getInt(1));
			}			
		}
		catch (Exception e) {}
		
		while (l.contains(tno)) {
			tno++;
		}
		return tno;

	}

	private int getSeatNo(ResultSet rs) {
		int seatno = 1;
		try {
			while (rs.next()) {
				int temp = rs.getInt(5);
				if (seatno == temp) {
					seatno++;
				}
				else if (seatno != temp) break;
			}
		}
		catch (Exception e) {}
		return seatno;
	}

	public void prompt() {
		int action;
		String prompt = "Action you would like to do? \n" +
						"1. Search for flight \n" +
						"2. Make a booking \n" +
						"3. View/Cancel your bookings \n" +
						"4. Record flight departure (agent) \n" +
						"5. Record flight arrival (agent) \n" +
						"6. Logout ";
		System.out.println(prompt);
		action = scnr.nextInt();
		scnr.nextLine();
		if (action == 1) Search();
		else if (action == 2) Book();
		else if (action == 3) displayBooking();
		else if (action < 6) recordFlight(action); 
		else if (action == 6) Logout();
	}

	public void recordFlight (int choice) {
		String opType = "";
		SimpleDateFormat slash = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat dash = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat fTime = new SimpleDateFormat("HH:mm");
		java.sql.Date nTime = null;
		// check if user is agent
		String query = "SELECT * FROM airline_agents WHERE email='"+username+"'";
		ResultSet rs = sql.sQuery(query);
		try {
			if(!rs.next()) {
				System.out.println("You must be an agent!");
				prompt();
			}
		}
		catch (SQLException e) {
			System.err.println("SQLException: " + e.getMessage());
		}
		// if 4 then modify dep time
		if (choice == 4) {
			// departure
			opType = "act_dep_time";
		}
		// else 5 then modify dep time
		else opType = "act_arr_time";
		System.out.println("Please enter the flightno.");
		String flightno = scnr.nextLine();
		System.out.println("Please enter the dep_date [dd/MM/yyyy]");
		String dep_date = scnr.nextLine();
		System.out.println("Please enter the time [hh:mm] (24 hour clock)");
		String time = scnr.nextLine();
		// parse date into yyyy-MM-dd to match rs.getString(2).substring(0,10)
		try {
			dep_date = dash.format(slash.parse(dep_date));
		} catch (ParseException e) { System.err.println(e); }
		// parse time to HH:mm
		try {
        	java.util.Date parsed = fTime.parse(time);
   	        nTime = new java.sql.Date(parsed.getTime());
        }
        catch (Exception e) { System.err.print(e); }
        // query db
        query = "SELECT "+opType+", dep_date FROM sch_flights WHERE flightno='"+
        				flightno+"'";
        rs = sql.sQuery(query);
        boolean exists = false;
        try {
        	// check if flight exists
        	while(rs.next()) {
        		String date = rs.getString(2).substring(0,10);
        		if (date.equals(dep_date)) {
        			exists = true;
        			break;
        		}
        	}
        	// if not then wrong flight
        	if (!exists) {
        		System.out.println("Invalid flight choice!");
        		prompt();
        	}
        	else {
        		// update db by recording time
        		rs.updateDate(1, nTime);
        		rs.updateRow();
        		System.out.println("Time was recorded.");
        		prompt();
        	}
        }
        catch (SQLException e) {
        	System.out.println("SQLException: " + e.getMessage());
        }

	}
	public String SearchAcode(String search) {
		ResultSet q = null;
		ArrayList<String> slist = new ArrayList<String>();
		String qstr;
		String acode = "";
		int action;
		qstr = "SELECT ACODE FROM AIRPORTS WHERE NAME = '"+search+"' OR CITY = '"+search+"'";
			q = sql.sQuery(qstr);
			int ccount = 1;
			try {
				System.out.println("Select from the following airports: ");
				while (q.next()) {
					slist.add(q.getString(1));
					System.out.println(ccount+": "+slist.get(ccount-1));
					ccount += 1;
				}
				if (ccount == 1) {
					System.out.println("Could not find any airports. Try again.");
					return acode;
				} else {
					action = scnr.nextInt();
					scnr.nextLine();
					acode = slist.get(action-1);
				}
			} 
			catch (Exception E) { System.out.println("query fail"); }
			return acode;
	}

	public void Search() {
		String src = "";
		String dest = "";
		String dep_date;
		String qstr;
		int action;2
		String price1 = null;
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		java.util.Date d = null;
		ResultSet q = null;

		// source prompt
		if (flight_count < 1) {
			System.out.println("Please enter source:");
			while (src.length() != 3) { 
				src = scnr.nextLine();
				if (src.length() == 3) {
					break;
				}
				src = SearchAcode(src);
			}
			rDest = src;

			// dest prompt
			System.out.println("Please enter destination:");
			while (dest.length() != 3) {
				dest = scnr.nextLine();
				if (dest.length() == 3) {
					break;
				}
				dest = SearchAcode(dest);
			}
			rSrc = dest;
		}
		else {
			src = rSrc;
			dest = rDest;
		}

		// dest date prompt
		System.out.println("Please enter departure date [dd/MM/YYYY]:");
		dep_date = scnr.nextLine();
		try {
			d = sdf.parse(dep_date);
		} catch (ParseException pe) {}
		Timestamp timestamp = new Timestamp(d.getTime());
		java.sql.Date sDate = new java.sql.Date(timestamp.getTime());
		java.sql.Date dateObj = null;
		updateAvailable(dest, src, dep_date);

		q = sql.sQuery(available_flights);
		System.out.println("FNO1  FNO2 SRC DST DEP_TIME ARR_TIME   LAYOVER PRICE"+
		" STOPS SEATS");
		String flightno2 = null;
		String layover = null;
		String stops = null;
		try {
			if (!q.next()) {
				System.out.println("No such flights exist :(");
				src = "";
				dest = "";
				isBooking = false;
				cFlight = null;
				prompt();
			}
			int ccount = 1;
			while (q.next()) {
				String flightno1 = q.getString(1).trim();
				try{
					flightno2 = q.getString(2).trim();
				}
				catch (NullPointerException e) {}
				String src1 = q.getString(3).trim();
				String dst = q.getString(4).trim();
				String dep_time = q.getString(5).trim().substring(11, 19);
				String arr_time = q.getString(6).trim().substring(11, 19);
				dateObj = q.getDate(11);
				try {
					layover = q.getString(7).trim();
				}
				catch (NullPointerException e) {}
				price1 = q.getString(8).trim();
				try {
					stops = q.getString(9).trim();
				}
				catch (NullPointerException e) {}
				String seats = q.getString(10).trim();
				if (seats.length() < 2) seats = seats + " ";
				while (price1.length() < 4) price1 = price1 + " ";
				System.out.println(flightno1+" "+flightno2+" "+src1+" "+dst+" "+
					dep_time+"  "+arr_time+"   "+layover+"    "+price1+" "+
					stops + "   " + seats);
				ccount += 1;
			}
		} catch (SQLException E) {
			System.err.println(E.getMessage());
		}
		System.out.println("Please enter flightno of chosen flight " +
							"or enter 'sort' to sort by # of stops.");
		String flightno = scnr.nextLine();
		if (flightno.equals("sort")) {
			available_flights = "SELECT flightno1, flightno2, src, dst, dep_time,"+
			" arr_time, layover, price, stops, seats FROM((SELECT flightno flightno1,"+
			" null flightno2, src, dst, dep_time, arr_time, null layover, "+
			"price, null stops, seats FROM available_flights WHERE src = '"+src+"' AND "+
			"dst = '"+dest+"' AND dep_date = TO_DATE('"+dep_date+"', 'dd/mm/yyyy')"+
				") UNION"+
				"(SELECT flightno1, flightno2, src, dst, dep_time,"+
						" arr_time, layover, price, 1 stops, seats "+  
					"FROM good_connections "+
					"WHERE src = '"+src+"' AND dst = '"+dest+"' "+ 
					"AND dep_date = TO_DATE('"+dep_date+"', 'dd/mm/yyyy')) " +
				"ORDER BY SEATS ASC"+
				")";
			q = sql.sQuery(available_flights);
			System.out.println("FNO1  FNO2 SRC DST DEP_TIME ARR_TIME   LAYOVER PRICE"+
			" STOPS SEATS");
			flightno2 = null;
			layover = null;
			stops = null;
			try {
				int ccount = 1;
				q.first();
				while (q.next()) {
					String flightno1 = q.getString(1).trim();
					try{
						flightno2 = q.getString(2).trim();
					}
					catch (NullPointerException e) {}
					String src1 = q.getString(3).trim();
					String dst = q.getString(4).trim();
					String dep_time = q.getString(5).trim().substring(11, 19);
					String arr_time = q.getString(6).trim().substring(11, 19);
					dateObj = q.getDate(11);
					try {
						layover = q.getString(7).trim();
					}
					catch (NullPointerException e) {}
					price1 = q.getString(8).trim();
					try {
						stops = q.getString(9).trim();
					}
					catch (NullPointerException e) {}
					String seats = q.getString(10).trim();
					if (seats.length() < 2) seats = seats + " ";
					while (price1.length() < 4) price1 = price1 + " ";
					System.out.println(flightno1+" "+flightno2+" "+src1+" "+dst+" "+
						dep_time+"  "+arr_time+"   "+layover+"    "+price1+" "+
						stops + "   " + seats);
					ccount += 1;
				}	
			}
			catch (SQLException e) {
				System.err.println("SQLException: " + e.getMessage());
			}
			System.out.println("Please enter flightno: ");
			flightno = scnr.nextLine();
		}
		try {
			q.first();
		}
		catch (Exception e) {}
		java.sql.Date dep_time = null;
		String fare = null;
		System.out.println("Choose a fare type: ");
		String query = "SELECT fare, price FROM available_flights WHERE " +
						"flightno = '"+flightno+"'";
		ResultSet nrs = sql.sQuery(query);
		try {
			while (nrs.next()) {
				System.out.print(nrs.getString(1));
				System.out.println(" - PRICE: " + nrs.getInt(2));
			}
		}
		catch (Exception e) {}
		fare = scnr.nextLine();
		query = "SELECT fare, price, dep_date FROM available_flights WHERE" +
				" flightno = '" + flightno+"' AND fare = '"+fare+"'";
		q = sql.sQuery(query);
		try {
			while (q.next()) {
				price1= q.getString(2);
				dep_time = q.getDate(3);
				System.out.println(fare + "- PRICE: " + price1 + " chosen.");
			}
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
		}
		System.out.println("Price: "+ price1);;

		cFlight = new Flight(flightno, dateObj, null, 
						Integer.parseInt(price1.trim()), fare);
		if (round && (flight_count == 0)) {
			System.out.println("Please choose return flight.");
			rCFlight = cFlight;
			cFlight = null;
			flight_count++;
			Search();
		}
		if (isBooking) Book();
		else prompt();
	}

	public void Book() {
		boolean inDataBase = false;
		ResultSet rs = null;
		String name = null;
		if (cFlight == null || 
			((rCFlight == null) && (round) && flight_count < 2)) {
			System.out.println("Please search for a flight.");
			isBooking = true;
			Search();
		}
		else {
			try {
				System.out.println("Please enter your name: ");
				name = scnr.nextLine();
				String query = "SELECT email, name, country FROM passengers";
				rs = sql.sQuery(query);
				System.out.println("about to query");
				while (rs.next()) {
					String user = rs.getString(1).trim().toLowerCase();
					String tName = rs.getString(2).trim().toLowerCase();
					if ((username.toLowerCase().equals(user)) && 
						!(name.toLowerCase().equals(tName))) {
						System.out.println("ERROR: Your account is registered " +
											"with another user.");
						System.exit(-1);
					}
					else if (username.toLowerCase().equals(user) && 
							name.toLowerCase().equals(tName)) {
						inDataBase = true;
						break;
					}
				}
			}
			catch (SQLException e) {
				System.err.println("SQL Exception: " + e.getMessage());
			}
			if (!inDataBase) {
				System.out.println("User not in database, created.");
				System.out.println("Please enter your country: ");
				String country = scnr.nextLine();
				try {
					rs.moveToInsertRow();
			    	rs.updateString(1, username);
			    	rs.updateString(2, name);
			    	rs.updateString(3, country);
			    	rs.insertRow();
			    }
			    catch (SQLException e) {
			    	System.out.println("SQLException: " + e.getMessage());
			    }
			}
			if (cFlight == null) System.out.println("flight is null");
			if(checkAvailable(cFlight)) {
				System.out.println("flight still available");
				String query = "SELECT tno, name, email, paid_price FROM" +
							" tickets";
				rs = sql.sQuery(query);
				int tno = chooseTicketNo(rs);
				try {
					rs.moveToInsertRow();
					rs.updateInt(1, tno);
					rs.updateString(2, name);
					rs.updateString(3, username);
					rs.updateInt(4, cFlight.getPrice());
					rs.insertRow();
				}
				catch (SQLException e) {
					System.err.println("SQL Exception: " + e.getMessage());
				}
				query = "SELECT tno, flightno, fare, dep_date, seat FROM " + 
						"bookings";
				rs = sql.sQuery(query);
				int seatno = getSeatNo(rs);
				try {
					rs.moveToInsertRow();
					rs.updateInt(1, tno);
					rs.updateString(2, cFlight.getFlightNo());
					rs.updateString(3, cFlight.getFare());
					rs.updateDate(4, cFlight.getDate());
					rs.updateString(5, Integer.toString(seatno));
					rs.insertRow();
				}
				catch (SQLException e) {
					System.err.println("SQL Exception: " + e.getMessage());
				}
			}
			else {
				System.out.println("Sorry, that flight is no longer available.");
				flight_count = 0;
				Search();
			}

		}
		if (round && flight_count < 2) {
			System.out.println("First flight booked.");
			cFlight = rCFlight;
			rCFlight = null;
			flight_count++;
			Book();
		}
		else if (round && flight_count == 2) {
			System.out.println("Round trip booked.");
			flight_count = 0;
			cFlight = null;
			rCFlight = null;
		}
		else {
			cFlight = null;
		}
		prompt();
	}

	public void displayBooking() {
		ArrayList<Integer> tList = new ArrayList<Integer>();
		// tno, passenger name, departure date, price
		String query = "SELECT bookings.tno, bookings.dep_date, tickets.name" + 
						", tickets.paid_price FROM tickets, bookings WHERE " +
						"tickets.tno = bookings.tno AND tickets.email = " + 
						"'" + username + "'";
		try {
			ResultSet rs = sql.sQuery(query);
			int count = 1;
			System.out.println("Ticket No.  Dep_Date  Name   Price");
			while (rs.next()) {
				int tno = rs.getInt(1);
				tList.add(tno);
				String date = rs.getString(2).substring(0,10);
				String nName = rs.getString(3).trim();
				int price = rs.getInt(4);
				System.out.println(count + ". " + tno + "        " + date + 
									"  " + nName + "    " + price);
				count++;
			}
		}
		catch (SQLException e) {
			System.err.println("SQLException: " + e.getMessage());
		}

		System.out.println("Enter the row num for cancellation/info OR '0' for " +
							"more options.");
		int new_in = scnr.nextInt();
		scnr.nextLine();
		boolean real = false;
		if (new_in == 0) prompt();
		else {
			ResultSet rs = sql.sQuery(query);
			try {
				while (rs.next()) {
					if (tList.get(new_in-1) == rs.getInt(1)) {
						real = true;
						break;					}
				}
			}
			catch (Exception e) {}
			if (!real) {
				System.out.println("Invalid choice.");
				displayBooking();
			}
			else {
				System.out.println("Valid choice.");
				String nQuery = "SELECT flights.dst, flights.est_dur, "+
								"flights.dep_time, "+
								"tickets.paid_price, bookings.fare, bookings.seat  FROM flights, bookings, "+
								"sch_flights, tickets WHERE bookings.tno = '"+
								tList.get(new_in-1) + "' AND bookings.tno = tickets.tno AND "+
								"bookings.flightno = flights.flightno AND "+
								"flights.flightno = sch_flights.flightno";
				ResultSet info = sql.sQuery(nQuery);
				try {
					info.next();
					System.out.println("Destination: " + info.getString(1));
					System.out.println("Est duration (mins): " + info.getString(2));
					System.out.println("Dep Time: " + info.getString(3).substring(11, 19));
					System.out.println("Price paid: " + info.getString(4));
					System.out.println("Fare type: " + info.getString(5));
					System.out.println("Seat no: " + info.getInt(6));
				}
				catch (Exception e) {
					System.err.println(e.getMessage());
				}
				System.out.println("Would you like to cancel this booking? [y/n]");
				String input = scnr.nextLine();
				while (!input.equals("y") && !input.equals("n")) {
					System.out.println("Try again (insert y for yes, n for no)");
					input = scnr.nextLine();
				}
				if (input.equals("n")) prompt();
				else cancelBooking(tList.get(new_in-1)); // cancel this tno
			}

		}
		prompt();
	}
	public void cancelBooking(int tno) {
		// find in bookings first
		String query = "Select tno, flightno, fare, dep_date, seat FROM bookings WHERE tno = '"+tno+"'";
		ResultSet rs = sql.sQuery(query);
		try {
			rs.next();
			rs.deleteRow();
		}
		catch (SQLException e) {
			System.err.println("SQLException: " + e.getMessage());
		}
		// now remove from tickets
		query = "Select tno, name, email, paid_price FROM tickets WHERE tno = '"+tno+"'";
		rs = sql.sQuery(query);
		try {
			rs.next();
			rs.deleteRow();
		}
		catch (SQLException e) {
			System.err.println("SQLException: " + e.getMessage());
		}
		System.out.println("Deleted booking.");
		prompt();
	}

	public void Logout() {
		java.util.Date date = new java.util.Date();
		Timestamp timestamp = new Timestamp(date.getTime()); 
		java.sql.Date sDate = new java.sql.Date(timestamp.getTime());
		String query = "SELECT email, last_login FROM USERS where email = '"+username+"'";
	    ResultSet rs = null;
		rs = sql.sQuery(query);
		try {
			rs.next();
			rs.updateDate(2, sDate);
			rs.updateRow();
		}
		catch (SQLException e) {
			System.err.println("SQL Exception: " + e.getMessage());
		}
		try {
			sql.disconnectSQL();
		} catch (Exception e) {}
		System.exit(0);
	}

}