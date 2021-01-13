// Essential imports

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;

public class Block {
    int index;
    Timestamp timestamp;
    String data;
    int difficulty;
    BigInteger nonce;
    String previousHash;

    /**
     * Constructor the the Block class
     *
     * @param index      index of the block to be added
     * @param timestamp  current system time
     * @param data       transaction data in the block
     * @param difficulty difficulty level for the proof of work
     */
    public Block(int index, Timestamp timestamp, String data, int difficulty) {
        this.index = index;
        this.timestamp = timestamp;
        this.data = data;
        this.difficulty = difficulty;
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
     * Creates hash of th entire block
     *
     * @return hexadecimal representation of the block
     */
    String calculateHash() {
        // Set initial digest
        byte[] messageDigest = null;

        // Generate exact text to hash
        String textToHash = getIndex() + "," + getTimestamp() + "," + getData() + "," + getPreviousHash() + ","
                + getNonce() + "," + getDifficulty();

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
     * Gets the transaction data in the block
     *
     * @return data String
     */
    String getData() {
        return data;
    }

    /**
     * Gets the difficulty level of the proof of work for the block
     *
     * @return difficulty level
     */
    int getDifficulty() {
        return difficulty;
    }

    /**
     * Gets the index of the current block
     *
     * @return integer index of the block
     */
    int getIndex() {
        return index;
    }

    /**
     * Gets the nonce of the current block
     *
     * @return BigInteger object of the nonce integer
     */
    BigInteger getNonce() {
        return nonce;
    }

    /**
     * Gets the hash of the previous block
     *
     * @return hexadecimal hash of the previous block in the chain
     */
    String getPreviousHash() {
        return previousHash;
    }

    /**
     * Gets the timestamp when the block was added to the block chain
     *
     * @return creation time in Timestamp format
     */
    Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Runs proof of work in order to find the hash which matches the difficulty level
     *
     * @return hexadecimal proof-of-work hash
     */
    String proofOfWork() {
        // Set nonce to 0
        nonce = new BigInteger("0");
        // For future use
        boolean success = false;
        String hash = null;
        // Generate a string of zeros with length equal to proof of work
        String tryZeros = "";
        for (int i = 0; i < difficulty; i++) {
            tryZeros = tryZeros + "0";
        }

        // Keep running until success
        while (!success) {
            // Find hash
            hash = calculateHash();
            // Strip out the part of the hash to be matched with zeros
            String tryHash = hash.substring(0, difficulty);
            if (tryHash.equalsIgnoreCase(tryZeros)) { // If done
                success = true;
            } else {
                // Add 1 to the nonce and recalculate hash
                nonce = nonce.add(BigInteger.ONE);
            }
        }
        return hash;
    }

    /**
     * Setter fot data
     *
     * @param data
     */
    void setData(String data) {
        this.data = data;
    }

    /**
     * Setter for difficulty
     *
     * @param difficulty
     */
    void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    /**
     * Setter for index
     *
     * @param index
     */
    void setIndex(int index) {
        this.index = index;
    }

    /**
     * Setter for previousHash
     *
     * @param previousHash
     */
    void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    /**
     * Setter for block time stamp
     *
     * @param timestamp
     */
    void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Converts and returns the entire block data into JSON format
     *
     * @return block data in json format
     */
    public String toString() {
        return "{" +
                "\\\"index\\\" : " + getIndex() + "," +
                "\\\"time stamp\\\" : \\\"" + getTimestamp() + "\\\"," +
                "\\\"Tx\\\" : \\\"" + getData() + "\\\"," +
                "\\\"PrevHash\\\" : \\\"" + getPreviousHash() + "\\\"," +
                "\\\"nonce\\\" : " + getNonce() + "," +
                "\\\"difficulty\\\" : " + getDifficulty() +
                "}";
    }

    static void main(String[] args) {
    }
}
