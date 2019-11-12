package kk.lichess.net;

public class LichessHTTPException extends RuntimeException {
    public LichessHTTPException(Exception e) {
        super(e);
    }

    public LichessHTTPException(String message, Exception e) {
        super(message, e);
    }
}
