import java.io.Serializable;
import java.time.LocalDateTime;

public class Flight implements Serializable{
    private String flightCode;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime; 
    private FlightTrackingURL trackingURL;

    public Flight(String flightCode, LocalDateTime departureTime, LocalDateTime arrivalTime) {
        this.flightCode = flightCode;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.trackingURL = new FlightTrackingURL(flightCode);
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

    public String toString() {
        
        return "Flightcode: " + flightCode + "\n"+"Departure Time: "+departureTime+"\n"+"Arrival Time: "+arrivalTime+"\n"+"Tracking URL: "+trackingURL;
    }
}