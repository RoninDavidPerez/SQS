package tournament;

import Model.Match;
import Model.MatchStatus;
import Model.MatchFormat;
import Model.Player;
import Model.Tournament;
import Model.TournamentEntrant;
import Model.TournamentMatch;
import Model.TournamentStatus;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.JPanel;

/** Draws the elimination bracket; clicking a match selects it. */
public class BracketPanel extends JPanel {

    private static final int BOX_W = 180;
    private static final int BOX_H = 52;
    private static final int H_GAP = 60;
    private static final int V_GAP = 18;
    private static final int MARGIN = 24;
    private static final int HEADER = 30;

    private Tournament tournament;
    private final MatchFormat format;
    private Rectangle[][] bounds;
    private TournamentMatch selected;
    private Runnable selectionListener;

    public BracketPanel(Tournament tournament) {
        this(tournament, MatchFormat.SINGLE);
    }

    public BracketPanel(Tournament tournament, MatchFormat format) {
        this.tournament = tournament;
        this.format = format;
        setBackground(Color.WHITE);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (bounds == null) {
                    return;
                }
                List<List<TournamentMatch>> rounds = tournament.getRounds(format);
                for (int r = 0; r < bounds.length; r++) {
                    for (int i = 0; i < bounds[r].length; i++) {
                        if (bounds[r][i].contains(e.getPoint())) {
                            selected = rounds.get(r).get(i);
                            repaint();
                            if (selectionListener != null) {
                                selectionListener.run();
                            }
                            return;
                        }
                    }
                }
            }
        });
    }

    public void setSelectionListener(Runnable r) { this.selectionListener = r; }
    public TournamentMatch getSelected() { return selected; }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
        clearSelection();
        refresh();
    }

    public void clearSelection() {
        selected = null;
        if (selectionListener != null) {
            selectionListener.run();
        }
        repaint();
    }

    /** Recomputes layout after the tournament changes. */
    public void refresh() {
        layoutBoxes();
        revalidate();
        repaint();
    }

    private void layoutBoxes() {
        List<List<TournamentMatch>> rounds = tournament.getRounds(format);
        if (rounds.isEmpty()) {
            bounds = null;
            return;
        }
        bounds = new Rectangle[rounds.size()][];
        boolean treeLayout = tournament.getBracketType() == Model.BracketType.SINGLE_ELIMINATION;
        for (int r = 0; r < rounds.size(); r++) {
            int count = rounds.get(r).size();
            bounds[r] = new Rectangle[count];
            int x = MARGIN + r * (BOX_W + H_GAP);
            for (int i = 0; i < count; i++) {
                int y = MARGIN + HEADER + i * (BOX_H + V_GAP);
                if (treeLayout && r == 0) {
                    y = MARGIN + HEADER + i * (BOX_H + V_GAP);
                } else if (treeLayout) {
                    int previousLast = bounds[r - 1].length - 1;
                    int top = bounds[r - 1][Math.min(2 * i, previousLast)].y;
                    int bottom = bounds[r - 1][Math.min(2 * i + 1, previousLast)].y;
                    y = (top + bottom) / 2;
                }
                bounds[r][i] = new Rectangle(x, y, BOX_W, BOX_H);
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        List<List<TournamentMatch>> rounds = tournament.getRounds(format);
        if (rounds.isEmpty()) {
            int entryCount = getPreviewEntryCount();
            int height = MARGIN * 2 + HEADER + Math.max(1, entryCount) * (BOX_H + V_GAP);
            return new Dimension(700, Math.max(300, height));
        }
        int firstCount = 0;
        for (List<TournamentMatch> round : rounds) {
            firstCount = Math.max(firstCount, round.size());
        }
        int w = MARGIN * 2 + rounds.size() * BOX_W + (rounds.size() - 1) * H_GAP;
        int h = MARGIN * 2 + HEADER + firstCount * BOX_H + (firstCount - 1) * V_GAP
            + (hasUnpairedDoublesPlayer() ? 44 : 0);
        return new Dimension(w, h);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (bounds == null) {
            g.setColor(Color.GRAY);
            g.setFont(getFont().deriveFont(Font.BOLD, 16f));
            g.drawString(tournament.getName(), 30, 48);
            g.setFont(getFont().deriveFont(Font.PLAIN, 13f));
            g.drawString(format.getDisplayName() + " · "
                    + tournament.getBracketType().getDisplayName(), 30, 72);
            drawEntrantPreview(g);
            g.dispose();
            return;
        }

        List<List<TournamentMatch>> rounds = tournament.getRounds(format);
        Font base = getFont().deriveFont(13f);
        Font bold = base.deriveFont(Font.BOLD);

        // connectors first so boxes paint over them
        g.setColor(new Color(150, 150, 150));
        g.setStroke(new BasicStroke(1.5f));
        for (int r = 0; tournament.getBracketType() == Model.BracketType.SINGLE_ELIMINATION
                && r < rounds.size() - 1; r++) {
            for (int i = 0; i < bounds[r].length; i++) {
                Rectangle a = bounds[r][i];
                Rectangle b = bounds[r + 1][Math.min(i / 2, bounds[r + 1].length - 1)];
                int x1 = a.x + a.width;
                int y1 = a.y + a.height / 2;
                int x2 = b.x;
                int y2 = b.y + b.height / 2;
                int mid = x1 + H_GAP / 2;
                g.drawLine(x1, y1, mid, y1);
                g.drawLine(mid, y1, mid, y2);
                g.drawLine(mid, y2, x2, y2);
            }
        }

        for (int r = 0; r < rounds.size(); r++) {
            g.setFont(bold);
            g.setColor(new Color(60, 60, 60));
            g.drawString(roundName(r + 1), bounds[r][0].x, MARGIN + 14);
            for (int i = 0; i < bounds[r].length; i++) {
                if (tournament.getBracketType() == Model.BracketType.DOUBLE_ELIMINATION) {
                    g.setFont(base.deriveFont(Font.BOLD, 10f));
                    g.setColor(new Color(90, 90, 90));
                    g.drawString(rounds.get(r).get(i).getSection().getDisplayName(),
                            bounds[r][i].x, bounds[r][i].y - 4);
                }
                drawMatch(g, rounds.get(r).get(i), bounds[r][i], base, bold);
            }
        }
        if (hasUnpairedDoublesPlayer()) {
            List<Player> doublesPlayers = tournament.getPlayers(format);
            Player waitingPlayer = doublesPlayers.get(doublesPlayers.size() - 1);
            int y = bounds[0][bounds[0].length - 1].y + BOX_H + 34;
            g.setFont(base);
            g.setColor(new Color(110, 110, 110));
            g.drawString("Awaiting doubles partner: " + waitingPlayer.getName(), MARGIN, y);
        }
        g.dispose();
    }

    private void drawMatch(Graphics2D g, TournamentMatch tournamentMatch, Rectangle b, Font base, Font bold) {
        Match match = tournamentMatch.getMatch();
        Color fill;
        if (tournamentMatch.isCompleted()) {
            fill = new Color(220, 245, 220);
        } else if (match != null && match.getStatus() == MatchStatus.RUNNING) {
            fill = new Color(205, 230, 255);
        } else {
            fill = new Color(255, 248, 210);
        }
        g.setColor(fill);
        g.fillRoundRect(b.x, b.y, b.width, b.height, 10, 10);

        boolean isSel = (tournamentMatch == selected);
        g.setColor(isSel ? new Color(30, 90, 200) : new Color(120, 120, 120));
        g.setStroke(new BasicStroke(isSel ? 3f : 1.2f));
        g.drawRoundRect(b.x, b.y, b.width, b.height, 10, 10);

        g.setStroke(new BasicStroke(1f));
        g.setColor(new Color(190, 190, 190));
        g.drawLine(b.x + 4, b.y + b.height / 2, b.x + b.width - 4, b.y + b.height / 2);

        drawRow(g, tournamentMatch, tournamentMatch.getEntrantA(),
            match == null ? 0 : match.getTeamAScore(), b.x, b.y, b.width, b.height / 2, base, bold);
        drawRow(g, tournamentMatch, tournamentMatch.getEntrantB(),
            match == null ? 0 : match.getTeamBScore(), b.x, b.y + b.height / 2,
            b.width, b.height / 2, base, bold);

        if (match != null && match.getStatus() == MatchStatus.RUNNING) {
            g.setFont(base.deriveFont(Font.BOLD, 10f));
            g.setColor(new Color(30, 90, 200));
            g.drawString("LIVE", b.x + b.width - 30, b.y - 3);
        }
    }

        private void drawRow(Graphics2D g, TournamentMatch tournamentMatch, TournamentEntrant entrant, int score,
                         int x, int y, int w, int h, Font base, Font bold) {
        boolean done = tournamentMatch.isCompleted();
        boolean won = done && entrant != null && entrant == tournamentMatch.getWinnerEntrant();
        String name = entrant == null ? (tournamentMatch.isBye() ? "BYE" : "TBD")
            : entrant.getDisplayName();

        g.setFont(won ? bold : base);
        FontMetrics fm = g.getFontMetrics();
        g.setColor(entrant == null ? Color.GRAY
            : (done && !won ? new Color(130, 130, 130) : Color.BLACK));

        String scoreText = (done && !tournamentMatch.isBye() && entrant != null)
            ? String.valueOf(score) : "";
        int maxName = w - 16 - fm.stringWidth("00") - 6;
        while (name.length() > 1 && fm.stringWidth(name) > maxName) {
            name = name.substring(0, name.length() - 2) + "…";
        }
        int baseline = y + (h + fm.getAscent() - fm.getDescent()) / 2;
        g.drawString(name, x + 8, baseline);
        if (!scoreText.isEmpty()) {
            g.drawString(scoreText, x + w - 8 - fm.stringWidth(scoreText), baseline);
        }
    }

    private String roundName(int roundNumber) {
        if (tournament.getBracketType() == Model.BracketType.DOUBLE_ELIMINATION) {
            return "Phase " + roundNumber;
        }
        if (tournament.getBracketType() == Model.BracketType.ROUND_ROBIN) {
            return "Round " + roundNumber;
        }
        if (tournament.getStatus() == TournamentStatus.SETUP) {
            return "Opening Matches";
        }
        int playerCount = tournament.getPlayers(format).size();
        int entrantCount = format == MatchFormat.DOUBLE
            ? (playerCount + 1) / 2 : playerCount;
        int totalRounds = 32 - Integer.numberOfLeadingZeros(Math.max(1, entrantCount) - 1);
        int fromEnd = totalRounds - roundNumber;
        switch (fromEnd) {
            case 0: return "Final";
            case 1: return "Semifinals";
            case 2: return "Quarterfinals";
            default: return "Round " + roundNumber;
        }
    }

    private int getPreviewEntryCount() {
        int players = tournament.getPlayers(format).size();
        return format == MatchFormat.DOUBLE
                ? (players + 1) / 2
                : players;
    }

    private boolean hasUnpairedDoublesPlayer() {
        return format == MatchFormat.DOUBLE
                && tournament.getPlayers(format).size() % 2 != 0;
    }

    private void drawEntrantPreview(Graphics2D g) {
        List<Player> players = tournament.getPlayers(format);
        if (players.isEmpty()) {
            g.setColor(new Color(110, 110, 110));
            g.drawString("Waiting for players checked in through the main dashboard.", 30, 104);
            return;
        }

        g.setColor(new Color(70, 70, 70));
        g.setFont(getFont().deriveFont(Font.BOLD, 13f));
        g.drawString("Entrants", 30, 106);
        g.setFont(getFont().deriveFont(Font.PLAIN, 13f));
        int teamSize = format == MatchFormat.DOUBLE ? 2 : 1;
        for (int start = 0, entry = 0; start < players.size(); start += teamSize, entry++) {
            int y = MARGIN + HEADER + 58 + entry * (BOX_H + V_GAP);
            g.setColor(new Color(242, 246, 250));
            g.fillRoundRect(30, y, 640, BOX_H, 8, 8);
            g.setColor(new Color(150, 160, 170));
            g.drawRoundRect(30, y, 640, BOX_H, 8, 8);
            g.setColor(new Color(50, 50, 50));

            String label;
            if (teamSize == 1) {
                label = String.format("%02d  %s", entry + 1, players.get(start).getName());
            } else {
                String teammate = start + 1 < players.size()
                        ? players.get(start + 1).getName()
                        : "Awaiting partner";
                label = String.format("Team %02d  %s  /  %s",
                        entry + 1, players.get(start).getName(), teammate);
            }
            g.drawString(label, 44, y + 32);
        }
    }
}
