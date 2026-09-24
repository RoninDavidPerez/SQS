package GUI;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

/** Standalone window hosting the court panels, kept separate from the main dashboard window. */
public class CourtWindow extends JFrame {

    private final JLabel titleLabel = new JLabel("Racket Sports Queue");
    private final JLabel descriptionLabel = new JLabel("Open play dashboard");

    public CourtWindow(JComponent courtsView) {
        super("Courts");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(java.awt.Font.BOLD, 24f));
        descriptionLabel.setHorizontalAlignment(JLabel.CENTER);

        JPanel header = new JPanel();
        header.setLayout(new javax.swing.BoxLayout(header, javax.swing.BoxLayout.Y_AXIS));
        header.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 4, 8));
        header.add(titleLabel);
        header.add(descriptionLabel);

        getContentPane().add(header, BorderLayout.NORTH);
        getContentPane().add(courtsView, BorderLayout.CENTER);
        setSize(1150, 430);
        positionAtTop();
    }

    public void setDashboardDetails(String title, String description) {
        titleLabel.setText(title);
        descriptionLabel.setText(description);
        setTitle(title);
    }

    private void positionAtTop() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = Math.max(0, (screenSize.width - getWidth()) / 2);
        setLocation(x, 0);
    }
}
