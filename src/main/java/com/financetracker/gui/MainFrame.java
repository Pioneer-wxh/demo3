package com.financetracker.gui;

import com.financetracker.model.Settings;
import com.financetracker.service.SettingsService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * The main application window that contains all GUI components.
 */
public class MainFrame extends JFrame {
    
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private HomePanel homePanel;
    private TransactionPanel transactionPanel;
    private AnalysisPanel analysisPanel;
    private SettingsPanel settingsPanel;
    
    private Settings settings;
    private SettingsService settingsService;
    
    /**
     * Constructor for MainFrame.
     */
    public MainFrame() {
        // Initialize services
        settingsService = new SettingsService();
        settings = settingsService.loadSettings();
        
        // Set up the frame
        setTitle("Personal Finance Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 600));
        
        // Set up the content panel with card layout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        
        // Initialize panels
        homePanel = new HomePanel(this);
        transactionPanel = new TransactionPanel(this);
        analysisPanel = new AnalysisPanel(this);
        settingsPanel = new SettingsPanel(this);
        
        // Add panels to the content panel
        contentPanel.add(homePanel, "home");
        contentPanel.add(transactionPanel, "transactions");
        contentPanel.add(analysisPanel, "analysis");
        contentPanel.add(settingsPanel, "settings");
        
        // Show the home panel by default
        cardLayout.show(contentPanel, "home");
        
        // Add the content panel to the frame
        add(contentPanel);
        
        // Add window listener to save settings on close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                settingsService.saveSettings(settings);
            }
        });
        
        // Apply theme based on settings
        applyTheme();
    }
    
    /**
     * Applies the theme based on the settings.
     */
    private void applyTheme() {
        try {
            if (settings.isDarkModeEnabled()) {
                // Set dark theme
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                // Additional dark theme customization can be added here
            } else {
                // Set light theme
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                // Additional light theme customization can be added here
            }
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Shows the specified panel.
     * 
     * @param panelName The name of the panel to show
     */
    public void showPanel(String panelName) {
        cardLayout.show(contentPanel, panelName);
    }
    
    /**
     * Gets the settings.
     * 
     * @return The settings
     */
    public Settings getSettings() {
        return settings;
    }
    
    /**
     * Sets the settings.
     * 
     * @param settings The settings to set
     */
    public void setSettings(Settings settings) {
        this.settings = settings;
        applyTheme();
    }
    
    /**
     * Gets the settings service.
     * 
     * @return The settings service
     */
    public SettingsService getSettingsService() {
        return settingsService;
    }
}
