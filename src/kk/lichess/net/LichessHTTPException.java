package kk.lichess.net;

public class LichessHTTPException extends RuntimeException {
    LichessHTTPException(Exception e) {
        super(e);
    }

    LichessHTTPException(String message, Exception e) {
        super(message, e);
    }
}
