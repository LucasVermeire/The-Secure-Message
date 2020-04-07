package protocol;

import gateway.ForwardJob;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Protocol {

    private static final String RX_DIGIT = "[0-9]";
    private static final String RX_LETTER = "[a-zA-Z]";
    private static final String RX_CHAR = "[\\x20-\\xff]";
    private static final String RX_SP = " ";
    private static final String RX_CRLF = "(?:\\x0d\\x0a)";
    private static final String RX_VERSION ="((?:" + RX_DIGIT + "|" + RX_LETTER + "|/){4,10})";
    private static final String RX_DOMAIN = "((?:" + RX_LETTER + "|" + RX_DIGIT + "|\\.){4,80})";
    private static final String PORT = "(" + RX_DIGIT+"{1,5})";
    private static final String RX_OTPNUM = "((?:" + RX_DIGIT + "){6})";
    private static final String RX_OTPKEY = "((?:" + RX_DIGIT + "|" + RX_LETTER + "){10,20})";
    private static final String RX_USER = "((?:" + RX_LETTER + "|" + RX_DIGIT + "){5,10})";
    private static final String RX_USER_DOMAIN = "("+RX_USER+"@" + RX_DOMAIN + ")";
    private static final String RX_HASH = "((?:" + RX_LETTER + "|" + RX_DIGIT + "){50,200})";
    private static final String RX_PASS = RX_HASH;
    private static final String RX_MESSAGE = "((?:" + RX_CHAR + "){1,200})";

    private static final String PROTOCOL = "PROTOCOL" + RX_SP + RX_VERSION + RX_SP + RX_DOMAIN + RX_CRLF;
    private static final String SIGN_IN = "SIGNIN" + RX_SP + RX_USER + RX_SP + RX_PASS + RX_SP + RX_OTPNUM + RX_CRLF;
    private static final String SIGN_UP = "SIGNUP" + RX_SP + RX_USER + RX_SP + RX_PASS + RX_SP + RX_OTPKEY + RX_CRLF;
    private static final String SIGN_OK = "SIGNOK" + RX_CRLF;
    private static final String SIGN_ERR = "SIGNERR" + RX_SP + RX_MESSAGE + RX_CRLF;
    private static final String HEY = "HEY" + RX_SP + RX_USER_DOMAIN + RX_SP + RX_MESSAGE + RX_CRLF;
    private static final String HEY_OK = "HEYOK" + RX_CRLF;
    private static final String HEY_ERR = "HEYERR" + RX_SP + RX_MESSAGE + RX_CRLF;
    private static final String BLAH = "BLAH" + RX_SP + RX_USER_DOMAIN + RX_SP + RX_MESSAGE + RX_CRLF;
    private static final String EXIT = "EXIT" + RX_CRLF;
    private static final String EXIT_OK = "EXITOK" + RX_CRLF;

    private static final String MGATE = "MGATE" + RX_SP + RX_DOMAIN + RX_SP + PORT + RX_CRLF;

    private static final String CONNECT = "CONNECT" + RX_SP + RX_DOMAIN + RX_CRLF;
    private static final String CONNECT_OK = "CONNECTOK" + RX_CRLF;
    private static final String CONNECT_ERR = "CONNECTERR" + RX_SP + RX_MESSAGE + RX_CRLF;
    private static final String FORWARDED =  "("+HEY_OK+"|"+HEY_ERR+"|"+BLAH+")";
    private static final String FORWARD = "FORWARD" + RX_SP + RX_USER_DOMAIN + RX_SP + RX_USER_DOMAIN + RX_SP + FORWARDED;

    private static final String[] ALL_REGEXP = {
            PROTOCOL, SIGN_UP, SIGN_IN, SIGN_OK, SIGN_ERR, HEY, HEY_OK, HEY_ERR, BLAH, EXIT, EXIT_OK,
            MGATE, CONNECT, CONNECT_OK, CONNECT_ERR, FORWARD
    };

    public static final int PARSE_UNKNOWN = -1;
    public static final int PARSE_PROTOCOL = 0;
    public static final int PARSE_SIGNUP = 1;
    public static final int PARSE_SIGNIN = 2;
    public static final int PARSE_SIGNOK = 3;
    public static final int PARSE_SIGNERR = 4;
    public static final int PARSE_HEY = 5;
    public static final int PARSE_HEYOK = 6;
    public static final int PARSE_HEYERR = 7;
    public static final int PARSE_BLAH = 8;
    public static final int PARSE_EXIT = 9;
    public static final int PARSE_EXITOK = 10;
    public static final int PARSE_MGATE = 11;
    public static final int PARSE_CONNECT = 12;
    public static final int PARSE_CONNECTOK = 13;
    public static final int PARSE_CONNECTERR = 14;
    public static final int PARSE_FORWARD = 15;

    //--------------------------------------------------------------------------------

    public static int parse(String line){
        return parse(line, false);
    }

    public static int parse(String line, boolean debug){
        if (debug) System.out.println("[ "+Protocol.class.getSimpleName()+ " - parse ] MESSAGE TO PARSE: \"" + line+"\"");
        for(int i = 0 ; i< ALL_REGEXP.length;++i) {
            Matcher matcher = Pattern.compile(ALL_REGEXP[i]).matcher(line);
            if (matcher.matches()) {
                if (debug) System.out.println("REGEX: " + ALL_REGEXP[i] + " OK");
                return i;
            }else{
                if(debug) System.out.println("REGEX: " + ALL_REGEXP[i] + " KO");
            }
        }
        return PARSE_UNKNOWN;
    }

    //-------------------------------------------------------------------------------------

    public static String makeProtocol(String domain){
        return "PROTOCOL <version> <domain>\r\n"
                .replace("<version>","secmes/1")
                .replace("<domain>",domain);
    }

    //-------------------------------------------------------------------------------------

    public static String makeSignUp(String nickname, String pass, String otpKey){
        return "SIGNUP <nickname> <pass> <otpKey>\r\n"
                .replace("<nickname>",nickname)
                .replace("<pass>",pass)
                .replace("<otpKey>",otpKey);
    }

    public static String makeSignIn(String nickname, String pass, String otpNum){
        return "SIGNIN <nickname> <pass> <otpNum>\r\n"
                .replace("<nickname>",nickname)
                .replace("<pass>",pass)
                .replace("<otpNum>",otpNum);
    }

    public static String makeSignOk(){
        return "SIGNOK\r\n";
    }

    public static String makeSignErr(String message){
        return "SIGNERR <message>\r\n"
                .replace("<message>", message);
    }

    //-------------------------------------------------------------------------------------


    public static String makeHey(String user, String domain, String message){ return makeHey(makeUserDomain(user, domain), message); }

    public static String makeHey(String userDomain, String message){
        return "HEY <user_domain> <message>\r\n"
                .replace("<user_domain>",userDomain)
                .replace("<message>",message);
    }

    public static String makeHeyOk(){
        return "HEYOK\r\n";
    }

    public static String makeHeyErr(String message){
        return "HEYERR <message>\r\n"
                .replace("<message>",message);
    }

    public static String makeBlah(String userDomain, String message){
        return "BLAH <user_domain> <message>\r\n"
                .replace("<user_domain>",userDomain)
                .replace("<message>",message);
    }

    public static String makeExit(){
        return "EXIT\r\n";
    }

    public static String makeExitOk(){
        return "EXITOK\r\n";
    }

    public static String makeMgate(String domain, int port){
        return "MGATE <domain> <port>\r\n"
                .replace("<domain>",domain)
                .replace("<port>", Integer.toString(port));
    }

    public static String makeUserDomain(String userNickname, String domain){
        return "<nickname>@<domain>"
                .replace("<nickname>", userNickname)
                .replace("<domain>", domain);
    }

    public static String makeForward(ForwardJob j) {
        return "FORWARD <user_domain_src> <user_domain_desti> <forwarded>"
                .replace("<user_domain_src>", j.getSource())
                .replace("<user_domain_desti>", j.getDestination())
                .replace("<forwarded>", makeBlah(j.getDestination(), j.getProtocol()));
    }

    public static String makeConnect(String serverDomain) {
        return "CONNECT <serverDomain>\r\n"
                .replace("<serverDomain>", serverDomain);
    }

    public static String makeConnectOk() { return "CONNECTOK\r\n"; }

    public static String makeConnectErr(String errorMessage) {
        return "CONNECTERR <message>\r\n"
                .replace("<message>", errorMessage);
    }

    //-------------------------------------------------------------------------------------

    public static String[] parseProtocol(String line){
        if(parse(line,false)==PARSE_PROTOCOL){
            Matcher matcher = Pattern.compile(PROTOCOL).matcher(line);
            if(matcher.find()){ return new String[]{matcher.group(1),matcher.group(2)}; }
        }
        return new String[]{};
    }

    public static String[] parseSignUp(String line){
        if(parse(line,false)==PARSE_SIGNUP){
            Matcher matcher = Pattern.compile(SIGN_UP).matcher(line);
            if(matcher.find()){ return new String[]{matcher.group(1),matcher.group(2),matcher.group(3)}; }
        }
        return new String[]{};
    }

    public static String[] parseSignIn(String line){
        if(parse(line,false)==PARSE_SIGNIN){
            Matcher matcher = Pattern.compile(SIGN_IN).matcher(line);
            if(matcher.find()){ return new String[]{matcher.group(1),matcher.group(2),matcher.group(3)}; }
        }
        return new String[]{};
    }

    public static String[] parseSignErr(String line){
        if(parse(line,false)==PARSE_SIGNERR){
            Matcher matcher = Pattern.compile(SIGN_ERR).matcher(line);
            if(matcher.find()){ return new String[]{matcher.group(1)}; }
        }
        return new String[]{};
    }

    public static String[] parseHey(String line){
        if(parse(line,false)==PARSE_HEY){
            Matcher matcher = Pattern.compile(HEY).matcher(line);
            if(matcher.find()){ return new String[]{matcher.group(1),matcher.group(4)}; }
        }
        return new String[]{};
    }

    public static String[] parseHeyErr(String line){
        if(parse(line,false)==PARSE_HEYERR){
            Matcher matcher = Pattern.compile(HEY_ERR).matcher(line);
            if(matcher.find()){ return new String[]{matcher.group(1)}; }
        }
        return new String[]{};
    }

    public static String[] parseBlah(String line){
        if(parse(line,false)==PARSE_BLAH){
            Matcher matcher = Pattern.compile(BLAH).matcher(line);
            if(matcher.find()){ return new String[]{matcher.group(1),matcher.group(4)}; }
        }
        return new String[]{};
    }

    public static String[] parseUserDomain(String line){
        Matcher matcher = Pattern.compile(RX_USER_DOMAIN).matcher(line);
        if(matcher.find()){ return new String[]{matcher.group(2),matcher.group(3)}; }
        return new String[]{};
    }

    public static String[] parseMgate(String line){
        if(parse(line,false) == PARSE_MGATE){
            Matcher matcher = Pattern.compile(MGATE).matcher(line);
            if(matcher.find()){ return new String[]{matcher.group(1),matcher.group(2)}; }
        }
        return new String[]{};
    }

    public static String[] parseForward(String line) {
        if(parse(line,false)==PARSE_FORWARD){
            Matcher matcher = Pattern.compile(FORWARD).matcher(line);
            if(matcher.find()){
                return new String[]{matcher.group(1),matcher.group(4), matcher.group(7)};
            }
        }
        return new String[]{};
        //FORWARD (((?:[a-zA-Z]|[0-9]){5,10})@((?:[a-zA-Z]|[0-9]|\.){4,80})) (((?:[a-zA-Z]|[0-9]){5,10})@((?:[a-zA-Z]|[0-9]|\.){4,80}))
        //(\(BLAH (((?:[a-zA-Z]|[0-9]){5,10})@((?:[a-zA-Z]|[0-9]|\.){4,80})) ((?:[\x20-\xff]){1,200})\))
    }

    public static String[] parseConnect(String line) {
        if(parse(line,false) == PARSE_CONNECT){
            Matcher matcher = Pattern.compile(CONNECT).matcher(line);
            if(matcher.find()){
                return new String[]{matcher.group(1)};
            }
        }
        return new String[]{};
    }
}