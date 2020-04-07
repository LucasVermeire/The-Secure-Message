package utils;

import java.util.regex.Pattern;

public class DomainUtils {
    public static boolean isDomainMatching(String domainPattern, String domainToCheck){
        return Pattern.compile( "(.*.)?" + domainPattern ).matcher(domainToCheck).matches();
    }
}
