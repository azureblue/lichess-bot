package kk.lichess.net;

import kk.lichess.Log;

import java.io.IOException;
import java.util.function.BiConsumer;

import static java.util.concurrent.CompletableFuture.runAsync;

public class LichessStream {

    private final JsonStreamSupplier stream;
    private final BiConsumer<LichessStream, StreamResult> whenComplete;
    private final JsonHandler handler;
    private NDJsonStream jsonReader;
    private Thread streamThread;
    private volatile boolean ended = false;

    LichessStream(JsonStreamSupplier stream, BiConsumer<LichessStream, StreamResult> whenComplete, JsonHandler handler) {
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
        jsonReader = stream.openStream();
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
            } catch (NDJsonStream.StreamStopped e) {
                whenComplete.accept(this, new StreamResult(StreamResultStatus.Stopped, null));
            } catch (Exception e) {
                    whenComplete.accept(this, new StreamResult(StreamResultStatus.Error, e));
            } finally {
                ended = true;
            }
        });

        streamThread.start();
    }

    public void stop() {
        Log.d(this.getClass().getSimpleName(), "stop()");
        jsonReader.stop();
    }

    public enum StreamResultStatus {
        EndOfStream, Stopped, Error
    }

    public interface JsonStreamSupplier {
        NDJsonStream openStream() throws IOException;
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
