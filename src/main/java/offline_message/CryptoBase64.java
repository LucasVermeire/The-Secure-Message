package offline_message;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class CryptoBase64 {
    public static byte[] encryptBase64(byte[] bytesInput){
        return Base64.getEncoder().encode(bytesInput);
    }
    public static String encryptBase64ToString(byte[] bytesInput){
        return Base64.getEncoder().encodeToString(bytesInput);
    }
    public static String encryptBase64(String input){
        return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }
    public static byte[] decryptBase64(byte[] bytesInput){
        return Base64.getDecoder().decode(bytesInput);
    }
    public static String decryptBase64(String input){
        return new String(Base64.getDecoder().decode(input));
    }
}
