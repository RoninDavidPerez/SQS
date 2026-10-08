package tournament;

import Management.TournamentManager;
import Model.BracketType;
import Model.Sport;
import Model.Tournament;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/** Collects the required tournament details before opening the dashboard. */
public final class TournamentSetupDialog extends JDialog {

    private final TournamentManager tournamentManager;
    private final JTextField nameField = new JTextField(24);
    private final JComboBox<BracketType> bracketTypeBox =
            new JComboBox<>(BracketType.values());
    private final JTextArea descriptionArea = new JTextArea(5, 24);
    private Tournament tournament;

    private TournamentSetupDialog(Frame owner, TournamentManager tournamentManager) {
        super(owner, "Tournament setup", true);
        this.tournamentManager = tournamentManager;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setBorder(BorderFactory.createEmptyBorder(16, 16, 6, 16));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(6, 6, 6, 6);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.gridy = 0;
        fields.add(new JLabel("Bracket name:"), constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        fields.add(nameField, constraints);

        constraints.gridx = 0;
        constraints.gridy++;
        constraints.weightx = 0;
        fields.add(new JLabel("Bracket type:"), constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        fields.add(bracketTypeBox, constraints);

        constraints.gridx = 0;
        constraints.gridy++;
        constraints.weightx = 0;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        fields.add(new JLabel("Description:"), constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        fields.add(new JScrollPane(descriptionArea), constraints);
        add(fields, BorderLayout.CENTER);

        bracketTypeBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(
                    javax.swing.JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {
                java.awt.Component component = super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value instanceof BracketType) {
                    setText(((BracketType) value).getDisplayName());
                }
                return component;
            }
        });

        JButton createButton = new JButton("Create");
        JButton cancelButton = new JButton("Cancel");
        createButton.addActionListener(event -> createTournament());
        cancelButton.addActionListener(event -> dispose());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));
        actions.add(cancelButton);
        actions.add(createButton);
        add(actions, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(createButton);
        pack();
        setLocationRelativeTo(owner);
    }

    public static Tournament showDialog(Frame owner, TournamentManager tournamentManager) {
        TournamentSetupDialog dialog = new TournamentSetupDialog(owner, tournamentManager);
        dialog.setVisible(true);
        return dialog.tournament;
    }

    private void createTournament() {
        String name = nameField.getText().trim();
        String description = descriptionArea.getText().trim();
        if (name.isEmpty() || description.isEmpty() || bracketTypeBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Enter a bracket name and description, and choose a bracket type.",
                    "Tournament setup",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            tournament = tournamentManager.createTournament(
                    name,
                    (BracketType) bracketTypeBox.getSelectedItem(),
                    description,
                    Sport.RACKET);
            dispose();
        } catch (IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getMessage(),
                    "Tournament setup",
                    JOptionPane.WARNING_MESSAGE);
        }
    }
}
