// Essential imports

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
import java.sql.Timestamp;
import java.util.*;

public class BlockChainServer extends Object {
    // Static socket and port for communication
    static Socket clientSocket = null;
    static int serverPort = 7777;
    // ArrayList to hold blocks in the chain
    List<Block> blocks;
    // String holding hash of the latest block
    String chainHash;
    // String to hold response to be sent to the server
    static String responseText = null;

    /**
     * Constructor for BlockChain. Initializes blocks ArrayList and chainHash
     */
    public BlockChainServer() {
        blocks = new ArrayList<>();
        chainHash = "";
    }

    /**
     * Converts byte array to a hexadecimal string
     * Code referenced from BabySign.java file
     *
     * @param dataToHex byte array
     * @return String with hex representation of the bytes passed
     */
    String convertToHex(byte[] dataToHex) {
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
     * verify() takes in message to check, its encrypted hash anf "e" and "n" RSA components
     *
     * @param messageToCheck:   message string
     * @param encryptedHashStr: hash string
     * @param e:                BigInteger "e" component of RSA
     * @param n:                BigInteger "n" component of RSA
     * @return true or false based on verification results
     * @throws Exception: Throws exception if operation failed
     */
    boolean verify(String messageToCheck, String encryptedHashStr, BigInteger e, BigInteger n) throws Exception {
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
     * 2. forms a json string of the request
     * 3. sends it to the client over TCP
     * 4. handles any exceptions
     *
     * @param toReply: string to be replied to the client.
     */
    void reply(String toReply) {
        try {
            // Initialize PrintWriter object to send response to the server
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));
            // Form json object of the reply text
            String reply = "{\"response\":\"" + toReply + "\"}";
            // Send neatly formatted request
            out.println(reply);
            // Flush out object
            out.flush();
        } catch (IOException e) {
            // Handle IO exceptions
            e.printStackTrace();
        }
    }

    /**
     * Returns current timestamp to be added to the block information
     *
     * @return current time stamp
     */
    public Timestamp getTime() {
        return (new Timestamp(System.currentTimeMillis()));
    }

    /**
     * Gets the last block in the chain
     *
     * @return Block object of the last block in the chain
     */
    public Block getLatestBlock() {
        return blocks.get(getChainSize() - 1);
    }

    /**
     * Gets the chain size
     *
     * @return number of blocks in the chain
     */
    public int getChainSize() {
        return blocks.size();
    }

    /**
     * Creates hash of a test String
     *
     * @param textToHash test to be hashed
     * @return SHA-256 hash of the text
     */
    private String createHash(String textToHash) {
        // Set initial digest
        byte[] messageDigest = null;

        // Try hashing the text with SHA-256 method
        try {
            MessageDigest doHash;
            doHash = MessageDigest.getInstance("SHA-256");
            messageDigest = doHash.digest(textToHash.getBytes());
        } catch (NoSuchAlgorithmException e) {
            // Print any error generated
            e.printStackTrace();
        }
        // Return the hex hash
        return convertToHex(messageDigest);
    }

    /**
     * Calculates and gives out approximate number of hashes can be generated in one second by current machine
     *
     * @return number of hashes that can be generated
     */
    public int hashesPerSecond() {
        // Set counter to 0
        int count = 0;
        // Note the start time
        long start = System.currentTimeMillis();
        // Run while 1 second is complete
        while (System.currentTimeMillis() - start < 1000) {
            // Find hash
            createHash("00000000");
            // Increment counter
            count++;
        }
        // Return count
        return count;
    }

    /**
     * Adds a new block in the chain after primary verification
     *
     * @param newBlock Block object of the block to be added
     */
    public void addBlock(Block newBlock) {
        // If this is not the first ever block added to the chain,
        //  verify previous hash value
        if (getChainSize() != 0 && !newBlock.getPreviousHash().equalsIgnoreCase(chainHash)) {
            // Access the string so that it can be rewritten
            responseText.toString();
            responseText += "Hash mismatch. Block adding failed.\t";
            responseText += "Chain Hash: " + chainHash + "\t";
            responseText += "Previous hash: " + newBlock.getPreviousHash() + "\t";
            return;
        } else {
            // Add block and update chainHash
            blocks.add(newBlock);
            chainHash = newBlock.calculateHash();
        }
    }

    /**
     * Represents the entire block chain in JSON format
     *
     * @return json representation of the block chain data
     */
    public String toString() {
        // Initialize block data String
        String blockString = "{\\\"ds_chain\\\" : [ ";
        // Traverse each block and pull out its information
        for (int i = 0; i < blocks.size(); i++) {
            blockString = blockString + blocks.get(i).toString();
            // If this is the last block in the chain, don't put comma
            if (i != (blocks.size() - 1)) {
                blockString = blockString + ",";
            }
            blockString = blockString + "\t";
        }
        // Add chainHash to the information
        blockString = blockString + "], \\\"chainHash\\\":\\\"" + chainHash + "\\\"}";
        // Return block JSON representation
        return blockString;
    }

    /**
     * Verifies if the current block chain is valid, if not prints the exact place where the chain was corrupt
     *
     * @return flag indicating success of the verification
     */
    public boolean isChainValid() {
        // Access the string so that it can be rewritten
        responseText.toString();
        // If there are blocks present in the chain
        if (getChainSize() > 0) {
            // Traverse each block and validate
            for (int i = 0; i < getChainSize(); i++) {
                // Get the block from the chain
                Block checkBlock = blocks.get(i);
                // Re-calculate its hash
                String checkHash = checkBlock.calculateHash();
                // Take out the part of the hash to be matched with zeros
                String tryHashZeros = checkHash.substring(0, checkBlock.getDifficulty());
                // Form the string of zeros with length equal to proof-of-work
                String tryZeros = "";
                for (int z = 0; z < checkBlock.getDifficulty(); z++) {
                    tryZeros = tryZeros + "0";
                }
                // If number of zeros in the start are not equal to proof-of-work
                if (!tryHashZeros.equalsIgnoreCase(tryZeros)) {
                    responseText += "..Improper hash on node " + i + " Does not begin with " + tryZeros + "\t";
                    return false;
                }

                // The below check is not for the 0th block as per the instructions
                if (i > 0) {
                    // Re-calculate the hash of previous block in the chain
                    String computedPreviousHash = blocks.get(i - 1).calculateHash();
                    // If the hashes do not match
                    if (!computedPreviousHash.equalsIgnoreCase(blocks.get(i).previousHash)) {
                        responseText += "..Improper previous hash value on node " + i + "\t";
                        return false;
                    }
                }
            }
            // If chain hash doesn't match with the hash of the latest block
            if (!chainHash.equalsIgnoreCase(getLatestBlock().calculateHash())) {
                responseText += "..Chain hash and latest block's hash values do not match\t";
                return false;
            }
        } else { // There are no blocks in the chain
            responseText += "No blocks added yet.\t";
            return false;
        }
        // No errors so far, the validation was successful
        return true;
    }

    /**
     * Finds out node from where chain is corrupt and repairs the entire chain
     */
    public void repairChain() {
        // Initialize repair node's location as -1
        int repairLoc = -1;
        // Traver se the whole chain
        for (int i = 0; i < getChainSize(); i++) {
            // Get the current block
            Block checkBlock = blocks.get(i);
            // Previous hash stays blank ("") for the very first node
            String prevHash = "";
            if (i > 0) {
                // Re-calculate the hash for the previous node
                prevHash = blocks.get(i - 1).calculateHash();
            }
            // Manually re-compute the hash for the current node
            String computedHash = createHash(checkBlock.getIndex() + "," + checkBlock.getTimestamp() + ","
                    + checkBlock.getData() + "," + prevHash + "," + checkBlock.getNonce() + ","
                    + checkBlock.getDifficulty());
            // Re-compute the hash based on details stored in the block and match
            if (!computedHash.equalsIgnoreCase(checkBlock.calculateHash())) {
                // If the hash calculated with the block data doesn't match with manually calculated hash,
                //  repairing is required this node onward
                repairLoc = i;
                break;
            }

            // Also, match the number of zeros at the starting of the hash with difficulty level
            String tryHashZeros = computedHash.substring(checkBlock.getDifficulty());
            String tryZeros = "";
            // Form string of zeros of length difficulty level
            for (int z = 0; z < checkBlock.getDifficulty(); z++) {
                tryZeros = tryZeros + "0";
            }
            // If number of zeros does not match,
            //  repairing is required this node onward
            if (!tryHashZeros.equalsIgnoreCase(tryZeros)) {
                repairLoc = i;
                break;
            }
        }

        // If the chain needs repair
        if (repairLoc != -1) {
            // Start from the corrupt node and repair the rest of the chain
            for (int i = repairLoc; i < getChainSize(); i++) {
                // Previous hash stays empty String for node 0
                String prevHash = "";
                if (i > 0) {
                    // Get previous hash
                    prevHash = blocks.get(i - 1).calculateHash();
                }
                // Update previous hash
                blocks.get(i).previousHash = prevHash;
                // Keep updating chain hash
                chainHash = blocks.get(i).proofOfWork();
            }
        }
    }

    public static void main(String[] args) {
        // Create and add the very first block to the chain
        BlockChainServer blockChain = new BlockChainServer();
        // Chain hash is blank at first
        blockChain.chainHash = "";
        // Create new block
        Block genesis = new Block(0, blockChain.getTime(), "Genesis", 2);
        // Set its previous hash as a blank string
        genesis.setPreviousHash(blockChain.chainHash);
        // Run proof of work and store the hash in chainHash
        blockChain.chainHash = genesis.proofOfWork();
        // Finally, add the block to the chain
        blockChain.addBlock(genesis);

        // Establish connection
        ServerSocket listenSocket = null;
        try {
            listenSocket = new ServerSocket(serverPort);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            JSONObject parsedRequest;
            try {
                // Get the request from client
                clientSocket = listenSocket.accept();
                Scanner in = new Scanner(clientSocket.getInputStream());
                // Split request string using commas
                String jsonRequest = in.nextLine();

                // JSONParser object to extract information from json text
                JSONParser parser = new JSONParser();
                // Parse the request json text
                parsedRequest = (JSONObject) parser.parse(jsonRequest);

                // Separate out request components: id, key (e+n), sign for verification
                String id = parsedRequest.get("id").toString();
                String e = parsedRequest.get("key").toString().substring(0, 10);
                String n = parsedRequest.get("key").toString().substring(10);
                String sign = parsedRequest.get("sign").toString();

                // Request verification
                //** Check 1: Client ID verification
                // Find hash of the key
                String keyHash = blockChain.ComputeSHA_256_as_Hex_String(e + n);
                // Take last 40 characters of that string as ID
                String checkID = keyHash != null ? keyHash.substring(keyHash.length() - 40) : null;
                //** Check 2: Signature verification
                // Separate out just the request json part without sign
                String checkRequest = jsonRequest.substring(0, jsonRequest.indexOf("\"sign\":\""));
                // Verify the sign
                boolean isSigned = blockChain.verify(checkRequest, sign, new BigInteger(e), new BigInteger(n));
                // If these two checks fail
                if (!checkID.equalsIgnoreCase(id) || !isSigned) {
                    blockChain.reply("ERROR: Error verifying the request.");
                    continue;
                }

                // Store user's choice
                int choice = Integer.parseInt(parsedRequest.get("choice").toString());

                switch (choice) {
                    // Blockchain status
                    case 0:
                        // Generate and send status text
                        responseText = "Current size of chain: " + blockChain.getChainSize()
                                + "\tCurrent hashes per second by this machine: " + blockChain.hashesPerSecond()
                                + "\tDifficulty of most recent block: " + blockChain.getLatestBlock().getDifficulty()
                                + "\tNonce for most recent block: " + blockChain.getLatestBlock().getNonce()
                                + "\tChain hash: " + blockChain.chainHash;
                        break;

                    // Add a node
                    case 1:
                        //*** Average time for block addition with difficulty 4: 140 ms.
                        //*** Average time for block addition with difficulty 5: 250 ms.
                        responseText = "";

                        // Get difficulty
                        int difficulty = Integer.parseInt(parsedRequest.get("difficulty").toString());
                        // Get transaction details
                        String blockData = (String) parsedRequest.get("Tx");

                        // Note the start time
                        long startAdding = System.currentTimeMillis();
                        // Generate an empty block
                        Block newBlock = new Block(blockChain.getChainSize(), blockChain.getTime(), blockData, difficulty);
                        // Set its previous hash
                        newBlock.setPreviousHash(blockChain.chainHash);
                        // Perform proof of work
                        String newHash = newBlock.proofOfWork();
                        // Add block to the chain
                        blockChain.addBlock(newBlock);
                        // Update chainHash
                        blockChain.chainHash = newHash;
                        // Note the end time
                        long endAdding = System.currentTimeMillis();

                        // Send time consumed
                        responseText += "Total execution time to add this block was " + (endAdding - startAdding) + " milliseconds";
                        break;

                    // Chain verification
                    case 2:
                        //*** Average time for chain verification with difficulty 4: ~0 ms.
                        //*** Average time for chain verification with difficulty 5: ~0 ms.
                        responseText = "";
                        boolean isValid;
                        // Note the start time
                        long startCheckTime = System.currentTimeMillis();
                        // Perform chain validation
                        isValid = blockChain.isChainValid();
                        // Note the end time
                        long endCheckTime = System.currentTimeMillis();
                        // Send validation result and time consumed
                        responseText += "Chain verification: " + isValid;
                        responseText += "\tTotal execution time required to verify the chain was "
                                + (endCheckTime - startCheckTime) + " milliseconds";
                        break;

                    // View block chain
                    case 3:
                        // Generate and send block information
                        responseText = "View the Blockchain:\t";
                        responseText += blockChain.toString();
                        break;

                    // Corrupt chain
                    case 4:
                        // Get node number to corrupt
                        int blockLoc = Integer.parseInt(parsedRequest.get("blockLoc").toString());
                        String newData = parsedRequest.get("newData").toString();

                        if (blockLoc < 0 || blockLoc >= blockChain.getChainSize()) {
                            // Error
                            responseText = "Enter a block index between 0 to " + (blockChain.getChainSize() - 1) + " and retry.";
                        } else {
                            // Manipulate block details
                            blockChain.blocks.get(blockLoc).setData(newData);
                            responseText = "Block " + blockLoc + " now holds " + blockChain.blocks.get(blockLoc).getData();
                        }
                        break;

                    // Chain repair
                    case 5:
                        //*** Average time for repairing chain of 10 blocks with difficulty 4 from start: ~30,500 ms.
                        //*** Average time for repairing chain of 10 blocks with difficulty 5 from start: ~90,000 ms.
                        // Note start time
                        long startRepairing = System.currentTimeMillis();
                        // Perform chain repairing
                        blockChain.repairChain();
                        // Note end time
                        long endRepairing = System.currentTimeMillis();
                        // Print time consumed
                        responseText = "Total execution time required to repair the chain was " + (endRepairing - startRepairing) + " milliseconds";
                        break;
                }
                // Once response text is formed, send it to the client
                blockChain.reply(responseText);
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

