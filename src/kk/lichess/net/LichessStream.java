package kk.lichess.net;

import kk.lichess.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.BiConsumer;

import static java.util.concurrent.CompletableFuture.runAsync;

public class LichessStream {

    public interface InputStreamSupplier {
        InputStream openStream() throws IOException;
    }

    private final InputStreamSupplier stream;
    private final BiConsumer<LichessStream, StreamResult> whenComplete;
    private final JsonHandler handler;
    private NDJsonStreamReader jsonReader;
    private Thread streamThread;
    private volatile boolean stopped = false;
    private volatile boolean ended = false;

    LichessStream(InputStreamSupplier stream, BiConsumer<LichessStream, StreamResult> whenComplete, JsonHandler handler) {
        this.stream = stream;
        this.whenComplete = whenComplete;
        this.handler = handler;
    }

    public boolean isEnded() {
        return ended;
    }

    public void sync() throws InterruptedException {
        streamThread.join();
    }

    public synchronized void start() throws IOException {
        Log.d(this.getClass().getSimpleName(), "start()");
        jsonReader = new NDJsonStreamReader(stream.openStream());
        streamThread = new Thread(() -> {
            try {
                while (true) {
                    String json = jsonReader.readJson();
                    if (json == null) {
                        Log.i("stream closed by remote host");
                        whenComplete.accept(this, new StreamResult(StreamResultStatus.EndOfStream, null));
                        return;
                    }

                    Log.vv("incoming json: " + json);

                    runAsync(() -> handler.handleJson(json))
                            .exceptionally(throwable -> {
                                Log.e("json handler error: " + json, throwable);
                                return null;
                            });
                }
            } catch (Exception e) {
                if (stopped)
                    whenComplete.accept(this, new StreamResult(StreamResultStatus.Stopped, null));
                whenComplete.accept(this, new StreamResult(StreamResultStatus.Error, e));
            } finally {
                ended = true;
            }
        });

        streamThread.start();
    }

    public void stop() {
        Log.d(this.getClass().getSimpleName(), "stop()");
        stopped = true;
        try {
            jsonReader.close();
        } catch (IOException e) {
            //what can you do...
            Log.w("stream close error: " + e.toString());
        }
    }

    public enum StreamResultStatus {
        EndOfStream, Stopped, Error
    }


    @FunctionalInterface
    public interface JsonHandler {
        void handleJson(String json);
    }

    public class StreamResult {
        private final StreamResultStatus resultStatus;
        private final Throwable throwable;

        StreamResult(StreamResultStatus resultStatus, Throwable throwable) {
            this.resultStatus = resultStatus;
            this.throwable = throwable;
        }

        public StreamResultStatus getResultStatus() {
            return resultStatus;
        }

        public Throwable getThrowable() {
            return throwable;
        }
    }

}
