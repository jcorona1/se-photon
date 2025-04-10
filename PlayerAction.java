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
    /*
    static int minutes = 6;
    static int seconds = 0;
    */
    static int minutes = 0;
    static int seconds = 5;
    Timer gameTimer = new Timer();

    private JLabel receivedMessageLabel = new JLabel("Waiting for messages...");

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

        // Middle Panel (Message + Timer)
        JPanel middlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        middlePanel.setBackground(Color.BLACK);
        receivedMessageLabel.setForeground(Color.WHITE);
        receivedMessageLabel.setFont(new Font("Arial", Font.BOLD, 18));
        middlePanel.add(receivedMessageLabel);

        JLabel timeRemaining = new JLabel();
        timeRemaining.setFont(new Font("Arial", Font.BOLD, 16));
        timeRemaining.setForeground(Color.WHITE);
        middlePanel.add(timeRemaining);

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

        // Start game timer
        startTimer(timeRemaining);

        // Start UDP listener
        startUdpListener();
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

    private void startUdpListener() {
        new Thread(() -> {
            try {
                DatagramSocket ds = new DatagramSocket(7501);
                ds.setBroadcast(true);
                byte[] receive = new byte[65535];

                while (true) {
                    DatagramPacket dpReceive = new DatagramPacket(receive, receive.length);
                    ds.receive(dpReceive);

                    String message = data(receive).toString();
                    System.out.println("Received UDP Message: " + message);

                    SwingUtilities.invokeLater(() -> {
                        receivedMessageLabel.setText("Received: " + message);
                    });

                    receive = new byte[65535];
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
