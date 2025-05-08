package com.financetracker;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.financetracker.gui.MainFrame;

/**
 * Main entry point for launching the Finance Tracker application using the MainFrame structure.
 */
public class Main { // Renamed from AppLauncher

    public static void main(String[] args) {
        // Ensure GUI updates are done on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Optional: Set a modern look and feel like Nimbus
                // UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");

                // Set system look and feel for better platform integration
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Failed to set Look and Feel: " + e.getMessage());
                // Continue with default look and feel
            }

            // Create and show the main application window
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}
