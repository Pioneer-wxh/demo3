package com.financetracker.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import com.financetracker.model.SavingGoal;
import com.financetracker.model.Settings;
import com.financetracker.service.SettingsService;
import com.financetracker.service.TransactionService;

/**
 * The home panel that serves as the main menu of the application.
 */
public class HomePanel extends JPanel {
    
    private TransactionService transactionService;
    private SettingsService settingsService;
    private ActionListener panelNavigationListener;

    private JLabel remainingBalanceLabel;
    private JPanel savingGoalsProgressPanel;
    private JLabel noGoalsLabel;
    private JLabel overallAccountBalanceLabel;
    
    /**
     * Constructor for HomePanel.
     * 
     * @param transactionService The transaction service
     * @param settingsService The settings service
     * @param panelNavigationListener The panel navigation listener
     */
    public HomePanel(TransactionService transactionService, SettingsService settingsService, ActionListener panelNavigationListener) {
        this.transactionService = transactionService;
        this.settingsService = settingsService;
        this.panelNavigationListener = panelNavigationListener;
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
        
        // Create a new central panel to hold both menu and savings goals
        JPanel centralContentPanel = new JPanel(new BorderLayout(0, 20));
        centralContentPanel.add(menuPanel, BorderLayout.NORTH);

        // Create saving goals progress panel
        savingGoalsProgressPanel = new JPanel();
        savingGoalsProgressPanel.setLayout(new BoxLayout(savingGoalsProgressPanel, BoxLayout.Y_AXIS));
        savingGoalsProgressPanel.setBorder(BorderFactory.createTitledBorder("Active Saving Goals Progress"));
        
        noGoalsLabel = new JLabel("No active saving goals to display.");
        noGoalsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        // Initially add the noGoalsLabel, it will be removed if there are goals.
        savingGoalsProgressPanel.add(noGoalsLabel);

        JScrollPane goalsScrollPane = new JScrollPane(savingGoalsProgressPanel);
        goalsScrollPane.setPreferredSize(new Dimension(400, 150)); // Adjust size as needed

        centralContentPanel.add(goalsScrollPane, BorderLayout.CENTER);
        
        add(centralContentPanel, BorderLayout.CENTER);
        
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
        
        // Add Overall Account Balance display to footer
        footerPanel.add(Box.createHorizontalStrut(20)); // Some spacing
        overallAccountBalanceLabel = new JLabel("Overall Account Balance: Loading...");
        overallAccountBalanceLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        footerPanel.add(overallAccountBalanceLabel);
        
        // Add footer to panel
        add(footerPanel, BorderLayout.SOUTH);
        
        refreshData(); // Initial call to populate data
    }
    
    /**
     * Creates a menu button with the specified text and action.
     * 
     * @param text The button text
     * @param panelName The name of the panel to show when the button is clicked
     * @return The created button
     */
    private JButton createMenuButton(String text, String panelNameCommand) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 16));
        button.setPreferredSize(new Dimension(300, 60));
        button.setActionCommand(panelNameCommand); // Set action command for the listener
        
        button.addActionListener(e -> {
            if (panelNavigationListener != null) {
                // Forward the action event (already contains the command)
                panelNavigationListener.actionPerformed(e);
            }
        });
        
        return button;
    }

    /**
     * Consolidated refresh method
     */
    public void refreshData() {
        updateRemainingBalance();
        updateSavingGoalsProgress();
        updateOverallAccountBalance();
    }

    /**
     * Updates the remaining balance display.
     * Called when the panel is shown or relevant data changes.
     */
    public void updateRemainingBalance() {
        if (transactionService == null || settingsService == null) {
            remainingBalanceLabel.setText("Remaining Balance: Error - Services not available");
            return;
        }
        Settings settings = settingsService.getSettings();
        if (settings == null) {
            remainingBalanceLabel.setText("Remaining Balance: Error - Settings not available");
            return;
        }
        try {
            double balance = transactionService.calculateRemainingBalanceForCurrentFinancialMonth(settings);
            String balanceText = String.format("Remaining Balance (Current Financial Month): %.2f %s", balance, settings.getDefaultCurrency());
            remainingBalanceLabel.setText(balanceText);
            remainingBalanceLabel.setForeground(balance < 0 ? Color.RED : Color.BLUE); 
        } catch (Exception e) {
            System.err.println("Error calculating remaining balance: " + e.getMessage());
            remainingBalanceLabel.setText("Remaining Balance: Error");
            remainingBalanceLabel.setForeground(Color.RED);
        }
    }

    /**
     * Updates the saving goals progress display.
     * Called when the panel is shown or relevant data changes.
     */
    public void updateSavingGoalsProgress() {
        if (settingsService == null || savingGoalsProgressPanel == null) {
            // Simplified error display
            if (noGoalsLabel != null) noGoalsLabel.setText("Error: Service unavailable for goals.");
            return;
        }
        Settings settings = settingsService.getSettings();
        if (settings == null || settings.getSavingGoals() == null) {
            if (noGoalsLabel != null) noGoalsLabel.setText("Error: Settings or goals unavailable.");
            return;
        }

        savingGoalsProgressPanel.removeAll();
        List<SavingGoal> activeGoals = settings.getSavingGoals().stream()
                                           .filter(SavingGoal::isActive)
                                           .filter(g -> !g.isCompleted())
                                           .collect(Collectors.toList());

        if (activeGoals.isEmpty()) {
            noGoalsLabel.setText("No active saving goals to display.");
            savingGoalsProgressPanel.add(noGoalsLabel);
        } else {
            for (SavingGoal goal : activeGoals) {
                JPanel goalEntryPanel = new JPanel(new BorderLayout(10, 2));
                goalEntryPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                String goalDisplayName = goal.getName() == null || goal.getName().trim().isEmpty() ? "(Unnamed Goal)" : goal.getName();
                JLabel goalNameLabel = new JLabel(goalDisplayName); 
                goalNameLabel.setFont(new Font("SimSun", Font.BOLD, 14));
                goalEntryPanel.add(goalNameLabel, BorderLayout.NORTH);

                JProgressBar progressBar = new JProgressBar(0, 100);
                progressBar.setValue((int) goal.getProgressPercentage());
                progressBar.setStringPainted(true);
                progressBar.setString(String.format("%.2f%%", goal.getProgressPercentage()));
                goalEntryPanel.add(progressBar, BorderLayout.CENTER);

                String progressText = String.format("Saved: %.2f / Target: %.2f %s",
                                                    goal.getCurrentAmount(), goal.getTargetAmount(), settings.getDefaultCurrency());
                JLabel progressDetailsLabel = new JLabel(progressText);
                progressDetailsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                goalEntryPanel.add(progressDetailsLabel, BorderLayout.SOUTH);
                goalEntryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, goalEntryPanel.getPreferredSize().height));
                savingGoalsProgressPanel.add(goalEntryPanel);
                savingGoalsProgressPanel.add(Box.createVerticalStrut(5));
            }
        }
        savingGoalsProgressPanel.revalidate();
        savingGoalsProgressPanel.repaint();
    }

    /**
     * Updates the overall account balance display.
     */
    public void updateOverallAccountBalance() {
        if (settingsService == null) {
            overallAccountBalanceLabel.setText("Overall Account Balance: Error - Service not available");
            return;
        }
        Settings settings = settingsService.getSettings();
        if (settings == null) {
            overallAccountBalanceLabel.setText("Overall Account Balance: Error - Settings not available");
            return;
        }
        String balanceText = String.format("Overall Account Balance: %.2f %s", settings.getOverallAccountBalance(), settings.getDefaultCurrency());
        overallAccountBalanceLabel.setText(balanceText);
        overallAccountBalanceLabel.setForeground(settings.getOverallAccountBalance() < 0 ? Color.RED : Color.GREEN); // Green for overall positive
    }

    /**
     * Applies theme specific colors or styles to the panel.
     * @param settings The application settings containing theme information.
     */
    public void applyTheme(Settings settings) {
        boolean isDark = settings.isDarkModeEnabled();
        // Example: Set panel background based on theme
        setBackground(isDark ? new Color(45, 45, 45) : UIManager.getColor("Panel.background"));
        
        // Update label colors that might have been set explicitly (e.g., balance labels)
        if (remainingBalanceLabel != null && remainingBalanceLabel.getText().startsWith("Remaining Balance:")){
            // Re-evaluate color based on current value and theme
            // This part needs careful handling if balance text itself indicates error.
             try {
                double balance = transactionService.calculateRemainingBalanceForCurrentFinancialMonth(settings);
                remainingBalanceLabel.setForeground(balance < 0 ? Color.RED : (isDark? new Color(152,195,121): Color.BLUE));
            } catch (Exception e) {
                remainingBalanceLabel.setForeground(Color.RED); // Error state
            }
        }
        if (overallAccountBalanceLabel != null && overallAccountBalanceLabel.getText().startsWith("Overall Account Balance:")){
            overallAccountBalanceLabel.setForeground(settings.getOverallAccountBalance() < 0 ? Color.RED : (isDark? new Color(152,195,121): Color.GREEN));
        }
        
        // You might need to update fonts/backgrounds of other components (buttons, text areas in goals) if they don't pick up UIManager changes well.
        // For instance, if JProgressBar in saving goals needs specific dark theme colors:
        if (savingGoalsProgressPanel != null) {
            for(Component comp : savingGoalsProgressPanel.getComponents()){
                if(comp instanceof JPanel){ // goalEntryPanel
                    comp.setBackground(isDark ? new Color(55,55,55) : UIManager.getColor("Panel.background"));
                    for(Component subComp : ((JPanel)comp).getComponents()){
                        if(subComp instanceof JLabel) subComp.setForeground(isDark ? Color.LIGHT_GRAY : Color.BLACK);
                        if(subComp instanceof JProgressBar) { /* UIManager should handle this, but can override */ }
                    }
                }
            }
        }
        // Propagate theme to children if they have their own applyTheme methods
    }
}
