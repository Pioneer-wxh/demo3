package com.financetracker.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.financetracker.model.Settings;

/**
 * The home panel that serves as the main menu of the application.
 */
public class HomePanel extends JPanel {
    
    private MainFrame mainFrame;
    private JLabel remainingBalanceLabel;
    
    /**
     * Constructor for HomePanel.
     * 
     * @param mainFrame The main frame
     */
    public HomePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initComponents();
    }
    
    /**
     * Initializes the panel components.
     */
    private void initComponents() {
        // Set layout
        setLayout(new BorderLayout());
        
        // Create header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        // Add title label to header
        JLabel titleLabel = new JLabel("Personal Finance Tracker");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(titleLabel);
        
        // Add header to panel
        add(headerPanel, BorderLayout.NORTH);
        
        // Create main menu panel
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new GridBagLayout());
        
        // Create constraints for menu buttons
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);
        
        // Create menu buttons
        JButton transactionButton = createMenuButton("Transaction Record Management", "transactions");
        JButton analysisButton = createMenuButton("AI Assisted Analysis", "analysis");
        JButton settingsButton = createMenuButton("Set Up", "settings");
        
        // Add buttons to menu panel
        menuPanel.add(transactionButton, gbc);
        menuPanel.add(analysisButton, gbc);
        menuPanel.add(settingsButton, gbc);
        
        // Add menu panel to center
        add(menuPanel, BorderLayout.CENTER);
        
        // Create footer panel
        JPanel footerPanel = new JPanel();
        footerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        // Add version label to footer
        JLabel versionLabel = new JLabel("Version 1.0");
        footerPanel.add(versionLabel);
        
        // Add Remaining Balance display to footer (Task 1.4)
        footerPanel.add(Box.createHorizontalStrut(20)); // Some spacing
        remainingBalanceLabel = new JLabel("Remaining Balance: Loading...");
        remainingBalanceLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        footerPanel.add(remainingBalanceLabel);
        
        // Add footer to panel
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Creates a menu button with the specified text and action.
     * 
     * @param text The button text
     * @param panelName The name of the panel to show when the button is clicked
     * @return The created button
     */
    private JButton createMenuButton(String text, String panelName) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 16));
        button.setPreferredSize(new Dimension(300, 60));
        
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainFrame.showPanel(panelName);
            }
        });
        
        return button;
    }

    /**
     * Updates the remaining balance display.
     * Called when the panel is shown or relevant data changes.
     */
    public void updateRemainingBalance() {
        if (mainFrame == null || mainFrame.getTransactionService() == null || mainFrame.getSettingsService() == null) {
            remainingBalanceLabel.setText("Remaining Balance: Error - Services not available");
            return;
        }

        Settings settings = mainFrame.getSettingsService().getSettings();
        if (settings == null) {
            remainingBalanceLabel.setText("Remaining Balance: Error - Settings not available");
            return;
        }

        try {
            double balance = mainFrame.getTransactionService().calculateRemainingBalanceForCurrentFinancialMonth(settings);
            String balanceText = String.format("Remaining Balance (Current Financial Month): %.2f %s", balance, settings.getDefaultCurrency());
            remainingBalanceLabel.setText(balanceText);
            if (balance < 0) {
                remainingBalanceLabel.setForeground(Color.RED);
            } else {
                remainingBalanceLabel.setForeground(Color.BLUE); // Or UIManager.getColor("Label.foreground") for default
            }
        } catch (Exception e) {
            System.err.println("Error calculating or displaying remaining balance: " + e.getMessage());
            e.printStackTrace();
            remainingBalanceLabel.setText("Remaining Balance: Error");
            remainingBalanceLabel.setForeground(Color.RED);
        }
    }
}
