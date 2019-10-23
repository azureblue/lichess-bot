package kk.lichess.net;

import java.util.HashSet;
import java.util.Set;

public class LichessStreamGroup {
    private final Set<LichessStream> streams = new HashSet<>();

    public synchronized int size() {
        return streams.size();
    }

    synchronized void addStream(LichessStream stream) {
        streams.add(stream);
    }

    synchronized void removeStream(LichessStream stream) {
        streams.remove(stream);
    }

    public synchronized void stopAll() {
        streams.forEach(LichessStream::stop);
    }
}
