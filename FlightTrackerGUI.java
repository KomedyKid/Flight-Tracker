import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.ZoneId;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.SwingConstants;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JSpinnerDateEditor;

public class FlightTrackerGUI extends JFrame {

    private UserFile userFile;
    private User currentUser;
    private FlightFile flightFile;
    private DefaultTableModel tableModel;
    private JTable flightTable;
    private JTextField flightCodeField, departureTimeField, arrivalTimeField;
    private JDateChooser departureDateChooser, arrivalDateChooser;
    private JSpinner departureTimeSpinner, arrivalTimeSpinner;
    private JTextField searchField;
    private JButton searchButton;
    private JLabel statusLabel;
    private JLabel nextTripLabel;

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
            
            if (currentUser.getRole() == User.Role.ADMIN) {
                add(createTablePanel(), BorderLayout.CENTER);
                add(createSearchPanel(), BorderLayout.NORTH);
                add(createInputPanel(), BorderLayout.SOUTH);
                pack();
            } else {
                add(createViewerPanel(), BorderLayout.CENTER);
                setSize(400, 200); // Set a default size for viewers
            }
            
            setLocationRelativeTo(null); // Center the window on the screen
            setVisible(true);
        } else {
            System.exit(0);
        }
        updateFlightInfo();
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
        tablePanel.add(new JScrollPane(flightTable), BorderLayout.CENTER);
        return tablePanel;
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new GridLayout(1, 9, 5, 5));
        inputPanel.add(new JLabel("Flight Code:"));
        flightCodeField = new JTextField();
        inputPanel.add(flightCodeField);
        
        inputPanel.add(new JLabel("Departure:"));
        departureDateChooser = new JDateChooser();
        inputPanel.add(departureDateChooser);
        departureTimeSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor departureTimeEditor = new JSpinner.DateEditor(departureTimeSpinner, "HH:mm");
        departureTimeSpinner.setEditor(departureTimeEditor);
        inputPanel.add(departureTimeSpinner);
        
        inputPanel.add(new JLabel("Arrival:"));
        arrivalDateChooser = new JDateChooser();
        inputPanel.add(arrivalDateChooser);
        arrivalTimeSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor arrivalTimeEditor = new JSpinner.DateEditor(arrivalTimeSpinner, "HH:mm");
        arrivalTimeSpinner.setEditor(arrivalTimeEditor);
        inputPanel.add(arrivalTimeSpinner);
        
        inputPanel.add(createButton("Add Flight", this::addFlight));
        inputPanel.add(createButton("Delete Flight", this::deleteFlight));
        return inputPanel;
    }

    private JButton createButton(String label, ActionListener action) {
        JButton button = new JButton(label);
        button.addActionListener(action);
        return button;
    }

    private void addFlight(ActionEvent e) {
        String flightCode = flightCodeField.getText();
        LocalDateTime departureTime = getLocalDateTimeFromChooser(departureDateChooser, departureTimeSpinner);
        LocalDateTime arrivalTime = getLocalDateTimeFromChooser(arrivalDateChooser, arrivalTimeSpinner);

        if (departureTime != null && arrivalTime != null && departureTime.isBefore(arrivalTime)) {
            Flight newFlight = new Flight(flightCode, departureTime, arrivalTime);
            try {
                flightFile.addFlight(newFlight);
                tableModel.addRow(new Object[]{
                    newFlight.getFlightCode(),
                    newFlight.getDepartureTime(),
                    newFlight.getArrivalTime(),
                    newFlight.getTrackingURL().toString()
                });
                flightCodeField.setText("");
                departureDateChooser.setDate(null);
                arrivalDateChooser.setDate(null);
                departureTimeSpinner.setValue(new Date());
                arrivalTimeSpinner.setValue(new Date());
                updateFlightInfo();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error adding flight: " + ex.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid input or arrival is before departure.");
        }
    }

    private LocalDateTime getLocalDateTimeFromChooser(JDateChooser dateChooser, JSpinner timeSpinner) {
        if (dateChooser.getDate() == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateChooser.getDate());
        Calendar timeCalendar = Calendar.getInstance();
        timeCalendar.setTime((Date) timeSpinner.getValue());
        calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
        return LocalDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault());
    }

    private void deleteFlight(ActionEvent e) {
        int selectedRow = flightTable.getSelectedRow();
        if (selectedRow != -1) {
            String flightCodeToDelete = (String) tableModel.getValueAt(selectedRow, 0); // Get flight code from the table
            try {
                if (flightFile.deleteFlight(flightCodeToDelete)) {
                    tableModel.removeRow(selectedRow);
                    updateFlightInfo();
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

    private LocalDateTime parseDateTime(String dateTimeString) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
            return LocalDateTime.parse(dateTimeString, formatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private void loadFlightsToTable() {
        tableModel.setRowCount(0);
        try {
            List<Flight> flights = flightFile.loadFlights();
            for (Flight flight : flights) {
                tableModel.addRow(new Object[]{
                    flight.getFlightCode(),
                    flight.getDepartureTime(),
                    flight.getArrivalTime(),
                    flight.getTrackingURL().toString()
                });
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error loading flights: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        searchButton = new JButton("Search");
        searchButton.addActionListener(this::searchFlight);
        
        searchPanel.add(new JLabel("Search Flight:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        
        return searchPanel;
    }

    private void searchFlight(ActionEvent e) {
        String searchCode = searchField.getText().trim();
        if (searchCode.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a flight code to search.");
            return;
        }

        try {
            List<Flight> flights = flightFile.loadFlights();
            Flight foundFlight = sequentialSearch(flights, searchCode);
            
            if (foundFlight != null) {
                highlightFlightInTable(foundFlight);
            } else {
                JOptionPane.showMessageDialog(this, "Flight not found.");
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error searching for flight: " + ex.getMessage());
        }
    }

    private Flight sequentialSearch(List<Flight> flights, String flightCode) {
        for (Flight flight : flights) {
            if (flight.getFlightCode().equalsIgnoreCase(flightCode)) {
                return flight;
            }
        }
        return null;
    }

    private void highlightFlightInTable(Flight flight) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 0).equals(flight.getFlightCode())) {
                flightTable.setRowSelectionInterval(i, i);
                flightTable.scrollRectToVisible(flightTable.getCellRect(i, 0, true));
                return;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FlightTrackerGUI::new);
    }
}

