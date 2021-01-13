import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class EchoServerUDP {
    // Static socket for communication
    static DatagramSocket aSocket;
    // Datagram packets for receiving request and sending reply
    static DatagramPacket requestPacket, reply;
    // Map to hold ID and its associated number
    static Map<String, Integer> valueMap = new HashMap<>();

    /**
     * reply() is a reusable method for sending reply to client. The method:
     * 1. takes in string to be replied
     * 2. form its appropriate datagram packet
     * 3. send reply message to the client
     *
     * @param toReply: string to be replied to the client.
     */
    static void reply(String toReply) {
        // Form an array of bytes out of the reply string
        byte[] replyBytes = toReply.getBytes();
        // Form a datagram packet of those bytes
        reply = new DatagramPacket(replyBytes, replyBytes.length, requestPacket.getAddress(), requestPacket.getPort());
        try {
            // Send that packet to the client over UDP
            aSocket.send(reply);
        } catch (IOException e) {
            // Handle IO exception
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Inform that the server is starting
        System.out.println("Server Running");
        // Create a buffer of bytes
        byte[] buffer = new byte[50];
        try {
            // Initialize the UDP socket for sending and receiving datagram packets
            aSocket = new DatagramSocket(6789);
            // Form a datagram packet of buffer bytes
            requestPacket = new DatagramPacket(buffer, buffer.length);

            while (true) {
                // Receive reply from the client
                aSocket.receive(requestPacket);
                // Copy response to an array with the correct number of bytes, so that the extra spaces are removed
                // The, split the string using commas
                String[] request = new String(Arrays.copyOf(requestPacket.getData(), requestPacket.getLength())).split(",");

                // From the request, separate out ID, operation [and number]
                String id = request[0];
                String operation = request[1];
                String num = null;

                // Check if ID is blank and send an appropriate response
                if (id.length() == 0) {
                    reply("ERROR: Enter an ID and try again.");
                    continue;
                }
                try {
                    // Check if ID is a positive integer and send an appropriate response
                    int i = Integer.parseInt(id);
                    if (i < 0) {
                        reply("ERROR: Enter a positive integer ID and try again.");
                        continue;
                    }
                } catch (NumberFormatException e) {
                    // The ID is not an integer
                    reply("ERROR: Enter a valid integer ID and try again.");
                    continue;
                }

                if (request.length > 2) {
                    // If request has 3 components, it is add/subtract operation
                    // Fetch the number to be added/subtracted
                    num = request[2];
                    // Check if it is a valid integer and send an appropriate response
                    try {
                        Integer.parseInt(num);
                    } catch (NumberFormatException e) {
                        reply("ERROR: Enter a valid number to perform operation and try again.");
                        continue;
                    }
                }

                // If we don'e have entry with the passed ID, make an entry with associated number 0
                if (!valueMap.containsKey(id)) {
                    valueMap.put(id, 0);
                }

                // Get the number associated with the ID to be processed further
                int operand = valueMap.get(id);

                // Addition operation
                if (operation.equalsIgnoreCase("add") ||
                        operation.equalsIgnoreCase("addition")) {
                    valueMap.put(id, operand + Integer.parseInt(num));
                    reply("OK"); // Send OK
                } // Subtraction operation
                else if (operation.equalsIgnoreCase("sub") ||
                        operation.equalsIgnoreCase("subtract") ||
                        operation.equalsIgnoreCase("subtraction")) {
                    valueMap.put(id, operand - Integer.parseInt(num));
                    reply("OK"); // Send OK
                } else { // View operation, by default
                    // Send the actual number associated with the ID
                    reply(String.valueOf(valueMap.get(id)));
                }
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
