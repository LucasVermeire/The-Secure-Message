package offline_message;

import javax.crypto.SecretKey;

public interface ICryptage {

    void encrypt(String content, String fileName, SecretKey secretKey);
    String decrypt(String fileName, SecretKey key);
    SecretKey generateSecretKey();

}
