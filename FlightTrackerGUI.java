import com.toedter.calendar.JDateChooser;
import java.awt.*;
import java.awt.event.*;
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
import javax.swing.table.DefaultTableCellRenderer;

import java.time.format.DateTimeFormatter;

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
    private JButton viewAllFlightsButton;


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
    
            // Initialize the logout button
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
                add(viewerPanel, BorderLayout.CENTER);
                setSize(400, 300); // Adjusted height to accommodate the new button
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
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0: // Flight Code
                    case 3: // Tracking URL
                        return String.class;
                    case 1: // Departure Time
                    case 2: // Arrival Time
                        return LocalDateTime.class;
                    default:
                        return Object.class;
                }
            }
        };
        
        flightTable = new JTable(tableModel);
        flightTable.getColumnModel().getColumn(1).setCellRenderer(new DateTimeRenderer());
        flightTable.getColumnModel().getColumn(2).setCellRenderer(new DateTimeRenderer());
        // Enable sorting on the table
        flightTable.setAutoCreateRowSorter(true);
    
        tablePanel.add(new JScrollPane(flightTable), BorderLayout.CENTER);
        return tablePanel;
    }

    public class DateTimeRenderer extends DefaultTableCellRenderer {
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        if (value instanceof LocalDateTime) {
            value = ((LocalDateTime) value).format(formatter);
        }
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }
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
                    departureTime,
                    arrivalTime,
                    newFlight.getTrackingURL().toString()
                });
                clearInputFields();
                updateFlightInfo();
                loadFlightsToTable();
    
            } catch (Exception ex) {
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
            String flightCodeToDelete = (String) tableModel.getValueAt(selectedRow, 0);
            try {
                if (flightFile.deleteFlight(flightCodeToDelete)) {
                    tableModel.removeRow(selectedRow);
                    updateFlightInfo();
                    loadFlightsToTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Flight not found or could not be deleted.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting flight: " + ex.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a flight to delete.");
        }
    }
    

    private void editFlight(ActionEvent e) {
        int selectedRow = flightTable.getSelectedRow();
        if (selectedRow != -1) {
            String oldFlightCode = (String) tableModel.getValueAt(selectedRow, 0);
            String newFlightCode = flightCodeField.getText().trim();
            LocalDateTime departureTime = getLocalDateTimeFromChooser(departureDateChooser, departureTimeSpinner);
            LocalDateTime arrivalTime = getLocalDateTimeFromChooser(arrivalDateChooser, arrivalTimeSpinner);
    
            if (newFlightCode.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Flight Code cannot be empty.");
                return;
            }
    
            if (departureTime != null && arrivalTime != null && departureTime.isBefore(arrivalTime)) {
                try {
                    Flight updatedFlight = new Flight(newFlightCode, departureTime, arrivalTime);
                    flightFile.updateFlight(oldFlightCode, updatedFlight);
                    // Update the table model
                    tableModel.setValueAt(newFlightCode, selectedRow, 0);
                    tableModel.setValueAt(departureTime, selectedRow, 1);
                    tableModel.setValueAt(arrivalTime, selectedRow, 2);
                    tableModel.setValueAt(updatedFlight.getTrackingURL().toString(), selectedRow, 3);
                    JOptionPane.showMessageDialog(this, "Flight updated successfully.");
                    clearInputFields();
                    updateFlightInfo();
                    loadFlightsToTable();
                } catch (Exception ex) {
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
                    String departureTimeStr = (String) tableModel.getValueAt(selectedRow, 1);
                    String arrivalTimeStr = (String) tableModel.getValueAt(selectedRow, 2);
    
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    LocalDateTime departureTime = LocalDateTime.parse(departureTimeStr, formatter);
                    LocalDateTime arrivalTime = LocalDateTime.parse(arrivalTimeStr, formatter);
    
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
        searchButton = new JButton("Search by Flight Code");
        searchButton.addActionListener(this::searchFlightByCode);
    
        JLabel dateSearchLabel = new JLabel("Or Search by Date:");
        JDateChooser dateChooser = new JDateChooser();
        JButton dateSearchButton = new JButton("Search by Date");
        dateSearchButton.addActionListener(e -> searchFlightByDate(dateChooser.getDate()));
    
        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> loadFlightsToTable());
    
        searchPanel.add(new JLabel("Search Flight:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(dateSearchLabel);
        searchPanel.add(dateChooser);
        searchPanel.add(dateSearchButton);
        searchPanel.add(resetButton);
    
        return searchPanel;
    }
    
    private void searchFlightByCode(ActionEvent e) {
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
    

    private void searchFlightByDate(Date date) {
        if (date == null) {
            JOptionPane.showMessageDialog(this, "Please select a date to search.");
            return;
        }
    
        LocalDateTime startOfDay = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1); // Adjusted to include the entire day
    
        List<Flight> flightsOnDate = flightFile.searchFlightsByDate(startOfDay, endOfDay);
        if (flightsOnDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No flights found on the selected date.");
        } else {
            tableModel.setRowCount(0);
            for (Flight flight : flightsOnDate) {
                tableModel.addRow(new Object[]{
                    flight.getFlightCode(),
                    flight.getDepartureTime(),
                    flight.getArrivalTime(),
                    flight.getTrackingURL().toString()
                });
            }
        }
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
    
        // Create the "View All Flights" button
        viewAllFlightsButton = new JButton("View All Flights");
        viewAllFlightsButton.addActionListener(e -> showFlightList());
    
        // Create a panel to hold the buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(viewAllFlightsButton);
        buttonPanel.add(logoutButton);
    
        viewerPanel.add(buttonPanel, gbc);
    
        return viewerPanel;
    }
    
    private void showFlightList() {
        JFrame flightListFrame = new JFrame("All Flights");
        flightListFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        flightListFrame.setSize(600, 500);
    
        String[] columnNames = {"Flight Code", "Departure Time", "Arrival Time", "Tracking URL"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
    
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0:
                    case 3:
                        return String.class;
                    case 1:
                    case 2:
                        return LocalDateTime.class;
                    default:
                        return Object.class;
                }
            }
        };
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
    
        table.getColumnModel().getColumn(1).setCellRenderer(new DateTimeRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(new DateTimeRenderer());
    
        loadFlightsIntoModel(model, flightFile.getAllFlights());
    
        JScrollPane scrollPane = new JScrollPane(table);
        flightListFrame.add(scrollPane, BorderLayout.CENTER);
    
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel dateSearchLabel = new JLabel("Search by Date:");
        JDateChooser dateChooser = new JDateChooser();
        JButton dateSearchButton = new JButton("Search");
        dateSearchButton.addActionListener(e -> searchFlightByDateInViewer(dateChooser.getDate(), model));
    
        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> {
            try {
                loadFlightsIntoModel(model, flightFile.getAllFlights());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error loading flights: " + ex.getMessage());
            }
        });
    
        searchPanel.add(dateSearchLabel);
        searchPanel.add(dateChooser);
        searchPanel.add(dateSearchButton);
        searchPanel.add(resetButton);
    
        flightListFrame.add(searchPanel, BorderLayout.NORTH);
    
        flightListFrame.setLocationRelativeTo(this);
        flightListFrame.setVisible(true);
    }
    

    private void searchFlightByDateInViewer(Date date, DefaultTableModel model) {
        if (date == null) {
            JOptionPane.showMessageDialog(this, "Please select a date to search.");
            return;
        }
    
        LocalDateTime startOfDay = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
    
        List<Flight> flightsOnDate = flightFile.searchFlightsByDate(startOfDay, endOfDay);
        if (flightsOnDate.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No flights found on the selected date.");
        } else {
            // Update the table model to display only the flights on the selected date
            model.setRowCount(0);
            for (Flight flight : flightsOnDate) {
                model.addRow(new Object[]{
                    flight.getFlightCode(),
                    flight.getDepartureTime(),
                    flight.getArrivalTime(),
                    flight.getTrackingURL().toString()
                });
            }
        }
    }

    private void loadFlightsIntoModel(DefaultTableModel model, List<Flight> flights) {
        model.setRowCount(0);
        for (Flight flight : flights) {
            model.addRow(new Object[]{
                flight.getFlightCode(),
                flight.getDepartureTime(),
                flight.getArrivalTime(),
                flight.getTrackingURL().toString()
            });
        }
    }
    
    
    

    private void updateViewerPanel(Flight flight) {
        if (flight != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            nextTripLabel.setText("Next Trip: " + flight.getFlightCode());
            departureLabel.setText("Departure: " + flight.getDepartureTime().format(formatter));
            arrivalLabel.setText("Arrival: " + flight.getArrivalTime().format(formatter));
            trackingUrlLabel.setText("Tracking URL: " + flight.getTrackingURL().toString());
            String flightStatus = flight.getDepartureTime().isAfter(LocalDateTime.now()) ? "Upcoming" : "In Progress";
            statusLabel.setText("Status: " + flightStatus + " Trip");
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
                List<Flight> flights = flightFile.getAllFlights();
                Flight nextFlight = findNextOrCurrentFlight(flights);
                updateViewerPanel(nextFlight);
            } catch (Exception ex) {
                statusLabel.setText("Status: Error loading flight information");
            }
        } else {
            loadFlightsToTable();
        }
    }

    private Flight findNextOrCurrentFlight(List<Flight> flights) {
        LocalDateTime now = LocalDateTime.now();
        for (Flight flight : flights) {
            if (flight.getDepartureTime().isAfter(now) || 
                (flight.getDepartureTime().isBefore(now) && flight.getArrivalTime().isAfter(now))) {
                return flight;
            }
        }
        return null;
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

