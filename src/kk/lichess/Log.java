package kk.lichess;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.WeakHashMap;
import java.util.function.Consumer;

public class Log {

    private static final int LOG_ENABLE_MASK = Type.bitSetOf(
            Type.Error,
            Type.Warning,
            Type.Info,
            Type.Debug
//            Type.Verbose
    );
    private static final PrintStream[] streams;

    private static EnumMap<Type, PrintStream> streamMap = new EnumMap<>(Type.class) {{
        put(Type.Error, System.err);
        put(Type.Warning, System.err);
        put(Type.Info, System.out);
        put(Type.Debug, System.out);
        put(Type.Verbose, System.out);
    }};

    static {
        streams = new PrintStream[Type.values().length];
        for (Type type : Type.values()) {
            if ((LOG_ENABLE_MASK & type.bitMask) != 0 && !streamMap.containsKey(type)) {
                System.err.println("Log: print stream not set for enabled log type " + type + ". Using System.out.");
                streamMap.put(type, System.out);
            }
        }
        streamMap.forEach((key, value) -> streams[key.ordinal()] = value);
    }

    public static void i(String msg) {
        log(Type.Info, msg);
    }

    public static void v(String msg) {
        log(Type.Verbose, msg);
    }

    public static void d(String msg) {
        log(Type.Debug, msg);
    }

    public static void d(String tag, String msg) {
        log(Type.Debug, tag + ":" + msg);
    }

    public static void w(String msg) {
        log(Type.Warning, msg);
    }

    public static void e(String msg) {
        log(Type.Error, msg);
    }

    public static void e(String msg, Throwable t) {
        log(Type.Error, msg);
        log(Type.Error, t::printStackTrace);
    }

    public static void log(Type type, Consumer<PrintStream> streamConsumer) {
        if ((LOG_ENABLE_MASK & type.bitMask) == 0)
            return;

        PrintStream printStream = streams[type.ordinal()];
        printStream.print(timestamp() + " | ");
        streamConsumer.accept(printStream);
        printStream.println();
    }

    public static void log(Type type, String text) {
        if ((LOG_ENABLE_MASK & type.bitMask) == 0)
            return;
        log(type, ps -> ps.print(text));
    }

    private static String timestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).replaceAll("\\.[^.]*", "");
    }

    public enum Type {
        Error, Warning, Info, Debug, Verbose;
        private final int bitMask = 1 << this.ordinal();

        private static int bitSetOf(Type... types) {
            int mask = 0;
            for (Type type : types)
                mask |= type.bitMask;
            return mask;
        }
    }
}
