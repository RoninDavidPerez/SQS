package GUI;

import Model.Player;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class OnHoldPlayerCard extends JPanel {

    private final Player player;
    private final JLabel numberLabel;

        public OnHoldPlayerCard(
            Player player,
            int index,
            Runnable onRemove) {
        this.player = player;
        setLayout(new BorderLayout(4, 2));
        setBorder(new LineBorder(Color.GRAY, 1));
        setPreferredSize(new Dimension(170, 98));
        setMinimumSize(new Dimension(150, 98));
        setMaximumSize(new Dimension(Short.MAX_VALUE, 98));

        numberLabel = new JLabel(String.valueOf(index));
        numberLabel.setHorizontalAlignment(SwingConstants.CENTER);
        numberLabel.setPreferredSize(new Dimension(28, 28));
        numberLabel.setBorder(new javax.swing.border.EtchedBorder());
        numberLabel.setToolTipText("Drag to Queue or Player List");

        JLabel nameLabel = new JLabel(player.getName());

        JButton removeButton = new JButton("X");
        removeButton.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        removeButton.setForeground(Color.RED);
        removeButton.setFont(removeButton.getFont().deriveFont(Font.BOLD, 16f));
        removeButton.setPreferredSize(new Dimension(34, 26));
        removeButton.setMinimumSize(new Dimension(34, 26));
        removeButton.setMaximumSize(new Dimension(34, 26));
        removeButton.setToolTipText("Return player to player list");
        removeButton.setBorderPainted(false);
        removeButton.setContentAreaFilled(false);
        removeButton.addActionListener(e -> onRemove.run());

        JPanel header = new JPanel(new BorderLayout(4, 0));
        header.add(nameLabel, BorderLayout.CENTER);
        header.add(removeButton, BorderLayout.EAST);

        JLabel skillIndicator = new JLabel();
        skillIndicator.setOpaque(true);
        skillIndicator.setPreferredSize(new Dimension(18, 18));
        skillIndicator.setBackground(skillColor(player));
        skillIndicator.setToolTipText(player.getSkillName());

        JLabel formatIndicator = new JLabel(player.getFormat() == Model.MatchFormat.SINGLE ? "SINGLES" : "DOUBLES");
        formatIndicator.setFont(formatIndicator.getFont().deriveFont(Font.PLAIN, 10f));
        formatIndicator.setForeground(new Color(90, 90, 90));

        JPanel infoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        infoRow.add(formatIndicator);
        infoRow.add(skillIndicator);

        JLabel winsLabel = new JLabel("W " + player.getWins());
        winsLabel.setFont(winsLabel.getFont().deriveFont(Font.BOLD, 11f));
        winsLabel.setForeground(new Color(0, 140, 0));

        JLabel lossesLabel = new JLabel("L " + player.getLosses());
        lossesLabel.setFont(lossesLabel.getFont().deriveFont(Font.BOLD, 11f));
        lossesLabel.setForeground(new Color(180, 0, 0));

        JPanel tallyRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        tallyRow.add(winsLabel);
        tallyRow.add(lossesLabel);

        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.add(infoRow);
        controls.add(tallyRow);

        add(numberLabel, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);
        add(controls, BorderLayout.CENTER);
    }

    public JLabel getDragHandle() {
        return numberLabel;
    }

    public Player getPlayer() {
        return player;
    }

    private Color skillColor(Player player) {
        switch (player.getSkillRank()) {
            case 1 -> { return new Color(95, 235, 95); }
            case 2 -> { return new Color(80, 190, 255); }
            case 3 -> { return new Color(255, 215, 70); }
            case 4 -> { return new Color(255, 170, 70); }
            case 5 -> { return new Color(255, 120, 120); }
            case 6 -> { return new Color(190, 130, 255); }
            default -> { return new Color(120, 120, 120); }
        }
    }
    
}
