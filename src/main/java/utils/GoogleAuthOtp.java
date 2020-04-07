package utils;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;

import java.util.ArrayList;

public class GoogleAuthOtp {
    public static GoogleAuthenticatorKey generateNewOtpKey(){ return new GoogleAuthenticator().createCredentials(); }

    public static GoogleAuthenticatorKey generateOtpKeyFromString(String otpKey){
        GoogleAuthenticatorConfig config = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder().build();

        return new GoogleAuthenticatorKey
                .Builder(otpKey)
                .setConfig(config)
                .setScratchCodes(new ArrayList<>())
                .build();
    }
    public static String generateQrUrl(String username, String host, GoogleAuthenticatorKey otpKey){ return GoogleAuthenticatorQRGenerator.getOtpAuthURL(host, username, otpKey); }

    public static String generateQrTotpUrl(String username, String host, GoogleAuthenticatorKey otpKey){ return GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(host, username, otpKey); }

    public static int generateOtpCodeFromOtpKey(String otpKey){ return new GoogleAuthenticator().getTotpPassword(otpKey);}

    public static boolean validateOtpCode(String otpKey, int otpCode){ return new GoogleAuthenticator().authorize(otpKey, otpCode); }
}
