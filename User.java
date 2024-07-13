
import org.mindrot.jbcrypt.BCrypt;

public class User {

    private final String username;
    private final String passwordHash;

    public User(String username, String password) {

        if (username == null) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        this.username = username;
        this.passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public String getUsername() {
        return username;
    }

    public boolean checkPassword(String password) {
        return BCrypt.checkpw(password, passwordHash);
    }

    public String toString() {

        return "Username: "+username;

    }
    
}
