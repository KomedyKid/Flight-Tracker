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

        FlightFile flightFile = new FlightFile();
        flightFile.loadFlights();

        System.out.println(flightFile.getFlights());

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("1: Add Flight"+"\n"+"2: Edit Flight"+"\n"+"3: Delete Flight"+"\n"+"4: View Flights"+"\n"+"5: Exit");

            int choice = scanner.nextInt();
            scanner.nextLine();
            switch (choice) {
                case 1:

                    System.out.println("Enter Flight Code");
                    String flightCode = scanner.nextLine();

                    System.out.println("Enter Departure Time (YYYYMMDDHHmm):");
                    LocalDateTime departureTime = getLocalDateTimeInput(scanner);

                    System.out.println("Enter Arrival Time (YYYYMMDDHHmm):");
                    LocalDateTime arrivalTime = getLocalDateTimeInput(scanner);

                    if (departureTime != null && arrivalTime != null && departureTime.isBefore(arrivalTime)) {
                        Flight newFlight = new Flight(flightCode, departureTime, arrivalTime);
                        flightFile.addFlight(newFlight);
                        System.out.println("Flight Added");
                    } else {
                        System.out.println("Invalid input or arrival time is before departure time.");
                    }

                    

                break;

                case 3:
                    System.out.println("Enter the flight code of the flight to delete:");
                    String flightCodeToDelete = scanner.nextLine();

                    if (flightFile.deleteFlight(flightCodeToDelete)) {
                        System.out.println("Flight " + flightCodeToDelete + " deleted successfully.");
                    } else {
                        System.out.println("Flight " + flightCodeToDelete + " not found.");
                    }


                case 4:
                    System.out.println(flightFile.loadFlights());
                break;

                case 5:
                    System.out.println("Exiting application.");
                    System.exit(0);
                break;

                default:
                    System.out.println("Invalid choice. Please select a valid option.");
            }
        }
    }

}
