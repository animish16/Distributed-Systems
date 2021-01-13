import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class EchoServerTCP {
    // Static socket and port for communication
    static Socket clientSocket = null;
    static int serverPort = 7777;
    // Map to hold ID and its associated number
    static Map<String, Integer> valueMap = new HashMap<>();

    /**
     * reply() is a reusable method for sending reply to client. The method:
     * 1. takes in string to be replied
     * 2. sends reply message to the client
     * 3. handles any exceptions
     *
     * @param toReply: string to be replied to the client.
     */
    static void reply(String toReply) {
        try {
            // Initialize PrintWriter object to send response to the server
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));
            // Send neatly formatted request
            out.println(toReply);
            // Flush out object
            out.flush();
        } catch (IOException e) {
            // Handle IO exceptions
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Inform that the server is starting
        System.out.println("Server Running");
        try {
            // Establish connection
            ServerSocket listenSocket = new ServerSocket(serverPort);
            while (true) {
                // Get the request from client
                clientSocket = listenSocket.accept();
                Scanner in = new Scanner(clientSocket.getInputStream());
                // Split the request string using commas
                String[] request = in.nextLine().split(",");

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
        } catch (IOException e) {
            // Handle IO exceptions
            System.out.println("IO: " + e.getMessage());
        } finally {
            // Attempt to close socket
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                // ignore exception on close
            }
        }
    }
}
