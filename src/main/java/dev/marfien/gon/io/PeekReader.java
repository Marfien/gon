package dev.marfien.gon.io;

import java.io.IOException;
import java.io.Reader;
import java.util.Objects;

public class PeekReader extends Reader {

    private final Object peekLock = new Object();

    private Reader in;

    private int peeked = -1;

    public PeekReader(Reader in) {
        this.in = Objects.requireNonNull(in);
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        synchronized (super.lock) {
            Objects.checkFromIndexSize(off, len, cbuf.length);
            if (len == 0) return 0;

            if (this.peeked < 0)
                return this.in.read(cbuf, off, len);

            cbuf[off] = (char) this.readPeak();

            if (len == 1) return 1;

            return this.in.read(cbuf, ++off, --len);
        }
    }

    @Override
    public int read() throws IOException {
        synchronized (super.lock) {
            if (this.peeked == -1)
                return this.in.read();

            return this.readPeak();
        }
    }

    private int readPeak() {
        synchronized (this.peekLock) {
            int peek = this.peeked;
            this.peeked = -1;
            return peek;
        }
    }

    public int peek() throws IOException {
        synchronized (this.peekLock) {
            this.ensureOpen();

            this.peeked = this.in.read();
            return this.peeked;
        }
    }

    public int getPeeked() {
        return this.peeked;
    }

    private void ensureOpen() throws IOException {
        if (!this.isOpen())
            throw new IOException("Reader is already closed.");
    }

    public boolean isOpen() {
        return this.in != null && this.peeked > -2;
    }

    @Override
    public void close() throws IOException {
        synchronized (super.lock) {
            this.in.close();
            this.in = null;
            this.peeked = -2;
        }
    }
}
