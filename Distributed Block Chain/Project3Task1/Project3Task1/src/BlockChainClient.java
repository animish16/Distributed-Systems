// Essential imports

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.InputMismatchException;
import java.util.Random;
import java.util.Scanner;

public class BlockChainClient {
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
    void generateKeys(BigInteger publicKeyInitializer) {
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

    /**
     * Converts byte array to a hexadecimal string
     * Code referenced from BabySign.java file
     *
     * @param dataToHex byte array
     * @return String with hex representation of the bytes passed
     */
    private String convertToHex(byte[] dataToHex) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < dataToHex.length; i++) {
            int halfByte = (dataToHex[i] >>> 4) & 0x0F;
            int twoHalfs = 0;
            do {
                if ((0 <= halfByte) && (halfByte <= 9))
                    buf.append((char) ('0' + halfByte));
                else
                    buf.append((char) ('a' + (halfByte - 10)));
                halfByte = dataToHex[i] & 0x0F;
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
    String ComputeSHA_256_as_Hex_String(String text) {
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
    String sign(String message) throws Exception {
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
     * Communicates with server to interact with the blockchain
     *
     * @param operation
     * @return
     */
    String sendOperation(String id, String operation) {
        // Declare an empty socket for communication
        Socket clientSocket = null;
        // Declare an empty string which will hold the response received from server
        String response = null;

        // Request formation: id, operation, key (e+n), sign
        /// "e" will always have length 10 because it is a strict 32 bit number
        ///     so, no comma needed to separate e and n
        String request = "{";
        request += "\"id\":\"" + id + "\",";
        request += operation;
        request += "\"key\":\"" + e.toString() + n.toString() + "\",";

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
        request += "\"sign\":\"" + requestSign + "\"}";

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
            String receivedResponse = in.readLine();

            // Create JSONObject and parser objects
            JSONObject parsedResponse;
            JSONParser responseParser = new JSONParser();
            // Parse received json text
            parsedResponse = (JSONObject) responseParser.parse(receivedResponse);
            // Take out response from it
            response = parsedResponse.get("response").toString();
        } catch (SocketException e) {
            // Handle failed socket connection
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            // Handle IO exceptions
            System.out.println("IO: " + e.getMessage());
        } catch (ParseException ex) {
            ex.printStackTrace();
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
        // Return received response. Tabs are now replaced with newlines
        return response.replace("\t", "\n");
    }

    public static void main(String[] args) {
        BlockChainClient blockChainClient = new BlockChainClient();
        // Form a 32 bit public key initializer
        // We are keeping this piece here so that the client can decide what the starting point should be
        // This will also be the "e" component of RSA
        /// "e" will always have length 10 because it is a strict 32 bit number
        ///     so, no comma needed to separate e and n
        BigInteger publicKeyInitializer = new BigInteger(32, 100, (new Random()));
        // Generate "d" and "n" components of RSA based on the "e" or publicKeyInitializer object
        blockChainClient.generateKeys(publicKeyInitializer);

        // Public key: e+n
        String publicKey = e.toString() + n.toString();
        // Private key: d
        String privateKey = d.toString();

        // Display the keys
        System.out.println("Public key (e+n): " + publicKey);
        System.out.println(" Private key (d): " + privateKey);

        // Find the hash of public key
        String hash = blockChainClient.ComputeSHA_256_as_Hex_String(publicKey);
        // Keep only its least 20 significant bytes as client ID
        String clientID = hash != null ? hash.substring(hash.length() - 40) : null;
        // Display client ID
        System.out.println("       Client ID: " + clientID);
        System.out.println("");

        // Keep running until user exits
        boolean done = false;
        while (!done) {
            // Print the menu
            System.out.println("Block Chain Menu");
            System.out.println("\t0. View basic blockchain status.");
            System.out.println("\t1. Add a transaction to the blockchain.");
            System.out.println("\t2. Verify the blockchain.");
            System.out.println("\t3. View the blockchain.");
            System.out.println("\t4. Corrupt the chain.");
            System.out.println("\t5. Hide the corruption by repairing the chain.");
            System.out.println("\t6. Exit.");

            // Initialize scanner to take user typed inputs
            Scanner input = new Scanner(System.in);
            // Initialize operation text and response text
            String operation = null;
            String response;

            // Store user's choice
            int choice;
            try {
                // Get user's choice
                choice = input.nextInt();
                // Remove leftover new line character
                input.nextLine();
            } catch (InputMismatchException ex) {
                // Error
                System.out.println("Enter a valid integer choice and retry.");
                continue;
            }

            switch (choice) {
                // Blockchain status
                case 0:
                    operation = "\"choice\" : 0";
                    break;

                // Add a node
                case 1:
                    //*** Average time for block addition with difficulty 4: 210 ms.
                    //*** Average time for block addition with difficulty 5: 630 ms.
                    System.out.println("Enter difficulty > 0");
                    int difficulty;
                    try {
                        // Get the difficulty level
                        difficulty = input.nextInt();
                        // Remove leftover new line character
                        input.nextLine();
                        if (difficulty < 1) {
                            // Error
                            System.out.println("Enter difficulty greater than 0 and retry.");
                            continue;
                        }
                    } catch (InputMismatchException ex) {
                        // Error
                        System.out.println("Enter a valid integer difficulty and retry.");
                        continue;
                    }

                    // Get transaction details
                    System.out.println("Enter transaction:");
                    String blockData = input.nextLine();

                    if (blockData.trim().equalsIgnoreCase("")) {
                        // Error
                        System.out.println("Enter valid transaction and retry");
                        continue;
                    }
                    // Create the json part to be added to the request being sent to the server
                    operation = "\"choice\" : 1, \"Tx\" : \"" + blockData + "\", \"difficulty\" : " + difficulty;
                    break;

                // Chain verification
                case 2:
                    //*** Average time for chain verification with difficulty 4: ~0 ms.
                    //*** Average time for chain verification with difficulty 5: ~0 ms.
                    System.out.println("Verifying entire chain");
                    // Create the json part to be added to the request being sent to the server
                    operation = "\"choice\" : 2";
                    break;

                // View block chain
                case 3:
                    // Create the json part to be added to the request being sent to the server
                    operation = "\"choice\" : 3";
                    break;

                // Corrupt chain
                case 4:
                    System.out.println("Corrupt the Blockchain");
                    System.out.println("Enter block ID of block to Corrupt");
                    // Get node number to corrupt
                    int blockLoc;
                    try {
                        blockLoc = input.nextInt();
                        // Remove leftover new line character
                        input.nextLine();
                    } catch (InputMismatchException ex) {
                        // Error
                        System.out.println("Enter a valid integer block location and retry.");
                        continue;
                    }

                    // Get new transaction data
                    System.out.println("Enter new data for block " + blockLoc);
                    String newData = input.nextLine();

                    if (newData.trim().equalsIgnoreCase("")) {
                        // Error
                        System.out.println("Enter valid data and retry");
                        continue;
                    }

                    // Create the json part to be added to the request being sent to the server
                    operation = "\"choice\" : 4, \"blockLoc\" : \"" + blockLoc + "\", \"newData\" : \"" + newData + "\"";
                    break;

                // Chain repair
                case 5:
                    //*** Average time for repairing chain of 10 blocks with difficulty 4 from start: ~6,500 ms.
                    //*** Average time for repairing chain of 10 blocks with difficulty 4 from start: ~15,000 ms.
                    System.out.println("Repairing the entire chain");

                    // Create the json part to be added to the request being sent to the server
                    operation = "\"choice\" : 5";
                    break;

                // Exit
                case 6:
                    System.out.println("Good bye!");
                    done = true;
                    break;
            }

            if (!done) {
                // Send the generated json subparts to the sendOperation method
                //  it will form a complete the json request text, send it to the server
                //  and also get client a response from the server
                response = blockChainClient.sendOperation(clientID, operation);
                // Once response received, print it
                System.out.println(response);
                System.out.print("\n");
            }
        }
    }
}
