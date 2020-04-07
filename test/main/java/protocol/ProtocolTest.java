package protocol;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ProtocolTest {

    @Test
    public void parseProtocol() {
        String message = "PROTOCOL secmes/1 server01.group2.chat\r\n";

        assertEquals(Protocol.PARSE_PROTOCOL, Protocol.parse(message, false));

        assertArrayEquals(new String[]{"secmes/1", "server01.group2.chat"},
                Protocol.parseProtocol(message));
    }
    @Test
    public void parseSignUp() {
        String message = "SIGNUP NickName sha384passsha384passsha384passsha384passsha384passsh otpKeyotpKey\r\n";

        assertEquals(Protocol.PARSE_SIGNUP, Protocol.parse(message, false));

        assertArrayEquals(new String[]{"NickName", "sha384passsha384passsha384passsha384passsha384passsh", "otpKeyotpKey"},
                Protocol.parseSignUp(message));
    }
    @Test
    public void parseSignIn() {
        String message = "SIGNIN NickName sha384passsha384passsha384passsha384passsha384passsh 123456\r\n";

        assertEquals(Protocol.PARSE_SIGNIN, Protocol.parse(message, false));

        assertArrayEquals(new String[]{"NickName", "sha384passsha384passsha384passsha384passsha384passsh", "123456"},
                Protocol.parseSignIn(message));
    }
    @Test
    public void parseSignOk() {
        String message = "SIGNOK\r\n";

        assertEquals(Protocol.PARSE_SIGNOK, Protocol.parse(message, false));
    }
    @Test
    public void parseSignErr() {
        String message = "SIGNERR Error message\r\n";

        assertEquals(Protocol.PARSE_SIGNERR, Protocol.parse(message, false));

        assertArrayEquals(new String[]{"Error message"},
                Protocol.parseSignErr(message));
    }
    @Test
    public void parseHey() {
        String message = "HEY DestUser@server01.group2.chat This is a message\r\n";

        assertEquals(Protocol.PARSE_HEY, Protocol.parse(message, false));

        assertArrayEquals(new String[]{"DestUser@server01.group2.chat", "This is a message"},
                Protocol.parseHey(message));
    }
    @Test
    public void parseHeyOk() {
        String message = "HEYOK\r\n";

        assertEquals(Protocol.PARSE_HEYOK, Protocol.parse(message, false));
    }
    @Test
    public void parseHeyErr() {
        String message = "HEYERR Error message\r\n";

        assertEquals(Protocol.PARSE_HEYERR, Protocol.parse(message, false));

        assertArrayEquals(new String[]{"Error message"},
                Protocol.parseHeyErr(message));
    }
    @Test
    public void parseExitOk() {
        String message = "EXITOK\r\n";

        assertEquals(Protocol.PARSE_EXITOK, Protocol.parse(message, false));
    }
    @Test
    public void parseBlah() {
        String message = "BLAH DestUser@server01.group2.chat This is a message\r\n";

        assertEquals(Protocol.PARSE_BLAH, Protocol.parse(message, false));

        assertArrayEquals(new String[]{"DestUser@server01.group2.chat", "This is a message"},
                Protocol.parseBlah(message));
    }
    @Test
    public void parseDestination() {
        String message = "DestUser@server01.group2.chat";

        assertArrayEquals(new String[]{"DestUser", "server01.group2.chat"},
                Protocol.parseUserDomain(message));
    }
    @Test
    public void parseMgate() {
        String message = "MGATE group2.chat 12345\r\n";

        assertEquals(Protocol.PARSE_MGATE, Protocol.parse(message, false));

        assertArrayEquals(new String[]{"group2.chat", "12345"},
                Protocol.parseMgate(message));
    }
    @Test
    public void parseForward() {
        String message = "FORWARD Romuald@server02.group2.chat Godswila@server01.group2.chat BLAH Godswila@server01.group2.chat Salut le MessageGateway\r\n";

        assertEquals(Protocol.PARSE_FORWARD, Protocol.parse(message, false));

        assertArrayEquals(new String[]{"Romuald@server02.group2.chat", "Godswila@server01.group2.chat", "BLAH Godswila@server01.group2.chat Salut le MessageGateway\r\n"},
                Protocol.parseForward(message));
    }
    @Test
    public void parseConnect() {
        String message = "CONNECT server02.group2.chat\r\n";

        assertEquals(Protocol.PARSE_CONNECT, Protocol.parse(message, false));

        assertArrayEquals(new String[]{"server02.group2.chat"},
                Protocol.parseConnect(message));
    }
}