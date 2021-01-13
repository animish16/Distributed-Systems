// Import necessary libraries

import java.net.*;
import java.io.*;
import java.util.Arrays;

public class EchoServerUDP {
    public static void main(String args[]) {
        // Inform that the server is starting
        System.out.println("Server Running");
        // Declare an empty socket for sending and receiving datagram packets
        DatagramSocket aSocket = null;
        // Create a buffer of bytes of length 1000
        byte[] buffer = new byte[1000];
        try {
            // Set port 6789
            aSocket = new DatagramSocket(6789);
            // Form a datagram packet of those bytes
            DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length);
            while (true) { // Keep the server running
                // Receive message from client
                aSocket.receive(requestPacket);
                // Copy response to an array with the correct number of bytes, so that the extra spaces are removed
                byte[] rec = Arrays.copyOf(requestPacket.getData(), requestPacket.getLength());
                // Form a string from the array of bytes
                String receivedString = new String(rec);
                // Show the message from client
                System.out.println("Echoing: " + receivedString);

                // If client sends "quit!", exit
                if (receivedString.equalsIgnoreCase("quit!")) {
                    // Print quitting message
                    System.out.println("Server quitting!");
                    // Close the socket
                    if (aSocket != null) aSocket.close();
                    // Exit the loop so that the program will exit
                    break;
                }
                // Form an empty packet of the length of the reply to be sent, and assigned with the appropriate address and port number
                DatagramPacket reply = new DatagramPacket(rec, rec.length, requestPacket.getAddress(), requestPacket.getPort());
                // Send reply (echo client's own text)
                aSocket.send(reply);
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
