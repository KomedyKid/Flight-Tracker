
import java.io.Serializable;

public class User implements Serializable{
    public enum Role { ADMIN, VIEWER }

    private final String username;
    private final String password;
    private final Role role;

    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password); // Simple plain text comparison
    }

    public Role getRole() {
        return role;
    }

    
}
