import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FlightFile {
    private static final String FILE_NAME = "flights.dat";

    public void addFlight(Flight flight) throws IOException {
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(FILE_NAME, true)))) {
            flight.write(out);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
            throw e;
        }
    }

    public boolean deleteFlight(String flightCode) throws IOException {
        List<Flight> flights = loadFlights();
        boolean found = false;

        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(FILE_NAME)))) {
            for (Flight flight : flights) {
                if (flight.getFlightCode().equals(flightCode) && !flight.isDeleted()) {
                    flight.setDeleted(true);
                    found = true;
                }
                flight.write(out);
            }
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
            throw e;
        }

        return found;
    }

    public List<Flight> loadFlights() throws IOException {
        List<Flight> flights = new ArrayList<>();

        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(FILE_NAME)))) {
            while (in.available() > 0) {
                Flight flight = Flight.read(in);
                if (!flight.isDeleted()) {
                    flights.add(flight);
                }
            }
        } catch (EOFException e) {
            // End of file reached
        } catch (IOException e) {
            System.err.println("Error reading from file: " + e.getMessage());
            throw e;
        }

        return flights;
    }

    public void updateFlight(Flight updatedFlight) throws IOException {
        List<Flight> flights = loadFlights();
        boolean found = false;

        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(FILE_NAME)))) {
            for (Flight flight : flights) {
                if (flight.getFlightCode().equals(updatedFlight.getFlightCode())) {
                    updatedFlight.write(out);
                    found = true;
                } else {
                    flight.write(out);
                }
            }
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
            throw e;
        }

        if (!found) {
            throw new IOException("Flight not found");
        }
    }
}

