package offline_message;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class ProgramPocCryptoAes {

    public static void main(String[] args) {
//        generateAesKeys(5);
//        System.out.println("\n-------------------\n");
        pocAes128Gcm();
        System.out.println("\n-------------------\n");
    }

    private static void pocAes128Gcm(){
        String plainText = "This is a plain text";

        String keyString = "kGq9zW39qv1WnuGUqXlcKQ==";
        System.out.printf("KeyString : \"%s\"\n", keyString);

        SecretKey key = encodeStringToSecretKey(keyString);
        System.out.printf("Key : \"%s\"\n", decodeSecretKeyToString(key));

        System.out.printf("Original Text : \"%s\"\n", plainText);

        String cipherText = Aes128Gcm.encrypt(plainText, keyString);

        System.out.printf("Encrypted Text : \"%s\"\n", cipherText);

        String decryptedText = Aes128Gcm.decrypt(cipherText, keyString);
        System.out.printf("DeCrypted Text : \"%s\"\n", decryptedText);


        System.out.printf("-> HARDCODED DeCrypted Text : \"%s\"\n", Aes128Gcm.decrypt("B0lND8KbHhIDQXsWbR0TWlZRU1LDgCR1woVhGsK0CGfCkXDCpMO3c8Knw60c", keyString));

        String testToDecrypt = "w61ww4jDmUdhDj7DqxDCty4Vw5fDjCEsw6fDuhgac3IGw64ULMKVwrJuwoouw5YOKCFiwpQcZMK4w6zDjX9Two7CtjDDoMKxwoZ6VEkYw5d+fikyTGzDrUvCqsOcdmxDQAV9MsOxw4vDpMO+KcKHTsO0C8KUw6/DpcKWwrXCvmjCv8KKw4UTw7NWLRInKsOlK0VLaFtXTxfCg8KGQcKpw6rDsMOZecOhw5cQwotdesKVC8K1aMKXwpTDuzVcw6XDiw7ClMORw6IEw6bCrMKiw4nClcOgw61ww4jDmUdhDj7DqxDCty4Vw5fDjCEsw6fDuhgac3IGw64ULMKVwrJuwoouw5YOKCFiwpQcZMK4w6zDjX9Two7CtjDDoMKxwoZ6VEkYw5d+fikyTGzDrUvCqsOcdmxDQAV9MsOxw4vDpMO+KcKHTsO0C8KUw6/DpcKWwrXCvmjCv8KKw4UTw7NWLRInKsOlK0VLaFtXTxfCg8KGQcKpw6rDsMOZecOhw5cQwotdesKVC8K0aMKXwpQbfRrCmVDCmMKcw77Cm8KoeRdxw40nwqc=";
        String testKeyString = "6TeLpijFj2BtYCpSr1Wc5Q==";
        System.out.printf("-> HARDCODED TEST DeCrypted Text : \"%s\"\n", Aes128Gcm.decrypt(testToDecrypt, testKeyString));
    }


    private static String decodeSecretKeyToString(SecretKey secretKey) {
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    private static SecretKey encodeStringToSecretKey(String secretKeyString) {
        // decode the base64 encoded string
        byte[] decodedKey = Base64.getDecoder().decode(secretKeyString);
        // rebuild key using SecretKeySpec
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }
}
