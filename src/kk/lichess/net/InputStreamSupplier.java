package kk.lichess.net;

import java.io.IOException;
import java.io.InputStream;

public interface InputStreamSupplier {
    InputStream openStream() throws IOException;
}
