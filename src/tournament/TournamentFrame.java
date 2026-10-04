package tournament;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/** Main tournament window: bracket, controls and progress. */
public class TournamentFrame extends JFrame {

    private final Tournament tournament = new Tournament();
    private final BracketPanel bracket = new BracketPanel(tournament);

    private final JTextField nameField = new JTextField(14);
    private final JButton addButton = new JButton("Add Player");
    private final JButton removeButton = new JButton("Remove Selected");
    private final JCheckBox shuffleBox = new JCheckBox("Shuffle seeding", true);
    private final JButton startTournamentButton = new JButton("Start Tournament");
    private final JButton resetButton = new JButton("Reset");

    private final DefaultListModel<Player> playerModel = new DefaultListModel<Player>();
    private final JList<Player> playerList = new JList<Player>(playerModel);

    private final JLabel selectedLabel = new JLabel("No match selected");
    private final JButton startMatchButton = new JButton("Start Match");
    private final JSpinner score1Spinner = new JSpinner(new SpinnerNumberModel(0, 0, 999, 1));
    private final JSpinner score2Spinner = new JSpinner(new SpinnerNumberModel(0, 0, 999, 1));
    private final JLabel p1Label = new JLabel("Player 1");
    private final JLabel p2Label = new JLabel("Player 2");
    private final JButton recordButton = new JButton("Record Result");

    private final JProgressBar progressBar = new JProgressBar(0, 100);
    private final JLabel progressLabel = new JLabel(" ");
    private final JTextArea log = new JTextArea(5, 40);

    public TournamentFrame() {
        super("Tournament Manager");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(6, 6));


        JScrollPane bracketScroll = new JScrollPane(bracket);
        bracketScroll.setBorder(BorderFactory.createTitledBorder("Bracket"));
        bracketScroll.getVerticalScrollBar().setUnitIncrement(16);
        bracketScroll.getHorizontalScrollBar().setUnitIncrement(16);

add(bracketScroll, BorderLayout.CENTER);

        wireEvents();
        tournament.addChangeListener(new Runnable() {
            public void run() { refresh(); }
        });
        refresh();

        setPreferredSize(new Dimension(1100, 700));
        pack();
        setLocationRelativeTo(null);
    }

    // ---------- layout ----------
    private JPanel buildTopBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        p.setBorder(BorderFactory.createTitledBorder("Tournament controls"));
        p.add(new JLabel("Player name:"));
        p.add(nameField);
        p.add(addButton);
        p.add(removeButton);
        p.add(shuffleBox);
        p.add(startTournamentButton);
        p.add(resetButton);
        return p;
    }

    private JPanel buildSidePanel() {
        JPanel side = new JPanel(new BorderLayout(4, 4));
        side.setBorder(BorderFactory.createTitledBorder("Players"));
        side.add(new JScrollPane(playerList), BorderLayout.CENTER);

        JPanel match = new JPanel();
        match.setLayout(new javax.swing.BoxLayout(match, javax.swing.BoxLayout.Y_AXIS));
        match.setBorder(BorderFactory.createTitledBorder("Selected match"));
        selectedLabel.setFont(selectedLabel.getFont().deriveFont(Font.BOLD));
        match.add(selectedLabel);
        match.add(javax.swing.Box.createVerticalStrut(6));
        match.add(startMatchButton);
        match.add(javax.swing.Box.createVerticalStrut(8));

        JPanel s1 = new JPanel(new BorderLayout(6, 0));
        s1.add(p1Label, BorderLayout.CENTER);
        s1.add(score1Spinner, BorderLayout.EAST);
        JPanel s2 = new JPanel(new BorderLayout(6, 0));
        s2.add(p2Label, BorderLayout.CENTER);
        s2.add(score2Spinner, BorderLayout.EAST);
        match.add(s1);
        match.add(javax.swing.Box.createVerticalStrut(4));
        match.add(s2);
        match.add(javax.swing.Box.createVerticalStrut(8));
        match.add(recordButton);
        side.add(match, BorderLayout.SOUTH);
        return side;
    }

    private JPanel buildBottomPanel() {
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setBorder(BorderFactory.createTitledBorder("Tournament progress"));
        progressBar.setStringPainted(true);
        JPanel top = new JPanel(new BorderLayout(4, 4));
        top.add(progressLabel, BorderLayout.NORTH);
        top.add(progressBar, BorderLayout.CENTER);
        p.add(top, BorderLayout.NORTH);
        log.setEditable(false);
        p.add(new JScrollPane(log), BorderLayout.CENTER);
        return p;
    }

    // ---------- events ----------
    private void wireEvents() {
        ActionListener add = new ActionListener() {
            public void actionPerformed(ActionEvent e) { onAddPlayer(); }
        };
        addButton.addActionListener(add);
        nameField.addActionListener(add);

        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Player p = playerList.getSelectedValue();
                if (p != null) {
                    tournament.removePlayer(p);
                    logLine("Removed player: " + p);
                }
            }
        });
        startTournamentButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    tournament.start(shuffleBox.isSelected());
                    logLine("Tournament started with " + tournament.getPlayers().size() + " players.");
                } catch (RuntimeException ex) {
                    error(ex.getMessage());
                }
            }
        });
        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int ok = JOptionPane.showConfirmDialog(TournamentFrame.this,
                        "Clear the bracket? Players are kept.", "Reset", JOptionPane.YES_NO_OPTION);
                if (ok == JOptionPane.YES_OPTION) {
                    bracket.clearSelection();
                    tournament.reset();
                    logLine("Tournament reset.");
                }
            }
        });
        startMatchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Match m = bracket.getSelected();
                if (m == null) {
                    return;
                }
                try {
                    tournament.startMatch(m);
                    logLine("Started " + m);
                } catch (RuntimeException ex) {
                    error(ex.getMessage());
                }
            }
        });
        recordButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { onRecordResult(); }
        });
        bracket.setSelectionListener(new Runnable() {
            public void run() { updateMatchControls(); }
        });
    }

    private void onAddPlayer() {
        try {
            tournament.addPlayer(nameField.getText());
            logLine("Added player: " + nameField.getText().trim());
            nameField.setText("");
            nameField.requestFocusInWindow();
        } catch (RuntimeException ex) {
            error(ex.getMessage());
        }
    }

    private void onRecordResult() {
        Match m = bracket.getSelected();
        if (m == null) {
            return;
        }
        try {
            int s1 = (Integer) score1Spinner.getValue();
            int s2 = (Integer) score2Spinner.getValue();
            tournament.recordResult(m, s1, s2);
            logLine(m.label() + ": " + m.getPlayer1() + " " + s1 + " - " + s2 + " " + m.getPlayer2()
                    + "  ->  " + m.getWinner() + " advances");
            if (tournament.getChampion() != null) {
                JOptionPane.showMessageDialog(this, "Champion: " + tournament.getChampion().getName(),
                        "Tournament complete", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (RuntimeException ex) {
            error(ex.getMessage());
        }
    }

    // ---------- view updates ----------
    private void refresh() {
        playerModel.clear();
        for (Player p : tournament.getPlayers()) {
            playerModel.addElement(p);
        }
        bracket.refresh();

        boolean started = tournament.isStarted();
        nameField.setEnabled(!started);
        addButton.setEnabled(!started);
        removeButton.setEnabled(!started);
        shuffleBox.setEnabled(!started);
        startTournamentButton.setEnabled(!started && tournament.getPlayers().size() >= 2);
        resetButton.setEnabled(started);

        int total = tournament.totalMatches();
        int done = tournament.completedMatches();
        progressBar.setValue(total == 0 ? 0 : done * 100 / total);
        progressBar.setString(started ? done + " / " + total + " matches" : "Not started");
        progressLabel.setText(tournament.progressText());

        updateMatchControls();
    }

    private void updateMatchControls() {
        Match m = bracket.getSelected();
        if (m == null || !tournament.isStarted()) {
            selectedLabel.setText("No match selected");
            p1Label.setText("Player 1");
            p2Label.setText("Player 2");
            startMatchButton.setEnabled(false);
            recordButton.setEnabled(false);
            score1Spinner.setEnabled(false);
            score2Spinner.setEnabled(false);
            return;
        }
        selectedLabel.setText(m.label() + " - " + m.getStatus().toString().replace('_', ' '));
        p1Label.setText(m.getPlayer1() == null ? "TBD" : m.getPlayer1().getName());
        p2Label.setText(m.getPlayer2() == null ? (m.isBye() ? "BYE" : "TBD") : m.getPlayer2().getName());
        boolean live = m.getStatus() == Match.Status.IN_PROGRESS;
        startMatchButton.setEnabled(m.getStatus() == Match.Status.READY);
        recordButton.setEnabled(live);
        score1Spinner.setEnabled(live);
        score2Spinner.setEnabled(live);
        if (m.getStatus() == Match.Status.COMPLETED) {
            score1Spinner.setValue(m.getScore1());
            score2Spinner.setValue(m.getScore2());
        } else if (!live) {
            score1Spinner.setValue(0);
            score2Spinner.setValue(0);
        }
    }

    private void logLine(String s) {
        log.append(s + "\n");
        log.setCaretPosition(log.getDocument().getLength());
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Tournament", JOptionPane.WARNING_MESSAGE);
    }
}
