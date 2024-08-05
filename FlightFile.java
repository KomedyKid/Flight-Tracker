import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FlightFile {
    private final String FILE_NAME = "flights.dat";
    private List<Flight> flights = new ArrayList<>();

    public void addFlight(Flight flight) {
        flights.add(flight);
        saveFlights(); // Save the updated list to the file
    }

    public List<Flight> loadFlights() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            flights.clear(); // Clear the list before loading
            while (true) {
                try {
                    Object obj = in.readObject();
                    if (obj instanceof Flight) {
                        flights.add((Flight) obj);
                    }
                } catch (EOFException e) {
                    break; // End of file reached
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading flights: " + e.getMessage());
        }
        return flights;
    }

    public List<Flight> getFlights() {
        return flights;
    }

    private void saveFlights() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            for (Flight flight : flights) {
                out.writeObject(flight);
            }
        } catch (IOException e) {
            System.err.println("Error saving flights: " + e.getMessage());
        }
    }

    public boolean deleteFlight(String flightCode) {
        boolean removed = flights.removeIf(flight -> flight.getFlightCode().equals(flightCode));
        if (removed) {
            saveFlights();
        }
        return removed;
    }
}

