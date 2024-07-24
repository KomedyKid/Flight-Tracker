import javax.swing.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            Main app = new Main();
        });

        FlightFile flightFile = new FlightFile();
        flightFile.loadFlights();

        LocalDateTime departure = LocalDateTime.of(2024, 7, 9, 8, 30);
        LocalDateTime arrival = LocalDateTime.of(2024, 7, 9, 11, 45); 

        Flight flight_1 = new Flight("AA123", departure, arrival);

        flightFile.addFlight(flight_1);

        System.out.println(flightFile.getFlights());

        Scanner myObj = new Scanner(System.in);
        System.out.println("1: Add Flight"+"\n"+"2: Edit Flight"+"\n"+"3: Delete Flight"+"\n"+"4: View Flights");

        int choice = myObj.nextInt();
        if (choice == 1) {
            
        }

    }
    
}