import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class EchoClientTCP {
    // Reusable array of operations for which we need 3rd input from user
    static String[] validOperations = {"add", "addition", "sub", "subtract", "subtraction"};
    // Reusable RSA components
    // e is the exponent of the public key
    // d is the exponent of the private key
    // n is the modulus for both the private and public keys
    static BigInteger e, d, n;

    /**
     * generateKeys() method takes in a starting BigInteger and generates RSA keys
     *
     * @param publicKeyInitializer: a BigInteger object containing a 32 bits prime number
     */
    static void generateKeys(BigInteger publicKeyInitializer) {
        // Each public and private key consists of an exponent and a modulus
        Random rnd = new Random();

        // Step 1: Generate two large random primes of 2048 bits
        BigInteger p = new BigInteger(2048, 100, rnd);
        BigInteger q = new BigInteger(2048, 100, rnd);

        // Step 2: Compute n by the equation n = p * q
        n = p.multiply(q); // n is the modulus for both the private and public keys

        // Step 3: Compute phi(n) = (p-1) * (q-1)
        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));

        // Step 4: Select a small odd integer e that is relatively prime to phi(n)
        /// "e" will always have length 10 because it is a strict 32 bit number
        e = publicKeyInitializer; // e is the exponent of the public key

        // Step 5: Compute d as the multiplicative inverse of e modulo phi(n)
        d = e.modInverse(phi); // d is the exponent of the private key
    }

    // code from Stack overflow
    // converts a byte array to a string
    static String convertToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfByte = (data[i] >>> 4) & 0x0F;
            int twoHalfs = 0;
            do {
                if ((0 <= halfByte) && (halfByte <= 9))
                    buf.append((char) ('0' + halfByte));
                else
                    buf.append((char) ('a' + (halfByte - 10)));
                halfByte = data[i] & 0x0F;
            } while (twoHalfs++ < 1);
        }
        return buf.toString();
    }

    /**
     * ComputeSHA_256_as_Hex_String() method takes in a string and returns its hex SHA 256 digest
     *
     * @param text: string to be hashed
     * @return hashed SHA 256 hex string
     */
    static String ComputeSHA_256_as_Hex_String(String text) {
        try {
            // Create a SHA256 digest
            MessageDigest digest;
            digest = MessageDigest.getInstance("SHA-256");
            // allocate room for the result of the hash
            byte[] hashBytes;
            // perform the hash
            digest.update(text.getBytes(StandardCharsets.UTF_8), 0, text.length());
            // collect result
            hashBytes = digest.digest();
            return convertToHex(hashBytes);
        } catch (NoSuchAlgorithmException nsa) {
            System.out.println("No such algorithm exception thrown " + nsa);
        }
        return null;
    }

    /**
     * Signing proceeds as follows:
     * 1) Get the bytes from the string to be signed.
     * 2) Compute a SHA-1 digest of these bytes.
     * 3) Copy these bytes into a byte array that is one byte longer than needed.
     * The resulting byte array has its extra byte set to zero. This is because
     * RSA works only on positive numbers. The most significant byte (in the
     * new byte array) is the 0'th byte. It must be set to zero.
     * 4) Create a BigInteger from the byte array.
     * 5) Encrypt the BigInteger with RSA d and n.
     * 6) Return to the caller a String representation of this BigInteger.
     *
     * @param message a sting to be signed
     * @return a string representing a big integer - the encrypted hash.
     */
    static String sign(String message) throws Exception {
        // compute the digest with SHA-256
        byte[] bytesOfMessage = message.getBytes(StandardCharsets.UTF_8);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] messageDigest = md.digest(bytesOfMessage);

        byte[] bigDigest = new byte[messageDigest.length + 1];
        // Add a 0 byte as the most significant byte to keep the value to be signed non-negative
        bigDigest[0] = 0;
        System.arraycopy(messageDigest, 0, bigDigest, 1, messageDigest.length);

        // From the digest, create a BigInteger
        BigInteger m = new BigInteger(bigDigest);

        // encrypt the digest with the private key
        BigInteger c = m.modPow(d, n);

        // return this as a big integer string
        return c.toString();
    }

    /**
     * add() method proceeds as follows:
     * 1. takes in client id, operation to perform and the number
     * 2. establishes connection with the server
     * 3. sends the appropriate request to the server via TCP
     * 4. Receives and validate response from the server
     * 5. Returns the response from the server
     *
     * @param id: a 40 character long client id;
     *            operation: the operation to be performed (add, subtract, view);
     *            n: number (in case of add and subtract)
     * @return an integer representing the running sum received from the server
     */
    static String add(String id, String operation, String num) {
        // Declare an empty socket for communication
        Socket clientSocket = null;
        // Declare an empty string which will hold the response received from server
        String receivedResponse = null;

        // Request formation: id,e+n,operation[,operand],sign
        /// "e" will always have length 10 because it is a strict 32 bit number
        ///     so, no comma needed to separate e and n
        String request = id + "," + e.toString() + n.toString() + "," + operation.toLowerCase();
        if (Arrays.asList(validOperations).contains(operation.toLowerCase())) {
            // If it is addition/subtraction, append the number to the request
            request = request + "," + num;
        }

        // Signing
        // Try generating the signature
        String requestSign = null;
        try {
            requestSign = sign(request);
        } catch (Exception ex) {
            // Handle any exception occurred
            ex.printStackTrace();
        }

        // Append signature to the request
        request = request + "," + requestSign;

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

        // Form a 32 bit public key initializer
        // We are keeping this piece here so that the client can decide what the starting point should be
        // This will also be the "e" component of RSA
        /// "e" will always have length 10 because it is a strict 32 bit number
        ///     so, no comma needed to separate e and n
        BigInteger publicKeyInitializer = new BigInteger(32, 100, (new Random()));
        // Generate "d" and "n" components of RSA based on the "e" or publicKeyInitializer object
        generateKeys(publicKeyInitializer);

        // Public key: e+n
        String publicKey = e.toString() + n.toString();
        // Private key: d
        String privateKey = d.toString();

        // Display the keys
        System.out.println("Public key (e+n): " + publicKey);
        System.out.println(" Private key (d): " + privateKey);

        // Find the hash of public key
        String hash = ComputeSHA_256_as_Hex_String(publicKey);
        // Keep only its least 20 significant bytes as client ID
        String clientID = hash != null ? hash.substring(hash.length() - 40) : null;
        // Display client ID
        System.out.println("       Client ID: " + clientID);

        while (true) { // Keep the client running
            // Initialize scanner to take user typed inputs
            Scanner input = new Scanner(System.in);

            // Get and validate operation to be performed
            System.out.println("\nEnter an operation to perform (add/subtract/view/exit):");
            String operation = input.nextLine();
            if (!Arrays.asList(validOperations).contains(operation.toLowerCase())
                    && !operation.equalsIgnoreCase("view")
                    && !operation.equalsIgnoreCase("exit")) {
                // Inform user that he/she needs to enter a valid operation
                System.out.println("ERROR: Enter a valid operation and try again.");
                // Get out of current iteration in the loop
                continue;
            }

            // If user enters exit, exit the client
            if (operation.equalsIgnoreCase("exit")) {
                System.out.println("Client closing. Goodbye!");
                // Exit the while loop
                break;
            }

            // Get and validate number (add/subtract only)
            String n = null;
            if (Arrays.asList(validOperations).contains(operation.toLowerCase())) {
                System.out.println("Enter number:");
                n = input.nextLine();
                try {
                    // Check if ID entered is an integer
                    Integer.parseInt(n);
                } catch (NumberFormatException ex) {
                    // Inform user that the number should be an integer
                    System.out.println("ERROR: Enter a valid number to perform operation and try again.");
                    // Get out of current iteration in the loop
                    continue;
                }
            }

            // Show server response
            System.out.println("Server Response:\n" + add(clientID, operation, n));
        }
    }
}
