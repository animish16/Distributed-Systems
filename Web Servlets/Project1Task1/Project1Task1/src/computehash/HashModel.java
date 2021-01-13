package computehash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashModel {
    Hash getHash(String originalText, String hashFunction) {
        /*** This method takes in text and hash function and returns hex and base64 hash values ***/
        String hexHashValue, base64HashValue;
        MessageDigest doHash = null;
        byte[] messageDigest = null;

        // If sha, generate sha hash
        if (hashFunction.equalsIgnoreCase("sha-256")) {
            try {
                doHash = MessageDigest.getInstance("SHA-256");
                messageDigest = doHash.digest(originalText.getBytes());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        } else { // md5 hash by default
            try {
                doHash = MessageDigest.getInstance("MD5");
                messageDigest = doHash.digest(originalText.getBytes());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        // Generate hash values and return them
        hexHashValue = javax.xml.bind.DatatypeConverter.printHexBinary(messageDigest);
        base64HashValue = javax.xml.bind.DatatypeConverter.printBase64Binary(messageDigest);

        return new Hash(originalText, hashFunction, hexHashValue, base64HashValue);
    }
}
