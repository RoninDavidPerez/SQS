package Model;

public class TournamentMatch {

    private final int round;
    private final int matchNumber;
    private final Player playerA;
    private final Player playerB;
    private final Match match;
    private Player winner;
    private boolean completed;

    public TournamentMatch(
            int round,
            int matchNumber,
            Player playerA,
            Player playerB,
            Match match) {
        this.round = round;
        this.matchNumber = matchNumber;
        this.playerA = playerA;
        this.playerB = playerB;
        this.match = match;
    }

    public int getRound() {
        return round;
    }

    public int getMatchNumber() {
        return matchNumber;
    }

    public Player getPlayerA() {
        return playerA;
    }

    public Player getPlayerB() {
        return playerB;
    }

    public Match getMatch() {
        return match;
    }

    public Player getWinner() {
        return winner;
    }

    public boolean isBye() {
        return playerA == null || playerB == null;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void complete(Player winner) {
        if (completed) {
            throw new IllegalStateException("This tournament match is already complete.");
        }
        if (winner != playerA && winner != playerB) {
            throw new IllegalArgumentException("Winner must be one of the tournament players.");
        }
        this.winner = winner;
        this.completed = true;
    }
}