package dev.marfien.gon.io;

import dev.marfien.gon.GonToken;
import dev.marfien.gon.exception.GonParseException;
import dev.marfien.gon.exception.GonUnexpectedTokenException;
import dev.marfien.gon.exception.GonUnknownValueException;
import dev.marfien.gon.object.EmptyGonObject;
import dev.marfien.gon.object.GonObject;
import dev.marfien.gon.object.NestedGonObject;
import dev.marfien.gon.object.SingleValueGonObject;
import dev.marfien.gon.value.*;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class GonReader implements AutoCloseable {

    private PeekReader reader;

    public GonReader(Reader in) {
        this.reader = in instanceof PeekReader peekReader ? peekReader : new PeekReader(in);
    }

    public GonObject nextObject() throws IOException {
        String name = this.nextName();
        String className = null;
        Map<String, GonValue> attributes = new HashMap<>();
        GonToken token = this.peekToken();

        // Read class name after name
        if (token == GonToken.CLASS_OPENER) {
            className = this.nextClass();
            token = this.peekToken();
        } else if (token != GonToken.ATTRIBUTE_VALUE_ASSIGNER) {
            name = name.isEmpty() ? null : name;
        // In case there is no name it is an attribute
        } else {
            this.consumeToken(token);
            GonValue value = this.nextValue();
            attributes.put(name, value);
            name = null;
            token = this.peekToken();
        }

        while (true) {
            switch (token) {
                case BREAK_POINT -> {
                    return new EmptyGonObject(name, className, attributes);
                }
                case BODY_OPENER -> {
                    return new NestedGonObject(name, className, attributes, this.nextBody());
                }
                case VALUE_OPENER -> {
                    this.consumeToken(token);
                    GonValue value = this.nextValue();
                    this.consumeToken(GonToken.BREAK_POINT);
                    return new SingleValueGonObject(name, className, attributes, value);
                }
                case FLAG_START -> {
                    Map.Entry<String, GonBoolean> flag = this.nextFlag();
                    attributes.put(flag.getKey(), flag.getValue());
                }
                // Probably an attribute.
                case UNKNOWN -> {
                    Map.Entry<String, GonValue> attribute = this.nextAttribute();
                    attributes.put(attribute.getKey(), attribute.getValue());
                }
                default -> throw new GonUnexpectedTokenException(
                        "'%S', '%S', '%S', '%S', attribute name".formatted(
                                GonToken.BREAK_POINT, GonToken.BODY_OPENER, GonToken.VALUE_OPENER, GonToken.FLAG_START
                        ),
                        (char) this.reader.getPeeked()
                );
            }

            token = this.peekToken();
        }
    }

    public List<GonObject> nextBody() throws IOException {
        this.consumeToken(GonToken.BODY_OPENER);
        List<GonObject> nestedObjects = new LinkedList<>();

        GonToken token = this.peekToken();
        while (token != GonToken.BODY_CLOSER) {
            nestedObjects.add(this.nextObject());
            token = this.peekToken();
        }

        this.consumeToken(GonToken.BODY_CLOSER);
        return nestedObjects;
    }

    public String nextClass() throws IOException {
        this.consumeToken(GonToken.CLASS_OPENER);
        String className = this.nextName();
        this.consumeToken(GonToken.CLASS_CLOSER);
        return className;
    }

    public Map.Entry<String, GonValue> nextAttribute() throws IOException {
        String name = this.nextName();
        this.consumeToken(GonToken.ATTRIBUTE_VALUE_ASSIGNER);
        GonValue value = this.nextValue();

        return new AbstractMap.SimpleEntry<>(name, value);
    }

    public Map.Entry<String, GonBoolean> nextFlag() throws IOException {
        this.consumeToken(GonToken.FLAG_START);
        boolean value = true;

        if (this.peekToken() == GonToken.FLAG_NEGATOR) {
            this.consumeToken(GonToken.FLAG_NEGATOR);
            value = false;
        }

        String name = this.nextName();
        return new AbstractMap.SimpleEntry<>(name, value ? GonValue.TRUE : GonValue.FALSE);
    }

    public GonValue nextValue() throws IOException {
        // if first is '"' than read do string stuff
        if (this.peekToken() == GonToken.STRING_QUOTES) {
            return new GonString(this.nextString());
        }

        String valueString = this.nextValueString();

        if (valueString.isEmpty()) {
            throw new GonParseException("Empty value");
        }

        // Literals
        if ("false".equals(valueString)) return GonValue.FALSE;
        if ("true".equals(valueString)) return GonValue.TRUE;
        if ("null".equals(valueString)) return GonValue.NULL;

        // Classic number declarations
        if (isNumber(valueString)) {
            // If it does not contain a period it is not a decimal number.
            return valueString.indexOf('.') == -1
                    ? new GonInt(Long.parseLong(valueString))
                    : new GonFloat(Double.parseDouble(valueString));
        }

        // Number declaration with custom radix
        if (valueString.startsWith("0x")) return new GonInt(parseHex(valueString));
        if (valueString.startsWith("0b")) return new GonInt(parseBinary(valueString));

        throw new GonUnknownValueException(valueString);
    }

    public String nextString() throws IOException {
        this.consumeToken(GonToken.STRING_QUOTES);

        StringBuilder builder = new StringBuilder();
        boolean nextEscaped = false;
        do {
            int next = this.reader.read();

            if (next == -1) {
                throw new GonParseException("Unexpected end of file in: String: %s<EOF>".formatted(builder.toString()));
            }

            if (nextEscaped) {
                builder.append(next);
                nextEscaped = false;
                // Jump to next. Char is already added
                continue;
            }

            if (next == '\\') {
                nextEscaped = true;
                // The escaping is not added to the string itself.
                continue;
            }

            // Closes the String
            if (next == GonToken.STRING_QUOTES.getToken()) {
                break;
            }

            builder.append(next);
        } while (true);

        return builder.toString();
    }

    public String nextValueString() throws IOException {
        StringBuilder builder = new StringBuilder();

        int ch = this.reader.peek();
        while (Character.isLetterOrDigit(ch) || ch == '.' || ch == '-') {
            builder.append((char) ch);
            ch = this.reader.peek();
        }

        return builder.toString();
    }

    public String nextName() throws IOException {
        StringBuilder builder = new StringBuilder();

        int ch = this.reader.peek();
        while (Character.isLetterOrDigit(ch)) {
            builder.append((char) ch);
            ch = this.reader.peek();
        }

        return builder.toString();
    }

    public void consumeToken(GonToken expected) throws IOException {
        GonToken actualToken = this.nextToken();
        if (actualToken != expected)
            throw new GonUnexpectedTokenException(expected.getToken(), actualToken.getToken());
    }

    public GonToken nextToken() throws IOException {
        int token;
        do {
            token = this.reader.read();

            if (token == GonToken.COMMENT_LINE.getToken())
                this.skipComment();
        } while (Character.isWhitespace(token));

        return GonToken.byToken((char) token);
    }

    public GonToken peekToken() throws IOException {
        int token;
        do {
            token = this.reader.peek();

            if (token == GonToken.COMMENT_LINE.getToken())
                skipComment();
        } while (Character.isWhitespace(token));

        return GonToken.byToken((char) token);
    }

    private void skipComment() throws IOException {
        this.consumeToken(GonToken.COMMENT_LINE);

        for (int token = this.reader.peek(); token != '\n'; token = this.reader.peek());

        // Actually read the character. Until now, it is just peeked, but not consumed.
        assert this.reader.read() == '\n';
    }

    @Override
    public void close() throws Exception {
        this.reader.close();
        this.reader = null;
    }

    private static boolean isNumber(String s) {
        boolean foundPeriod = false;
        char[] chars = s.toCharArray();
        // Skipping the '-' if it is present
        for (int i = chars[0] == '-' ? 1 : 0; i < chars.length; i++) {
            char c = chars[i];
            // One period is allowed
            if (c == '.') {
                // If there is a second is cannot be cast to a number.
                if (foundPeriod) return false;

                foundPeriod = true;
            }

            if (c < '0' || c > '9')
                return false;
        }

        return true;
    }

    private static long parseHex(String s) throws GonParseException {
        // HEX: radix 16
        try {
            return Long.parseLong(s, 16);
        } catch (NumberFormatException e) {
            throw new GonParseException("Cannot parse HEX number. Invalid format: " + s, e);
        }
    }

    private static long parseBinary(String s) throws GonParseException {
        // Binary: radix 8
        try {
            return Long.parseLong(s, 8);
        } catch (NumberFormatException e) {
            throw new GonParseException("Cannot parse number in binary format. Invalid format: " + s, e);
        }
    }

}
