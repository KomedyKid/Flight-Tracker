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

    //search for flight by flightcode in the BSt
    public Flight search(String flightCode) {
        return searchRec(root, flightCode);
    }

    private Flight searchRec(FlightNode current, String flightCode) {
        if (current == null) {
            return null;
        }
        if (flightCode.equals(current.getFlight().getFlightCode())) {
            return current.getFlight();
        }
        if (flightCode.compareTo(current.getFlight().getFlightCode()) < 0) {
            return searchRec(current.getLeft(), flightCode);
        } else {
            return searchRec(current.getRight(), flightCode);
        }
    }

        
    //deleting a flight from the BST
    public void delete(String flightCode) throws Exception {
        root = deleteRec(root, flightCode);
    }

    private FlightNode deleteRec(FlightNode current, String flightCode) throws Exception {
        if (current == null) {
            throw new Exception("Flight not found.");
        }
        if (flightCode.equals(current.getFlight().getFlightCode())) {
            // Node to be deleted found
            if (current.getLeft() == null && current.getRight() == null) {
                return null; // No children
            }
            if (current.getLeft() == null) {
                return current.getRight(); // One child
            }
            if (current.getRight() == null) {
                return current.getLeft(); // One child
            }
            // Two children
            Flight smallestFlight = findSmallestFlight(current.getRight());
            current.setFlight(smallestFlight);
            current.setRight(deleteRec(current.getRight(), smallestFlight.getFlightCode()));
            return current;
        }
        if (flightCode.compareTo(current.getFlight().getFlightCode()) < 0) {
            current.setLeft(deleteRec(current.getLeft(), flightCode));
            return current;
        } else {
            current.setRight(deleteRec(current.getRight(), flightCode));
            return current;
        }
    }

    private Flight findSmallestFlight(FlightNode root) {
        return root.getLeft() == null ? root.getFlight() : findSmallestFlight(root.getLeft());
    }
    
}
