import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class EchoServerTCP {
    // Static socket and port for communication
    static Socket clientSocket = null;
    static int serverPort = 7777;
    // Map to hold ID and its associated number
    static Map<String, Integer> valueMap = new HashMap<>();

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
     * verify() takes in message to check, its encrypted hash anf "e" and "n" RSA components
     *
     * @param messageToCheck:   message string
     * @param encryptedHashStr: hash string
     * @param e:                BigInteger "e" component of RSA
     * @param n:                BigInteger "n" component of RSA
     * @return true or false based on verification results
     * @throws Exception: Throws exception if operation failed
     */
    static boolean verify(String messageToCheck, String encryptedHashStr, BigInteger e, BigInteger n) throws Exception {
        // Take the encrypted string and make it a big integer
        BigInteger encryptedHash = new BigInteger(encryptedHashStr);
        // Decrypt it
        BigInteger decryptedHash = encryptedHash.modPow(e, n);

        // Get the bytes from messageToCheck
        byte[] bytesOfMessageToCheck = messageToCheck.getBytes(StandardCharsets.UTF_8);

        // compute the digest of the message with SHA-256
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        byte[] messageToCheckDigest = md.digest(bytesOfMessageToCheck);

        byte[] bigDigest = new byte[messageToCheckDigest.length + 1];
        // we add a 0 byte as the most significant byte to keep
        // the value to be signed non-negative
        bigDigest[0] = 0;
        System.arraycopy(messageToCheckDigest, 0, bigDigest, 1, messageToCheckDigest.length);

        // Make it a big int
        BigInteger bigIntegerToCheck = new BigInteger(bigDigest);

        // inform the client on how the two compare
        return bigIntegerToCheck.compareTo(decryptedHash) == 0;
    }

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

                // Split request string using commas
                String[] encryptedRequest = in.nextLine().split(",");
                // Separate out request components: id,e+n,operation[,operand],sign
                String id = encryptedRequest[0];
                String e = encryptedRequest[1].substring(0, 10);
                String n = encryptedRequest[1].substring(10);
                String operation = encryptedRequest[2];
                String num = null;
                if (encryptedRequest.length == 5) {
                    num = encryptedRequest[3];
                }
                String sign = encryptedRequest[encryptedRequest.length - 1];

                // Request verification
                // Check 1: Client ID verification
                String keyHash = ComputeSHA_256_as_Hex_String(e + n);
                String checkID = keyHash != null ? keyHash.substring(keyHash.length() - 40) : null;
                // Check 2: Signature verification
                String checkRequest = id + "," + e + n + "," + operation;
                if (num != null) {
                    checkRequest = checkRequest + "," + num;
                }
                boolean isSigned = verify(checkRequest, sign, new BigInteger(e), new BigInteger(n));
                // If these two checks fail
                if (!checkID.equalsIgnoreCase(id) || !isSigned) {
                    reply("ERROR: Some error in request.");
                    continue;
                }

                // Check if client ID is of different length and send an appropriate response
                if (id.length() != 40) {
                    reply("ERROR: ID generated is not valid.");
                    continue;
                }

                if (num != null) {
                    // Check if it is a valid integer and send an appropriate response
                    try {
                        Integer.parseInt(num);
                    } catch (NumberFormatException ex) {
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
        } catch (IOException ex) {
            // Handle IO exceptions
            System.out.println("IO: " + ex.getMessage());
        } catch (Exception ex) {
            // Handle exception occurred while verification
            System.out.println("Verification: " + ex.getMessage());
        } finally {
            // Attempt to close socket
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException ex) {
                // ignore exception on close
            }
        }
    }
}
