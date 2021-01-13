import java.net.*;
import java.io.*;
import java.util.Arrays;

public class EchoClientUDP {

    /**
     * add() method proceeds as follows:
     * 1. takes in an integer n
     * 2. establishes connection with the server
     * 3. sends the number to the server via UDP
     * 4. Receives and validate response from the server
     * 5. Returns the sum
     *
     * @param n a number to be added to the grand sum
     * @return an integer representing the running sum received from the server
     */
    static int add(int n) {
        // Initialize the sum integer
        int receivedSum = -1;
        // Declare an empty socket for sending and receiving datagram packets
        DatagramSocket aSocket = null;
        try {
            // Declare IP address as localhost (127.0.0.1)
            InetAddress aHost = InetAddress.getByName("localhost");
            // Set port 6789
            int serverPort = 6789;
            // Initialize the UDP socket for sending and receiving datagram packets
            aSocket = new DatagramSocket();
            // Convert the number to be sent to the server into bytes
            byte[] m = ("" + n).getBytes();
            // Form a datagram packet of those bytes
            DatagramPacket requestPacket = new DatagramPacket(m, m.length, aHost, serverPort);
            // Send that packet to the server over UDP
            aSocket.send(requestPacket);

            // Create a buffer of bytes of length 11
            // (maximum integer in Java is of length 10. So we don't need a longer buffer for this task)
            byte[] buffer = new byte[11];
            // Form an empty packet with the buffer
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            // Receive reply from the server
            aSocket.receive(reply);
            // First, copy response to an array with the correct number of bytes, so that the extra spaces are removed
            // Then, convert that response into an integer
            receivedSum = Integer.parseInt(new String(Arrays.copyOf(reply.getData(), reply.getLength())));
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
        // Return the received running sum from the server
        return receivedSum;
    }

    public static void main(String[] args) {
        // Inform that the client is starting
        System.out.println("Client Running");
        // Initialize sum as 0
        int runningSum = 0;

        // Add numbers from 1 to 1000
        for (int i = 1; i <= 1000; i++) {
            // Call add function with the current number in the for loop
            runningSum = add(i);
            // If there was something wrong, -1 will be returned
            if (runningSum == -1) {
                // Print an appropriate message to inform failure in the current iteration
                System.out.println("Error occurred while summing " + i);
            }
        }

        // Show the sum to the client
        System.out.println("Final sum: " + runningSum);
    }
}
