package Model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Match {

    private final List<Player> players = new ArrayList<>();

    private Court court;
    private final MatchFormat format;

    private LocalDateTime endTime;

    private MatchStatus status;

    private int teamAScore;
    private int teamBScore;
    private Duration pausedRemaining;
    private ScheduledFuture<?> endTask;

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    public Match(MatchFormat format) {

        this.format = format;
        this.status = MatchStatus.PENDING;

        teamAScore = 0;
        teamBScore = 0;
    }

    public void start(int durationMinutes) {

        this.status = MatchStatus.RUNNING;
        this.endTime = LocalDateTime.now().plusMinutes(durationMinutes);

        for (Player player : players) {
            player.setStatus(PlayerStatus.IN_MATCH);
        }

        scheduleEndTask(Duration.ofMinutes(durationMinutes));
    }

    public void resume() {
        if (status != MatchStatus.PAUSED) {
            return;
        }

        Duration remaining = pausedRemaining != null ? pausedRemaining : Duration.ZERO;

        this.status = MatchStatus.RUNNING;
        this.endTime = LocalDateTime.now().plus(remaining);

        scheduleEndTask(remaining);
    }

    private void scheduleEndTask(Duration delay) {
        long millis = Math.max(0, delay.toMillis());
        endTask = scheduler.schedule(this::end, millis, TimeUnit.MILLISECONDS);
    }

    public void pause() {
        if (status != MatchStatus.RUNNING) {
            return;
        }
        pausedRemaining = Duration.between(
                LocalDateTime.now(),
                endTime);
        if (pausedRemaining.isNegative()) {
            pausedRemaining = Duration.ZERO;
        }
        if (endTask != null) {
            endTask.cancel(false);
        }
        status = MatchStatus.PAUSED;
    }

    public void end() {

        if (endTask != null) {
            endTask.cancel(false);
        }

        this.status = MatchStatus.FINISHED;

        for (Player player : players) {
            player.setStatus(PlayerStatus.OH_HOLD);
        }

        if (court != null) {
            court.freeCourt();
        }

        scheduler.shutdownNow();
    }

    public void addPointTeamA() {
        teamAScore++;
    }

    public void addPointTeamB() {
        teamBScore++;
    }

    public int getTeamAScore() {
        return teamAScore;
    }

    public int getTeamBScore() {
        return teamBScore;
    }

    public String getScore() {
        return teamAScore + " - " + teamBScore;
    }

    public String getWinner() {

        if (status != MatchStatus.FINISHED) {
            return "Match not finished";
        }

        if (teamAScore > teamBScore) {
            return "Team A";
        }

        if (teamBScore > teamAScore) {
            return "Team B";
        }

        return "Draw";
    }

    public String getRemainingTime() {

        if (status == MatchStatus.PAUSED && pausedRemaining != null) {
            return formatDuration(pausedRemaining);
        }

        if (endTime == null) {
            return "00:00";
        }

        Duration remaining =
                Duration.between(
                        LocalDateTime.now(),
                        endTime
                );

        if (remaining.isNegative()) {
            return "00:00";
        }

        long minutes = remaining.toMinutes();
        long seconds =
                remaining.minusMinutes(minutes)
                        .getSeconds();

        return formatDuration(Duration.ofMinutes(minutes).plusSeconds(seconds));
    }

    private String formatDuration(Duration duration) {
        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();
        return String.format("%02d:%02d", minutes, seconds);
    }

    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<Player> getTeamAPlayers() {
        int half = players.size() / 2;
        return new ArrayList<>(players.subList(0, half));
    }

    public List<Player> getTeamBPlayers() {
        int half = players.size() / 2;
        return new ArrayList<>(players.subList(half, players.size()));
    }

    public MatchFormat getFormat() {
        return format;
    }

    public Court getCourt() {
        return court;
    }

    public void setCourt(Court court) {
        this.court = court;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }
}