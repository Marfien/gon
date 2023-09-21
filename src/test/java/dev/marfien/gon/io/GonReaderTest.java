package dev.marfien.gon.io;

import dev.marfien.gon.GonToken;
import dev.marfien.gon.api.GonReaderConsumer;
import dev.marfien.gon.value.GonBoolean;
import dev.marfien.gon.value.GonFloat;
import dev.marfien.gon.value.GonInt;
import dev.marfien.gon.value.GonValue;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

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

    @Test
    void testNextValueString() throws Exception {
        withReader(
                """
                        test=-1234.567
                        """,
                reader -> {
                    reader.nextName();
                    reader.consumeToken(GonToken.ATTRIBUTE_VALUE_ASSIGNER);
                    String value = reader.nextValueString();
                    assertEquals("-1234.567", value, "value string dose not match");
                }
        );
    }

    @Test
    void testNextString() throws Exception {
        String s = "This is \\\"a\\\" String";
        withReader(
                '"' + s + '"',
                reader -> {
                    assertEquals(s, reader.nextString(), "nextString");
                }
        );
    }

    @Test
    void testNextValue() throws Exception {
        withReader(
                "true false null 1.23 #F8D1",
                reader -> {
                    assertSame(reader.nextValue(), GonValue.TRUE);
                    assertSame(reader.nextValue(), GonValue.FALSE);
                    assertSame(reader.nextValue(), GonValue.NULL);
                    assertEquals(reader.nextValue(), new GonFloat(1.23));
                    assertEquals(reader.nextValue(), new GonInt(0xF8D1L));
                }
        );
    }

    @Test
    void testNextFlag() throws Exception {
        withReader(
                "-TestFlag -!NegTestFlag",
                reader -> {
                    Map.Entry<String, GonBoolean> first = reader.nextFlag();
                    assertEquals(first.getKey(), "TestFlag");
                    assertEquals(first.getValue(), GonValue.TRUE);

                    Map.Entry<String, GonBoolean> second = reader.nextFlag();
                    assertEquals(second.getKey(), "NegTestFlag");
                    assertEquals(second.getValue(), GonValue.FALSE);
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
