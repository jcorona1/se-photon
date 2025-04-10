import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerAction extends JFrame {
    static int minutes = 6;
    static int seconds = 0;

    Timer gameTimer = new Timer();

    String udpMessage;

    private JTextArea eventLog;

    public PlayerAction(DefaultTableModel redTeamModel, DefaultTableModel greenTeamModel) {
        // Duplicate the provided models
        DefaultTableModel redTableModel = duplicateTableModel(redTeamModel);
        DefaultTableModel greenTableModel = duplicateTableModel(greenTeamModel);

        // Set up full-screen window
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new GridLayout(1, 3));

        // Left Panel (Red Team)
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(new Color(139, 0, 0));
        JTable redTable = new JTable(redTableModel);
        JScrollPane redScrollPane = new JScrollPane(redTable);
        redScrollPane.setPreferredSize(new Dimension(400, 300));
        leftPanel.add(redScrollPane);

        // Middle Panel (Message + Timer + Event log)
        JPanel middlePanel = new JPanel(new GridBagLayout());
        middlePanel.setBackground(Color.BLACK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        // Timer
        JLabel timeRemaining = new JLabel();
        timeRemaining.setFont(new Font("Arial", Font.BOLD, 16));
        timeRemaining.setForeground(Color.WHITE);
        middlePanel.add(timeRemaining, gbc);

        // Event log
        eventLog = new JTextArea(10, 30);
        eventLog.setEditable(false);
        eventLog.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane eventScroll = new JScrollPane(eventLog);
        eventScroll.setPreferredSize(new Dimension(400, 200));
        gbc.gridy = 1;
        middlePanel.add(eventScroll, gbc);

        // Right Panel (Green Team)
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(new Color(0, 100, 0));
        JTable greenTable = new JTable(greenTableModel);
        JScrollPane greenScrollPane = new JScrollPane(greenTable);
        greenScrollPane.setPreferredSize(new Dimension(400, 300));
        rightPanel.add(greenScrollPane);

        // Add panels to frame
        add(leftPanel);
        add(middlePanel);
        add(rightPanel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        // Start UDP listener
        listen();

        // Start game timer
        startTimer(timeRemaining);
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

                    SwingUtilities.invokeLater(() -> {
                        JDialog closeDialog = new JDialog();
                        closeDialog.setSize(400, 300);
                        closeDialog.setLayout(new GridLayout(2, 1));
                        closeDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                        closeDialog.setLocationRelativeTo(null);

                        JLabel closeDialogMessage = new JLabel("The maneuver has completed.");
                        JButton closeButton = new JButton("Click here to return to Player Entry Screen.");
                        closeButton.addActionListener(e -> {
                            dispose();
                            closeDialog.dispose();
                        });

                        closeDialog.add(closeDialogMessage);
                        closeDialog.add(closeButton);

                        closeDialog.pack();
                        closeDialog.setVisible(true);
                    });
                }

                timerMessage = String.format("Time Remaining: %d:%02d", minutes, seconds);
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
                            String hitter = "ID " + equipmentIDs[0];
                            String victim = "ID " + equipmentIDs[1];

                            if (victim == 53)
                            {
                                victim = "red base";
                            }
                            else if (victim == 43)
                            {
                                victim = "green base";
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

    private DefaultTableModel duplicateTableModel(DefaultTableModel original) {
        DefaultTableModel duplicate = new DefaultTableModel();
        for (int col = 0; col < original.getColumnCount(); col++) {
            duplicate.addColumn(original.getColumnName(col));
        }
        for (int row = 0; row < original.getRowCount(); row++) {
            Object[] rowData = new Object[original.getColumnCount()];
            for (int col = 0; col < original.getColumnCount(); col++) {
                rowData[col] = original.getValueAt(row, col);
            }
            duplicate.addRow(rowData);
        }
        return duplicate;
    }

    public static StringBuilder data(byte[] a) {
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
}
