import java.net.*;
import java.io.*;
import java.util.Arrays;

public class EchoServerUDP {
    // Initialize sum as 0
    static int sum = 0;

    public static void main(String[] args) {
        // Inform that the server is starting
        System.out.println("Server Running");
        // Declare an empty socket for sending and receiving datagram packets
        DatagramSocket aSocket = null;
        // Create a buffer of bytes of length 11
        // (maximum integer in Java is of length 10. So we don't need a longer buffer for this task)
        byte[] buffer = new byte[11];
        try {
            // Set port 6789
            aSocket = new DatagramSocket(6789);
            // Form a datagram packet of those bytes
            DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length);
            while (true) { // Keep the server running
                // Receive message from client
                aSocket.receive(requestPacket);
                // First, copy response to an array with the correct number of bytes, so that the extra spaces are removed
                // Then, convert that response into an integer
                sum += Integer.parseInt(new String(Arrays.copyOf(requestPacket.getData(), requestPacket.getLength())));
                // Print the updated sum
                System.out.println("Updated sum: " + sum);
                // Convert the number to be sent to the server into bytes
                byte[] replyBytes = ("" + sum).getBytes();
                // Form a datagram packet of those bytes
                DatagramPacket reply = new DatagramPacket(replyBytes, replyBytes.length, requestPacket.getAddress(), requestPacket.getPort());
                // Send that packet to the server over UDP
                aSocket.send(reply);
            }
        } catch (SocketException e) {
            // Handle failed socket connection
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            // Handle IO exceptions
            System.out.println("IO: " + e.getMessage());
        } catch (NumberFormatException e) {
            // Handle number conversion exceptions
            System.out.println("Bad integer received from server.");
        } finally {
            // If the socket is still open, close it while exiting
            if (aSocket != null) aSocket.close();
        }
    }
}
