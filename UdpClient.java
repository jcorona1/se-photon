import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class UdpClient {

    public static void main(String[] args) {
        // Define the broadcast address and port
        String broadcastAddress = "127.0.0.1"; // Broadcast address
        int port = 7500; // Port to broadcast on

        try (Scanner scanner = new Scanner(System.in)) {
            // Create a DatagramSocket
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true); // Enable broadcast

            while (true) {
                // Prompt the user for a message to broadcast
                System.out.print("Enter a message to broadcast: ");
                String message = scanner.nextLine(); // Read user input

                if (message.equalsIgnoreCase("exit")) {
                    break; // Exit the loop if the user types "exit".
                }

                byte[] buffer = message.getBytes();

                // Create a DatagramPacket with the broadcast address and port
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(broadcastAddress), port);

                // Send the packet
                socket.send(packet);
                System.out.println("Broadcast message sent: " + message);
				
		if (message.equals("221"))
		{
			break; // Exit the loop once the exit code has been broadcast.
		}
            }

            // Close the socket
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
