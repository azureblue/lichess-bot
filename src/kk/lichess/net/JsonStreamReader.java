package kk.lichess.net;

import kk.lichess.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

class JsonStreamReader {

    private final StringBuilder buffer = new StringBuilder(256);
    private final BufferedReader reader;
    private final InputStream stream;

    JsonStreamReader(InputStream inputStream) {
        stream = inputStream;
        reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }

    private static class ReaderClosed extends Throwable {
        ReaderClosed() {
            super(null, null, true, false);
        }
    }

    private void nextJson() throws IOException, ReaderClosed {
        char ch = nextNonWhitespace();
        if (ch == '{' || ch == '[') {
            char closing = (char) (ch + 2);
            int openings = 0;
            while (true) {
                char next = next();
                if (next == '"') {
                    moveAfterClosingQuote();
                    continue;
                }
                if (next == closing)
                    if (openings == 0)
                        return;
                    else openings--;
                else if (next == ch)
                    openings++;
            }
        }

        if (ch == '"') {
            moveAfterClosingQuote();
            return;
        }

        for (; ; ch = next()) {
            if (ch >= 'a' && ch <= 'u')
                continue;
            if (ch >= '-' && ch <= '9')
                continue;
            if (ch == 'E')
                continue;
            return;
        }
    }

    private char nextNonWhitespace() throws IOException, ReaderClosed {
        while (true) {
            char ch = next();
            if (!Character.isWhitespace(ch))
                return ch;

            discardLast();
        }
    }

    private char next() throws IOException, ReaderClosed {
        int read = reader.read();
        if (read == -1)
            throw new ReaderClosed();
        char ch = (char) read;
        buffer.append(ch);
        return ch;
    }

    private void discardLast() {
        if (buffer.length() == 0)
            throw new IllegalStateException("buffer len = 0: probably malformed json");
        buffer.delete(buffer.length() - 1, buffer.length());
    }

    private char beforeLast() {
        if (buffer.length() < 2)
            throw new IllegalStateException("buffer len < 2: probably malformed json");
        return buffer.charAt(buffer.length() - 2);
    }

    private void moveAfterClosingQuote() throws IOException, ReaderClosed {
        while (true)
            if (next() == '"' && beforeLast() != '\\')
                return;
    }

    String readJson() throws IOException {
        try {
            nextJson();
        } catch (ReaderClosed readerClosed) {
            return null;
        }
        String json = buffer.toString();
        buffer.delete(0, buffer.length());
        return json.trim();
    }

    void close() throws IOException {
        Log.d("JsonStreamReader", "close()");
        stream.close();
    }
}
