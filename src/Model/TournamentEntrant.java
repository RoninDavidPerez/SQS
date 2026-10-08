package Model;

import java.util.List;
import java.util.stream.Collectors;

public final class TournamentEntrant {

    private final List<Player> players;

    public TournamentEntrant(List<Player> players) {
        if (players == null || players.isEmpty()) {
            throw new IllegalArgumentException("A tournament entrant needs at least one player.");
        }
        this.players = List.copyOf(players);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Player getRepresentative() {
        return players.get(0);
    }

    public boolean contains(Player player) {
        return players.contains(player);
    }

    public String getDisplayName() {
        return players.stream()
                .map(Player::getName)
                .collect(Collectors.joining(" / "));
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}