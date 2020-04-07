package offline_message;

import utils.Logger;

import javax.crypto.SecretKey;
import java.io.*;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class KeyStoreManager {

    private static String KEYSTORE_FILE_PATH = "keystore.ks";
    private static char[] KEYSTORE_FILE_PASSWORD = "laKey".toCharArray();

    private KeyStore ks;

    public KeyStoreManager() {
        //instancie le keystore
        try {
            ks = KeyStore.getInstance(KeyStore.getDefaultType());
        } catch (KeyStoreException e) {
//            System.err.printf("%s:KeyStoreManager() -> %s\n\tMessage : %s\n\tCause : %s\n", getClass().getSimpleName(), e, e.getMessage(), e.getCause());

            Logger.logError(getClass().getSimpleName(), "KeyStoreManager()",
                    String.format("%s\n\tMessage : %s\n\tCause : %s\n",
                            e, e.getMessage(), e.getCause()
                    )
            );
        }
    }

    public SecretKey getKey(String alias, String keyStorePassword) {
        try {
            InputStream readStream = new FileInputStream(KEYSTORE_FILE_PATH);
            try {
                ks.load(readStream, KEYSTORE_FILE_PASSWORD);
            } catch (IOException e) {
                ks.load(null,null); //si fichier non initialisé -> l'initialise
            }
            SecretKey sk = (SecretKey) ks.getKey(alias, keyStorePassword.toCharArray());
            readStream.close();
            return sk;
        } catch (IOException | CertificateException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException e) {
            return null;
        }
    }

    public void storeKey(String alias, SecretKey sk,String keyStorePassword) {
        try {
            OutputStream writeStream = new FileOutputStream(KEYSTORE_FILE_PATH);
            try {
                ks.setKeyEntry(alias, sk, keyStorePassword.toCharArray(), null);
            } catch (KeyStoreException e) {
                ks.load(null, null); //si fichier non initialisé -> l'initialise
                ks.setKeyEntry(alias, sk, keyStorePassword.toCharArray(), null);
            }
            ks.store(writeStream, KEYSTORE_FILE_PASSWORD);
            writeStream.close();
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            System.err.printf("%s:storeKey -> %s\n\tMessage : %s\n\tCause : %s\n", getClass().getSimpleName(), e, e.getMessage(), e.getCause());
        }
    }

    public void deleteKey(String alias) {
        try {
            OutputStream writeStream = new FileOutputStream(KEYSTORE_FILE_PATH);
            ks.deleteEntry(alias);
            writeStream.close();
        } catch (KeyStoreException | IOException e) {
            System.err.printf("%s:deleteKey -> %s\n\tMessage : %s\n\tCause : %s\n", getClass().getSimpleName(), e, e.getMessage(), e.getCause());
        }
    }
}
