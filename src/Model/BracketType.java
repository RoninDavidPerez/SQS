package Model;

public enum BracketType {
    SINGLE_ELIMINATION("Single Elimination"),
    DOUBLE_ELIMINATION("Double Elimination"),
    ROUND_ROBIN("Round Robin");

    private final String displayName;

    BracketType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}