import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FlightFile {
    private static final String FILE_NAME = "flights.dat";

    public void addFlight(Flight flight) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile("flights.dat", "rw")) {
            file.seek(file.length());  // Go to the end of the file
            file.writeUTF(flight.getFlightCode());
            file.writeUTF(flight.getDepartureTime().toString());
            file.writeUTF(flight.getArrivalTime().toString());
            file.writeBoolean(false);  // Flight is not deleted
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
            throw e;
        }
    }

    public boolean deleteFlight(String flightCode) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(FILE_NAME, "rw")) {
            while (file.getFilePointer() < file.length()) {
                long position = file.getFilePointer();
                Flight flight = readFlight(file);
                if (flight.getFlightCode().equals(flightCode) && !flight.isDeleted()) {
                    file.seek(position);
                    file.writeBoolean(true);  // Mark as deleted
                    return true;
                }
            }
        }
        return false;
    }

    public Flight searchFlight(String flightCode) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(FILE_NAME, "r")) {
            while (file.getFilePointer() < file.length()) {
                Flight flight = readFlight(file);
                if (flight.getFlightCode().equals(flightCode) && !flight.isDeleted()) {
                    return flight;
                }
            }
        }
        return null;
    }

    public List<Flight> loadFlights() throws IOException {
        List<Flight> flights = new ArrayList<>();
        try (RandomAccessFile file = new RandomAccessFile("flights.dat", "r")) {
            while (file.getFilePointer() < file.length()) {
                String flightCode = file.readUTF();
                LocalDateTime departure = LocalDateTime.parse(file.readUTF());
                LocalDateTime arrival = LocalDateTime.parse(file.readUTF());
                boolean isDeleted = file.readBoolean();
                if (!isDeleted) {
                    flights.add(new Flight(flightCode, departure, arrival));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading from file: " + e.getMessage());
            throw e;
        }
        return flights;
    }

    private void writeFlight(RandomAccessFile file, Flight flight) throws IOException {
        file.writeUTF(flight.getFlightCode());
        file.writeUTF(flight.getDepartureTime().toString());
        file.writeUTF(flight.getArrivalTime().toString());
        file.writeBoolean(flight.isDeleted());
    }

    private Flight readFlight(RandomAccessFile file) throws IOException {
        String flightCode = file.readUTF();
        LocalDateTime departure = LocalDateTime.parse(file.readUTF().trim());
        LocalDateTime arrival = LocalDateTime.parse(file.readUTF().trim());
        boolean isDeleted = file.readBoolean();
        Flight flight = new Flight(flightCode, departure, arrival);
        flight.setDeleted(isDeleted);
        return flight;
    }
}
