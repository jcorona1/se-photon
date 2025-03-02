import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

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
    // 2: Enter Equipment ID (1-40)
    // 3: For a new player, Enter Player Codename; for an existing player, Enter Team
    // 4: (For a new player only) Enter Team
    private int state;
    
    // Flag to indicate if the player exists in the database.
    private boolean playerFound;
    
    // Database variables (ensure DB and Record classes are implemented)
    private DB db = new DB();
    private Record record = new Record();
    
    // Reference to the pop-up dialog
    private JDialog playerEntryDialog;
    
    // Table models for each half (to allow dynamic updating)
    private DefaultTableModel leftModel;
    private DefaultTableModel rightModel;
    
    // Tables for red and green halves
    private JTable leftTable;
    private JTable rightTable;
    
    public PlayerEntry() {
        // Open the database.
        db.openDB("photon.csv");
        
        // Set up the full-screen window.
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
        
        // --- Top Panel for Indicator ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.setOpaque(false);
        JLabel indicator = new JLabel("Press F1 to add a player");
        indicator.setFont(new Font("Arial", Font.BOLD, 24)); // Smaller font
        indicator.setForeground(Color.BLACK); // Indicator text set to black.
        topPanel.add(indicator);
        add(topPanel, BorderLayout.NORTH);
        
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
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
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
        jbutton = new JButton("Enter");
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
                if (db.search(Integer.toString(id).trim()) >= 0) {
                    playerFound = true;
                    record = db.readRecord(db.search(Integer.toString(id).trim()));
                    codename = record.Codename;
                } else {
                    playerFound = false;
                }
                // Next: ask for Equipment ID.
                jlabel.setText("Enter Equipment ID (1-40):");
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
                if (equipmentId < 1 || equipmentId > 40) {
                    JOptionPane.showMessageDialog(playerEntryDialog, "Equipment ID must be between 1 and 40.");
                    return;
                }
                // Broadcast the equipment code via UDP.
                broadcastEquipmentCode(equipmentId);
                
                // After a valid equipment ID, proceed.
                if (playerFound) {
                    // Existing player: go directly to team selection.
                    jlabel.setText("Enter Player Team (red/green):");
                    idText.setText("");
                    state = 3;
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
                    db.addRecord(Integer.toString(id), codename);
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
        update();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PlayerEntry());
    }
}
