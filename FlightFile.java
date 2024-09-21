import java.io.*;
import java.util.Comparator;
import java.util.List;

public class FlightFile {
    private static final String FILE_NAME = "flights.dat";
    private FlightBST flightBST;

    public FlightFile() {
        this.flightBST = new FlightBST();
        loadFlights();
    }

    public void addFlight(Flight flight) throws Exception {
        flightBST.insert(flight);
        saveFlightsToFile();
    }

    public boolean deleteFlight(String flightCode) throws Exception {
        try {
            flightBST.delete(flightCode);
            saveFlightsToFile();
            return true;
        } catch (Exception e) {
            System.err.println("Error deleting flight: " + e.getMessage());
            return false;
        }
    }

    public Flight searchFlight(String flightCode) {
        return flightBST.search(flightCode);
    }

    public List<Flight> getAllFlights() {
        List<Flight> flights = flightBST.inOrderTraversal();
        flights.sort(Comparator.comparing(Flight::getDepartureTime));
        return flights;
    }

    private void saveFlightsToFile() throws IOException {
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(FILE_NAME)))) {
            List<Flight> flights = flightBST.inOrderTraversal();
            for (Flight flight : flights) {
                flight.write(out);
            }
        }
    }

    private void loadFlights() {
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(FILE_NAME)))) {
            while (true) {
                try {
                    Flight flight = Flight.read(in);
                    if (!flight.isDeleted()) {
                        flightBST.insert(flight);
                    }
                } catch (EOFException e) {
                    break; // End of file reached
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading from file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error loading flights: " + e.getMessage());
        }
    }
    
}

