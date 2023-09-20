package dev.marfien.gon;

public enum GonToken {

    CLASS_OPENER('['),
    CLASS_CLOSER(']'),
    BODY_OPENER('{'),
    BODY_CLOSER('}'),
    FLAG_START('-'),
    FLAG_NEGATOR('!'),
    VALUE_OPENER(':'),
    ATTRIBUTE_VALUE_ASSIGNER('='),
    STRING_QUOTES('"'),
    BREAK_POINT(';'),
    // Needs to return false on Character.isLetterOrDigit(char) and not be '.' or '-'.
    COMMENT_LINE('#'),
    END_OF_FILE((char) -1),
    UNKNOWN((char) Integer.MIN_VALUE);

    private final char token;

    GonToken(char token) {
        this.token = token;
    }

    public char getToken() {
        return this.token;
    }

    public static GonToken byToken(char c) {
        for (GonToken value : values()) {
            if (value.token == c) {
                return value;
            }
        }

        return UNKNOWN;
    }

    @Override
    public String toString() {
        return String.valueOf(this.token);
    }
}
