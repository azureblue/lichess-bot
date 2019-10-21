package kk.lichess.api;

public enum Side {
    White, Black, Random;

    public static Side fromChar(char ch) {
        if (ch < 'a')
            ch += 32;
        if (ch == 'w')
            return White;
        if (ch == 'b')
            return Black;
        if (ch == 'r')
            return Random;

        throw new IllegalArgumentException("invalid character: " + ch);
    }
}
