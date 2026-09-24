package GUI;

import Model.Match;
import Model.Player;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QueueMatchCard extends JPanel {

    public QueueMatchCard(
            Match match,
            int matchNumber,
            Runnable onStarted,
            Runnable onSelected,
            boolean selected) {

        setLayout(new BorderLayout(5, 5));
        setBorder(selected
                ? BorderFactory.createLineBorder(new Color(0, 120, 215), 3)
                : new LineBorder(Color.GRAY, 1));
        setBackground(selected ? new Color(215, 235, 255) : Color.WHITE);
        setPreferredSize(new Dimension(170, 180));
                addMouseListener(new java.awt.event.MouseAdapter() {
                        @Override
                        public void mousePressed(java.awt.event.MouseEvent event) {
                                onSelected.run();
                        }
                });

        JLabel titleLabel =
                new JLabel(String.valueOf(matchNumber));

        titleLabel.setBorder(
                BorderFactory.createEmptyBorder(
                        3, 5, 3, 5
                )
        );

        JLabel formatLabel =
                new JLabel(match.getFormat() == Model.MatchFormat.SINGLE ? "SINGLES" : "DOUBLES");

        formatLabel.setFont(formatLabel.getFont().deriveFont(Font.BOLD, 10f));
        formatLabel.setForeground(new Color(90, 90, 90));
        formatLabel.setBorder(
                BorderFactory.createEmptyBorder(
                        3, 5, 3, 5
                )
        );

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        JPanel matchInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        matchInfo.setOpaque(false);
        matchInfo.add(titleLabel);
        addDifficultyIndicators(matchInfo, match.getPlayers());
        titleRow.add(matchInfo, BorderLayout.WEST);
        titleRow.add(formatLabel, BorderLayout.EAST);

        add(titleRow, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();

        centerPanel.setBackground(selected ? new Color(215, 235, 255) : Color.WHITE);

        centerPanel.setLayout(
                new BoxLayout(
                        centerPanel,
                        BoxLayout.Y_AXIS
                )
        );

        List<Player> players = match.getPlayers();

        int half = players.size() / 2;

        JLabel teamALabel = new JLabel("TEAM A");
        teamALabel.setFont(teamALabel.getFont().deriveFont(Font.BOLD, 9f));
        teamALabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(teamALabel);

        for (int i = 0; i < half; i++) {

            centerPanel.add(
                    playerBox(players.get(i))
            );

            centerPanel.add(
                    Box.createVerticalStrut(4)
            );
        }

        JLabel vsLabel = new JLabel("VS");
        vsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(vsLabel);

        centerPanel.add(
                Box.createVerticalStrut(4)
        );

        JLabel teamBLabel = new JLabel("TEAM B");
        teamBLabel.setFont(teamBLabel.getFont().deriveFont(Font.BOLD, 9f));
        teamBLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(teamBLabel);

        for (int i = half; i < players.size(); i++) {

            centerPanel.add(
                    playerBox(players.get(i))
            );

            centerPanel.add(
                    Box.createVerticalStrut(4)
            );
        }

        add(centerPanel, BorderLayout.CENTER);

        JButton startButton =
                new JButton("START MATCH");

        startButton.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        9
                )
        );

       startButton.addActionListener(e -> {

   boolean started =
        MainGUI.getInstance()
               .assignMatchToCourt(match);

        if (started) {
    onStarted.run();
}
});

        add(startButton, BorderLayout.SOUTH);
    }

    private JPanel playerBox(Player p) {

        JPanel box =
                new JPanel(new BorderLayout());

        box.setMaximumSize(
                new Dimension(140, 24)
        );

        box.setPreferredSize(
                new Dimension(140, 24)
        );

        box.setBorder(
                BorderFactory.createLineBorder(
                        Color.LIGHT_GRAY
                )
        );

        JLabel nameLabel =
                new JLabel(" " + p.getName());

        nameLabel.setFont(
                new Font(
                        "Arial",
                        Font.PLAIN,
                        10
                )
        );

        box.add(
                nameLabel,
                BorderLayout.WEST
        );

        return box;
    }

        private void addDifficultyIndicators(JPanel parent, List<Player> players) {
                Set<Integer> skillRanks = new HashSet<>();
                for (Player player : players) {
                        skillRanks.add(player.getSkillRank());
                }

                for (Integer skillRank : skillRanks) {
                        JLabel indicator = new JLabel();
                        indicator.setOpaque(true);
                        indicator.setBackground(skillColor(skillRank));
                        indicator.setPreferredSize(new Dimension(18, 18));
                        indicator.setToolTipText(skillName(skillRank));
                        parent.add(indicator);
                }
        }

        private Color skillColor(int skillRank) {
                switch (skillRank) {
                        case 1 -> { return new Color(95, 235, 95); }
                        case 2 -> { return new Color(80, 190, 255); }
                        case 3 -> { return new Color(255, 215, 70); }
                        case 4 -> { return new Color(255, 170, 70); }
                        case 5 -> { return new Color(255, 120, 120); }
                        case 6 -> { return new Color(190, 130, 255); }
                        default -> { return new Color(120, 120, 120); }
                }
        }

        private String skillName(int skillRank) {
                for (Model.SkillLevel level : Model.SkillLevel.values()) {
                        if (level.getRank() == skillRank) {
                                return level.getDisplayName();
                        }
                }
                return "Unknown difficulty";
        }

}