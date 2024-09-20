public class FlightBST {
    private FlightNode root;

    public FlightBST() {
        root = null;
    }

    //insert flight into BST
    public void insert(Flight flight) throws Exception {
        if (root == null) {
            root = new FlightNode(flight);
        } else {
            insertRec(root, flight);
        }
    }
    // recursive helper method to insert flight into BST
    private void insertRec(FlightNode current, Flight flight) throws Exception {
        if (flight.getFlightCode().compareTo(current.getFlight().getFlightCode()) < 0) {
            if (current.getLeft() == null) {
                current.setLeft(new FlightNode(flight));
            } else {
                insertRec(current.getLeft(), flight);
            }
        } else if (flight.getFlightCode().compareTo(current.getFlight().getFlightCode()) > 0) {
            if (current.getRight() == null) {
                current.setRight(new FlightNode(flight));
            } else {
                insertRec(current.getRight(), flight);
            }
        } else {
            throw new Exception("Duplicate flight code not allowed.");
        }
    }
        
    
    
}
