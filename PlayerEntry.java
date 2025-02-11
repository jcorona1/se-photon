import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import java.lang.Record;

public class PlayerEntry extends JFrame implements ActionListener {
    // GUI components
    private JLabel jlabel;
    private JTextField idText;
    private JButton jbutton;
    
    // Player variables
    private int id;
    private String codename;
    private String team;

    // Variable for tracking state
    private int state;

    // Database variables
    private DB db = new DB();
    private Record record = new Record(); 

    // Constructor to initialize the player entry screen
    public PlayerEntry() {
        
        // Open the player database
        db.openDB("photon.csv");

        this.state = 1;  // Start with ID input

        // Create JFrame with title
        setTitle("Player Entry");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 1));

        // Create components
        jlabel = new JLabel("Enter Player ID:");
        idText = new JTextField(20);
        jbutton = new JButton("Enter");

        // Add action listener
        jbutton.addActionListener(this);

        // Add components to frame
        add(jlabel);
        add(idText);
        add(jbutton);

        setVisible(true);
    }

    // Method to handle updates
    public void update() {

        switch (state) {
            case 1:
                try {
                    id = Integer.parseInt(idText.getText().trim());    
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid ID. Please enter a valid number.");
                    return;
                }
                if (db.search(Integer.toString(id).trim()) >= 0) {
                    record = db.readRecord(db.search(Integer.toString(id).trim()));
                    JOptionPane.showMessageDialog(this, "Player ID found. Welcome, " + record.Codename + ".");
                    codename = record.Codename;

                    jlabel.setText("Enter Player Team (red/green):");
                    idText.setText("");
                    state = 3;
                } else {
                    jlabel.setText("Enter Player Codename:");
                    idText.setText("");
                    state = 2;
                }
                break;

            case 2:
                
                codename = idText.getText().trim();
                if (codename.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Codename cannot be empty.");
                    return;
                }
                db.addRecord(Integer.toString(id), codename);
                    
                jlabel.setText("Enter Player Team (red/green):");
                idText.setText("");
                state = 3;
                break;

            case 3:
                
                team = idText.getText().trim().toLowerCase();
                if (!team.equals("red") && !team.equals("green")) {
                    JOptionPane.showMessageDialog(this, "Invalid team. Please enter 'red' or 'green'.");
                    return;
                }
                JOptionPane.showMessageDialog(this, "Player Registered!\nID: " + id + "\nCodename: " + codename + "\nTeam: " + team);
                
                // Reset for new entry
                jlabel.setText("Enter Player ID:");
                idText.setText("");
                state = 1;
                break;
        }
    }

    // Method triggered when button is clicked
    @Override
    public void actionPerformed(ActionEvent e) {
        update();
    }

    public static void main(String[] args) {
        new PlayerEntry();
    }
}