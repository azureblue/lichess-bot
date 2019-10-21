package kk.lichess.net;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

public class JsonStream {

    private InputStream in;

    @FunctionalInterface
    public interface InputStreamSupplier {
        InputStream open() throws IOException;
    }

    private boolean isStarted = false;
    private volatile boolean forceStop = false;
    private final InputStreamSupplier inputStreamSupplier;
    private final Consumer<String> jsonConsumer;

    public JsonStream(InputStreamSupplier inputStreamSupplier, Consumer<String> jsonConsumer) {
        this.inputStreamSupplier = inputStreamSupplier;
        this.jsonConsumer = jsonConsumer;
    }

    public synchronized void start(boolean startInNewThread) throws IOException {
        if (isStarted)
            throw new IllegalStateException("already started!");

        forceStop = false;
        in = inputStreamSupplier.open();

        Runnable runnable = () -> {
            try {
                StringBuilder buffer = new StringBuilder(256);
                int numberOfOpenBraces = 0;
                while (true) {
                    try {
                        if (forceStop)
                            break;

                        int read = in.read();
                        if (read == -1)
                            break;
                        char ch = (char) read;
                        buffer.append(ch);
                        if (ch == '{')
                            numberOfOpenBraces++;
                        else if (ch == '}') {
                            numberOfOpenBraces--;
                            if (numberOfOpenBraces == 0) {
                                jsonConsumer.accept(buffer.toString());
                                buffer.delete(0, buffer.length());
                            }
                            if (numberOfOpenBraces < 0)
                                throw new IllegalStateException("malformed request");
                        }

                    } catch (IOException e) {
                        System.out.println("JsonStream error: " + e);
                        try {
                            in.close();
                        } catch (IOException e1) {
                            // what can you do
                        }
                        break;
                    }
                }
            } finally {
                System.out.println("stream terminated");
                isStarted = false;
            }

        };

        if (startInNewThread) {
            Thread streamThread = new Thread(runnable);
            streamThread.setName(streamThread.getName() + "-JsonStream");
            streamThread.start();
        } else {
            runnable.run();
        }

    }

    public synchronized void stop() {
        forceStop = true;
        try {
            in.close();
        } catch (IOException e) {
            // ignore
        }
    }
}
