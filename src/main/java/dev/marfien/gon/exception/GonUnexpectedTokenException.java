package dev.marfien.gon.exception;

public class GonUnexpectedTokenException extends GonParseException {

    public GonUnexpectedTokenException(char expectedToken, char tokenGot) {
        this("'%s'".formatted(expectedToken), tokenGot);
    }

    public GonUnexpectedTokenException(String expectedToken, char tokenGot) {
        super("Received unexpected token. Expected: %s, but was '%s'".formatted(expectedToken, tokenGot));
    }
}
