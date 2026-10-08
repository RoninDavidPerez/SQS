package Model;

import java.util.List;

public class TournamentMatch {

    public enum Section {
        WINNERS("Winners Bracket"),
        LOSERS("Losers Bracket"),
        GRAND_FINAL("Grand Final"),
        RESET_FINAL("Reset Final"),
        ROUND_ROBIN("Round Robin");

        private final String displayName;

        Section(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private final int round;
    private final int matchNumber;
    private final Section section;
    private final TournamentEntrant entrantA;
    private final TournamentEntrant entrantB;
    private final Match match;
    private TournamentEntrant winner;
    private boolean completed;

    public TournamentMatch(
            int round,
            int matchNumber,
            Player playerA,
            Player playerB,
            Match match) {
        this(round, matchNumber,
                playerA == null ? null : new TournamentEntrant(List.of(playerA)),
                playerB == null ? null : new TournamentEntrant(List.of(playerB)),
                match,
                Section.WINNERS);
    }

    public TournamentMatch(
            int round,
            int matchNumber,
            TournamentEntrant entrantA,
            TournamentEntrant entrantB,
            Match match) {
        this(round, matchNumber, entrantA, entrantB, match, Section.WINNERS);
    }

    public TournamentMatch(
            int round,
            int matchNumber,
            TournamentEntrant entrantA,
            TournamentEntrant entrantB,
            Match match,
            Section section) {
        this.round = round;
        this.matchNumber = matchNumber;
        this.section = section;
        this.entrantA = entrantA;
        this.entrantB = entrantB;
        this.match = match;
    }

    public int getRound() {
        return round;
    }

    public int getMatchNumber() {
        return matchNumber;
    }

    public Section getSection() {
        return section;
    }

    public Player getPlayerA() {
        return entrantA == null ? null : entrantA.getRepresentative();
    }

    public Player getPlayerB() {
        return entrantB == null ? null : entrantB.getRepresentative();
    }

    public TournamentEntrant getEntrantA() {
        return entrantA;
    }

    public TournamentEntrant getEntrantB() {
        return entrantB;
    }

    public List<Player> getTeamAPlayers() {
        return entrantA == null ? List.of() : entrantA.getPlayers();
    }

    public List<Player> getTeamBPlayers() {
        return entrantB == null ? List.of() : entrantB.getPlayers();
    }

    public Match getMatch() {
        return match;
    }

    public Player getWinner() {
        return winner == null ? null : winner.getRepresentative();
    }

    public TournamentEntrant getWinnerEntrant() {
        return winner;
    }

    public TournamentEntrant getLoserEntrant() {
        if (winner == null || isBye()) {
            return null;
        }
        return winner == entrantA ? entrantB : entrantA;
    }

    public boolean isBye() {
        return entrantA == null || entrantB == null;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void complete(Player winner) {
        if (entrantA != null && entrantA.contains(winner)) {
            complete(entrantA);
        } else if (entrantB != null && entrantB.contains(winner)) {
            complete(entrantB);
        } else {
            throw new IllegalArgumentException("Winner must be one of the tournament entrants.");
        }
    }

    public void complete(TournamentEntrant winner) {
        if (completed) {
            throw new IllegalStateException("This tournament match is already complete.");
        }
        if (winner != entrantA && winner != entrantB) {
            throw new IllegalArgumentException("Winner must be one of the tournament entrants.");
        }
        this.winner = winner;
        this.completed = true;
    }
}