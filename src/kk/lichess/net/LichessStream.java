package kk.lichess.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public abstract class LichessStream {


    @FunctionalInterface
    public interface InputStreamSupplier {
        InputStream open() throws IOException;
    }

    private final InputStreamSupplier inputStreamSupplier;

    private Thread streamThread = null;
    private boolean isStarted = false;
    private volatile boolean stop = false;

    private BufferedReader reader;

    public LichessStream(InputStreamSupplier inputStreamSupplier) {
        this.inputStreamSupplier = inputStreamSupplier;
    }

    protected abstract void nextChar(char ch);

    public synchronized void start(boolean startInNewThread, LichessStreamGroup lichessStreamGroup) throws IOException {
        if (isStarted)
            throw new IllegalStateException("already started!");

        lichessStreamGroup.addStream(this);

        stop = false;
        reader = new BufferedReader(new InputStreamReader(inputStreamSupplier.open(), StandardCharsets.UTF_8));


        Runnable runnable = () -> {
            try {
                while (true) {
                    try {
                        if (stop)
                            break;
                        int read = reader.read();
                        if (read == -1)
                            break;
                        nextChar(((char) read));

                    } catch (IOException e) {
                        System.out.println("JsonStream error: " + e);
                        try {
                            this.reader.close();
                        } catch (IOException e1) {
                            // what can you do
                        }
                        break;
                    }
                }
            } finally {
                System.out.println("stream terminated");
                isStarted = false;
                lichessStreamGroup.removeStream(this);
            }

        };

        if (startInNewThread) {
            streamThread = new Thread(runnable);
            streamThread.setName(streamThread.getName() + "-lichess-stream");
            streamThread.start();
        } else {
            runnable.run();
        }

    }

    public synchronized void stop() {
        stop = true;
        try {
            reader.close();
        } catch (IOException e) {
            // ignore
        }
//
//        if (joinThread) {
//            if (streamThread == null)
//                throw new IllegalStateException("thread is null");
//            streamThread.join();
//        }
    }

}
