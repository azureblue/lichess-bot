package kk.lichess.net;

import kk.lichess.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

class NDJsonStream {

    private final InputStream stream;
    private final ByteArray buffer = new ByteArray();
    private volatile boolean stopped = false;
    NDJsonStream(InputStream inputStream) {
        stream = inputStream;
    }

    String readJson() throws IOException {

        while (true) {
            try {
                int read = stream.read();
                if (read == -1)
                    return null;

                if (read == '\n') {
                    String line = new String(buffer.array, 0, buffer.size(), StandardCharsets.UTF_8).trim();
                    buffer.reset();
                    if (line.isBlank())
                        continue;
                    return line;
                }

                buffer.put((byte) read);
            } catch (SocketTimeoutException te) {
                if (stopped) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        //what can you do
                        Log.e("NDJsonStream close error", e);
                    }
                    Log.i("NDJsonStream stopped");
                    throw new StreamStopped();
                }
            }
        }
    }

    void stop() {
        Log.d("NDJsonStream", "close()");
        stopped = true;
    }

    private static class ByteArray {
        private static final int INITIAL_SIZE = 512;
        private static final int MAX_SIZE = 512 * 1024;
        private static final int INCREMENT_STEP = 512;
        private int pos = 0;
        private byte[] array = new byte[INITIAL_SIZE];

        public final void put(byte b) {
            if (pos == array.length)
                expand();
            array[pos++] = b;
        }

        private void expand() {
            int newSize = array.length + INCREMENT_STEP;
            System.out.println("grow " + newSize);
            if (newSize > MAX_SIZE)
                throw new IllegalStateException("ByteArray exceeded max size");
            byte[] newArray = new byte[newSize];
            System.arraycopy(array, 0, newArray, 0, array.length);
            array = newArray;
        }

        public int size() {
            return pos;
        }

        public void reset() {
            pos = 0;
        }
    }

    public static class StreamStopped extends IOException {

    }
}
