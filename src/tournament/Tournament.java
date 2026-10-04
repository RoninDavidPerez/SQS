package tournament;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Single-elimination tournament model (supports byes for any player count). */
public class Tournament {

    private final List<Player> players = new ArrayList<Player>();
    private final List<List<Match>> rounds = new ArrayList<List<Match>>();
    private final List<Runnable> listeners = new ArrayList<Runnable>();
    private boolean started;
    private Player champion;

    // ---------- listeners ----------
    public void addChangeListener(Runnable r) { listeners.add(r); }

    private void fireChanged() {
        for (Runnable r : listeners) {
            r.run();
        }
    }

    // ---------- players ----------
    public void addPlayer(String name) {
        if (started) {
            throw new IllegalStateException("Tournament already started.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Enter a player name.");
        }
        name = name.trim();
        for (Player p : players) {
            if (p.getName().equalsIgnoreCase(name)) {
                throw new IllegalArgumentException("\"" + name + "\" is already registered.");
            }
        }
        players.add(new Player(name));
        fireChanged();
    }

    public void removePlayer(Player p) {
        if (started) {
            throw new IllegalStateException("Tournament already started.");
        }
        players.remove(p);
        fireChanged();
    }

    public List<Player> getPlayers() { return Collections.unmodifiableList(players); }
    public List<List<Match>> getRounds() { return Collections.unmodifiableList(rounds); }
    public boolean isStarted() { return started; }
    public Player getChampion() { return champion; }

    // ---------- bracket ----------
    public void start(boolean shuffle) {
        if (started) {
            throw new IllegalStateException("Tournament already started.");
        }
        if (players.size() < 2) {
            throw new IllegalStateException("Add at least 2 players.");
        }
        List<Player> seeds = new ArrayList<Player>(players);
        if (shuffle) {
            Collections.shuffle(seeds);
        }

        int size = 2;
        while (size < seeds.size()) {
            size *= 2;
        }
        int roundCount = Integer.numberOfTrailingZeros(size);

        rounds.clear();
        for (int r = 0; r < roundCount; r++) {
            int count = size >> (r + 1);
            List<Match> list = new ArrayList<Match>();
            for (int i = 0; i < count; i++) {
                list.add(new Match(r, i));
            }
            rounds.add(list);
        }
        for (int r = 0; r < roundCount - 1; r++) {
            List<Match> list = rounds.get(r);
            for (int i = 0; i < list.size(); i++) {
                Match m = list.get(i);
                m.next = rounds.get(r + 1).get(i / 2);
                m.feedsPlayer1 = (i % 2 == 0);
            }
        }

        // Seed round 1: match k gets player k vs player k + size/2 (null = bye).
        List<Match> first = rounds.get(0);
        int half = size / 2;
        for (int k = 0; k < half; k++) {
            Match m = first.get(k);
            m.setPlayer1(seeds.get(k));
            m.setPlayer2(k + half < seeds.size() ? seeds.get(k + half) : null);
        }
        started = true;
        champion = null;

        for (Match m : first) {
            if (m.getPlayer2() == null) {
                m.setBye(true);
                m.complete(m.getPlayer1(), 0, 0);
                advance(m);
            } else {
                m.setStatus(Match.Status.READY);
            }
        }
        fireChanged();
    }

    private void advance(Match m) {
        if (m.next == null) {
            champion = m.getWinner();
            return;
        }
        if (m.feedsPlayer1) {
            m.next.setPlayer1(m.getWinner());
        } else {
            m.next.setPlayer2(m.getWinner());
        }
        if (m.next.getPlayer1() != null && m.next.getPlayer2() != null) {
            m.next.setStatus(Match.Status.READY);
        }
    }

    // ---------- match actions ----------
    public void startMatch(Match m) {
        if (m.getStatus() != Match.Status.READY) {
            throw new IllegalStateException("That match is not ready to start.");
        }
        m.setStatus(Match.Status.IN_PROGRESS);
        fireChanged();
    }

    public void recordResult(Match m, int score1, int score2) {
        if (m.getStatus() != Match.Status.IN_PROGRESS) {
            throw new IllegalStateException("Start the match before recording a result.");
        }
        if (score1 == score2) {
            throw new IllegalArgumentException("Scores can't be tied in a knockout match.");
        }
        m.complete(score1 > score2 ? m.getPlayer1() : m.getPlayer2(), score1, score2);
        advance(m);
        fireChanged();
    }

    public void reset() {
        rounds.clear();
        started = false;
        champion = null;
        fireChanged();
    }

    // ---------- progress ----------
    /** Matches that actually get played (byes excluded). */
    public int totalMatches() {
        return started ? players.size() - 1 : 0;
    }

    public int completedMatches() {
        int n = 0;
        for (List<Match> list : rounds) {
            for (Match m : list) {
                if (m.getStatus() == Match.Status.COMPLETED && !m.isBye()) {
                    n++;
                }
            }
        }
        return n;
    }

    public String roundName(int r) {
        int fromEnd = rounds.size() - 1 - r;
        switch (fromEnd) {
            case 0: return "Final";
            case 1: return "Semifinals";
            case 2: return "Quarterfinals";
            default: return "Round " + (r + 1);
        }
    }

    public String progressText() {
        if (!started) {
            return "Registration open - " + players.size() + " player(s)";
        }
        if (champion != null) {
            return "Tournament complete - Champion: " + champion.getName();
        }
        int current = 0;
        for (int r = 0; r < rounds.size() && current == 0; r++) {
            for (Match m : rounds.get(r)) {
                if (m.getStatus() != Match.Status.COMPLETED) {
                    current = r + 1;
                    break;
                }
            }
        }
        return "Now playing: " + roundName(Math.max(current - 1, 0))
                + " - " + completedMatches() + " of " + totalMatches() + " matches done";
    }
}
