package Model;

public enum Sport {
    RACKET("Racket"),
    BALL("Ball"),
    BOARD("Board");

    private final String displayName;

    Sport(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}