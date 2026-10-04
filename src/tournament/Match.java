package tournament;

/** One match in the bracket. Winners advance to {@link #next}. */
public class Match {

    public enum Status { WAITING, READY, IN_PROGRESS, COMPLETED }

    private final int round;
    private final int index;
    private Player player1;
    private Player player2;
    private Player winner;
    private int score1;
    private int score2;
    private Status status = Status.WAITING;
    private boolean bye;

    Match next;
    boolean feedsPlayer1;

    public Match(int round, int index) {
        this.round = round;
        this.index = index;
    }

    public int getRound() { return round; }
    public int getIndex() { return index; }
    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }
    public Player getWinner() { return winner; }
    public int getScore1() { return score1; }
    public int getScore2() { return score2; }
    public Status getStatus() { return status; }
    public boolean isBye() { return bye; }
    public Match getNext() { return next; }

    void setPlayer1(Player p) { this.player1 = p; }
    void setPlayer2(Player p) { this.player2 = p; }
    void setStatus(Status s) { this.status = s; }
    void setBye(boolean bye) { this.bye = bye; }

    void complete(Player winner, int s1, int s2) {
        this.winner = winner;
        this.score1 = s1;
        this.score2 = s2;
        this.status = Status.COMPLETED;
    }

    /** Short label such as "R1 M3". */
    public String label() {
        return "R" + (round + 1) + " M" + (index + 1);
    }

    @Override
    public String toString() {
        String a = player1 == null ? "TBD" : player1.getName();
        String b = player2 == null ? "TBD" : player2.getName();
        return label() + ": " + a + " vs " + b;
    }
}
