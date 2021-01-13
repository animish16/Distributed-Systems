// Essential imports

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class BlockChain extends java.lang.Object {
    // ArrayList to hold blocks in the chain
    List<Block> blocks;
    // String holding hash of the latest block
    String chainHash;

    /**
     * Constructor for BlockChain. Initializes blocks ArrayList and chainHash
     */
    public BlockChain() {
        blocks = new ArrayList<>();
        chainHash = "";
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
            System.out.println("Hash mismatch. Block adding failed.");
            System.out.println("Chain Hash: " + chainHash);
            System.out.println("Previous hash: " + newBlock.getPreviousHash());
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
        String blockString = "{\"ds_chain\" : [ ";
        // Traverse each block and pull out its information
        for (int i = 0; i < blocks.size(); i++) {
            blockString = blockString + blocks.get(i).toString();
            // If this is the last block in the chain, don't put comma
            if (i != (blocks.size() - 1)) {
                blockString = blockString + ",";
            }
            blockString = blockString + "\n";
        }
        // Add chainHash to the information
        blockString = blockString + "], \"chainHash\":\"" + chainHash + "\"}";
        // Return block JSON representation
        return blockString;
    }

    /**
     * Verifies if the current block chain is valid, if not prints the exact place where the chain was corrupt
     *
     * @return flag indicating success of the verification
     */
    public boolean isChainValid() {
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
                    System.out.println("..Improper hash on node " + i + " Does not begin with " + tryZeros);
                    return false;
                }

                // The below check is not for the 0th block as per the instructions
                if (i > 0) {
                    // Re-calculate the hash of previous block in the chain
                    String computedPreviousHash = blocks.get(i - 1).calculateHash();
                    // If the hashes do not match
                    if (!computedPreviousHash.equalsIgnoreCase(blocks.get(i).previousHash)) {
                        System.out.println("..Improper previous hash value on node " + i);
                        return false;
                    }
                }
            }
            // If chain hash doesn't match with the hash of the latest block
            if (!chainHash.equalsIgnoreCase(getLatestBlock().calculateHash())) {
                System.out.println("..Chain hash and latest block's hash values do not match");
                return false;
            }
        } else { // There are no blocks in the chain
            System.out.println("No blocks added yet.");
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
        BlockChain blockChain = new BlockChain();
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
                    // Generate and print status text
                    System.out.println("Current size of chain: " + blockChain.getChainSize());
                    System.out.println("Current hashes per second by this machine: " + blockChain.hashesPerSecond());
                    System.out.println("Difficulty of most recent block: " + blockChain.getLatestBlock().getDifficulty());
                    System.out.println("Nonce for most recent block: " + blockChain.getLatestBlock().getNonce());
                    System.out.println("Chain hash: " + blockChain.chainHash);
                    break;

                // Add a node
                case 1:
                    //*** Average time for block addition with difficulty 4: 110 ms.
                    //*** Average time for block addition with difficulty 5: 200 ms.
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

                    // Print time consumed
                    System.out.println("Total execution time to add this block was " + (endAdding - startAdding) + " milliseconds");
                    break;

                // Chain verification
                case 2:
                    //*** Average time for chain verification with difficulty 4: ~0 ms.
                    //*** Average time for chain verification with difficulty 5: ~0 ms.
                    System.out.println("Verifying entire chain");
                    boolean isValid;
                    // Note the start time
                    long startCheckTime = System.currentTimeMillis();
                    // Perform chain validation
                    isValid = blockChain.isChainValid();
                    // Note the end time
                    long endCheckTime = System.currentTimeMillis();
                    // Print validation result and time consumed
                    System.out.println("Chain verification: " + isValid);
                    System.out.println("Total execution time required to verify the chain was " + (endCheckTime - startCheckTime) + " milliseconds");
                    break;

                // View block chain
                case 3:
                    // Generate and print block information
                    System.out.println("View the Blockchain:\n");
                    System.out.println(blockChain.toString());
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
                    if (blockLoc < 0 || blockLoc >= blockChain.getChainSize()) {
                        // Error
                        System.out.println("Enter a block index between 0 to " + (blockChain.getChainSize() - 1) + " and retry.");
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

                    // Manipulate block details
                    blockChain.blocks.get(blockLoc).setData(newData);
                    System.out.println("Block " + blockLoc + " now holds " + blockChain.blocks.get(blockLoc).getData());
                    break;

                // Chain repair
                case 5:
                    //*** Average time for repairing chain of 10 blocks with difficulty 4 from start: ~30,500 ms.
                    //*** Average time for repairing chain of 10 blocks with difficulty 5 from start: ~95,000 ms.
                    System.out.println("Repairing the entire chain");
                    // Note start time
                    long startRepairing = System.currentTimeMillis();
                    // Perform chain repairing
                    blockChain.repairChain();
                    // Note end time
                    long endRepairing = System.currentTimeMillis();
                    // Print time consumed
                    System.out.println("Total execution time required to repair the chain was " + (endRepairing - startRepairing) + " milliseconds");
                    break;

                // Exit
                case 6:
                    System.out.println("Good bye!");
                    done = true;
                    break;
            }
            System.out.print("\n");
        }
    }
}
