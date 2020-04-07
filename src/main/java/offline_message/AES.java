package offline_message;

import exceptions.AesException;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class AES implements ICryptage {

    private Cipher cipher;

    public AES() {
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void encrypt(String content, String fileName, SecretKey secretKey) throws AesException {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] iv = cipher.getIV();

            try (FileOutputStream fileOut = new FileOutputStream(fileName);
                 CipherOutputStream cipherOut = new CipherOutputStream(fileOut, cipher)) {
                fileOut.write(iv);
                cipherOut.write(content.getBytes());
            }
        } catch (InvalidKeyException | UnsupportedEncodingException e) {
            throw new AesException("Error while encrypting", e);
        } catch (IOException e) {
            throw new AesException("IOException while encrypting", e);
        }
    }

    public String decrypt(String fileName, SecretKey secretKey) throws AesException {
        try (FileInputStream fileIn = new FileInputStream(fileName)) {
            byte[] fileIv = new byte[16];
            fileIn.read(fileIv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(fileIv));

            CipherInputStream cipherIn = new CipherInputStream(fileIn, cipher);
            InputStreamReader inputReader = new InputStreamReader(cipherIn);
            BufferedReader reader = new BufferedReader(inputReader);

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (InvalidKeyException | InvalidAlgorithmParameterException | UnsupportedEncodingException e) {
            throw new AesException("Error while decrypting", e);
        } catch (IOException e) {
            throw new AesException("IOException while decrypting", e);
        }
    }

    public SecretKey generateSecretKey() {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(256);
            return kgen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new AesException("Error while generating Secret Key", e);
        }
    }

}
