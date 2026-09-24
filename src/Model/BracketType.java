package Model;

public enum BracketType {
    SINGLE_ELIMINATION("Single Elimination");

    private final String displayName;

    BracketType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}