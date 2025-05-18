package com.financetracker.gui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

public class AppStatusBar extends JPanel {

    private JLabel statusLabel;
    private Timer timeUpdateTimer; // For continuous time update
    private Timer clearStatusTimer; // For temporary messages
    private String defaultStatusText = "欢迎使用个人财务跟踪器";

    public AppStatusBar() {
        setLayout(new FlowLayout(FlowLayout.LEFT)); // Align status text to the left
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(3, 5, 3, 5))); // Adjusted padding

        statusLabel = new JLabel(defaultStatusText + getCurrentTimeFormatted());
        add(statusLabel);

        // Timer to update time every second
        timeUpdateTimer = new Timer(1000, e -> {
            statusLabel.setText(getDynamicStatusText() + getCurrentTimeFormatted());
        });
        timeUpdateTimer.start();

        // Timer for clearing temporary messages (initialized but not started here)
        clearStatusTimer = new Timer(5000, e -> {
            // Restore default text with current time
            statusLabel.setText(defaultStatusText + getCurrentTimeFormatted()); 
        });
        clearStatusTimer.setRepeats(false);
    }

    private String getCurrentTimeFormatted() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(" | HH:mm:ss"); // Simplified time format
        return now.format(formatter);
    }
    
    private String getDynamicStatusText(){
        String currentText = statusLabel.getText();
        int timeSeparatorIndex = currentText.lastIndexOf(" | ");
        if (clearStatusTimer.isRunning() || !currentText.startsWith(defaultStatusText)) {
            // If a temporary message is active (or was just set before timer started)
            // return its text part
            if(timeSeparatorIndex > 0){
                return currentText.substring(0, timeSeparatorIndex);
            } else {
                return currentText; // Should not happen if time is always appended
            }
        }
        return defaultStatusText; // Otherwise, use the default greeting
    }

    /**
     * Displays a temporary message on the status bar.
     * The message will be cleared after a few seconds, reverting to the default status.
     * @param message The message to display.
     */
    public void showTemporaryMessage(String message) {
        statusLabel.setText(message + getCurrentTimeFormatted());
        if (clearStatusTimer.isRunning()) {
            clearStatusTimer.restart();
        } else {
            clearStatusTimer.start();
        }
    }

    /**
     * Displays a persistent message on the status bar.
     * This will not be cleared automatically and will replace the default greeting part.
     * @param message The message to display.
     */
    public void setPersistentMessage(String message) {
        this.defaultStatusText = message; // Update the default text itself
        statusLabel.setText(this.defaultStatusText + getCurrentTimeFormatted());
        if (clearStatusTimer.isRunning()) {
            clearStatusTimer.stop(); // Stop any pending clear operations
        }
    }

    // Call this when the application is closing to stop timers
    public void dispose() {
        if (timeUpdateTimer != null && timeUpdateTimer.isRunning()) {
            timeUpdateTimer.stop();
        }
        if (clearStatusTimer != null && clearStatusTimer.isRunning()) {
            clearStatusTimer.stop();
        }
    }
    
    // Method to allow MainFrame or a ThemeManager to apply theme colors
    public void applyThemeColors(Color background, Color foreground, Color borderColor) {
        setBackground(background);
        statusLabel.setForeground(foreground);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, borderColor),
                BorderFactory.createEmptyBorder(3, 5, 3, 5)));
    }
} 