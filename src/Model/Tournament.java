package Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Tournament {

    private final String name;
    private final BracketType bracketType;
    private final String description;
    private final Sport sport;
    private final MatchFormat matchFormat;
    private final List<Player> players = new ArrayList<>();
    private final List<List<TournamentMatch>> rounds = new ArrayList<>();

    private TournamentStatus status = TournamentStatus.SETUP;
    private Player winner;

    public Tournament(
            String name,
            BracketType bracketType,
            String description,
            Sport sport,
            MatchFormat matchFormat) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tournament name is required.");
        }
        if (bracketType == null || description == null || sport == null || matchFormat == null) {
            throw new IllegalArgumentException("Tournament details are required.");
        }
        this.name = name.trim();
        this.bracketType = bracketType;
        this.description = description.trim();
        this.sport = sport;
        this.matchFormat = matchFormat;
    }

    public boolean addPlayer(Player player) {
        if (status != TournamentStatus.SETUP || player == null || players.contains(player)) {
            return false;
        }
        players.add(player);
        return true;
    }

    public String getName() {
        return name;
    }

    public BracketType getBracketType() {
        return bracketType;
    }

    public String getDescription() {
        return description;
    }

    public Sport getSport() {
        return sport;
    }

    public MatchFormat getMatchFormat() {
        return matchFormat;
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public List<List<TournamentMatch>> getRounds() {
        List<List<TournamentMatch>> copy = new ArrayList<>();
        for (List<TournamentMatch> round : rounds) {
            copy.add(Collections.unmodifiableList(round));
        }
        return Collections.unmodifiableList(copy);
    }

    public TournamentStatus getStatus() {
        return status;
    }

    public Player getWinner() {
        return winner;
    }

    public void start() {
        if (players.size() < 2) {
            throw new IllegalStateException("At least two players are required.");
        }
        if (status != TournamentStatus.SETUP) {
            throw new IllegalStateException("Tournament has already started.");
        }
        status = TournamentStatus.IN_PROGRESS;
    }

    public void addRound(List<TournamentMatch> round) {
        rounds.add(new ArrayList<>(round));
    }

    public void complete(Player winner) {
        this.winner = winner;
        this.status = TournamentStatus.COMPLETED;
    }
}