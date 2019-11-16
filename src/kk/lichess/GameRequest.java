package kk.lichess;

public class GameRequest {

    public enum Side {
        White, Black, Random;
    }

    private final String requesterId;
    private final int requesterRating;

    private final int time;
    private final int timeBonus;

    private final Side side;
    private final boolean ranking;

    public GameRequest(String requesterId, int requesterRating, int time, int timeBonus, Side side, boolean ranking) {
        this.requesterId = requesterId;
        this.requesterRating = requesterRating;
        this.time = time;
        this.timeBonus = timeBonus;
        this.side = side;
        this.ranking = ranking;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public int getRequesterRating() {
        return requesterRating;
    }

    public int getTime() {
        return time;
    }

    public int getTimeBonus() {
        return timeBonus;
    }

    public Side getSide() {
        return side;
    }

    public boolean isRanking() {
        return ranking;
    }
}
