import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlayerAction extends JFrame {
    static int minutes = 6;
    static int seconds = 0;

    java.util.Timer gameTimer = new Timer();
    String udpMessage;
    private JTextArea eventLog;

    DefaultTableModel redTableModel;
    DefaultTableModel greenTableModel;
    private HashMap<String, String> equipmentIdToCodename;

    private JTable redTable;
    private JTable greenTable;

    private TableRowSorter<DefaultTableModel> redSorter;
    private TableRowSorter<DefaultTableModel> greenSorter;

    javax.swing.Timer flashTimer;
    private int redFlashingRow;
    private int greenFlashingRow;
    private boolean flashing = false;

    /**
     * Constructor that accepts the red and green team table models
     * from PlayerEntry, duplicates them, and uses them to populate the UI.
     */
    public PlayerAction(DefaultTableModel redTeamModel, DefaultTableModel greenTeamModel, HashMap equipmentIdToCodename) {
        // Duplicate the provided models and HashMap so changes here don't affect PlayerEntry.
        this.redTableModel = duplicateTableModel(redTeamModel);
        this.greenTableModel = duplicateTableModel(greenTeamModel);
        this.equipmentIdToCodename = new HashMap<String, String>(equipmentIdToCodename);

        // Add B column as the first column
        insertColumnAtFront(redTableModel, "Has Hit Base");
        insertColumnAtFront(greenTableModel, "");

        // Adds score column to table models
        redTableModel.addColumn("Score");
        greenTableModel.addColumn("Score");

        // Set up the full-screen window
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new GridLayout(1, 3));

        // Create the left panel (dark red) with a centered table
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(new Color(139, 0, 0)); // Dark red
        this.redTable = new JTable(redTableModel);
        JScrollPane redScrollPane = new JScrollPane(this.redTable);
        redScrollPane.setPreferredSize(new Dimension(400, 300)); // Adjust as needed
        leftPanel.add(redScrollPane);

        // Middle Panel (Message + Timer + Event log)
        JPanel middlePanel = new JPanel(new GridBagLayout());
        middlePanel.setBackground(Color.BLACK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        // Create a label to display the time remaining
        JLabel timeRemaining = new JLabel();
        timeRemaining.setFont(new Font("Arial", Font.BOLD, 16));
        timeRemaining.setForeground(Color.WHITE);
        middlePanel.add(timeRemaining, gbc); // Add the label once here

        // Event log
        eventLog = new JTextArea(10, 30);
        eventLog.setEditable(false);
        eventLog.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane eventScroll = new JScrollPane(eventLog);
        eventScroll.setPreferredSize(new Dimension(400, 200));
        gbc.gridy = 1;
        middlePanel.add(eventScroll, gbc);

        // Create the right panel (dark green) with a centered table
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(new Color(0, 100, 0)); // Dark green
        this.greenTable = new JTable(greenTableModel);
        JScrollPane greenScrollPane = new JScrollPane(this.greenTable);
        greenScrollPane.setPreferredSize(new Dimension(400, 300)); // Adjust as needed
        rightPanel.add(greenScrollPane);

        // redTable sorter
        this.redSorter = new TableRowSorter<>(redTableModel);
        redTable.setRowSorter(redSorter);
        sortByScore(redSorter, redTableModel);

        // greenTable sorter
        this.greenSorter = new TableRowSorter<>(greenTableModel);
        greenTable.setRowSorter(greenSorter);
        sortByScore(greenSorter, greenTableModel);

        // Flash renderers
        redTable.setDefaultRenderer(Object.class, new FlashRenderer());
        greenTable.setDefaultRenderer(Object.class, new FlashRenderer());

        // Hide equipment ID tables
        hideColumn(this.redTable, 2);
        hideColumn(this.greenTable, 2);

        // Add panels to the frame
        add(leftPanel);
        add(middlePanel);
        add(rightPanel);

        // Display the window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        // Start UDP listener
        listen();

        // Start game timer
        startTimer(timeRemaining);

        // Start flash renderer
        startFlashing();
    }

    private void startTimer(JLabel timeRemaining) {
        gameTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                String timerMessage;
                if (seconds == -1) {
                    minutes--;
                    seconds = 59;
                }
                if (minutes == -1) {
                    gameTimer.cancel();
                    minutes = 6;
                    seconds = 0;

                    UdpClient.broadcastMessage("221");

		            // Show the dialog on the Event Dispatch Thread (EDT)
                    SwingUtilities.invokeLater(() -> {
			            // Create the dialog
                        JDialog closeDialog = new JDialog();
                        closeDialog.setSize(400, 300);
                        closeDialog.setLayout(new GridLayout(2, 1));
                        closeDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                        closeDialog.setLocationRelativeTo(null); // Center the dialog

			            // Add the message and button
                        JLabel closeDialogMessage = new JLabel("The maneuver has completed.");
                        JButton closeButton = new JButton("Click here to return to Player Entry Screen.");
                        closeButton.addActionListener(e -> {
                            dispose();
                            closeDialog.dispose();
                        });

                        closeDialog.add(closeDialogMessage);
                        closeDialog.add(closeButton);

                        // Pack and make it visible
                        closeDialog.pack(); // Ensures proper sizing
                        closeDialog.setVisible(true);
                    });
                }

                timerMessage = String.format("Time Remaining: %d:%02d", minutes, seconds);
		        // Update the timer message
                timeRemaining.setText(timerMessage);
                seconds--;
            }
        }, 0, 1000);        
    }

    private void listen() {
        new Thread(() -> {
            try {
                DatagramSocket socket = new DatagramSocket(7502);
                byte[] buffer = new byte[65535];
                
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    String udpMessage = new String(packet.getData(), 0, packet.getLength());
                    System.out.println("Received message in PlayerAction: " + udpMessage);

                    SwingUtilities.invokeLater(() -> {
                        if (udpMessage.contains(":"))
                        {
                            String[] equipmentIDs = udpMessage.split(":");
                            // Get the codename mapped to equipemntID into a String
                            String hitter = equipmentIdToCodename.get(equipmentIDs[0]);
                            String victim = equipmentIdToCodename.get(equipmentIDs[1]);

                            // Get data from models
                            Vector<Vector> redTeam = redTableModel.getDataVector();
                            Vector<Vector> greenTeam = greenTableModel.getDataVector();

                            // Determine what teams the hitter and victim are on
                            boolean hitterOnRed = isOnTeam(hitter, redTeam);
                            boolean victimOnRed;
                            if (equipmentIDs[1].equals("53"))
                            {
                                victimOnRed = true;
                            }
                            else if (equipmentIDs[1].equals("43"))
                            {
                                victimOnRed = false;
                            }
                            else
                            {
                                victimOnRed = isOnTeam(victim, redTeam);
                            }
                            boolean friendlyFire = (hitterOnRed == victimOnRed); // true if both booleans are true or both booleans are false
                            
                            // Determine points to give or take
                            int points = 0;
                            if (friendlyFire)
                            {
                                points = -10;
                            }
                            else
                            {
                                points = 10;
                            }

                            // If player hit base, add "B" to the leftmost column
                            if((equipmentIDs[1].equals("43") || equipmentIDs[1].equals("53")) && !friendlyFire)
                            {
                                // Call markBaseHit on each team
                                markBaseHit(hitter, redTeam, redTableModel);
                                markBaseHit(hitter, greenTeam, greenTableModel);
                            }
                            else
                            {
                                // Update scores on both teams
                                updateScore(hitter, redTeam, redTableModel, points);
                                updateScore(hitter, greenTeam, greenTableModel, points);
                            }
                            
                            // Scrolls with newest messages at the top
                            eventLog.insert(hitter + " hit " + victim + "\n", 0);
                            eventLog.setCaretPosition(0);
                        }
                    });

                    buffer = new byte[65535];
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Creates a duplicate of the given DefaultTableModel.
     * This method copies the column names and all row data.
     */
    private DefaultTableModel duplicateTableModel(DefaultTableModel original) {
        // Create a new model with the same column names
        DefaultTableModel duplicate = new DefaultTableModel();
        for (int col = 0; col < original.getColumnCount(); col++) {
            if (col == 0)
            {
                duplicate.addColumn("Player");
            }
            else
            {
                duplicate.addColumn(original.getColumnName(col));
            }
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

    /**
     * Searches through a given table model and updates player's score.
     * Repaints the table model to after updating score
     * @param hitter 
     * @param team
     * @param model
     */
    private void updateScore(String hitter, Vector<Vector> team, DefaultTableModel model, int pointValue)
    {
        // Search for hitter in team model
        for(int row = 0; row < team.size(); row++)
        {   
            Vector<Object> player = team.get(row);
            // If player is hitter, update score value
            if(player.contains(hitter))
            {   // If player has not scored yet
                if(player.get(3) == null)
                {
                    player.setElementAt(10, 3);
                }
                else
                {
                    player.setElementAt(Integer.parseInt(player.get(3).toString()) + pointValue, 3);
                }
                
                // Get current list of column names
                Vector<String> columnNames = new Vector<>();
                for (int i = 0; i < model.getColumnCount(); i++) {
                    columnNames.add(redTableModel.getColumnName(i));
                }
                // Update redTableModel to match team
                model.setDataVector(team, columnNames);
                
                // Repaint redTableModel in sidebar
                model.fireTableDataChanged();

                // Re-hide equipment id columns
                if (model == redTableModel)
                {
                    hideColumn(redTable, 2);
                    sortByScore(redSorter, redTableModel);
                } else if (model == greenTableModel) 
                {
                    hideColumn(greenTable, 2);
                    sortByScore(greenSorter, greenTableModel);
                } 

                break;
            }
        }
    }


    /**
     * Searches through the given table model to look for the hitter codename.
     * Prepends "B - " to codename if it is found.
     * Updates and repaints the table model after prepending "B - "
     * @param hitter
     * @param team
     * @param model
     */
    private void markBaseHit(String hitter, Vector<Vector> team, DefaultTableModel model)
    {
        // Search for hitter in team model
        for(int row = 0; row < team.size(); row++)
        {   
            Vector player = team.get(row);
            // If player is hitter, add "B - " to beginning of codename
            if(player.contains(hitter))
            {
                player.setElementAt("B", 0);
                // Get current list of column names
                Vector<String> columnNames = new Vector<>();
                for (int i = 0; i < model.getColumnCount(); i++) {
                    columnNames.add(redTableModel.getColumnName(i));
                }
                // Update redTableModel to match team
                model.setDataVector(team, columnNames);
                // Call updateScore on scoring team
                updateScore(hitter, team, model, 100);

                break;
            }
        }
    }

    private boolean isOnTeam(String codename, Vector<Vector> team) 
    {
        for (Vector player : team) {
            if (player.contains(codename)) {
                return true;
            }
        }
        return false;
    }

    public static StringBuilder data(byte[] a) 
    {
        if (a == null)
            return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (i < a.length && a[i] != 0) {
            ret.append((char) a[i]);
            i++;
        }
        
        return ret;
    }

    private void hideColumn(JTable table, int colIndex)
    {
        TableColumn column = table.getColumnModel().getColumn(colIndex);
        table.getColumnModel().removeColumn(column);
    }

    private void insertColumnAtFront(DefaultTableModel model, String columnName) 
    {
        int rowCount = model.getRowCount();
        Vector<Vector> data = model.getDataVector();

        // Add the new column data to the start of each row
        for (int i = 0; i < rowCount; i++) {
            data.get(i).add(0, null); // Insert a null (or default value) at the beginning of each row
        }

        // Update the column identifiers
        Vector<String> columnNames = new Vector<>();
        columnNames.add(columnName); // Add the new column first

        for (int i = 0; i < model.getColumnCount(); i++) {
            columnNames.add(model.getColumnName(i));
        }

        model.setDataVector(data, columnNames);
    }

    private void sortByScore(TableRowSorter<DefaultTableModel> sorter, DefaultTableModel model) 
    {
        int scoreColIndex = model.findColumn("Score");
        if (scoreColIndex != -1) {
            sorter.setComparator(scoreColIndex, (o1, o2) -> {
                Integer i1 = o1 == null ? 0 : Integer.parseInt(o1.toString());
                Integer i2 = o2 == null ? 0 : Integer.parseInt(o2.toString());
                return i1.compareTo(i2); // controls ordering
            });

            // Apply descending sort on the score column
            sorter.setSortKeys(java.util.List.of(new RowSorter.SortKey(scoreColIndex, SortOrder.DESCENDING)));
            sorter.sort();
        }
    }

    private class FlashRenderer extends DefaultTableCellRenderer {
        private Color flashColor;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
        {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            int modelRow = table.convertRowIndexToModel(row);

            if ((table == redTable && modelRow == redFlashingRow) || (table == greenTable && modelRow == greenFlashingRow)) 
            {
                if (flashing) {
                    if (table == redTable)
                    {
                        flashColor = Color.RED;
                    }
                    else // table == greenTable
                    {
                        flashColor = Color.GREEN;
                    }
                    c.setBackground(flashColor);
                } else {
                    c.setBackground(Color.WHITE);
                }
            } 
            else 
            {
                c.setBackground(Color.WHITE);
            }

            return c;
        }
    }

    // Finds which player currently has the highest score and sets their row to be blinking
    private void updateFlashingRow()
    {
        int highestScore = Integer.MIN_VALUE;
        redFlashingRow = -1;
        greenFlashingRow = -1;

        for (int i = 0; i < redTableModel.getRowCount(); i++) {
            Object scoreObj = redTableModel.getValueAt(i, redTableModel.findColumn("Score"));
            int score = (scoreObj == null ? 0 : Integer.parseInt(scoreObj.toString()));
            if (score > highestScore) 
            {
                highestScore = score;
                redFlashingRow = i;
                greenFlashingRow = -1; 
            }
        }

        for (int i = 0; i < greenTableModel.getRowCount(); i++) {
            Object scoreObj = greenTableModel.getValueAt(i, greenTableModel.findColumn("Score"));
            int score = (scoreObj == null ? 0 : Integer.parseInt(scoreObj.toString()));
            if (score > highestScore) 
            {
                highestScore = score;
                greenFlashingRow = i;
                redFlashingRow = -1;
            }
        }
    }

    private void startFlashing()
    {
        flashTimer = new javax.swing.Timer(500, e -> {
            updateFlashingRow(); // Recalculate top player
            flashing = !flashing; // Toggle blink on/off
            redTable.repaint();
            greenTable.repaint();
        });
        flashTimer.start();
    }

}
