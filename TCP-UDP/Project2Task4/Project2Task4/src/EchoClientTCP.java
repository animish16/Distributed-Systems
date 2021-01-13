import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Scanner;

public class EchoClientTCP {
    // Reusable array of operations for which we need 3rd input from user
    static String[] validOperations = {"add", "addition", "sub", "subtract", "subtraction"};

    /**
     * add() method proceeds as follows:
     * 1. takes in operation id, operation to perform and the number
     * 2. establishes connection with the server
     * 3. sends the appropriate request to the server via TCP
     * 4. Receives and validate response from the server
     * 5. Returns the response from the server
     *
     * @param id: an integer (converted to string) client id;
     *            operation: the operation to be performed (add, subtract, view);
     *            n: number (in case of add and subtract)
     * @return an integer representing the running sum received from the server
     */
    static String add(String id, String operation, String n) {
        // Declare an empty socket for communication
        Socket clientSocket = null;
        // Declare an empty string which will hold the response received from server
        String receivedResponse = null;

        // Form request as id,operation[,number]
        String request = id + "," + operation.toLowerCase();
        if (Arrays.asList(validOperations).contains(operation.toLowerCase())) {
            // If it is addition/subtraction, append the number to the request
            request = request + "," + n;
        }

        try {
            // Set port for communication as 7777
            int serverPort = 7777;
            // Initialize the socket over localhost (127.0.0.1)
            clientSocket = new Socket("localhost", serverPort);
            // Initialize PrintWriter object to send response to the server
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));
            // Send neatly formatted request
            out.println(request);
            // Flush out object
            out.flush();

            // Initialize BufferedReader object to receive response from the server
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            // Receive response from the server
            receivedResponse = in.readLine();
        } catch (SocketException e) {
            // Handle failed socket connection
            System.out.println("Socket: " + e.getMessage());
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
        // Return received response
        return receivedResponse;
    }

    public static void main(String[] args) {
        // Inform that the client is starting
        System.out.println("Client Running");

        while (true) { // Keep the client running
            // Initialize scanner to take user typed inputs
            Scanner input = new Scanner(System.in);

            // Get and validate ID
            System.out.println("\nEnter ID:");
            String id = input.nextLine();
            try {
                // Check if ID entered is an integer and also positive
                int i = Integer.parseInt(id);
                if (i < 0) {
                    // Inform user that ID should be a positive integer
                    System.out.println("ERROR: Enter a positive integer ID and try again.");
                    // Get out of current iteration in the loop
                    continue;
                }
            } catch (NumberFormatException e) {
                // Inform user that ID should be an integer
                System.out.println("ERROR: Enter a valid integer ID and try again.");
                // Get out of current iteration in the loop
                continue;
            }

            // Get and validate operation to be performed
            System.out.println("Enter an operation to perform (add/subtract/view):");
            String operation = input.nextLine();
            if (!Arrays.asList(validOperations).contains(operation.toLowerCase())
                    && !operation.equalsIgnoreCase("view")) {
                // Inform user that he/she needs to enter a valid operation
                System.out.println("ERROR: Enter a valid operation and try again.");
                // Get out of current iteration in the loop
                continue;
            }

            // Get and validate number (add/subtract only)
            String n = null;
            if (Arrays.asList(validOperations).contains(operation.toLowerCase())) {
                System.out.println("Enter number:");
                n = input.nextLine();
                try {
                    // Check if ID entered is an integer
                    Integer.parseInt(n);
                } catch (NumberFormatException e) {
                    // Inform user that the number should be an integer
                    System.out.println("ERROR: Enter a valid number to perform operation and try again.");
                    // Get out of current iteration in the loop
                    continue;
                }
            }

            // Show server response
            System.out.println("Server Response:\n" + add(id, operation, n));
        }
    }
}
