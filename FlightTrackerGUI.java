import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;


public class FlightTrackerGUI extends JFrame {

    private UserFile userFile;
    private User currentUser;
    private FlightFile flightFile;
    private DefaultTableModel tableModel;
    private JTable flightTable;
    private JTextField flightCodeField, departureTimeField, arrivalTimeField;

    public FlightTrackerGUI() {
        initializeFiles();
        showLoginDialog();
        setupGUI();
    }

    private void initializeFiles() {
        flightFile = new FlightFile();
        userFile = new UserFile();
    }

    private void showLoginDialog() {
        JPanel loginPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        loginPanel.add(new JLabel("Username:"));
        JTextField usernameField = new JTextField();
        loginPanel.add(usernameField);
        loginPanel.add(new JLabel("Password:"));
        JPasswordField passwordField = new JPasswordField();
        loginPanel.add(passwordField);

        int result = JOptionPane.showConfirmDialog(this, loginPanel, "Login",
                                                   JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            authenticateUser(usernameField.getText(), new String(passwordField.getPassword()));
        } else {
            System.exit(0);
        }
    }

    private void authenticateUser(String username, String password) {
        currentUser = userFile.authenticateUser(username, password);
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, "Invalid username or password.");
            showLoginDialog();
        }
    }

    private void setupGUI() {
        if (currentUser != null) {
            setTitle("Flight Tracker (" + currentUser.getUsername() + " - " + currentUser.getRole() + ")");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLayout(new BorderLayout());
            add(createTablePanel(), BorderLayout.CENTER);
            add(createInputPanel(), BorderLayout.SOUTH);
            pack(); // Adjusts window to fit components
            setVisible(true);
        } else {
            System.exit(0);
        }
        loadFlightsToTable();
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        String[] columnNames = {"Flight Code", "Departure Time", "Arrival Time", "Tracking URL"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Only allow admins to edit the table
                return currentUser.getRole() == User.Role.ADMIN;
            }
        };
        flightTable = new JTable(tableModel);
        loadFlightsToTable();
        tablePanel.add(new JScrollPane(flightTable), BorderLayout.CENTER);
        return tablePanel;
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new GridLayout(1, 6, 5, 5));
        inputPanel.add(new JLabel("Flight Code:"));
        flightCodeField = new JTextField();
        inputPanel.add(flightCodeField);
        inputPanel.add(new JLabel("Departure (YYYYMMDDHHmm):"));
        departureTimeField = new JTextField();
        inputPanel.add(departureTimeField);
        inputPanel.add(new JLabel("Arrival (YYYYMMDDHHmm):"));
        arrivalTimeField = new JTextField();
        inputPanel.add(arrivalTimeField);
        inputPanel.add(createButton("Add Flight", this::addFlight));
        inputPanel.add(createButton("Delete Flight", this::deleteFlight));
        return inputPanel;
    }

    private JButton createButton(String label, ActionListener action) {
        JButton button = new JButton(label);
        button.addActionListener(action);
        // Enable or disable based on the user role
        button.setEnabled(currentUser.getRole() == User.Role.ADMIN);
        return button;
    }

    // Method to add a new flight
    private void addFlight(ActionEvent e) {
        String flightCode = flightCodeField.getText();
        LocalDateTime departureTime = parseDateTime(departureTimeField.getText());
        LocalDateTime arrivalTime = parseDateTime(arrivalTimeField.getText());

        if (departureTime != null && arrivalTime != null && departureTime.isBefore(arrivalTime)) {
            Flight newFlight = new Flight(flightCode, departureTime, arrivalTime);
            try {
                flightFile.addFlight(newFlight);
                tableModel.addRow(new Object[]{
                    newFlight.getFlightCode(),
                    newFlight.getDepartureTime(),
                    newFlight.getArrivalTime()
                });
                flightCodeField.setText("");
                departureTimeField.setText("");
                arrivalTimeField.setText("");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error adding flight: " + ex.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid input or arrival is before departure.");
        }
    }

    // Method to delete a flight
    private void deleteFlight(ActionEvent e) {
        int selectedRow = flightTable.getSelectedRow();
        if (selectedRow != -1) {
            String flightCodeToDelete = (String) tableModel.getValueAt(selectedRow, 0); // Get flight code from the table
            try {
                if (flightFile.deleteFlight(flightCodeToDelete)) {
                    tableModel.removeRow(selectedRow);
                } else {
                    JOptionPane.showMessageDialog(this, "Flight not found or could not be deleted.");
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting flight: " + ex.getMessage());
            }
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
    private void loadFlightsToTable() {
        try {
            List<Flight> flights = flightFile.loadFlights();
            for (Flight flight : flights) {
                tableModel.addRow(new Object[]{
                    flight.getFlightCode(),
                    flight.getDepartureTime(),
                    flight.getArrivalTime()
                });
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error loading flights: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FlightTrackerGUI::new);
    }
}

