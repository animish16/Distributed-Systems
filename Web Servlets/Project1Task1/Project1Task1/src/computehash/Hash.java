package computehash;

public class Hash {
    public String originalText, hashFunction, hexHashValue, base64HashValue;

    // Class for Hash, which stores hash's details
    public Hash(String originalText, String hashFunction, String hexHashValue, String base64HashValue) {
        this.originalText = originalText;
        this.hashFunction = hashFunction;
        this.hexHashValue = hexHashValue;
        this.base64HashValue = base64HashValue;
    }
}
