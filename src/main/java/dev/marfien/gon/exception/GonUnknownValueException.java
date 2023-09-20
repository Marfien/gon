package dev.marfien.gon.exception;

public class GonUnknownValueException extends GonParseException {

    public GonUnknownValueException(String actual) {
        super("Unknown value received: " + actual);
    }
}
