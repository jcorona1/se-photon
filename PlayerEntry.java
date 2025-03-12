import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.*;
import java.util.regex.*;

public class PlayerEntry extends JFrame implements ActionListener {
    // Player entry GUI components (for the pop-up dialog)
    private JLabel jlabel;
    private JTextField idText;
    private JButton jbutton;
    
    // Player variables
    private int id;
    private String codename;
    private String team;
    private int equipmentId; // New: Equipment ID
    
    // Variable for tracking entry state:
    // 1: Enter Player ID
    // 2: Enter Equipment ID (1-100)
    // 3: For a new player, Enter Player Codename; for an existing player, Enter Team
    // 4: (For a new player only) Enter Team
    private int state;
    
    // Flag to indicate if the player exists in the database.
    private boolean playerFound;
    
    // Reference to the pop-up dialog
    private JDialog playerEntryDialog;
    private JDialog changeNetworkDialog;
    
    // Table models for each half (to allow dynamic updating)
    private DefaultTableModel leftModel; // red
    private DefaultTableModel rightModel; // green
    
    // Tables for red and green halves
    private JTable leftTable; // red
    private JTable rightTable; // green
    
    // Database variables
    public Connection photon; 
    public PreparedStatement codenameQuery;
    public PreparedStatement insertPlayer;
    public ResultSet codenameQueryResult;

    // TODO - Move main functionality outside of constructor and into its own method
    public PlayerEntry() {
        
        // Open the player database
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        }
        try {
            photon = DriverManager.getConnection("jdbc:postgresql://localhost:5432/photon", "student", "student");
        } catch (SQLException e) {
            System.out.println("Error establishing database connection."+e);
        }

        // Set up the full-screen window.
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
        
        // TODO - Create a method to streamline the creation of new labels
        // --- Top Panel for Indicators ---
        // Creates label for adding a player
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.setOpaque(false);

        JLabel addPlayer = new JLabel("Press F1 to add a player, ");
        addPlayer.setFont(new Font("Arial", Font.BOLD, 24)); // Smaller font
        addPlayer.setForeground(Color.BLACK); // Text set to black.
        topPanel.add(addPlayer);

        add(topPanel, BorderLayout.NORTH);

        // Creates label for changing network address
        JLabel changeNetworkAddress = new JLabel("Press F3 to change network address, ");
        changeNetworkAddress.setFont(new Font("Arial", Font.BOLD, 24)); 
        changeNetworkAddress.setForeground(Color.BLACK);
        topPanel.add(changeNetworkAddress);

        // Creates label for starting game
        JLabel startGame = new JLabel("Press F5 to start the game.");
        startGame.setFont(new Font("Arial", Font.BOLD, 24)); // Smaller font
        startGame.setForeground(Color.BLACK); // Text set to black.
        topPanel.add(startGame);

        
        // --- Center Panel divided into two halves ---
        JPanel centerPanel = new JPanel(new GridLayout(1, 2));
        
        // Left half panel (dark red background)
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(new Color(139, 0, 0)); // Dark red
        leftModel = new DefaultTableModel(new Object[]{"Red Team"}, 0);
        leftTable = new JTable(leftModel);
        JScrollPane leftScroll = new JScrollPane(leftTable);
        JPanel leftContainer = new JPanel(new GridBagLayout());
        leftContainer.setOpaque(false);
        leftContainer.add(leftScroll);
        leftPanel.add(leftContainer, BorderLayout.CENTER);
        
        // Right half panel (dark green background)
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(new Color(0, 100, 0)); // Dark green
        rightModel = new DefaultTableModel(new Object[]{"Green Team"}, 0);
        rightTable = new JTable(rightModel);
        JScrollPane rightScroll = new JScrollPane(rightTable);
        JPanel rightContainer = new JPanel(new GridBagLayout());
        rightContainer.setOpaque(false);
        rightContainer.add(rightScroll);
        rightPanel.add(rightContainer, BorderLayout.CENTER);
        
        centerPanel.add(leftPanel);
        centerPanel.add(rightPanel);
        add(centerPanel, BorderLayout.CENTER);
        
        // --- Key Binding for F1 ---
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("F1"), "openPlayerEntry");
        getRootPane().getActionMap().put("openPlayerEntry", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openPlayerEntryDialog();
            }
        });

        // --- Key Binding for F2 ---
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("F3"), "changeNetworkAddress");
        getRootPane().getActionMap().put("changeNetworkAddress", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeNetworkAddressDialog();
            }
        });

        // --- Key Binding for F5 ---
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("F5"), "startGame");
        getRootPane().getActionMap().put("startGame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startGame();
            }
        });
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public DefaultTableModel getLeftModel() {
        return leftModel;
    }

    public DefaultTableModel getRightModel() {
        return rightModel;
    }

    // Creates and shows the pop-up dialog for player entry.
    private void openPlayerEntryDialog() {
        playerEntryDialog = new JDialog(this, "Player Entry", true);
        playerEntryDialog.setSize(400, 200);
        playerEntryDialog.setLayout(new GridLayout(3, 1));
        playerEntryDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        playerEntryDialog.setLocationRelativeTo(null); // center the dialog
        
        jlabel = new JLabel("Enter Player ID:");
        idText = new JTextField(20);
        jbutton = new JButton(new AbstractAction("Enter") {
            @Override
            public void actionPerformed(ActionEvent e) {
                update();
            }
        });
        jbutton.addActionListener(this);
        
        playerEntryDialog.add(jlabel);
        playerEntryDialog.add(idText);
        playerEntryDialog.add(jbutton);
        
        // Start with state 1.
        state = 1;
        
        playerEntryDialog.setVisible(true);
    }
    
    // Processes user input from the pop-up dialog.
    public void update() {
        switch (state) {
            case 1: // Enter Player ID.
                try {
                    id = Integer.parseInt(idText.getText().trim());
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(playerEntryDialog, "Invalid ID. Please enter a valid number.");
                    return;
                }
                // Check if player exists.
                try {
                    codenameQuery = photon.prepareStatement("SELECT codename FROM players WHERE id = "+id+";");
                    codenameQueryResult = codenameQuery.executeQuery();
                    codenameQueryResult.next();
                    
                } catch (SQLException e) {
                    System.out.println("Query error." + e);
                }
                try {
                    codename = codenameQueryResult.getString("codename");
                    playerFound = true;
                    JOptionPane.showMessageDialog(this, "Player ID found. Welcome, " + codename + ".");
                } catch (SQLException e) {
                    playerFound = false;
                }
                // Next: ask for Equipment ID.
                jlabel.setText("Enter Equipment ID (1-100):");
                idText.setText("");
                state = 2;
                break;
                
            case 2: // Enter Equipment ID.
                try {
                    equipmentId = Integer.parseInt(idText.getText().trim());
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(playerEntryDialog, "Invalid Equipment ID. Please enter a number between 1 and 40.");
                    return;
                }
                if (equipmentId < 1 || equipmentId > 100) {
                    JOptionPane.showMessageDialog(playerEntryDialog, "Equipment ID must be between 1 and 100.");
                    return;
                }
                // Broadcast the equipment code via UDP.
                broadcastEquipmentCode(equipmentId);
                
                // After a valid equipment ID, proceed.
                if (playerFound) {
                    // Existing player: go directly to team selection.
                    jlabel.setText("Enter Player Team (red/green):");
                    idText.setText("");
                    state = 4;
                } else {
                    // New player: first ask for codename.
                    jlabel.setText("Enter Player Codename:");
                    idText.setText("");
                    state = 3;
                }
                break;
                
            case 3:
                if (!playerFound) {
                    // For a new player: we expect the codename here.
                    codename = idText.getText().trim();
                    if (codename.isEmpty()) {
                        JOptionPane.showMessageDialog(playerEntryDialog, "Codename cannot be empty.");
                        return;
                    }
                    // Add the new record to the database.
                    try {
                        insertPlayer = photon.prepareStatement("INSERT INTO players VALUES("+id+", \'"+codename+"\');");            
                        insertPlayer.execute();
                    } catch (SQLException e) {
                        System.out.println("Error in Prepared Statements."+e);
                    } 
                    // Next, ask for the team.
                    jlabel.setText("Enter Player Team (red/green):");
                    idText.setText("");
                    state = 4;
                } else {
                    // For an existing player, state 3 expects team input.
                    team = idText.getText().trim().toLowerCase();
                    if (!team.equals("red") && !team.equals("green")) {
                        JOptionPane.showMessageDialog(playerEntryDialog, "Invalid team. Please enter 'red' or 'green'.");
                        return;
                    }
                    registerPlayer();
                    playerEntryDialog.dispose();
                }
                break;
                
            case 4: // For a new player: now enter team.
                team = idText.getText().trim().toLowerCase();
                if (!team.equals("red") && !team.equals("green")) {
                    JOptionPane.showMessageDialog(playerEntryDialog, "Invalid team. Please enter 'red' or 'green'.");
                    return;
                }
                registerPlayer();
                playerEntryDialog.dispose();
                break;
        }
    }

    // Start a countdown and then deploy player action screen
    private void startGame()
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                PlayerAction playerAction = new PlayerAction(getLeftModel(), getRightModel());
            }
        };

        new Countdown(runnable);
    }

    // Creates pop-up dialog for changing the network address used by the game
    private void changeNetworkAddressDialog() {
        changeNetworkDialog = new JDialog(this, "Change Network Address", true);
        changeNetworkDialog.setSize(400, 200);
        changeNetworkDialog.setLayout(new GridLayout(4, 1));
        changeNetworkDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        changeNetworkDialog.setLocationRelativeTo(null);

        jlabel = new JLabel("Enter New Network Address: ");
        JLabel currentIP = new JLabel("(current address is " + UdpClient.getBroadcastAddress() + ")");
        idText = new JTextField(9);
        jbutton = new JButton(new AbstractAction("Change Network") {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeNetworkAddress();
            }
        });
        jbutton.addActionListener(this);

        changeNetworkDialog.add(jlabel);
        changeNetworkDialog.add(currentIP);
        changeNetworkDialog.add(idText);
        changeNetworkDialog.add(jbutton);

        changeNetworkDialog.setVisible(true);
    }

    // Changes the network address used by UDP client
    private void changeNetworkAddress() {
        // Retreives text entered into dialog text field
        String address = idText.getText().trim();
        
        if(isValidAddress(address)) {
            // Changes boradcastAddress in UdpClient
            UdpClient.setBroadcastAddress(address);
            // Displays message confirming address change
            JOptionPane.showMessageDialog(changeNetworkDialog, "Network Address Changed! "  
                + "New IP address is: \n" + UdpClient.getBroadcastAddress());
            // Removes dialog
            changeNetworkDialog.dispose();
        }
        else {
            // Displays message dialog that address is invalid 
            JOptionPane.showMessageDialog(changeNetworkDialog, "Invalid IP address. Please enter a valid address.");
        }
    }

    private boolean isValidAddress(String address) {
        // Checks that the address is in valid length range
        if(address == null) {
            return false;
        }

        // Regex for a number 0 - 255
        String zeroTo255 = "(\\d{1,2}|(0|1)\\d{2}|2[0-4]\\d|25[0-5])";
        // Regex for the format of IP addresses
        String ipFormat = zeroTo255 + "\\." + zeroTo255 + "\\." + zeroTo255 + "\\." + zeroTo255;
        // Turn ipFormat into Regex pattern
        Pattern ipPattern = Pattern.compile(ipFormat);
        // Turn ipPattern into a matchable object
        Matcher ipAddress = ipPattern.matcher(address);
        // Return whether address matches IP address pattern
        return ipAddress.matches();
    }
    
    // Sends the equipment code via UDP broadcast to port 7500.
    private void broadcastEquipmentCode(int equipmentId) {
        try {
            DatagramSocket udpSocket = new DatagramSocket();
            udpSocket.setBroadcast(true);
            String message = Integer.toString(equipmentId);
            byte[] buffer = message.getBytes();
            InetAddress broadcastAddress = InetAddress.getByName(UdpClient.getBroadcastAddress());
            // Equipment will be listening on port 7500.
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, broadcastAddress, 7500);
            udpSocket.send(packet);
            udpSocket.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    // Register the player: show message and add codename to the appropriate table.
    private void registerPlayer() {
        JOptionPane.showMessageDialog(playerEntryDialog,
                "Player Registered!\nID: " + id + "\nEquipment ID: " + equipmentId +
                "\nCodename: " + codename + "\nTeam: " + team);
        
        // Add the codename to the respective table, if there's room.
        if (team.equals("red")) {
            if (leftModel.getRowCount() < 20) {
                leftModel.addRow(new Object[]{codename});
            } else {
                JOptionPane.showMessageDialog(playerEntryDialog, "Red team table is full.");
            }
        } else { // team is "green"
            if (rightModel.getRowCount() < 20) {
                rightModel.addRow(new Object[]{codename});
            } else {
                JOptionPane.showMessageDialog(playerEntryDialog, "Green team table is full.");
            }
        }
    }
    
    // ActionListener method for the button in the dialog.
    @Override
    public void actionPerformed(ActionEvent e) {
        //update();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PlayerEntry());
    }
}
