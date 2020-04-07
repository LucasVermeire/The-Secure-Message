package server;


public class User {

    private String nickname;
    private String sha384pass;
    private String base64otpkey;
    private transient int otpCode;

    public User(String nickname, String sha384pass){
        this(nickname, sha384pass, "");
    }

    public User(String nickname, String sha384pass, String base64otpkey) {
        this.nickname = nickname;
        this.sha384pass = sha384pass;
        this.base64otpkey = base64otpkey;
    }

    public String getNickname() { return nickname; }
    public String getSha384pass() { return sha384pass; }
    public String getBase64otpkey() { return base64otpkey; }
    public int getOtpCode() { return otpCode; }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (this.getClass() != obj.getClass()) return false;
        User that = (User) obj;

        return (this.nickname.equals(that.nickname));
    }

    public boolean checkPass(String passToCheck){ return sha384pass.equals(passToCheck); }

    public static User signinInUser(String nickname, String sha384pass, String otpCode){
        User result = new User(nickname, sha384pass);
        result.otpCode = Integer.parseInt(otpCode);
        return result;
    }
}
