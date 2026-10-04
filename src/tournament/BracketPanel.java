package tournament;

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

    private final Tournament tournament;
    private Rectangle[][] bounds;
    private Match selected;
    private Runnable selectionListener;

    public BracketPanel(Tournament tournament) {
        this.tournament = tournament;
        setBackground(Color.WHITE);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (bounds == null) {
                    return;
                }
                List<List<Match>> rounds = tournament.getRounds();
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
    public Match getSelected() { return selected; }

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
        List<List<Match>> rounds = tournament.getRounds();
        if (rounds.isEmpty()) {
            bounds = null;
            return;
        }
        bounds = new Rectangle[rounds.size()][];
        for (int r = 0; r < rounds.size(); r++) {
            int count = rounds.get(r).size();
            bounds[r] = new Rectangle[count];
            int x = MARGIN + r * (BOX_W + H_GAP);
            for (int i = 0; i < count; i++) {
                int y;
                if (r == 0) {
                    y = MARGIN + HEADER + i * (BOX_H + V_GAP);
                } else {
                    int top = bounds[r - 1][2 * i].y;
                    int bottom = bounds[r - 1][2 * i + 1].y;
                    y = (top + bottom) / 2;
                }
                bounds[r][i] = new Rectangle(x, y, BOX_W, BOX_H);
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        List<List<Match>> rounds = tournament.getRounds();
        if (rounds.isEmpty()) {
            return new Dimension(500, 300);
        }
        int firstCount = rounds.get(0).size();
        int w = MARGIN * 2 + rounds.size() * BOX_W + (rounds.size() - 1) * H_GAP;
        int h = MARGIN * 2 + HEADER + firstCount * BOX_H + (firstCount - 1) * V_GAP;
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
            g.setFont(getFont().deriveFont(Font.ITALIC, 15f));
            g.drawString("Add players, then press \"Start Tournament\" to build the bracket.", 30, 50);
            g.dispose();
            return;
        }

        List<List<Match>> rounds = tournament.getRounds();
        Font base = getFont().deriveFont(13f);
        Font bold = base.deriveFont(Font.BOLD);

        // connectors first so boxes paint over them
        g.setColor(new Color(150, 150, 150));
        g.setStroke(new BasicStroke(1.5f));
        for (int r = 0; r < rounds.size() - 1; r++) {
            for (int i = 0; i < bounds[r].length; i++) {
                Rectangle a = bounds[r][i];
                Rectangle b = bounds[r + 1][i / 2];
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
            g.drawString(tournament.roundName(r), bounds[r][0].x, MARGIN + 14);
            for (int i = 0; i < bounds[r].length; i++) {
                drawMatch(g, rounds.get(r).get(i), bounds[r][i], base, bold);
            }
        }
        g.dispose();
    }

    private void drawMatch(Graphics2D g, Match m, Rectangle b, Font base, Font bold) {
        Color fill;
        switch (m.getStatus()) {
            case READY:       fill = new Color(255, 248, 210); break;
            case IN_PROGRESS: fill = new Color(205, 230, 255); break;
            case COMPLETED:   fill = new Color(220, 245, 220); break;
            default:          fill = new Color(240, 240, 240); break;
        }
        g.setColor(fill);
        g.fillRoundRect(b.x, b.y, b.width, b.height, 10, 10);

        boolean isSel = (m == selected);
        g.setColor(isSel ? new Color(30, 90, 200) : new Color(120, 120, 120));
        g.setStroke(new BasicStroke(isSel ? 3f : 1.2f));
        g.drawRoundRect(b.x, b.y, b.width, b.height, 10, 10);

        g.setStroke(new BasicStroke(1f));
        g.setColor(new Color(190, 190, 190));
        g.drawLine(b.x + 4, b.y + b.height / 2, b.x + b.width - 4, b.y + b.height / 2);

        drawRow(g, m, m.getPlayer1(), m.getScore1(), b.x, b.y, b.width, b.height / 2, base, bold);
        drawRow(g, m, m.getPlayer2(), m.getScore2(), b.x, b.y + b.height / 2, b.width, b.height / 2, base, bold);

        if (m.getStatus() == Match.Status.IN_PROGRESS) {
            g.setFont(base.deriveFont(Font.BOLD, 10f));
            g.setColor(new Color(30, 90, 200));
            g.drawString("LIVE", b.x + b.width - 30, b.y - 3);
        }
    }

    private void drawRow(Graphics2D g, Match m, Player p, int score,
                         int x, int y, int w, int h, Font base, Font bold) {
        boolean done = m.getStatus() == Match.Status.COMPLETED;
        boolean won = done && p != null && p == m.getWinner();
        String name = p == null ? (m.isBye() ? "BYE" : "TBD") : p.getName();

        g.setFont(won ? bold : base);
        FontMetrics fm = g.getFontMetrics();
        g.setColor(p == null ? Color.GRAY : (done && !won ? new Color(130, 130, 130) : Color.BLACK));

        String scoreText = (done && !m.isBye() && p != null) ? String.valueOf(score) : "";
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
}
