import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UdpServer
{
    public static void main(String[] args) throws IOException
	{
		// Step 1 : Create a socket to listen at port 7501
		DatagramSocket ds = new DatagramSocket(7501);
		ds.setBroadcast(true);
		byte[] receive = new byte[65535];

		DatagramPacket DpReceive = null;
		while (true)
		{

			// Step 2 : create a DatgramPacket to receive the data.
			DpReceive = new DatagramPacket(receive, receive.length);

			// Step 3 : recieve the data in byte buffer.
			ds.receive(DpReceive);
			String message = data(receive).toString();
			System.out.println("Client:-" + message);

			if (message.contains(":"))
			{
				// [0] = player transmitting, [1] = victim player
				String[] equipmentIDs = message.split(":");
				String hitter = equipmentIDs[0];
				String victim = equipmentIDs[1];

				UdpClient.broadcastMessage(victim);
				UdpClient.broadcastMessageToPlayerAction(message);
			}
			else
			{
				UdpClient.broadcastMessage(message);
			}

			// Exit the server if the client sends "bye"
			if (data(receive).toString().equals("bye"))
			{
				System.out.println("Client sent bye.....EXITING");
				break;
			}

			// Clear the buffer after every message.
			receive = new byte[65535];
		}
	}

	// A utility method to convert the byte array
	// data into a string representation.
	public static StringBuilder data(byte[] a)
	{
		if (a == null)
			return null;
		StringBuilder ret = new StringBuilder();
		int i = 0;
		while (a[i] != 0)
		{
			ret.append((char) a[i]);
			i++;
		}
		return ret;
	}
}