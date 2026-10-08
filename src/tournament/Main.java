package tournament;

import GUI.MainGUI;
import Management.TournamentManager;
import Model.Sport;
import Model.Tournament;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception exception) {
            java.util.logging.Logger.getLogger(Main.class.getName())
                    .log(java.util.logging.Level.WARNING, "Could not set system look and feel.", exception);
        }
        SwingUtilities.invokeLater(() -> {
            StartupSelectionDialog.Selection selection = StartupSelectionDialog.showDialog();
            if (selection == null) {
                return;
            }

            if (selection.getSport() != Sport.RACKET) {
                return;
            }
            if (selection.getMode() == StartupSelectionDialog.Mode.OPEN_PLAY) {
                MainGUI.getOrCreate().setVisible(true);
                return;
            }

            TournamentManager tournamentManager = new TournamentManager();
            Tournament tournament = TournamentSetupDialog.showDialog(null, tournamentManager);
            if (tournament != null) {
                MainGUI dashboard = MainGUI.getOrCreate();
                dashboard.showTournamentMode(tournament);
            }
        });
    }
}
