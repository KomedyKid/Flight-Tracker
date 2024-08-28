import java.io.Serializable;
import java.time.LocalDateTime;

public class Flight implements Serializable{
    private static final int FLIGHT_CODE_SIZE = 10;  // Maximum characters
    private static final int DATE_TIME_SIZE = 19;    // "yyyy-MM-ddTHH:mm:ss"
    private static final int DELETED_FLAG_SIZE = 1;  // '0' for active, '1' for deleted
    private static final int RECORD_SIZE = FLIGHT_CODE_SIZE + 2 * DATE_TIME_SIZE + DELETED_FLAG_SIZE;

    private String flightCode;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime; 
    private FlightTrackingURL trackingURL;
    private boolean isDeleted;

    public Flight(String flightCode, LocalDateTime departureTime, LocalDateTime arrivalTime) {
        this.flightCode = flightCode;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.trackingURL = new FlightTrackingURL(flightCode);
        this.isDeleted = false;
    }

    public String getFlightCode() {
        return flightCode;
    }

    public void setFlightCode(String flightCode) {
        this.flightCode = flightCode;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public FlightTrackingURL getTrackingURL() {
        return new FlightTrackingURL(flightCode);
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public String toString() {
        
        return "Flightcode: " + flightCode + "\n"+"Departure Time: "+departureTime+"\n"+"Arrival Time: "+arrivalTime+"\n"+"Tracking URL: "+trackingURL;
    }
}