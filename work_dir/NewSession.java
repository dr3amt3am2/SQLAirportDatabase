public class NewSession {
	
	public static void main(String [] args) {
		try {
			if (args[0].equals("round")) {
				Session n = new Session(true);
			}
			else {
				System.out.println("Unknown argument...");
				System.out.println("Running in regular mode");
				Session n = new Session(false);
			}
		}
		catch (Exception e) {
			Session n = new Session(false);
		}
	}
}
