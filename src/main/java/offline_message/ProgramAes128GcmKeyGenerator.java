package offline_message;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class ProgramAes128GcmKeyGenerator {
    private static final int AES_KEY_SIZE = 128;

    public static void main(String[] args) throws NoSuchAlgorithmException {
        generateAesKeys(5);
    }


    private static void generateAesKeys(int nbKeysToGenerate) throws NoSuchAlgorithmException {
        for (int i = 0; i < nbKeysToGenerate; i++){
            SecretKey secretKey = generateKey();

            System.out.printf("Key #%d : \"%s\"\n", i+1, decodeSecretKeyToString(secretKey));
        }
    }


    private static SecretKey generateKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(AES_KEY_SIZE);
        // Generate Key
        return keyGenerator.generateKey();
    }

    private static String decodeSecretKeyToString(SecretKey secretKey) {
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }
}
