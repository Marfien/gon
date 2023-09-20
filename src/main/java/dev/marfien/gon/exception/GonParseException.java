package dev.marfien.gon.exception;

import java.io.IOException;

public class GonParseException extends IOException {

    public GonParseException() {
    }

    public GonParseException(String message) {
        super(message);
    }

    public GonParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public GonParseException(Throwable cause) {
        super(cause);
    }
}
