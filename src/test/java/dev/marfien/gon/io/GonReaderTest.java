package dev.marfien.gon.io;

import dev.marfien.gon.GonToken;
import dev.marfien.gon.api.GonReaderConsumer;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

class GonReaderTest {

    @Test
    void testPeekReadToken() throws Exception {
        withReader(
                "{}",
                reader -> {

                    GonToken peek = reader.peekToken();
                    GonToken read = reader.nextToken();

                    assertTrue(peek == read && peek == GonToken.BODY_OPENER, "Peek == Read == BODY_OPENER");
                }
        );
    }

    @Test
    void testPeekTokenSkipWhitespaces() throws Exception {
        withReader(
                "   \n{}",
                reader -> {
                    GonToken peek = reader.peekToken();
                    GonToken read = reader.nextToken();

                    assertTrue(peek == read && peek == GonToken.BODY_OPENER, "Peek == Read == BODY_OPENER");
                }
        );
    }

    @Test
    void testConsumeToken() throws Exception {
        withReader(
                "{}",
                reader -> {
                    GonToken token = reader.peekToken();
                    assertSame(GonToken.BODY_OPENER, token, "Wrong token received.");
                    reader.consumeToken(token);
                    assertSame(GonToken.BODY_CLOSER, reader.nextToken());
                }
        );
    }

    @Test
    void testNextName() throws Exception {
        withReader(
                "name: true;",
                reader -> {
                    String name = reader.nextName();
                    assertEquals("name", name, "Name dose not match");
                }
        );
    }

    private static GonReader createReader(String s) {
        return new GonReader(new StringReader(s));
    }

    private static void withReader(String input, GonReaderConsumer method) throws Exception {
        try (GonReader reader = createReader(input)) {
            method.accept(reader);
        }
    }

}
