package utils;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;

import java.util.ArrayList;

public class ProgramPocOtp {

    public static void main (String [] args){
        displayInfosForOtpKeyString(); // Generate a new OTP Key
//        displayInfosForOtpKeyString("Y73ZEPV6PLUXWZIK", "Lucas", "Secure_Message");  // For an already generated OTP Key
    }

    private static void displayInfosForOtpKeyString(){
        displayInfosForOtpKeyString("", "John Doe", "example.com");
    }

    private static void displayInfosForOtpKeyString(String otpKeyString, String username, String host){
        System.out.println("\n------------------\n");

        GoogleAuthenticatorKey gAuthOtpKey = otpKeyString.length() > 0 ? generateOtpKeyFromString(otpKeyString) : generateNewOtpKey();

        System.out.printf("G Auth key -> \"%s\"\n", gAuthOtpKey.getKey());

        int otpCode = generateOtpCodeFromOtpKey(gAuthOtpKey.getKey());

        System.out.printf("Generated code -> \"%d\"\n", otpCode);
        boolean valid = isOtpCodeValidForOtpKey(gAuthOtpKey.getKey(), otpCode);
        System.out.printf("Code is valid ? -> %b\n", valid);

        String qrFromOtpKey = generateQrUrl(username, host, gAuthOtpKey);
        System.out.printf("Generated qrFromOtpKey -> \"%s\"\n", qrFromOtpKey);
        String qrTotpFromOtpKey = generateQrTotpUrl(username, host, gAuthOtpKey);
        System.out.printf("Generated qrTotpFromOtpKey -> \"%s\"\n", qrTotpFromOtpKey);

        System.out.println("\n------------------\n");
    }

    private static GoogleAuthenticatorKey generateNewOtpKey(){ return new GoogleAuthenticator().createCredentials(); }

    private static GoogleAuthenticatorKey generateOtpKeyFromString(String otpKey){
        GoogleAuthenticatorConfig config = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder().build();

        return new GoogleAuthenticatorKey
                .Builder(otpKey)
                .setConfig(config)
                .setScratchCodes(new ArrayList<>())
                .build();
    }
    private static String generateQrUrl(String username, String host, GoogleAuthenticatorKey otpKey){ return GoogleAuthenticatorQRGenerator.getOtpAuthURL(host, username, otpKey); }

    private static String generateQrTotpUrl(String username, String host, GoogleAuthenticatorKey otpKey){ return GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(host, username, otpKey); }

    private static int generateOtpCodeFromOtpKey(String otpKey){ return new GoogleAuthenticator().getTotpPassword(otpKey);}

    private static boolean isOtpCodeValidForOtpKey(String otpKey, int otpCode){ return new GoogleAuthenticator().authorize(otpKey, otpCode); }
}
