import java.time.LocalDateTime;
public class Main {



    public static void main(String[] args) {

        LocalDateTime departure = LocalDateTime.of(2024, 7, 9, 8, 30);
        LocalDateTime arrival = LocalDateTime.of(2024, 7, 9, 11, 45); 

        Flight flight_1 = new Flight("AA123", departure, arrival);

        System.out.println(flight_1);

        String username = "ABC123";
        String password = "1234";

        User user1 = new User(username, password);

        System.out.println(user1);


    }
    
}