import com.toedter.calendar.JDateChooser;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

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
    private JLabel departureLabel;
    private JLabel arrivalLabel;
    private JLabel trackingUrlLabel;
    private JButton logoutButton;
    private JButton editFlightButton;

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
            
            logoutButton = new JButton("Log Out");
            logoutButton.addActionListener(e -> logout());
            
            if (currentUser.getRole() == User.Role.ADMIN) {
                add(createTablePanel(), BorderLayout.CENTER);
                add(createSearchPanel(), BorderLayout.NORTH);
                JPanel southPanel = new JPanel(new BorderLayout());
                southPanel.add(createInputPanel(), BorderLayout.CENTER);
                southPanel.add(logoutButton, BorderLayout.EAST);
                add(southPanel, BorderLayout.SOUTH);
                pack();
                setupTableSelectionListener();
            } else {
                JPanel viewerPanel = createViewerPanel();
                viewerPanel.add(logoutButton);
                add(viewerPanel, BorderLayout.CENTER);
                setSize(400, 250); // Increased height to accommodate the logout button
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
        JPanel inputPanel = new JPanel(new GridLayout(1, 10, 5, 5)); // Changed to 10 columns
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
        editFlightButton = createButton("Edit Flight", this::editFlight);
        inputPanel.add(editFlightButton);
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

    private void editFlight(ActionEvent e) {
        int selectedRow = flightTable.getSelectedRow();
        if (selectedRow != -1) {
            String flightCode = (String) tableModel.getValueAt(selectedRow, 0);
            LocalDateTime departureTime = getLocalDateTimeFromChooser(departureDateChooser, departureTimeSpinner);
            LocalDateTime arrivalTime = getLocalDateTimeFromChooser(arrivalDateChooser, arrivalTimeSpinner);

            if (departureTime != null && arrivalTime != null && departureTime.isBefore(arrivalTime)) {
                try {
                    Flight updatedFlight = new Flight(flightCode, departureTime, arrivalTime);
                    flightFile.updateFlight(updatedFlight);
                    tableModel.setValueAt(departureTime, selectedRow, 1);
                    tableModel.setValueAt(arrivalTime, selectedRow, 2);
                    JOptionPane.showMessageDialog(this, "Flight updated successfully.");
                    clearInputFields();
                    updateFlightInfo();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Error updating flight: " + ex.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid input or arrival is before departure.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a flight to edit.");
        }
    }

    private void clearInputFields() {
        flightCodeField.setText("");
        departureDateChooser.setDate(null);
        arrivalDateChooser.setDate(null);
        departureTimeSpinner.setValue(new Date());
        arrivalTimeSpinner.setValue(new Date());
    }

    private void setupTableSelectionListener() {
        flightTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = flightTable.getSelectedRow();
                if (selectedRow != -1) {
                    String flightCode = (String) tableModel.getValueAt(selectedRow, 0);
                    LocalDateTime departureTime = (LocalDateTime) tableModel.getValueAt(selectedRow, 1);
                    LocalDateTime arrivalTime = (LocalDateTime) tableModel.getValueAt(selectedRow, 2);
                    
                    flightCodeField.setText(flightCode);
                    departureDateChooser.setDate(Date.from(departureTime.atZone(ZoneId.systemDefault()).toInstant()));
                    arrivalDateChooser.setDate(Date.from(arrivalTime.atZone(ZoneId.systemDefault()).toInstant()));
                    departureTimeSpinner.setValue(Date.from(departureTime.atZone(ZoneId.systemDefault()).toInstant()));
                    arrivalTimeSpinner.setValue(Date.from(arrivalTime.atZone(ZoneId.systemDefault()).toInstant()));
                }
            }
        });
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
            List<Flight> flights = flightFile.getAllFlights();
            for (Flight flight : flights) {
                tableModel.addRow(new Object[]{
                    flight.getFlightCode(),
                    flight.getDepartureTime(),
                    flight.getArrivalTime(),
                    flight.getTrackingURL().toString()
                });
            }
        } catch (Exception ex) {
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

        Flight foundFlight = flightFile.searchFlight(searchCode);

        if (foundFlight != null) {
            highlightFlightInTable(foundFlight);

        } else {
            JOptionPane.showMessageDialog(this, "Flight not found.");
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

    private JPanel createViewerPanel() {
        JPanel viewerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        nextTripLabel = new JLabel("Next Trip: None scheduled");
        nextTripLabel.setFont(new Font("Arial", Font.BOLD, 16));
        viewerPanel.add(nextTripLabel, gbc);

        departureLabel = new JLabel("Departure: N/A");
        viewerPanel.add(departureLabel, gbc);

        arrivalLabel = new JLabel("Arrival: N/A");
        viewerPanel.add(arrivalLabel, gbc);

        trackingUrlLabel = new JLabel("Tracking URL: N/A");
        trackingUrlLabel.setForeground(Color.BLUE);
        trackingUrlLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        trackingUrlLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String urlText = trackingUrlLabel.getText().substring(13).trim();
                if (!urlText.startsWith("http://") && !urlText.startsWith("https://")) {
                    urlText = "https://" + urlText;
                }
                
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    try {
                        URL url = new URL(urlText);
                        Desktop.getDesktop().browse(url.toURI());
                    } catch (MalformedURLException ex) {
                        JOptionPane.showMessageDialog(FlightTrackerGUI.this, 
                            "Invalid URL format: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(FlightTrackerGUI.this, 
                            "Error opening URL: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        viewerPanel.add(trackingUrlLabel, gbc);

        statusLabel = new JLabel("Status: No upcoming flights");
        viewerPanel.add(statusLabel, gbc);

        return viewerPanel;
    }

    private void updateViewerPanel(Flight flight) {
        if (flight != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            nextTripLabel.setText("Next Trip: " + flight.getFlightCode());
            departureLabel.setText("Departure: " + flight.getDepartureTime().format(formatter));
            arrivalLabel.setText("Arrival: " + flight.getArrivalTime().format(formatter));
            trackingUrlLabel.setText("Tracking URL: " + flight.getTrackingURL().toString());
            String flightStatus = flight.getDepartureTime().isAfter(LocalDateTime.now()) ? "Upcoming" : "In Progress";
            statusLabel.setText("Status: " + flightStatus + " flight");
        } else {
            nextTripLabel.setText("Next Trip: None scheduled");
            departureLabel.setText("Departure: N/A");
            arrivalLabel.setText("Arrival: N/A");
            trackingUrlLabel.setText("Tracking URL: N/A");
            statusLabel.setText("Status: No upcoming or current flights");
        }
    }

    private void updateFlightInfo() {
        if (currentUser.getRole() == User.Role.VIEWER) {
            try {
                List<Flight> flights = flightFile.loadFlights();
                Flight nextFlight = findNextOrCurrentFlight(flights);
                updateViewerPanel(nextFlight);
            } catch (IOException ex) {
                statusLabel.setText("Status: Error loading flight information");
            }
        } else {
            loadFlightsToTable();
        }
    }

    private Flight findNextOrCurrentFlight(List<Flight> flights) {
        LocalDateTime now = LocalDateTime.now();
        return flights.stream()
                      .filter(f -> f.getArrivalTime().isAfter(now))
                      .min((f1, f2) -> f1.getDepartureTime().compareTo(f2.getDepartureTime()))
                      .orElse(null);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to log out?", "Confirm Logout",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            dispose(); // Close the current window
            new FlightTrackerGUI(); // Create a new instance of the application
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FlightTrackerGUI::new);
    }
}

