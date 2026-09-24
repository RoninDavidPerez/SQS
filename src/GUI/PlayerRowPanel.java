package GUI;

import Model.Player;
import java.awt.Container;
import javax.swing.JButton;
public class PlayerRowPanel extends javax.swing.JPanel {

    private javax.swing.JLabel dragHandle;

    public javax.swing.JLabel getDragHandle() {
        return dragHandle;
    }

        public String getPlayerName() {
    return jTextField1.getText().trim();
}

public Model.SkillLevel getSelectedSkill() {
    String selected = (String) jComboBox1.getSelectedItem();
    if (selected == null) return null;
   for (Model.SkillLevel level : Model.SkillLevel.values()) {
        if (level.getDisplayName().equals(selected)) {
            return level;
        }
    }
    return null;
}

public String getMatchFormat() {
    return jButton2.getText();
}
    public PlayerRowPanel() {
        initComponents();
jTextField1.setText("SAMPLE NAME");
this.setPreferredSize(new java.awt.Dimension(340, 45));
this.setMaximumSize(new java.awt.Dimension(Short.MAX_VALUE, 45));
    }

    public PlayerRowPanel(Player player) {
        this();
        jTextField1.setText(player.getName());
        jComboBox1.setSelectedItem(player.getSkillName());
        jButton2.setText(player.getFormat() == Model.MatchFormat.SINGLE ? "S" : "D");
    }

public Model.MatchFormat getSelectedFormat() {
    String text = jButton2.getText();
    if (text.equals("S")) {
        return Model.MatchFormat.SINGLE;
    } else {
        return Model.MatchFormat.DOUBLE;
    }
}


    @SuppressWarnings("unchecked")
    private void initComponents() {

        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jComboBox1 = new javax.swing.JComboBox<>();
        dragHandle = new javax.swing.JLabel();

        dragHandle.setText("\u2630");
        dragHandle.setToolTipText("Drag to On Hold");
        dragHandle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        dragHandle.setPreferredSize(new java.awt.Dimension(20, 28));
        dragHandle.setMinimumSize(new java.awt.Dimension(20, 28));
        dragHandle.setMaximumSize(new java.awt.Dimension(20, 28));

        jTextField1.setText("SAMPLE NAME");

        jButton1.setForeground(new java.awt.Color(250, 0, 0));
        jButton1.setText("X");
        jButton1.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        jButton1.setFont(jButton1.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        jButton1.setPreferredSize(new java.awt.Dimension(36, 28));
        jButton1.setMinimumSize(new java.awt.Dimension(36, 28));
        jButton1.setMaximumSize(new java.awt.Dimension(36, 28));
        jButton1.setToolTipText("Remove player");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("D");
        jButton2.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        jButton2.setFont(jButton2.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        jButton2.setPreferredSize(new java.awt.Dimension(36, 28));
        jButton2.setMinimumSize(new java.awt.Dimension(36, 28));
        jButton2.setMaximumSize(new java.awt.Dimension(36, 28));
        jButton2.setToolTipText("Switch between doubles and singles");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Beginner", "High Beginner", "Intermediate", "High Intermediate", "Advanced", "Expert", "Professional" }));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(4, 4)
                .addComponent(dragHandle, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(4, 4))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(5, 5)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dragHandle)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1)
                    .addComponent(jButton2)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(5, 5))
        );
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        Container parent = this.getParent();
    if (parent != null) {
    parent.remove(this);
    parent.revalidate();
    parent.repaint();
}
    }

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
        JButton btn = (JButton) evt.getSource();
if (btn.getText().equals("D")) {
    btn.setText("S");
} else {
    btn.setText("D");
}
    }

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {
    }


    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JTextField jTextField1;
}
