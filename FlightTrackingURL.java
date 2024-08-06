import java.io.Serializable;
public class FlightTrackingURL implements Serializable{
    public final String flightTrackingURL;

    public FlightTrackingURL(String flightcode) {
        this.flightTrackingURL = "https://www.flightradar24.com/"+flightcode;
    }
    
    public String toString() {
        
        return flightTrackingURL;
    }
}