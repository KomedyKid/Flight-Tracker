import java.time.LocalDateTime;
import java.util.List;
public class Main {



    public static void main(String[] args) {

        FlightFile flightFile = new FlightFile();
        flightFile.loadFlights();

        LocalDateTime departure = LocalDateTime.of(2024, 7, 9, 8, 30);
        LocalDateTime arrival = LocalDateTime.of(2024, 7, 9, 11, 45); 

        Flight flight_1 = new Flight("AA123", departure, arrival);

        flightFile.addFlight(flight_1);

        List<Flight> allFlights = flightFile.getFlights();
        for (Flight flight : allFlights) {
            System.out.println(flight);
        }


    }
    
}