package kk.lichess.net;

import java.util.function.Consumer;

public class JsonStream extends LichessStream {

    private final Consumer<String> jsonConsumer;
    private final StringBuilder buffer = new StringBuilder(256);
    private int numberOfOpenBraces = 0;

    public JsonStream(InputStreamSupplier inputStreamSupplier, Consumer<String> jsonConsumer) {
        super(inputStreamSupplier);
        this.jsonConsumer = jsonConsumer;
    }

    @Override
    protected void nextChar(char ch) {
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

    }

}
