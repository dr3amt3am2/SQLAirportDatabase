public class Login extends Session {
	public Login() {
		super();
	}
	private void login() {
		String query = "SELECT email, pass, last_login FROM USERS";
		super.users = super.querySQL(query);
		// check if user is in database
		while (users.next()) {
			if (user.equals(users.getString("email"))) {
				// user is in database
				if (pass.equals(users.getString("pass"))) {
					// password is correct
					// todo: update last login time
					break;
				}
			else {
				System.out.println("User in system, but incorrect pass");
				System.out.println("Please try again...");
				promptLogin();
				}
			}
		}
	}
	private void promptLogin() {
		System.out.println("Please enter your username: ");
		user = scnr.nextLine();
		// if user is in database, ask for password.
		char [] PassArray = co.readPassword("Please enter your password: ");
		pass = new String(PassArray);
		login();
	}
	public getUser() {
		return user;
	}
	public get

}