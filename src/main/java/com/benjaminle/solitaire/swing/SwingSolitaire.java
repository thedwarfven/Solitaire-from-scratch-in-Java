package com.benjaminle.solitaire.swing;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Entry point for the graphical Java Swing version of Solitaire. */
public final class SwingSolitaire {
    private SwingSolitaire() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            useSystemLookAndFeel();
            new SolitaireFrame().setVisible(true);
        });
    }

    private static void useSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ignored) {
            // Swing's cross-platform look and feel remains a safe fallback.
        }
    }
}
