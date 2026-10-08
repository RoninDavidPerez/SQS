package Management;

import Model.BracketType;
import Model.Match;
import Model.MatchFormat;
import Model.Player;
import Model.PlayerStatus;
import Model.SkillLevel;
import Model.Sport;
import Model.Tournament;
import Model.TournamentEntrant;
import Model.TournamentMatch;
import Model.TournamentStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TournamentManager {

    public Tournament createTournament(
            String name,
            BracketType bracketType,
            String description,
            Sport sport) {
        if (sport != Sport.RACKET) {
            throw new IllegalArgumentException("Only racket tournaments are supported.");
        }
        if (bracketType == null) {
            throw new IllegalArgumentException("Choose a bracket type.");
        }
        return new Tournament(name, bracketType, description, sport);
    }

    public boolean addPlayer(Tournament tournament, Player player) {
        requireTournament(tournament);
        boolean lateEntrant = tournament.getStatus() == TournamentStatus.IN_PROGRESS;
        if (lateEntrant && !canAcceptLateEntrants(tournament, player.getFormat())) {
            return false;
        }
        if (!tournament.addPlayer(player)) {
            return false;
        }
        if (lateEntrant) {
            appendLateEntrantMatches(tournament, player.getFormat());
        }
        return true;
    }

    public void startTournament(Tournament tournament) {
        startTournament(tournament, false);
    }

    public void refreshBracketPreview(Tournament tournament) {
        requireTournament(tournament);
        if (tournament.getStatus() != TournamentStatus.SETUP) {
            return;
        }

        tournament.reset();
        for (MatchFormat format : tournament.getFormats()) {
            List<TournamentEntrant> entrants = getEntrants(tournament, format);
            if (entrants.size() < 2) {
                continue;
            }
            switch (tournament.getBracketType()) {
                case SINGLE_ELIMINATION:
                    tournament.addRound(format, createRoundMatches(
                            tournament, format, entrants, 1, false));
                    break;
                case DOUBLE_ELIMINATION:
                    tournament.addRound(format, createRoundMatches(
                            tournament, format, entrants, 1, false,
                            TournamentMatch.Section.WINNERS));
                    break;
                case ROUND_ROBIN:
                    addRoundRobinSchedule(tournament, format, entrants);
                    break;
            }
        }
    }

    public Player registerPlayer(
            Tournament tournament,
            String name,
            MatchFormat matchFormat,
            SkillLevel skillLevel) {
        requireTournament(tournament);
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Enter a player name.");
        }
        if (matchFormat == null || skillLevel == null) {
            throw new IllegalArgumentException("Player format and skill level are required.");
        }
        Player player = new Player(name.trim(), matchFormat, skillLevel);
        if (!addPlayer(tournament, player)) {
            throw new IllegalArgumentException("That player is already registered or registration is closed.");
        }
        return player;
    }

    public boolean removePlayer(Tournament tournament, Player player) {
        requireTournament(tournament);
        return tournament.removePlayer(player);
    }

    public void resetTournament(Tournament tournament) {
        requireTournament(tournament);
        for (Player player : tournament.getPlayers()) {
            player.setStatus(PlayerStatus.OH_HOLD);
        }
        tournament.reset();
    }

    public void startTournament(Tournament tournament, boolean shuffle) {
        requireTournament(tournament);
        if (tournament.getStatus() == TournamentStatus.IN_PROGRESS) {
            return;
        }
        if (tournament.getFormats().isEmpty()) {
            throw new IllegalStateException("Add players before starting the tournament.");
        }
        for (MatchFormat format : tournament.getFormats()) {
            validateFormatRoster(tournament, format);
            if (getEntrants(tournament, format).size() < 2) {
                String required = format == MatchFormat.DOUBLE ? "four players" : "two players";
                throw new IllegalStateException(format.getDisplayName()
                        + " needs at least " + required + " to form a bracket.");
            }
        }
        tournament.reset();
        for (MatchFormat format : tournament.getFormats()) {
            List<TournamentEntrant> entrants = getEntrants(tournament, format);
            if (shuffle) {
                Collections.shuffle(entrants);
            }
            tournament.activateFormat(format);
            if (tournament.getBracketType() == BracketType.ROUND_ROBIN) {
                addRoundRobinSchedule(tournament, format, entrants);
            } else {
                tournament.addRound(format, createRoundMatches(
                        tournament, format, entrants, 1, false,
                        TournamentMatch.Section.WINNERS));
            }
        }
        tournament.start();
        for (MatchFormat format : tournament.getActiveFormats()) {
            List<TournamentMatch> firstRound = tournament.getRounds(format).get(0);
            if (tournament.getBracketType() != BracketType.ROUND_ROBIN) {
                for (TournamentMatch match : firstRound) {
                    if (match.isBye() && !match.isCompleted()) {
                        match.complete(match.getEntrantA());
                    }
                }
                if (isRoundComplete(firstRound)) {
                    advanceRound(tournament, format, firstRound, 2);
                }
            }
        }
    }

    public TournamentMatch getNextMatchForPlayer(Tournament tournament, Player player) {
        requireTournament(tournament);
        if (player == null) {
            return null;
        }
        if (tournament.getStatus() != TournamentStatus.IN_PROGRESS) {
            return null;
        }
        List<TournamentMatch> currentRound = getCurrentRound(tournament, player.getFormat());
        for (TournamentMatch match : currentRound) {
            if (!match.isCompleted() && !match.isBye()
                    && match.getMatch().getStatus() == Model.MatchStatus.PENDING
                && (match.getTeamAPlayers().contains(player)
                    || match.getTeamBPlayers().contains(player))) {
                return match;
            }
        }
        return null;
    }

    public boolean recordCompletedCourtMatch(Tournament tournament, Match courtMatch) {
        requireTournament(tournament);
        if (tournament.getStatus() != TournamentStatus.IN_PROGRESS || courtMatch == null
                || courtMatch.getStatus() != Model.MatchStatus.FINISHED
                || courtMatch.getTeamAScore() == courtMatch.getTeamBScore()) {
            return false;
        }

        TournamentMatch tournamentMatch = null;
        MatchFormat matchFormat = null;
        for (MatchFormat format : tournament.getActiveFormats()) {
            for (List<TournamentMatch> round : tournament.getRounds(format)) {
                for (TournamentMatch match : round) {
                    if (match.getMatch() == courtMatch) {
                        tournamentMatch = match;
                        matchFormat = format;
                        break;
                    }
                }
                if (tournamentMatch != null) {
                    break;
                }
            }
            if (tournamentMatch != null) {
                break;
            }
        }
        if (tournamentMatch == null || tournamentMatch.isCompleted()
                || !getCurrentRound(tournament, matchFormat).contains(tournamentMatch)) {
            return false;
        }

        TournamentEntrant winner = courtMatch.getTeamAScore() > courtMatch.getTeamBScore()
            ? tournamentMatch.getEntrantA()
            : tournamentMatch.getEntrantB();
        TournamentEntrant loser = winner == tournamentMatch.getEntrantA()
            ? tournamentMatch.getEntrantB()
            : tournamentMatch.getEntrantA();
        tournamentMatch.complete(winner);
        recordEntrantResult(winner, loser);

        List<TournamentMatch> round = tournament.getRounds(matchFormat).get(tournamentMatch.getRound() - 1);
        if (isRoundComplete(round)) {
            processCompletedRound(tournament, matchFormat, round);
        }
        return true;
    }

    public void startMatch(Tournament tournament, TournamentMatch tournamentMatch) {
        requireCurrentMatch(tournament, tournamentMatch);
        Match match = tournamentMatch.getMatch();
        if (tournamentMatch.isCompleted() || match.getStatus() != Model.MatchStatus.PENDING) {
            throw new IllegalStateException("That match is not ready to start.");
        }
        match.setStatus(Model.MatchStatus.RUNNING);
        for (Player player : tournamentMatch.getTeamAPlayers()) {
            player.setStatus(PlayerStatus.IN_MATCH);
        }
        for (Player player : tournamentMatch.getTeamBPlayers()) {
            player.setStatus(PlayerStatus.IN_MATCH);
        }
    }

    public void recordResult(
            Tournament tournament,
            TournamentMatch tournamentMatch,
            int scoreA,
            int scoreB) {
        requireCurrentMatch(tournament, tournamentMatch);
        if (tournamentMatch.getMatch().getStatus() != Model.MatchStatus.RUNNING) {
            throw new IllegalStateException("Start the match before recording a result.");
        }
        if (scoreA == scoreB) {
            throw new IllegalArgumentException("Scores can't be tied in a knockout match.");
        }

        TournamentEntrant winner = scoreA > scoreB
            ? tournamentMatch.getEntrantA()
            : tournamentMatch.getEntrantB();
        TournamentEntrant loser = winner == tournamentMatch.getEntrantA()
            ? tournamentMatch.getEntrantB()
            : tournamentMatch.getEntrantA();
        Match match = tournamentMatch.getMatch();
        match.setScore(scoreA, scoreB);
        tournamentMatch.complete(winner);
        recordEntrantResult(winner, loser);
        match.end();

        MatchFormat format = tournamentMatch.getTeamAPlayers().get(0).getFormat();
        List<TournamentMatch> currentRound =
                tournament.getRounds(format).get(tournamentMatch.getRound() - 1);
        if (isRoundComplete(currentRound)) {
            processCompletedRound(tournament, format, currentRound);
        }
    }

    public List<TournamentMatch> getCurrentRound(Tournament tournament, MatchFormat format) {
        requireTournament(tournament);
        List<List<TournamentMatch>> rounds = tournament.getRounds(format);
        for (List<TournamentMatch> round : rounds) {
            if (!isRoundComplete(round)) {
                return round;
            }
        }
        return List.of();
    }

    public void recordWinner(
            Tournament tournament,
            TournamentMatch tournamentMatch,
            Player winner) {
        requireCurrentMatch(tournament, tournamentMatch);
        TournamentEntrant winnerEntrant = tournamentMatch.getEntrantA().contains(winner)
            ? tournamentMatch.getEntrantA()
            : tournamentMatch.getEntrantB().contains(winner)
                ? tournamentMatch.getEntrantB()
                : null;
        if (winnerEntrant == null) {
            throw new IllegalArgumentException("Winner must be one of the tournament players.");
        }
        if (tournamentMatch.getMatch().getStatus() == Model.MatchStatus.PENDING) {
            startMatch(tournament, tournamentMatch);
        }
        recordResult(tournament, tournamentMatch,
            winnerEntrant == tournamentMatch.getEntrantA() ? 1 : 0,
            winnerEntrant == tournamentMatch.getEntrantB() ? 1 : 0);
    }

    public int getTotalMatchCount(Tournament tournament) {
        requireTournament(tournament);
        if (tournament.getStatus() == TournamentStatus.SETUP) {
            return 0;
        }
        int total = 0;
        for (MatchFormat format : tournament.getActiveFormats()) {
            TournamentMatch grandFinal = null;
            boolean hasResetFinal = false;
            for (List<TournamentMatch> round : tournament.getRounds(format)) {
                for (TournamentMatch match : round) {
                    if (!match.isBye()) {
                        total++;
                    }
                    if (match.getSection() == TournamentMatch.Section.GRAND_FINAL) {
                        grandFinal = match;
                    } else if (match.getSection() == TournamentMatch.Section.RESET_FINAL) {
                        hasResetFinal = true;
                    }
                }
            }
            boolean mayNeedReset = grandFinal != null
                    && (!grandFinal.isCompleted()
                            || grandFinal.getWinnerEntrant() == grandFinal.getEntrantB());
            if (tournament.getBracketType() == BracketType.DOUBLE_ELIMINATION
                    && mayNeedReset && !hasResetFinal) {
                total++;
            }
        }
        return total;
    }

    public int getCompletedMatchCount(Tournament tournament) {
        requireTournament(tournament);
        int completed = 0;
        for (MatchFormat format : tournament.getActiveFormats()) {
            for (List<TournamentMatch> round : tournament.getRounds(format)) {
                for (TournamentMatch match : round) {
                    if (!match.isBye() && match.isCompleted()) {
                        completed++;
                    }
                }
            }
        }
        return completed;
    }

    public String getProgressText(Tournament tournament) {
        requireTournament(tournament);
        if (tournament.getStatus() == TournamentStatus.SETUP) {
            return "Registration open - " + tournament.getPlayers().size() + " player(s)";
        }
        if (tournament.getStatus() == TournamentStatus.COMPLETED) {
            List<String> champions = new ArrayList<>();
            for (MatchFormat format : tournament.getActiveFormats()) {
                Player champion = tournament.getWinner(format);
                if (champion != null) {
                    champions.add(format.getDisplayName() + ": " + champion.getName());
                }
            }
            return "Tournament complete - " + String.join("; ", champions);
        }
        for (MatchFormat format : tournament.getActiveFormats()) {
            for (List<TournamentMatch> round : tournament.getRounds(format)) {
                for (TournamentMatch match : round) {
                    if (!match.isCompleted()) {
                        return format.getDisplayName() + " - "
                                + getRoundName(tournament, format, match.getRound())
                                + " - " + getCompletedMatchCount(tournament) + " of "
                                + getTotalMatchCount(tournament) + " matches done";
                    }
                }
            }
        }
        return "Tournament in progress";
    }

    public String getStandingsText(Tournament tournament, MatchFormat format) {
        requireTournament(tournament);
        if (tournament.getBracketType() != BracketType.ROUND_ROBIN) {
            return "";
        }
        Map<Player, int[]> stats = new java.util.IdentityHashMap<>();
        for (TournamentEntrant entrant : getEntrants(tournament, format)) {
            stats.put(entrant.getRepresentative(), new int[2]);
        }
        for (List<TournamentMatch> round : tournament.getRounds(format)) {
            for (TournamentMatch match : round) {
                if (!match.isCompleted() || match.getMatch() == null) {
                    continue;
                }
                Player playerA = match.getEntrantA().getRepresentative();
                Player playerB = match.getEntrantB().getRepresentative();
                int scoreA = match.getMatch().getTeamAScore();
                int scoreB = match.getMatch().getTeamBScore();
                stats.get(playerA)[0] += scoreA > scoreB ? 1 : 0;
                stats.get(playerB)[0] += scoreB > scoreA ? 1 : 0;
                stats.get(playerA)[1] += scoreA - scoreB;
                stats.get(playerB)[1] += scoreB - scoreA;
            }
        }
        List<Player> ranking = new ArrayList<>(stats.keySet());
        ranking.sort((left, right) -> compareStanding(left, right, stats, tournament));
        List<String> entries = new ArrayList<>();
        for (int index = 0; index < ranking.size(); index++) {
            Player player = ranking.get(index);
            int[] row = stats.get(player);
            String label = format == MatchFormat.DOUBLE
                    ? getEntrantDisplayName(tournament, player)
                    : player.getName();
            entries.add((index + 1) + ". " + label + " - "
                    + row[0] + " win(s), point diff " + row[1]);
        }
        return entries.isEmpty() ? "Standings will appear after players are registered."
                : "Standings (wins, then point differential): " + String.join("   |   ", entries);
    }

    private String getEntrantDisplayName(Tournament tournament, Player representative) {
        for (TournamentEntrant entrant : getEntrants(tournament, MatchFormat.DOUBLE)) {
            if (entrant.getRepresentative() == representative) {
                return entrant.getDisplayName();
            }
        }
        return representative.getName();
    }

    public String getRoundName(Tournament tournament, MatchFormat format, int roundNumber) {
        if (tournament.getBracketType() == BracketType.DOUBLE_ELIMINATION) {
            return "Phase " + roundNumber;
        }
        if (tournament.getBracketType() == BracketType.ROUND_ROBIN) {
            return "Round " + roundNumber;
        }
        int fromEnd = tournament.getRounds(format).size() - roundNumber;
        switch (fromEnd) {
            case 0: return "Final";
            case 1: return "Semifinals";
            case 2: return "Quarterfinals";
            default: return "Round " + roundNumber;
        }
    }

    private void advanceRound(
            Tournament tournament,
            MatchFormat format,
            List<TournamentMatch> completedRound,
            int nextRoundNumber) {
        List<TournamentEntrant> winners = new ArrayList<>();
        for (TournamentMatch tournamentMatch : completedRound) {
            winners.add(tournamentMatch.getWinnerEntrant());
        }

        if (winners.size() == 1) {
            tournament.complete(format, winners.get(0).getRepresentative());
            return;
        }
        createRound(tournament, format, winners, nextRoundNumber);
    }

    private void processCompletedRound(
            Tournament tournament,
            MatchFormat format,
            List<TournamentMatch> completedRound) {
        switch (tournament.getBracketType()) {
            case SINGLE_ELIMINATION:
                advanceRound(tournament, format, completedRound,
                        completedRound.get(0).getRound() + 1);
                break;
            case DOUBLE_ELIMINATION:
                advanceDoubleElimination(tournament, format, completedRound);
                break;
            case ROUND_ROBIN:
                if (getCurrentRound(tournament, format).isEmpty()) {
                    tournament.complete(format, getRoundRobinChampion(tournament, format));
                }
                break;
        }
    }

    private void advanceDoubleElimination(
            Tournament tournament,
            MatchFormat format,
            List<TournamentMatch> completedRound) {
        int nextRoundNumber = completedRound.get(0).getRound() + 1;
        TournamentMatch grandFinal = findSectionMatch(
                completedRound, TournamentMatch.Section.GRAND_FINAL);
        if (grandFinal != null) {
            TournamentMatch finalMatch = grandFinal;
            if (finalMatch.getWinnerEntrant() == finalMatch.getEntrantA()) {
                tournament.complete(format, finalMatch.getWinner());
            } else {
                tournament.addRound(format, List.of(createTournamentMatch(
                        format, finalMatch.getEntrantA(), finalMatch.getEntrantB(),
                        nextRoundNumber, 1, TournamentMatch.Section.RESET_FINAL)));
            }
            return;
        }
        TournamentMatch resetFinal = findSectionMatch(
                completedRound, TournamentMatch.Section.RESET_FINAL);
        if (resetFinal != null) {
            tournament.complete(format, resetFinal.getWinner());
            return;
        }

        List<TournamentEntrant> undefeated = new ArrayList<>();
        List<TournamentEntrant> lowerBracket = new ArrayList<>();
        boolean hasLosersMatches = false;
        for (TournamentMatch match : completedRound) {
            if (match.getSection() == TournamentMatch.Section.WINNERS) {
                undefeated.add(match.getWinnerEntrant());
                if (!match.isBye()) {
                    lowerBracket.add(match.getLoserEntrant());
                }
            } else if (match.getSection() == TournamentMatch.Section.LOSERS) {
                hasLosersMatches = true;
                lowerBracket.add(match.getWinnerEntrant());
            }
        }
        if (undefeated.isEmpty()) {
            List<List<TournamentMatch>> rounds = tournament.getRounds(format);
            for (int roundIndex = rounds.size() - 2; roundIndex >= 0; roundIndex--) {
                List<TournamentMatch> upperRound = rounds.get(roundIndex);
                List<TournamentEntrant> upperWinners = new ArrayList<>();
                for (TournamentMatch match : upperRound) {
                    if (match.getSection() == TournamentMatch.Section.WINNERS) {
                        upperWinners.add(match.getWinnerEntrant());
                    }
                }
                if (!upperWinners.isEmpty()) {
                    undefeated.addAll(upperWinners);
                    break;
                }
            }
        }
        if (!hasLosersMatches) {
            List<List<TournamentMatch>> rounds = tournament.getRounds(format);
            for (int roundIndex = rounds.size() - 2; roundIndex >= 0; roundIndex--) {
                List<TournamentEntrant> lowerWinners = new ArrayList<>();
                for (TournamentMatch match : rounds.get(roundIndex)) {
                    if (match.getSection() == TournamentMatch.Section.LOSERS) {
                        lowerWinners.add(match.getWinnerEntrant());
                    }
                }
                if (!lowerWinners.isEmpty()) {
                    lowerBracket.addAll(lowerWinners);
                    break;
                }
            }
        }

        if (undefeated.size() == 1 && lowerBracket.isEmpty()) {
            tournament.complete(format, undefeated.get(0).getRepresentative());
            return;
        }

        List<TournamentMatch> nextRound = new ArrayList<>();
        if (undefeated.size() > 1) {
            nextRound.addAll(createRoundMatches(
                    tournament, format, undefeated, nextRoundNumber, true,
                    TournamentMatch.Section.WINNERS));
        }
        if (lowerBracket.size() >= 2) {
            nextRound.addAll(createRoundMatches(
                    tournament, format, lowerBracket, nextRoundNumber, true,
                    TournamentMatch.Section.LOSERS));
        } else if (lowerBracket.size() == 1 && undefeated.size() > 1) {
            TournamentMatch bye = new TournamentMatch(
                    nextRoundNumber, 1, lowerBracket.get(0), null, null,
                    TournamentMatch.Section.LOSERS);
            bye.complete(lowerBracket.get(0));
            nextRound.add(bye);
        }
        if (undefeated.size() == 1 && lowerBracket.size() == 1) {
            nextRound.add(createTournamentMatch(
                    format, undefeated.get(0), lowerBracket.get(0),
                    nextRoundNumber, 1, TournamentMatch.Section.GRAND_FINAL));
        }
        if (nextRound.isEmpty()) {
            throw new IllegalStateException("Double-elimination bracket could not advance.");
        }
        tournament.addRound(format, nextRound);
        if (isRoundComplete(nextRound)) {
            advanceDoubleElimination(tournament, format, nextRound);
        }
    }

    private TournamentMatch createTournamentMatch(
            MatchFormat format,
            TournamentEntrant entrantA,
            TournamentEntrant entrantB,
            int roundNumber,
            int matchNumber,
            TournamentMatch.Section section) {
        Match match = new Match(format);
        for (Player player : entrantA.getPlayers()) {
            match.addPlayer(player);
        }
        for (Player player : entrantB.getPlayers()) {
            match.addPlayer(player);
        }
        return new TournamentMatch(
                roundNumber, matchNumber, entrantA, entrantB, match, section);
    }

    private TournamentMatch findSectionMatch(
            List<TournamentMatch> matches,
            TournamentMatch.Section section) {
        for (TournamentMatch match : matches) {
            if (match.getSection() == section) {
                return match;
            }
        }
        return null;
    }

    private void addRoundRobinSchedule(
            Tournament tournament,
            MatchFormat format,
            List<TournamentEntrant> entrants) {
        List<TournamentEntrant> rotation = new ArrayList<>(entrants);
        if (rotation.size() % 2 != 0) {
            rotation.add(null);
        }
        int playerCount = rotation.size();
        int roundsCount = playerCount - 1;
        int matchesPerRound = playerCount / 2;
        for (int round = 0; round < roundsCount; round++) {
            List<TournamentMatch> matches = new ArrayList<>();
            for (int index = 0; index < matchesPerRound; index++) {
                TournamentEntrant entrantA = rotation.get(index);
                TournamentEntrant entrantB = rotation.get(playerCount - 1 - index);
                if (entrantA != null && entrantB != null) {
                    matches.add(createTournamentMatch(
                            format, entrantA, entrantB, round + 1, index + 1,
                            TournamentMatch.Section.ROUND_ROBIN));
                }
            }
            tournament.addRound(format, matches);
            TournamentEntrant fixed = rotation.get(0);
            TournamentEntrant last = rotation.remove(rotation.size() - 1);
            rotation.add(1, last);
            rotation.set(0, fixed);
        }
    }

    private Player getRoundRobinChampion(Tournament tournament, MatchFormat format) {
        List<TournamentMatch> allMatches = new ArrayList<>();
        for (List<TournamentMatch> round : tournament.getRounds(format)) {
            allMatches.addAll(round);
        }
        Map<Player, int[]> standings = new java.util.IdentityHashMap<>();
        for (TournamentEntrant entrant : getEntrants(tournament, format)) {
            standings.put(entrant.getRepresentative(), new int[2]);
        }
        for (TournamentMatch match : allMatches) {
            if (!match.isCompleted() || match.getMatch() == null) {
                continue;
            }
            int scoreA = match.getMatch().getTeamAScore();
            int scoreB = match.getMatch().getTeamBScore();
            int[] statsA = standings.get(match.getEntrantA().getRepresentative());
            int[] statsB = standings.get(match.getEntrantB().getRepresentative());
            statsA[0] += scoreA > scoreB ? 1 : 0;
            statsB[0] += scoreB > scoreA ? 1 : 0;
            statsA[1] += scoreA - scoreB;
            statsB[1] += scoreB - scoreA;
        }
        Player champion = null;
        for (Player player : standings.keySet()) {
            if (champion == null || compareStanding(player, champion, standings, tournament) < 0) {
                champion = player;
            }
        }
        return champion;
    }

    private int compareStanding(
            Player left,
            Player right,
            Map<Player, int[]> standings,
            Tournament tournament) {
        int[] leftStats = standings.get(left);
        int[] rightStats = standings.get(right);
        int wins = Integer.compare(rightStats[0], leftStats[0]);
        if (wins != 0) {
            return wins;
        }
        int differential = Integer.compare(rightStats[1], leftStats[1]);
        if (differential != 0) {
            return differential;
        }
        return Integer.compare(
                tournament.getPlayers().indexOf(left),
                tournament.getPlayers().indexOf(right));
    }

    private void createRound(
            Tournament tournament,
            MatchFormat format,
            List<TournamentEntrant> entrants,
            int roundNumber) {
        List<TournamentMatch> round =
                createRoundMatches(tournament, format, entrants, roundNumber, true);
        tournament.addRound(format, round);

        if (isRoundComplete(round) && round.size() > 1) {
            advanceRound(tournament, format, round, roundNumber + 1);
        } else if (isRoundComplete(round)) {
            tournament.complete(format, round.get(0).getWinner());
        }
    }

    private List<TournamentEntrant> getEntrants(Tournament tournament, MatchFormat format) {
        List<TournamentEntrant> entrants = new ArrayList<>();
        List<Player> players = tournament.getPlayers(format);
        int teamSize = format == MatchFormat.DOUBLE ? 2 : 1;
        for (int index = 0; index + teamSize <= players.size(); index += teamSize) {
            entrants.add(new TournamentEntrant(players.subList(index, index + teamSize)));
        }
        return entrants;
    }

    private boolean canAcceptLateEntrants(Tournament tournament, MatchFormat format) {
        List<List<TournamentMatch>> rounds = tournament.getRounds(format);
        if (tournament.getBracketType() != BracketType.SINGLE_ELIMINATION
                || rounds.size() > 1) {
            return false;
        }
        if (rounds.isEmpty()) {
            return true;
        }
        for (TournamentMatch match : rounds.get(0)) {
            if (!match.isBye()
                    && (match.isCompleted()
                            || match.getMatch().getStatus() != Model.MatchStatus.PENDING)) {
                return false;
            }
        }
        return true;
    }

    private void appendLateEntrantMatches(Tournament tournament, MatchFormat format) {
        List<List<TournamentMatch>> rounds = tournament.getRounds(format);
        List<TournamentMatch> firstRound = rounds.isEmpty()
                ? new ArrayList<>()
                : rounds.get(0);
        Set<Player> scheduledPlayers = new HashSet<>();
        for (TournamentMatch match : firstRound) {
            scheduledPlayers.addAll(match.getTeamAPlayers());
            scheduledPlayers.addAll(match.getTeamBPlayers());
        }

        List<Player> unpairedPlayers = new ArrayList<>();
        for (Player player : tournament.getPlayers(format)) {
            if (!scheduledPlayers.contains(player)) {
                unpairedPlayers.add(player);
            }
        }

        int teamSize = format == MatchFormat.DOUBLE ? 2 : 1;
        List<TournamentEntrant> newEntrants = new ArrayList<>();
        for (int index = 0; index + teamSize <= unpairedPlayers.size(); index += teamSize) {
            newEntrants.add(new TournamentEntrant(
                    unpairedPlayers.subList(index, index + teamSize)));
        }

        if (rounds.isEmpty() && newEntrants.size() >= 2) {
            tournament.activateFormat(format);
            tournament.addRound(format, createRoundMatches(
                    tournament, format, newEntrants, 1, true));
            return;
        }
        if (rounds.isEmpty()) {
            return;
        }
        int matchNumber = firstRound.size() + 1;
        for (int index = 0; index + 1 < newEntrants.size(); index += 2) {
            TournamentEntrant entrantA = newEntrants.get(index);
            TournamentEntrant entrantB = newEntrants.get(index + 1);
            Match match = new Match(format);
            for (Player player : entrantA.getPlayers()) {
                match.addPlayer(player);
            }
            for (Player player : entrantB.getPlayers()) {
                match.addPlayer(player);
            }
            tournament.addMatchToRound(format, 1, new TournamentMatch(
                    1, matchNumber++, entrantA, entrantB, match));
        }
    }

    private List<TournamentMatch> createRoundMatches(
            Tournament tournament,
            MatchFormat format,
            List<TournamentEntrant> entrants,
            int roundNumber,
            boolean completeByes) {
        return createRoundMatches(
                tournament, format, entrants, roundNumber, completeByes,
                TournamentMatch.Section.WINNERS);
    }

    private List<TournamentMatch> createRoundMatches(
            Tournament tournament,
            MatchFormat format,
            List<TournamentEntrant> entrants,
            int roundNumber,
            boolean completeByes,
            TournamentMatch.Section section) {
        List<TournamentMatch> round = new ArrayList<>();
        int matchNumber = 1;
        for (int index = 0; index < entrants.size(); index += 2) {
            TournamentEntrant entrantA = entrants.get(index);
            TournamentEntrant entrantB = index + 1 < entrants.size() ? entrants.get(index + 1) : null;
            Match match = null;

            if (entrantB != null) {
                round.add(createTournamentMatch(
                        format, entrantA, entrantB, roundNumber, matchNumber++, section));
                continue;
            }

            TournamentMatch tournamentMatch = new TournamentMatch(
                    roundNumber,
                    matchNumber++,
                    entrantA,
                    entrantB,
                    match,
                    section);
            if (completeByes && entrantB == null) {
                tournamentMatch.complete(entrantA);
            }
            round.add(tournamentMatch);
        }
        return round;
    }

    private void recordEntrantResult(TournamentEntrant winner, TournamentEntrant loser) {
        for (Player player : winner.getPlayers()) {
            player.recordWin();
        }
        for (Player player : loser.getPlayers()) {
            player.recordLoss();
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

    private void validateFormatRoster(Tournament tournament, MatchFormat format) {
        if (format == MatchFormat.DOUBLE
                && tournament.getPlayers(format).size() % 2 != 0) {
            throw new IllegalStateException(
                    "Doubles players must be registered in pairs before the bracket starts.");
        }
    }

    private void requireTournament(Tournament tournament) {
        if (tournament == null) {
            throw new IllegalArgumentException("Tournament is required.");
        }
    }

    private void requireCurrentMatch(Tournament tournament, TournamentMatch tournamentMatch) {
        requireTournament(tournament);
        if (tournament.getStatus() != TournamentStatus.IN_PROGRESS) {
            throw new IllegalStateException("Tournament is not in progress.");
        }
        if (tournamentMatch == null || tournamentMatch.isBye()
                || tournamentMatch.getTeamAPlayers().isEmpty()) {
            throw new IllegalArgumentException("A playable match in the current round is required.");
        }
        MatchFormat format = tournamentMatch.getTeamAPlayers().get(0).getFormat();
        List<TournamentMatch> currentRound = getCurrentRound(tournament, format);
        if (!currentRound.contains(tournamentMatch)) {
            throw new IllegalArgumentException("A playable match in the current round is required.");
        }
    }
}