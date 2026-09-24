package Model;

public class Player {
    private String name;
    private MatchFormat format;
    private SkillLevel skill;
    private boolean ready;
    private PlayerStatus status;
    private int wins;
    private int losses;

    public Player(String name, MatchFormat format, SkillLevel skill) {
        this.name = name;
        this.format = format; 
        this.skill = skill; 
    }

    public void setStatus(PlayerStatus status) {
        this.status = status;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public String getName() {
        return name;
    }

    public MatchFormat getFormat() {
        return format;
    }

    public String getSkillName() {
        return skill.getDisplayName();
    }
    public int getSkillRank() {
        return skill.getRank();
    }

    public boolean isReady() {
        return ready;
    }

    public PlayerStatus getStatus() {
        return status;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public void recordWin() {
        wins++;
    }

    public void recordLoss() {
        losses++;
    }
    
}
