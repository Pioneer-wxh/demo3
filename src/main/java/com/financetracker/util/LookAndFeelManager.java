package com.financetracker.util;

import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.financetracker.model.Settings;

public class LookAndFeelManager {

    private static final Logger LOGGER = Logger.getLogger(LookAndFeelManager.class.getName());

    /**
     * Applies the initial LookAndFeel (Nimbus if available).
     * This should be called once at application startup before any UI is created.
     */
    public static void setInitialLookAndFeel() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Could not set system default LookAndFeel: " + ex.getMessage(), ex);
            }
        }
    }

    /**
     * Applies the theme (Light or Dark) based on settings.
     * This method updates UIManager properties for Nimbus to reflect the chosen theme.
     * It should be called after setInitialLookAndFeel and whenever the theme setting changes.
     * 
     * @param settings The application settings containing the theme preference.
     */
    public static void applyTheme(Settings settings) {
        if (settings == null) {
            LOGGER.warning("Settings object is null. Cannot apply theme.");
            return;
        }

        LOGGER.info("Applying theme: " + (settings.isDarkModeEnabled() ? "Dark Mode" : "Light Mode"));

        if (settings.isDarkModeEnabled()) {
            // Dark Mode Colors for Nimbus
            UIManager.put("control", new Color(60, 63, 65)); // General control background
            UIManager.put("info", new Color(60, 63, 65));    // Tooltip background
            UIManager.put("nimbusBase", new Color(30, 30, 30)); // Base color for many components
            UIManager.put("nimbusDisabledText", new Color(128, 128, 128));
            UIManager.put("nimbusFocus", new Color(81, 168, 221)); // Focus highlight color
            UIManager.put("nimbusLightBackground", new Color(45, 45, 45)); // Background for lists, tables, trees
            UIManager.put("nimbusSelectionBackground", new Color(81, 168, 221)); // Selected item background
            UIManager.put("nimbusSelectedText", Color.WHITE); // Text color for selected items
            UIManager.put("text", Color.LIGHT_GRAY); // Default text color
            UIManager.put("TextField.background", new Color(60, 63, 65));
            UIManager.put("TextField.foreground", Color.LIGHT_GRAY);
            UIManager.put("TextArea.background", new Color(60, 63, 65));
            UIManager.put("TextArea.foreground", Color.LIGHT_GRAY);
            UIManager.put("ComboBox.background", new Color(60, 63, 65));
            UIManager.put("ComboBox.foreground", Color.LIGHT_GRAY);
            UIManager.put("Button.background", new Color(75, 78, 80));
            UIManager.put("Button.foreground", Color.LIGHT_GRAY);
            UIManager.put("Panel.background", new Color(45, 45, 45));
            UIManager.put("Label.foreground", Color.LIGHT_GRAY);
            // Add more UIManager properties as needed for dark theme
        } else {
            // Light Mode (revert to Nimbus defaults or specify light theme colors)
            // Setting LookAndFeel to Nimbus again effectively resets many properties
            // or you can selectively put light theme values.
            try {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                LOGGER.log(Level.WARNING, "Failed to re-apply Nimbus for light theme", e);
            }
            // Explicitly set some common ones if needed for consistency after dark mode changes
            UIManager.put("Panel.background", new Color(240, 240, 240)); // Default light panel bg
            UIManager.put("Label.foreground", Color.BLACK);
            UIManager.put("text", Color.BLACK);
            UIManager.put("TextField.background", Color.WHITE);
            UIManager.put("TextField.foreground", Color.BLACK);
            UIManager.put("TextArea.background", Color.WHITE);
            UIManager.put("TextArea.foreground", Color.BLACK);
            // ... reset other properties if they were heavily changed by dark mode
        }
        LOGGER.info("Theme properties updated.");
    }
    
    // Old method, renamed to setInitialLookAndFeel and new applyTheme(Settings) added.
    // public static void applyLookAndFeel() { ... }
} 