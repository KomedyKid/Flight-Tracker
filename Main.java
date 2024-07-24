import javax.swing.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;
public class Main {

    private static LocalDateTime getLocalDateTimeInput(Scanner scanner) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        while (true) {
            try {
                String input = scanner.nextLine();
                return LocalDateTime.parse(input, formatter);
            }
            catch (DateTimeParseException e) {
                System.out.println("Invalid date/time format. Please use YYYYMMDDHHmm:");
            }
        }
    }

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

        Scanner 1 = new Scanner(System.in);
        System.out.println("1: Add Flight"+"\n"+"2: Edit Flight"+"\n"+"3: Delete Flight"+"\n"+"4: View Flights");

        int choice = myObj.nextInt();
        if (choice == 1) {
            Scanner scanner = new Scanner(System.in);

            System.out.println("Enter Flight Code");
            String flightCode = scanner.nextLine();

            System.out.println("Enter Departure Time (YYYYMMDDHHmm):");
            LocalDateTime departureTime = getLocalDateTimeInput(scanner);

            System.out.println("Enter Arrival Time (YYYYMMDDHHmm):");
            LocalDateTime arrivalTime = getLocalDateTimeInput(scanner);

            if (departureTime != null && arrivalTime != null && departureTime.isBefore(arrivalTime)) {
                Flight newFlight = new Flight(flightCode, departureTime, arrivalTime);
                flightFile.addFlight(newFlight);
            } else {
                System.out.println("Invalid input or arrival time is before departure time.");
            }


            


        }

       

    }
    
}