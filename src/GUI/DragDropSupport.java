package GUI;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.BorderFactory;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.function.Supplier;

/**
 * Lightweight in-app drag-and-drop that avoids java.awt.dnd/TransferHandler,
 * since those rely on native OS drag gestures that can misbehave across platforms.
 * Dragging is simulated with mouse listeners and a floating label on the glass pane.
 */
public final class DragDropSupport {

    private static volatile boolean dragInProgress = false;

    private DragDropSupport() {
    }

    /** True while the user is mid-drag; callers should avoid rebuilding drag sources during this window. */
    public static boolean isDragInProgress() {
        return dragInProgress;
    }

    public interface DropZone {
        boolean containsScreenPoint(Point screenPoint);
        void onDrop(Object payload);
    }

    public static DropZone dropZone(Component target, java.util.function.Consumer<Object> onDrop) {
        return new DropZone() {
            @Override
            public boolean containsScreenPoint(Point screenPoint) {
                if (target == null || !target.isShowing()) {
                    return false;
                }
                Rectangle bounds = new Rectangle(target.getLocationOnScreen(), target.getSize());
                return bounds.contains(screenPoint);
            }

            @Override
            public void onDrop(Object payload) {
                onDrop.accept(payload);
            }
        };
    }

    public static void makeDraggable(JComponent handle, Supplier<String> labelSupplier, Supplier<Object> payloadSupplier, DropZone... zones) {
        handle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        DragState state = new DragState();

        handle.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                JRootPane rootPane = SwingUtilities.getRootPane(handle);
                if (rootPane == null) {
                    return;
                }
                state.rootPane = rootPane;
                state.glassPane = (JPanel) rootPane.getGlassPane();
                state.glassPane.setLayout(null);
                state.ghost = new JLabel(labelSupplier.get());
                state.ghost.setOpaque(true);
                state.ghost.setBackground(new Color(255, 255, 210));
                state.ghost.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                state.ghost.setSize(state.ghost.getPreferredSize());
                state.glassPane.add(state.ghost);
                state.glassPane.setVisible(true);
                state.dragging = true;
                dragInProgress = true;
                positionGhost(state, handle, e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!state.dragging) {
                    return;
                }
                state.dragging = false;
                dragInProgress = false;
                state.glassPane.setVisible(false);
                state.glassPane.remove(state.ghost);
                state.glassPane.repaint();

                Point screenPoint = e.getLocationOnScreen();
                for (DropZone zone : zones) {
                    if (zone.containsScreenPoint(screenPoint)) {
                        zone.onDrop(payloadSupplier.get());
                        break;
                    }
                }
            }
        });

        handle.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!state.dragging) {
                    return;
                }
                positionGhost(state, handle, e);
            }
        });
    }

    private static void positionGhost(DragState state, Component source, MouseEvent e) {
        Point p = SwingUtilities.convertPoint(source, e.getPoint(), state.glassPane);
        state.ghost.setLocation(p.x + 10, p.y + 10);
        state.glassPane.repaint();
    }

    private static final class DragState {
        boolean dragging;
        JRootPane rootPane;
        JPanel glassPane;
        JLabel ghost;
    }
}
