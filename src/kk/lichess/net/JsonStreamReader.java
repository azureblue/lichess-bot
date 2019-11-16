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
    private int numberOfOpenBraces = 0;

    JsonStreamReader(InputStream inputStream) {
        stream = inputStream;
        reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }

    String readJson() throws IOException {
        while (true) {
            int read = reader.read();
            if (read == -1) return null;
            char ch = (char) read;
            buffer.append(ch);
            if (ch == '{')
                numberOfOpenBraces++;
            else if (ch == '}') {
                numberOfOpenBraces--;
                if (numberOfOpenBraces == 0) {
                    String json = buffer.toString();
                    buffer.delete(0, buffer.length());
                    return json;
                }
                if (numberOfOpenBraces < 0) {
                    numberOfOpenBraces = 0;
                    throw new IllegalStateException("malformed json");
                }
            }
        }
    }

    void close() throws IOException {
        Log.d("JsonStreamReader", "close()");
        stream.close();
    }
}
