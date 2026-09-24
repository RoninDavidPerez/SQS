package Management;

import Collection.OnHoldList;
import Collection.QueueList;

import Model.Match;
import Model.MatchFormat;
import Model.Player;
import Model.PlayerStatus;
import Model.SkillLevel;

import java.util.List;

public class QueueService {

    private OnHoldList onHold;
    private QueueList queue;
    private MatchMaker matchMaker;

    public QueueService(
            OnHoldList onHold,
            QueueList queue,
            MatchMaker matchMaker) {

        this.onHold = onHold;
        this.queue = queue;
        this.matchMaker = matchMaker;
    }

    public void addPlayer(
            String name,
            MatchFormat format,
            SkillLevel skill) {

        Player player = new Player(
                name,
                format,
                skill
        );

        player.setStatus(PlayerStatus.OH_HOLD);

        onHold.add(player);
    }

    public OnHoldList getOnHoldList() {
        return onHold;
    }

    public List<Match> getAllPendingMatches() {
        return queue.getAllMatches();
    }

    public void completeMatch(Match match) {
        if (match == null) {
            return;
        }

        recordMatchResult(match);

        queue.removeMatch(match);
        for (Player player : match.getPlayers()) {
            onHold.add(player);
        }
    }

    private void recordMatchResult(Match match) {
        int scoreA = match.getTeamAScore();
        int scoreB = match.getTeamBScore();

        if (scoreA == scoreB) {
            return;
        }

        List<Player> winners = scoreA > scoreB ? match.getTeamAPlayers() : match.getTeamBPlayers();
        List<Player> losers = scoreA > scoreB ? match.getTeamBPlayers() : match.getTeamAPlayers();

        for (Player player : winners) {
            player.recordWin();
        }
        for (Player player : losers) {
            player.recordLoss();
        }
    }

    public void removeMatch(Match match) {
        completeMatch(match);
    }

    public void clearQueue() {
        for (Match match : queue.getAllMatches()) {
            if (match.getCourt() == null && match.getStatus() == Model.MatchStatus.PENDING) {
                removeMatch(match);
            }
        }
    }

    public void movePlayerToQueue(Player player) {

        onHold.remove(player);

        Match match =
                matchMaker.findCompatibleMatch(
                        player,
                        queue.getAllPendingMatches()
                );

        if (match == null) {

            match =
                    matchMaker.createNewMatch(player);

            match.addPlayer(player);

            queue.addPending(match);

        } else {

            match.addPlayer(player);

            if (match.getPlayers().size()
                    >= match.getFormat().getMaxPlayer()) {

                queue.markAsReady(match);
            }
        }

        player.setStatus(PlayerStatus.IN_QUEUE);
    }

    public Match createManualMatch(
            List<Player> players,
            MatchFormat format) {

        if (players == null || players.isEmpty()) {
            return null;
        }

        Match match = new Match(format);

        for (Player player : players) {

            onHold.remove(player);

            match.addPlayer(player);

            player.setStatus(
                    PlayerStatus.IN_QUEUE
            );
        }

        queue.addReady(match);

        return match;
    }
}