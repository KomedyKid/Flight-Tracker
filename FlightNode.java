public class FlightNode {

    private Flight flight;
    private FlightNode left;
    private FlightNode right;

    public FlightNode(Flight flight) {
        this.flight = flight;
        this.left = null;
        this.right = null;
    }


    // getters and setters
    public Flight getFlight() {
        return flight;
    }

    public FlightNode getLeft() {
        return left;
    }

    public FlightNode getRight() {
        return right;
    }

    public void setLeft(FlightNode left) {
        this.left = left;
    }

    public void setRight(FlightNode right) {
        this.right = right;
    }

}
    
    