package kk.lichess;

public enum Side {
    White, Black;

    public boolean isWhite() {
        return this == White;
    }

    public Side other() {
        return this == White ? Black : White;
    }
}
