package Management;

import Model.BracketType;
import Model.Match;
import Model.MatchFormat;
import Model.Player;
import Model.Sport;
import Model.Tournament;
import Model.TournamentMatch;
import Model.TournamentStatus;

import java.util.ArrayList;
import java.util.List;

public class TournamentManager {

    public Tournament createTournament(
            String name,
            BracketType bracketType,
            String description,
            Sport sport,
            MatchFormat matchFormat) {
        if (sport != Sport.RACKET) {
            throw new IllegalArgumentException("Only racket tournaments are supported.");
        }
        if (bracketType != BracketType.SINGLE_ELIMINATION) {
            throw new IllegalArgumentException("Only single-elimination tournaments are supported.");
        }
        return new Tournament(name, bracketType, description, sport, matchFormat);
    }

    public boolean addPlayer(Tournament tournament, Player player) {
        requireTournament(tournament);
        return tournament.addPlayer(player);
    }

    public void startTournament(Tournament tournament) {
        requireTournament(tournament);
        tournament.start();
        createRound(tournament, new ArrayList<>(tournament.getPlayers()), 1);
    }

    public void recordWinner(
            Tournament tournament,
            TournamentMatch tournamentMatch,
            Player winner) {
        requireInProgress(tournament);
        if (tournamentMatch == null || tournamentMatch.isBye()) {
            throw new IllegalArgumentException("A playable tournament match is required.");
        }

        tournamentMatch.complete(winner);
        Match match = tournamentMatch.getMatch();
        if (winner == tournamentMatch.getPlayerA()) {
            match.addPointTeamA();
            tournamentMatch.getPlayerA().recordWin();
            tournamentMatch.getPlayerB().recordLoss();
        } else {
            match.addPointTeamB();
            tournamentMatch.getPlayerB().recordWin();
            tournamentMatch.getPlayerA().recordLoss();
        }
        match.end();

        List<TournamentMatch> currentRound = tournament.getRounds().get(tournamentMatch.getRound() - 1);
        if (isRoundComplete(currentRound)) {
            advanceRound(tournament, currentRound, tournamentMatch.getRound() + 1);
        }
    }

    private void advanceRound(
            Tournament tournament,
            List<TournamentMatch> completedRound,
            int nextRoundNumber) {
        List<Player> winners = new ArrayList<>();
        for (TournamentMatch tournamentMatch : completedRound) {
            winners.add(tournamentMatch.getWinner());
        }

        if (winners.size() == 1) {
            tournament.complete(winners.get(0));
            return;
        }
        createRound(tournament, winners, nextRoundNumber);
    }

    private void createRound(
            Tournament tournament,
            List<Player> players,
            int roundNumber) {
        List<TournamentMatch> round = new ArrayList<>();
        int matchNumber = 1;
        for (int index = 0; index < players.size(); index += 2) {
            Player playerA = players.get(index);
            Player playerB = index + 1 < players.size() ? players.get(index + 1) : null;
            Match match = null;

            if (playerB != null) {
                match = new Match(tournament.getMatchFormat());
                match.addPlayer(playerA);
                match.addPlayer(playerB);
            }

            TournamentMatch tournamentMatch = new TournamentMatch(
                    roundNumber,
                    matchNumber++,
                    playerA,
                    playerB,
                    match);
            if (playerB == null) {
                tournamentMatch.complete(playerA);
            }
            round.add(tournamentMatch);
        }
        tournament.addRound(round);

        if (isRoundComplete(round) && round.size() > 1) {
            advanceRound(tournament, round, roundNumber + 1);
        } else if (isRoundComplete(round)) {
            tournament.complete(round.get(0).getWinner());
        }
    }

    private boolean isRoundComplete(List<TournamentMatch> round) {
        for (TournamentMatch tournamentMatch : round) {
            if (!tournamentMatch.isCompleted()) {
                return false;
            }
        }
        return true;
    }

    private void requireTournament(Tournament tournament) {
        if (tournament == null) {
            throw new IllegalArgumentException("Tournament is required.");
        }
    }

    private void requireInProgress(Tournament tournament) {
        requireTournament(tournament);
        if (tournament.getStatus() != TournamentStatus.IN_PROGRESS) {
            throw new IllegalStateException("Tournament is not in progress.");
        }
    }
}