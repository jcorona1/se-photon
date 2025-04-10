// PlayerAction.java (no music logic here)
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class PlayerAction extends JFrame {
    static int minutes = 6;
    static int seconds = 0;
    Timer gameTimer = new Timer();

    public PlayerAction(DefaultTableModel redTeamModel, DefaultTableModel greenTeamModel) {
        DefaultTableModel redTableModel = duplicateTableModel(redTeamModel);
        DefaultTableModel greenTableModel = duplicateTableModel(greenTeamModel);

        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new GridLayout(1, 3));

        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(new Color(139, 0, 0));
        JTable redTable = new JTable(redTableModel);
        JScrollPane redScrollPane = new JScrollPane(redTable);
        redScrollPane.setPreferredSize(new Dimension(400, 300));
        leftPanel.add(redScrollPane);

        JPanel middlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        middlePanel.setBackground(Color.BLACK);
        JLabel timeRemaining = new JLabel();
        timeRemaining.setFont(new Font("Arial", Font.BOLD, 16));
        timeRemaining.setForeground(Color.WHITE);

        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(new Color(0, 100, 0));
        JTable greenTable = new JTable(greenTableModel);
        JScrollPane greenScrollPane = new JScrollPane(greenTable);
        greenScrollPane.setPreferredSize(new Dimension(400, 300));
        rightPanel.add(greenScrollPane);

        add(leftPanel);
        add(middlePanel);
        add(rightPanel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        gameTimer.scheduleAtFixedRate(new TimerTask() {
            public void run () {
                String timerMessage; 
                if (seconds == -1) {
                    minutes--;
                    seconds = 59;
                }
                if (minutes == -1) {
                    gameTimer.cancel();
                    minutes = 6;
                    seconds = 0;

                    try {
                        DatagramSocket udpSocket = new DatagramSocket();
                        udpSocket.setBroadcast(true);
                        String message = "221";
                        byte[] buffer = message.getBytes();
                        InetAddress broadcastAddress = InetAddress.getByName(UdpClient.getBroadcastAddress());
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, broadcastAddress, 7500);
                        udpSocket.send(packet);
                        udpSocket.send(packet);
                        udpSocket.send(packet);
                        udpSocket.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    JDialog closeDialog = new JDialog();
                    closeDialog.setSize(400, 300);
                    closeDialog.setLayout(new GridLayout(2, 1));
                    closeDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                    closeDialog.setLocationRelativeTo(null);
                    closeDialog.setVisible(true);

                    JLabel closeDialogMessage = new JLabel("The maneuver has completed.");
                    JButton closeButton = new JButton("Click here to return to Player Entry Screen.");
                    closeButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            dispose();
                            closeDialog.dispose();
                        }
                    });

                    closeDialog.add(closeDialogMessage);
                    closeDialog.add(closeButton);
                }

                if (seconds <= 9) {
                    timerMessage = "Time Remaining: " + minutes + ":0" + seconds;
                } else {
                    timerMessage = "Time Remaining: " + minutes + ":" + seconds;
                }
                timeRemaining.setText(timerMessage);
                middlePanel.add(timeRemaining);
                seconds--;
            }
        }, 0, 1000);
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

    public static void main(String[] args) {
        DefaultTableModel redModel = new DefaultTableModel(new Object[]{"Red Team"}, 0);
        redModel.addRow(new Object[]{"Alice"});
        redModel.addRow(new Object[]{"Bob"});

        DefaultTableModel greenModel = new DefaultTableModel(new Object[]{"Green Team"}, 0);
        greenModel.addRow(new Object[]{"Carol"});
        greenModel.addRow(new Object[]{"Dave"});

        SwingUtilities.invokeLater(() -> new PlayerAction(redModel, greenModel));
    }
}