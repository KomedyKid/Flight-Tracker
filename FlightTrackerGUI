import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class FlightTrackerGUI extends JFrame {


    private FlightFile flightFile;
    private DefaultListModel<Flight> flightListModel;
    private JList<Flight> flightList;
    private JTextField flightCodeField;
    private JTextField departureTimeField;
    private JTextField arrivalTimeField;

    public FlightTrackerGUI() {
        flightFile = new FlightFile();
        flightFile.loadFlights();

        // GUI Initialization
        setTitle("Flight Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLayout(new BorderLayout());

        // 1. Flight List Panel (Center)
        JPanel listPanel = new JPanel(new BorderLayout());
        flightListModel = new DefaultListModel<>();
        loadFlightsToList(); // Load flights from file into the list model
        flightList = new JList<>(flightListModel);
        listPanel.add(new JScrollPane(flightList), BorderLayout.CENTER); 
        add(listPanel, BorderLayout.CENTER);

        // 2. Input/Control Panel (South)
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5)); // 4 rows, 2 cols

        inputPanel.add(new JLabel("Flight Code:"));
        flightCodeField = new JTextField();
        inputPanel.add(flightCodeField);

        inputPanel.add(new JLabel("Departure (YYYYMMDDHHmm):"));
        departureTimeField = new JTextField();
        inputPanel.add(departureTimeField);

        inputPanel.add(new JLabel("Arrival (YYYYMMDDHHmm):"));
        arrivalTimeField = new JTextField();
        inputPanel.add(arrivalTimeField);

        JButton addButton = new JButton("Add Flight");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addFlight();
            }
        });
        inputPanel.add(addButton);

        JButton deleteButton = new JButton("Delete Flight");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteFlight();
            }
        });
        inputPanel.add(deleteButton);

        add(inputPanel, BorderLayout.SOUTH);

        // Make the GUI visible
        setVisible(true); 
    }

    // Method to add a new flight
    private void addFlight() {
    String flightCode = flightCodeField.getText();
    LocalDateTime departureTime = parseDateTime(departureTimeField.getText());
    LocalDateTime arrivalTime = parseDateTime(arrivalTimeField.getText());

    if (departureTime != null && arrivalTime != null && departureTime.isBefore(arrivalTime)) {
        Flight newFlight = new Flight(flightCode, departureTime, arrivalTime);
        flightFile.addFlight(newFlight);
        flightListModel.addElement(newFlight); // Update the list model

        // Clear input fields
        flightCodeField.setText("");
        departureTimeField.setText("");
        arrivalTimeField.setText("");
    } else {
        JOptionPane.showMessageDialog(this, "Invalid input or arrival is before departure.");
    }
    }

    // Method to delete a flight
    private void deleteFlight() {
        int selectedIndex = flightList.getSelectedIndex();
        if (selectedIndex != -1) { 
            Flight flightToDelete = flightListModel.get(selectedIndex);
            flightFile.deleteFlight(flightToDelete.getFlightCode()); 
            flightListModel.remove(selectedIndex); 
        } else {
            JOptionPane.showMessageDialog(this, "Please select a flight to delete.");
        }
    }

    // Helper method to parse date and time from text fields
    private LocalDateTime parseDateTime(String dateTimeString) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
            return LocalDateTime.parse(dateTimeString, formatter);
        } catch (DateTimeParseException e) {
            return null; 
        }
    }

    // Load flights from file to list model
    private void loadFlightsToList() {
        List<Flight> flights = flightFile.loadFlights();
        flightListModel.clear();
        for (Flight flight : flights) {
            flightListModel.addElement(flight);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FlightTrackerGUI());
    }
}

