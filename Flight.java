import java.time.LocalDateTime;

public class Flight{
    private string flightcode;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime; 

    public Flight(String flightCode, LocalDateTime departureTime, LocalDateTime arrivalTime) {
        this.flightCode = flightCode;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }
}