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
        if (isFloatingNumber(valueString)) {
            return new GonFloat(Double.parseDouble(valueString));
        }

        OptionalLong number = decodeLong(valueString);
        if (number.isPresent()) return new GonInt(number.getAsLong());
        else throw new GonUnknownValueException(valueString);
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

    private static boolean isFloatingNumber(String s) {
        if (s.isEmpty())
            return false;

        char firstChar = s.charAt(0);
        int startIndex = firstChar == '-' || firstChar == '+' ? 1 : 0;
        char[] chars = s.toCharArray();
        boolean foundPeriod = false;

        for (int i = startIndex; i < chars.length; i++) {
            char current = chars[i];

            if (current == '.') {
                // Two periods
                // A floating point number can only have one
                if (foundPeriod) return false;

                foundPeriod = true;
                continue;
            }

            if (current < '0' || current > '9') {
                return false;
            }
        }

        // Needs to find exactly one period to be a FLOATING point number
        return foundPeriod;
    }

    private static OptionalLong decodeLong(String s) throws IOException {
        int radix = 10;
        int index = 0;
        boolean negative = false;
        long result;

        if (s.isBlank())
            throw new GonParseException("Received empty string to decode as long");
        char firstChar = s.charAt(0);

        // Check for sign
        if (firstChar == '-') {
            negative = true;
            index++;
        } else if (firstChar == '+') {
            index++;
        }

        // Handle different radix'
        if (s.startsWith("0x", index) || s.startsWith("0X", index)) {
            radix = 16;
            index += 2;
        } else if (s.startsWith("#", index)) {
            radix = 16;
            index++;
        } else if (s.startsWith("0b", index) || s.startsWith("0B", index)) {
            radix = 2;
            index += 2;
        } else if (s.startsWith("0", index)) {
            radix = 8;
            index++;
        }

        if (s.startsWith("+", index) || s.startsWith("-"))
            throw new GonParseException("Sign in wrong position. Needs to be the first character: '%s'".formatted(s));

        try {
            result = Long.parseLong(s, index, s.length(), radix);
            result *= negative ? -1 : 1;
        } catch (NumberFormatException e) {
            // If the number is Long.MIN_VALUE, it will throw an error because it is out of bounds.
            String parsable = negative ? '-' + s.substring(index) : s.substring(index);
            result = Long.parseLong(parsable, radix);
        } catch (Throwable t) {
            return OptionalLong.empty();
        }

        return OptionalLong.of(result);
    }

}
