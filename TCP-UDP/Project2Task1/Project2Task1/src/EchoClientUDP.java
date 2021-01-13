// Import necessary libraries

import java.net.*;
import java.io.*;
import java.util.Arrays;

public class EchoClientUDP {
    public static void main(String[] args) {
        // Inform that the client is starting
        System.out.println("Client Running");
        // Declare an empty socket for sending and receiving datagram packets
        DatagramSocket aSocket = null;
        try {
            // Declare IP address as localhost (127.0.0.1)
            InetAddress aHost = InetAddress.getByName("localhost");
            // Set port 6789
            int serverPort = 6789;
            // Initialize the UDP socket for sending and receiving datagram packets
            aSocket = new DatagramSocket();
            // String to hold each received line
            String nextLine;
            // Get the user input line
            BufferedReader typed = new BufferedReader(new InputStreamReader(System.in));
            // Assign typed user line to nextLine variable and run the loop till ic becomes null
            while ((nextLine = typed.readLine()) != null) {
                // Convert typed line to bytes array
                byte[] m = nextLine.getBytes();
                // Form a datagram packet of those bytes
                DatagramPacket requestPacket = new DatagramPacket(m, m.length, aHost, serverPort);
                // Send that packet to the server over UDP
                aSocket.send(requestPacket);

                // If the typed line is "quit!", exit client
                if (nextLine.equalsIgnoreCase("quit!")) {
                    // Print quitting message
                    System.out.println("Client quitting!");
                    // Close the socket
                    if (aSocket != null) aSocket.close();
                    // Exit the loop so that the program will exit
                    break;
                }

                // Create a buffer of bytes of length 1000
                byte[] buffer = new byte[1000];
                // Form an empty packet with the buffer
                DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                // Receive reply from the server
                aSocket.receive(reply);
                // Copy response to an array with the correct number of bytes, so that the extra spaces are removed
                byte[] rec = Arrays.copyOf(reply.getData(), reply.getLength());
                // Form a string from the array of bytes
                String receivedString = new String(rec);
                // Show the response from the server
                System.out.println("Reply: " + receivedString);
            }
        } catch (SocketException e) {
            // Handle failed socket connection
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            // Handle IO exceptions
            System.out.println("IO: " + e.getMessage());
        } finally {
            // If the socket is still open, close it while exiting
            if (aSocket != null) aSocket.close();
        }
    }
}
