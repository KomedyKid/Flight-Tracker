import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Flight {
    private String flightCode;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private boolean isDeleted;
    private FlightTrackingURL trackingURL;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public Flight(String flightCode, LocalDateTime departureTime, LocalDateTime arrivalTime) {
        this.flightCode = flightCode;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.isDeleted = false;
        this.trackingURL = new FlightTrackingURL(flightCode);
    }

    public String getFlightCode() {
        return flightCode;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public FlightTrackingURL getTrackingURL() {
        return trackingURL;
    }

    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(flightCode);
        out.writeUTF(departureTime.format(formatter));
        out.writeUTF(arrivalTime.format(formatter));
        out.writeBoolean(isDeleted);
        out.writeUTF(trackingURL.toString());
    }

    public static Flight read(DataInputStream in) throws IOException {
        String flightCode = in.readUTF();
        LocalDateTime departureTime = LocalDateTime.parse(in.readUTF(), formatter);
        LocalDateTime arrivalTime = LocalDateTime.parse(in.readUTF(), formatter);
        boolean isDeleted = in.readBoolean();
        String trackingURL = in.readUTF();
        Flight flight = new Flight(flightCode, departureTime, arrivalTime);
        flight.setDeleted(isDeleted);
        flight.trackingURL = new FlightTrackingURL(flightCode); // Recreate tracking URL
        return flight;
    }
}
