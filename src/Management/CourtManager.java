package Management;

import Model.Court;
import Model.Match;

import java.util.ArrayList;
import java.util.List;

public class CourtManager {

    private List<Court> courts = new ArrayList<>();

    public CourtManager() {

        for (int x = 1; x <= 4; x++) {
            courts.add(new Court(x));
        }
    }

    public Court getCourt(int courtNum) {

        if (courtNum < 1 || courtNum > courts.size()) {
            throw new IllegalArgumentException(
                    "Invalid court number: " + courtNum
            );
        }

        return courts.get(courtNum - 1);
    }

    public boolean hasAvailableCourt() {

        for (Court court : courts) {
            if (court.isAvailable()) {
                return true;
            }
        }

        return false;
    }

    public Court getAvailableCourt() {

    for (Court court : courts) {

        if (court.isAvailable()) {
            return court;
        }
    }

    return null;
}   

    public void assignMatchToCourt(
        Match match,
        int courtNum) {

    Court court = getCourt(courtNum);

    court.setCurrentMatch(match);
    court.setOccupied(true);

    if (match != null) {
        match.setCourt(court);
    }
}


    public boolean tryStartMatchOnAvailableCourt(
            Match match) {
        
        if (match == null) {
            return false;
        }

        Court court = getAvailableCourt();

        if (court == null) {
            return false;
        }

        assignMatchToCourt(
                match,
                court.getCourtNum()
        );

        startMatch(
                court.getCourtNum(),
                15
        );

        return true;
    }

    public void freeCourt(int courtNum) {
        getCourt(courtNum).freeCourt();
    }

    public void startMatch(
            int courtNum,
            int durationMins) {

        Match match =
                getCourt(courtNum)
                        .getCurrentMatch();

        if (match != null) {
            match.start(durationMins);
        }
    }

    public void endMatch(int courtNum) {

        Match match =
                getCourt(courtNum)
                        .getCurrentMatch();

        if (match != null) {
            match.end();
        }
    }

    public void pauseMatch(int courtNum) {
        Match match = getCourt(courtNum).getCurrentMatch();
        if (match != null) {
            match.pause();
        }
    }

    public void resumeMatch(int courtNum) {
        Match match = getCourt(courtNum).getCurrentMatch();
        if (match != null) {
            match.resume();
        }
    }

    public List<Court> getAllCourt() {
        return courts;
    }
}