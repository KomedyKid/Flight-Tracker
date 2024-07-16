public class FlightTrackingURL {
    public String flightTrackingURL;

    public FlightTrackingURL(String flightcode) {
        this.flightTrackingURL = "https://www.flightradar24.com/"+flightcode;
    }
    
    public String toString() {
        
        return flightTrackingURL;
    }
}