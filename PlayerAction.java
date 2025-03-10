import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PlayerAction extends JFrame {
    
    /**
     * Constructor that accepts the red and green team table models
     * from PlayerEntry, duplicates them, and uses them to populate the UI.
     */
    public PlayerAction(DefaultTableModel redTeamModel, DefaultTableModel greenTeamModel) {
        // Duplicate the provided models so changes here don't affect PlayerEntry.
        DefaultTableModel redTableModel = duplicateTableModel(redTeamModel);
        DefaultTableModel greenTableModel = duplicateTableModel(greenTeamModel);

        // Set up the full-screen window
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new GridLayout(1, 3));

        // Create the left panel (dark red) with a centered table
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(new Color(139, 0, 0)); // Dark red
        JTable redTable = new JTable(redTableModel);
        JScrollPane redScrollPane = new JScrollPane(redTable);
        redScrollPane.setPreferredSize(new Dimension(400, 300)); // Adjust as needed
        leftPanel.add(redScrollPane);

        // Create the middle panel (black)
        JPanel middlePanel = new JPanel();
        middlePanel.setBackground(Color.BLACK);

        // Create the right panel (dark green) with a centered table
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(new Color(0, 100, 0)); // Dark green
        JTable greenTable = new JTable(greenTableModel);
        JScrollPane greenScrollPane = new JScrollPane(greenTable);
        greenScrollPane.setPreferredSize(new Dimension(400, 300)); // Adjust as needed
        rightPanel.add(greenScrollPane);

        // Add panels to the frame
        add(leftPanel);
        add(middlePanel);
        add(rightPanel);

        // Display the window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
    
    /**
     * Creates a duplicate of the given DefaultTableModel.
     * This method copies the column names and all row data.
     */
    private DefaultTableModel duplicateTableModel(DefaultTableModel original) {
        // Create a new model with the same column names
        DefaultTableModel duplicate = new DefaultTableModel();
        for (int col = 0; col < original.getColumnCount(); col++) {
            duplicate.addColumn(original.getColumnName(col));
        }
        // Copy all rows
        for (int row = 0; row < original.getRowCount(); row++) {
            Object[] rowData = new Object[original.getColumnCount()];
            for (int col = 0; col < original.getColumnCount(); col++) {
                rowData[col] = original.getValueAt(row, col);
            }
            duplicate.addRow(rowData);
        }
        return duplicate;
    }

    // A main method for testing PlayerAction independently.
    public static void main(String[] args) {
        // Create sample table models for testing purposes.
        DefaultTableModel redModel = new DefaultTableModel(new Object[]{"Red Team"}, 0);
        redModel.addRow(new Object[]{"Alice"});
        redModel.addRow(new Object[]{"Bob"});
        
        DefaultTableModel greenModel = new DefaultTableModel(new Object[]{"Green Team"}, 0);
        greenModel.addRow(new Object[]{"Carol"});
        greenModel.addRow(new Object[]{"Dave"});
        
        SwingUtilities.invokeLater(() -> new PlayerAction(redModel, greenModel));
    }
}
