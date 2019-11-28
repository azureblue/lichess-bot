package kk.lichess.net;

import kk.lichess.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

class NDJsonStreamReader {

    private final BufferedReader br;
    private final InputStream stream;

    NDJsonStreamReader(InputStream inputStream) {
        stream = inputStream;
        br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }

    String readJson() throws IOException {
        while (true) {
            String line = br.readLine();
            if (line == null)
                return null;

            if (line.trim().equals(""))
                continue;

            return line.trim();
        }
    }

    void close() throws IOException {
        Log.d("NDJsonStreamReader", "close()");
        stream.close();
    }
}
