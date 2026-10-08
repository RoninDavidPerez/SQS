package tournament;

import Management.TournamentManager;
import Model.MatchFormat;
import Model.Sport;
import Model.Tournament;
import java.util.EnumMap;
import java.util.Map;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

/** Tournament window for format-specific brackets and progress. */
public class TournamentFrame extends JFrame {

    private final TournamentManager tournamentManager;
    private final JLabel progressLabel = new JLabel();
    private final JLabel selectedMatchLabel = new JLabel("Select a match in the bracket.");
    private final Map<MatchFormat, BracketPanel> brackets = new EnumMap<>(MatchFormat.class);
    private final Map<MatchFormat, JLabel> standingsLabels = new EnumMap<>(MatchFormat.class);
    private final JTabbedPane bracketTabs = new JTabbedPane();
    private Tournament tournament;

    public TournamentFrame() {
        this(new TournamentManager(), null);
    }

    public TournamentFrame(
            TournamentManager tournamentManager,
            Tournament initialTournament) {
        super("Tournament");
        this.tournamentManager = tournamentManager;
        tournament = initialTournament != null
                ? initialTournament
                : tournamentManager.createTournament(
                        "Racket Tournament",
                        Model.BracketType.SINGLE_ELIMINATION,
                        "Racket single-elimination tournament",
                        Sport.RACKET);
        addBracketTab(MatchFormat.SINGLE, "Singles");
        addBracketTab(MatchFormat.DOUBLE, "Doubles");
        bracketTabs.addChangeListener(event -> refreshSelection());

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        add(buildHeader(), BorderLayout.NORTH);
        add(bracketTabs, BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);

        setPreferredSize(new Dimension(1100, 680));
        pack();
        setLocationRelativeTo(null);
        refreshBracket();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(8, 4));
        header.setBorder(BorderFactory.createEmptyBorder(8, 10, 4, 10));
        JLabel title = new JLabel(tournament.getName());
        title.setFont(title.getFont().deriveFont(java.awt.Font.BOLD, 18f));
        JLabel details = new JLabel(tournament.getDescription()
                + "   |   Singles and Doubles"
                + "   |   " + tournament.getBracketType().getDisplayName());
        JPanel text = new JPanel(new BorderLayout(2, 2));
        text.add(title, BorderLayout.NORTH);
        text.add(details, BorderLayout.CENTER);
        header.add(text, BorderLayout.CENTER);
        return header;
    }

    private void addBracketTab(MatchFormat format, String title) {
        BracketPanel bracket = new BracketPanel(tournament, format);
        bracket.setSelectionListener(this::refreshSelection);
        brackets.put(format, bracket);
        JScrollPane scroll = new JScrollPane(bracket);
        scroll.setBorder(BorderFactory.createTitledBorder("Bracket"));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getHorizontalScrollBar().setUnitIncrement(16);
        JPanel tab = new JPanel(new BorderLayout(4, 4));
        tab.add(scroll, BorderLayout.CENTER);
        JLabel standings = new JLabel();
        standings.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        standingsLabels.put(format, standings);
        tab.add(standings, BorderLayout.SOUTH);
        bracketTabs.addTab(title, tab);
    }

    private JPanel buildActions() {
        JPanel actions = new JPanel(new BorderLayout(4, 4));
        actions.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        actions.add(selectedMatchLabel, BorderLayout.NORTH);
        actions.add(progressLabel, BorderLayout.CENTER);
        actions.add(new JLabel(
                "Add players, queue matches, start play, and record scores in the racket dashboard."),
                BorderLayout.SOUTH);
        return actions;
    }

    private void refreshSelection() {
        BracketPanel bracket = brackets.get(getSelectedFormat());
        Model.TournamentMatch selected = bracket == null ? null : bracket.getSelected();
        if (selected == null) {
            selectedMatchLabel.setText("Select a match in the bracket.");
        } else {
            String state = selected.isCompleted()
                    ? "Completed"
                    : selected.getMatch() == null
                            ? "Bye"
                            : selected.getMatch().getStatus().toString();
            selectedMatchLabel.setText("Round " + selected.getRound()
                    + ", " + selected.getSection().getDisplayName()
                    + ", match " + selected.getMatchNumber() + ": " + state);
        }
    }

    public void refreshBracket() {
        for (Map.Entry<MatchFormat, BracketPanel> entry : brackets.entrySet()) {
            entry.getValue().refresh();
            standingsLabels.get(entry.getKey()).setText(
                    tournamentManager.getStandingsText(tournament, entry.getKey()));
        }
        progressLabel.setText(tournamentManager.getProgressText(tournament));
        refreshSelection();
    }

    private MatchFormat getSelectedFormat() {
        return bracketTabs.getSelectedIndex() == 1
                ? MatchFormat.DOUBLE
                : MatchFormat.SINGLE;
    }
}
