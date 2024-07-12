public class FlightTrackingURL {
    public string flightTrackingURL;

    public FlightTrackingURL(String flightcode) {
        this.flightTrackingURL = "https://www.flightradar24.com/"+flightcode;
    }
    
}