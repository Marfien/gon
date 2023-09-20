package dev.marfien.gon;

import dev.marfien.gon.io.GonReader;
import dev.marfien.gon.object.GonObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class GonParser {

    public GonReader newReader(String s) {
        return this.newReader(new StringReader(s));
    }

    public GonReader newReader(Path path) throws IOException {
        return this.newReader(Files.newInputStream(path));
    }

    public GonReader newReader(File file) throws IOException {
        return this.newReader(new FileReader(file));
    }

    public GonReader newReader(InputStream reader) {
        return this.newReader(new InputStreamReader(reader));
    }

    public GonReader newReader(Reader reader) {
        return new GonReader(reader);
    }

    public GonReader newReader(byte[] bytes) {
        return this.newReader(new ByteArrayInputStream(bytes));
    }

    public GonObject parse(String s) throws IOException {
        return this.newReader(s).nextObject();
    }

    public GonObject parse(Path path) throws IOException {
        return this.newReader(path).nextObject();
    }

    public GonObject parse(File file) throws IOException {
        return this.newReader(file).nextObject();
    }

    public GonObject parse(InputStream s) throws IOException {
        return this.newReader(s).nextObject();
    }

    public GonObject parse(Reader reader) throws IOException {
        return this.newReader(reader).nextObject();
    }

    public GonObject parse(byte[] bytes) throws IOException {
        return this.newReader(bytes).nextObject();
    }

}
