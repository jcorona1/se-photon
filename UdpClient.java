import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class UdpClient {

    private static String broadcastAddress = "127.0.0.1"; // Broadcast address
    private static int port = 7500; // Port to broadcast to

    public static String getBroadcastAddress() {
        return broadcastAddress;
    }

    public static void setBroadcastAddress(String address) {
        broadcastAddress = address;
    }

    public static void broadcastMessage(String message) {
        try {
	    // Create a DatagramSocket
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true); // Enable broadcast

            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(getBroadcastAddress()), port);

            socket.send(packet);
            System.out.println(message);

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void broadcastMessageToPlayerAction(String message) {
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);

            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(getBroadcastAddress()), 7502);

            socket.send(packet);
            System.out.println(message);

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
    try (Scanner scanner = new Scanner(System.in)) {
        while (true) {
            System.out.print("Enter a message to broadcast: ");
            String message = scanner.nextLine();

            if (message.equalsIgnoreCase("exit") || message.equals("221")) {
                break;
            }

            broadcastMessage(message);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}
}