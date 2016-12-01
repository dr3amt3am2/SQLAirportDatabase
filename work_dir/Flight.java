public class Flight {
	private String flightno;
	private java.sql.Date date;
	private String seat;
	private int price;
	private boolean chosen;
	private String fare;
	
	public Flight(String flightno, java.sql.Date date, String seat, int price,
									String fare) {
		this.flightno = flightno;
		this.date = date;
		this.seat = seat;
		this.fare = fare;
		this.price = price;
		chosen = true;
	}
	public String getFlightNo() {
		return flightno;
	}
	public java.sql.Date getDate() {
		return date;
	}
	public String getSeat() {
		return seat;
	}
	public int getPrice() {
		return price;
	}
	public String getFare() {
		return fare;
	}
	public boolean isChosen() {
		return chosen;
	}
	public String getDateString() {
		String string = date.toString();
		return (string.substring(8, 10) + "/" + string.substring(5,7) +
			"/" + string.substring(0, 4));
	}
}