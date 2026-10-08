package tournament;

import Model.Sport;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/** First window shown when the application starts. */
public final class StartupSelectionDialog extends JDialog {

    public enum Mode {
        OPEN_PLAY("Open Play"),
        TOURNAMENT("Tournament");

        private final String displayName;

        Mode(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public static final class Selection {
        private final Sport sport;
        private final Mode mode;

        private Selection(Sport sport, Mode mode) {
            this.sport = sport;
            this.mode = mode;
        }

        public Sport getSport() {
            return sport;
        }

        public Mode getMode() {
            return mode;
        }
    }

    private final JComboBox<Sport> sportBox = new JComboBox<>(Sport.values());
    private final JComboBox<Mode> modeBox = new JComboBox<>(Mode.values());
    private Selection selection;

    private StartupSelectionDialog() {
        super((Frame) null, "Choose sport and mode", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(12, 12));

        JPanel form = new JPanel(new java.awt.GridLayout(2, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 8, 20));
        form.add(new JLabel("Sport:"));
        form.add(sportBox);
        form.add(new JLabel("Mode:"));
        form.add(modeBox);
        add(form, BorderLayout.CENTER);

        sportBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(
                    javax.swing.JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {
                java.awt.Component component = super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value instanceof Sport) {
                    setText(((Sport) value).getDisplayName());
                }
                return component;
            }
        });

        JButton continueButton = new JButton("Continue");
        JButton cancelButton = new JButton("Cancel");
        continueButton.addActionListener(event -> continueSelection());
        cancelButton.addActionListener(event -> dispose());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));
        actions.add(cancelButton);
        actions.add(continueButton);
        add(actions, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(continueButton);
        pack();
        setLocationRelativeTo(null);
    }

    public static Selection showDialog() {
        StartupSelectionDialog dialog = new StartupSelectionDialog();
        dialog.setVisible(true);
        return dialog.selection;
    }

    private void continueSelection() {
        Sport selectedSport = (Sport) sportBox.getSelectedItem();
        Mode selectedMode = (Mode) modeBox.getSelectedItem();
        if (selectedSport != Sport.RACKET) {
            JOptionPane.showMessageDialog(
                    this,
                    selectedSport.getDisplayName()
                            + " is not available yet. Choose Racket to continue.",
                    "Sport unavailable",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        selection = new Selection(selectedSport, selectedMode);
        dispose();
    }
}
