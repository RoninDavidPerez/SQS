package Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Tournament {

    private final String name;
    private final BracketType bracketType;
    private final String description;
    private final Sport sport;
    private final List<Player> players = new ArrayList<>();
    private final Map<MatchFormat, List<List<TournamentMatch>>> rounds =
            new EnumMap<>(MatchFormat.class);
    private final Map<MatchFormat, Player> winners = new EnumMap<>(MatchFormat.class);
    private final Set<MatchFormat> activeFormats = EnumSet.noneOf(MatchFormat.class);

    private TournamentStatus status = TournamentStatus.SETUP;
    private Player winner;

    public Tournament(
            String name,
            BracketType bracketType,
            String description,
            Sport sport) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tournament name is required.");
        }
        if (bracketType == null || description == null || sport == null) {
            throw new IllegalArgumentException("Tournament details are required.");
        }
        this.name = name.trim();
        this.bracketType = bracketType;
        this.description = description.trim();
        this.sport = sport;
    }

    public boolean addPlayer(Player player) {
        if (status == TournamentStatus.COMPLETED || player == null
                || player.getName() == null || player.getName().trim().isEmpty()
                || players.contains(player)) {
            return false;
        }
        for (Player registeredPlayer : players) {
            if (registeredPlayer.getName() != null
                    && registeredPlayer.getName().equalsIgnoreCase(player.getName())) {
                return false;
            }
        }
        players.add(player);
        return true;
    }

    public boolean removePlayer(Player player) {
        return status == TournamentStatus.SETUP && players.remove(player);
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

    public Set<MatchFormat> getFormats() {
        Set<MatchFormat> formats = EnumSet.noneOf(MatchFormat.class);
        for (Player player : players) {
            formats.add(player.getFormat());
        }
        return Collections.unmodifiableSet(formats);
    }

    public Set<MatchFormat> getActiveFormats() {
        return Collections.unmodifiableSet(activeFormats);
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public List<Player> getPlayers(MatchFormat format) {
        List<Player> matchingPlayers = new ArrayList<>();
        for (Player player : players) {
            if (player.getFormat() == format) {
                matchingPlayers.add(player);
            }
        }
        return Collections.unmodifiableList(matchingPlayers);
    }

    public List<List<TournamentMatch>> getRounds(MatchFormat format) {
        List<List<TournamentMatch>> formatRounds = rounds.get(format);
        if (formatRounds == null) {
            return List.of();
        }
        List<List<TournamentMatch>> copy = new ArrayList<>();
        for (List<TournamentMatch> round : formatRounds) {
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

    public Player getWinner(MatchFormat format) {
        return winners.get(format);
    }

    public Map<MatchFormat, Player> getWinners() {
        return Collections.unmodifiableMap(winners);
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

    public void addRound(MatchFormat format, List<TournamentMatch> round) {
        rounds.computeIfAbsent(format, ignored -> new ArrayList<>()).add(new ArrayList<>(round));
    }

    public boolean addMatchToRound(MatchFormat format, int roundNumber, TournamentMatch match) {
        List<List<TournamentMatch>> formatRounds = rounds.get(format);
        if (status != TournamentStatus.IN_PROGRESS || match == null
                || roundNumber < 1 || formatRounds == null || roundNumber > formatRounds.size()) {
            return false;
        }
        formatRounds.get(roundNumber - 1).add(match);
        return true;
    }

    public void activateFormat(MatchFormat format) {
        activeFormats.add(format);
    }

    public void complete(MatchFormat format, Player winner) {
        winners.put(format, winner);
        if (!activeFormats.isEmpty() && winners.keySet().containsAll(activeFormats)) {
            this.winner = activeFormats.size() == 1 ? winners.get(activeFormats.iterator().next()) : null;
            this.status = TournamentStatus.COMPLETED;
        }
    }

    public void reset() {
        rounds.clear();
        winners.clear();
        activeFormats.clear();
        winner = null;
        status = TournamentStatus.SETUP;
    }
}