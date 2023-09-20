package dev.marfien.gon.api;

import dev.marfien.gon.io.GonReader;

import java.io.IOException;

@FunctionalInterface
public interface GonReaderConsumer {

    void accept(GonReader gonReader) throws IOException;

}
