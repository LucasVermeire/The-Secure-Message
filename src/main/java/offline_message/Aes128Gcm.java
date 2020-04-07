package offline_message;

import exceptions.AesException;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Source code: https://javainterviewpoint.com/java-aes-256-gcm-encryption-and-decryption/
 * Slightly modified to fit usages of application
 */
public class Aes128Gcm
{
    private static final int AES_KEY_SIZE = 128;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

    public static String encrypt(String plaintext, String keyString) throws AesException {
        return encrypt(plaintext, encodeStringToSecretKey(keyString));
    }

    public static String encrypt(String plaintext, SecretKey key) throws AesException {
        byte[] IV = new byte[GCM_IV_LENGTH];

        return encrypt(plaintext, key, IV);
    }

    /**
     * @param plaintext plaintext
     * @param key Key used to encrypt
     * @param IV initialisation vector
     * @return Encrypted string encoded in base64
     * @throws AesException
     */
    private static String encrypt(String plaintext, SecretKey key, byte[] IV) throws AesException {
        byte[] plaintextBytes = plaintext.getBytes();
        try {
            // Get Cipher Instance
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            // Create SecretKeySpec
            SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "AES");

            // Create GCMParameterSpec
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, IV);

            // Initialize Cipher for ENCRYPT_MODE
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);

            // Perform Encryption
            byte[] cipherText = cipher.doFinal(plaintextBytes);

            return CryptoBase64.encryptBase64(new String(cipherText, StandardCharsets.ISO_8859_1));
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            throw new AesException("Unable to encrypt", e);
        }
    }

    public static String decrypt(String encryptedPlaintext, String keyString) throws AesException {
        return decrypt(encryptedPlaintext, encodeStringToSecretKey(keyString));
    }
    public static String decrypt(String encryptedPlaintext, SecretKey key) throws AesException {
        byte[] IV = new byte[GCM_IV_LENGTH];

        return decrypt(encryptedPlaintext, key, IV);
    }


    /**
     * @param encryptedPlaintext encryptedPlaintext base64
     * @param key Key used to encrypt
     * @param IV initialisation vector
     * @return Decrypted string
     * @throws AesException
     */
    private static String decrypt(String encryptedPlaintext, SecretKey key, byte[] IV) throws AesException {
        try {
            byte[] cipherText = CryptoBase64.decryptBase64(encryptedPlaintext).getBytes(StandardCharsets.ISO_8859_1);
            // Get Cipher Instance
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            // Create SecretKeySpec
            SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "AES");

            // Create GCMParameterSpec
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, IV);

            // Initialize Cipher for DECRYPT_MODE
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);

            // Perform Decryption
            byte[] decryptedText = cipher.doFinal(cipherText);

            return new String(decryptedText);
        } catch (IllegalArgumentException | NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            throw new AesException("Unable to decrypt", e);
        }
    }

    private static SecretKey encodeStringToSecretKey(String secretKeyString) {
        // decode the base64 encoded string
        byte[] decodedKey = Base64.getDecoder().decode(secretKeyString);
        // rebuild key using SecretKeySpec
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }
}