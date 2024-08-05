import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UserFile {
    private final String FILE_NAME = "users.dat";
    private List<User> users = new ArrayList<>();

    public UserFile() {
        loadUsers(); // Load users from file when the object is created
    }

     // Add a new user (for initial setup)
     public void addUser(User user) {
        users.add(user);
        saveUsers();
    }

    public User authenticateUser(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username) && user.checkPassword(password)) {
                return user; 
            }
        }
        return null; // Authentication failed
    }

    private void loadUsers() { 
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            users.clear(); // Clear the list before loading
            while (true) {
                try {
                    Object obj = in.readObject();
                    if (obj instanceof User) {
                        users.add((User) obj);
                    }
                } catch (EOFException e) {
                    break; // End of file reached
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            // Handle the exception, e.g., print an error message or 
            // create a new "users.dat" file if it doesn't exist
            System.err.println("Error loading users: " + e.getMessage());
        }
    }

    private void saveUsers() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            for (User user : users) {
                out.writeObject(user);
            }
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

}


