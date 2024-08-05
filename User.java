
import org.mindrot.jbcrypt.BCrypt;

public class User {
    public enum Role { ADMIN, VIEWER }

    private final String username;
    private final String passwordHash;
    private final Role role;

    public User(String username, String password, Role role) {
        if (username == null || password == null || role == null) {
            throw new IllegalArgumentException("Username, password, and role cannot be null.");
        }
        this.username = username;
        this.passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public boolean checkPassword(String password) {
        return BCrypt.checkpw(password, passwordHash);
    }

    public Role getRole() {
        return role;
    }

    
}
