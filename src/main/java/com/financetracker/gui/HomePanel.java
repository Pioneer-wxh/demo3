package com.financetracker.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.financetracker.model.Settings;
import com.financetracker.model.SavingGoal;
import java.util.List;

/**
 * The home panel that serves as the main menu of the application.
 */
public class HomePanel extends JPanel {
    
    private MainFrame mainFrame;
    private JLabel remainingBalanceLabel;
    private JPanel savingGoalsProgressPanel;
    private JLabel noGoalsLabel;
    private JLabel overallAccountBalanceLabel;
    
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
        
        updateSavingGoalsProgress(); // Initial call to populate goals
        updateOverallAccountBalance(); // Initial call to populate overall balance
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
        updateSavingGoalsProgress(); // Update goals when balance updates
        updateOverallAccountBalance(); // Update overall balance as well
    }

    /**
     * Updates the saving goals progress display.
     * Called when the panel is shown or relevant data changes.
     */
    public void updateSavingGoalsProgress() {
        if (mainFrame == null || mainFrame.getSettingsService() == null || savingGoalsProgressPanel == null) {
            if (noGoalsLabel != null && savingGoalsProgressPanel != null) {
                savingGoalsProgressPanel.removeAll();
                noGoalsLabel.setText("Error: Services not available for goals.");
                savingGoalsProgressPanel.add(noGoalsLabel);
                savingGoalsProgressPanel.revalidate();
                savingGoalsProgressPanel.repaint();
            }
            return;
        }

        Settings settings = mainFrame.getSettingsService().getSettings();
        if (settings == null || settings.getSavingGoals() == null) {
            if (noGoalsLabel != null && savingGoalsProgressPanel != null) {
                savingGoalsProgressPanel.removeAll();
                noGoalsLabel.setText("Error: Settings or goals not available.");
                savingGoalsProgressPanel.add(noGoalsLabel);
                savingGoalsProgressPanel.revalidate();
                savingGoalsProgressPanel.repaint();
            }
            return;
        }

        savingGoalsProgressPanel.removeAll(); // Clear previous entries

        List<SavingGoal> activeGoals = settings.getSavingGoals().stream()
                                           .filter(SavingGoal::isActive)
                                           .filter(g -> !g.isCompleted())
                                           .collect(java.util.stream.Collectors.toList());

        if (activeGoals.isEmpty()) {
            noGoalsLabel.setText("No active saving goals to display.");
            savingGoalsProgressPanel.add(noGoalsLabel);
        } else {
            for (SavingGoal goal : activeGoals) {
                JPanel goalEntryPanel = new JPanel(new BorderLayout(10, 2)); // Gap between components
                goalEntryPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

                JLabel goalNameLabel = new JLabel(goal.getName());
                goalNameLabel.setFont(new Font("Arial", Font.BOLD, 14));
                goalEntryPanel.add(goalNameLabel, BorderLayout.NORTH);

                JProgressBar progressBar = new JProgressBar(0, 100);
                progressBar.setValue((int) goal.getProgressPercentage());
                progressBar.setStringPainted(true);
                progressBar.setString(String.format("%.2f%%", goal.getProgressPercentage()));
                goalEntryPanel.add(progressBar, BorderLayout.CENTER);

                String progressText = String.format("Saved: %.2f / Target: %.2f %s",
                                                    goal.getCurrentAmount(),
                                                    goal.getTargetAmount(),
                                                    settings.getDefaultCurrency());
                JLabel progressDetailsLabel = new JLabel(progressText);
                progressDetailsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                goalEntryPanel.add(progressDetailsLabel, BorderLayout.SOUTH);
                
                goalEntryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, goalEntryPanel.getPreferredSize().height));


                savingGoalsProgressPanel.add(goalEntryPanel);
                savingGoalsProgressPanel.add(Box.createVerticalStrut(5)); // Spacing between goals
            }
        }
        savingGoalsProgressPanel.revalidate();
        savingGoalsProgressPanel.repaint();
    }

    /**
     * Updates the overall account balance display.
     */
    public void updateOverallAccountBalance() {
        if (mainFrame == null || mainFrame.getSettingsService() == null) {
            overallAccountBalanceLabel.setText("Overall Account Balance: Error - Services not available");
            return;
        }

        Settings settings = mainFrame.getSettingsService().getSettings();
        if (settings == null) {
            overallAccountBalanceLabel.setText("Overall Account Balance: Error - Settings not available");
            return;
        }

        try {
            double overallBalance = settings.getOverallAccountBalance();
            String balanceText = String.format("Overall Account Balance: %.2f %s", overallBalance, settings.getDefaultCurrency());
            overallAccountBalanceLabel.setText(balanceText);
            if (overallBalance < 0) {
                overallAccountBalanceLabel.setForeground(Color.RED);
            } else {
                overallAccountBalanceLabel.setForeground(new Color(0, 100, 0)); // Dark Green
            }
        } catch (Exception e) {
            System.err.println("Error displaying overall account balance: " + e.getMessage());
            e.printStackTrace();
            overallAccountBalanceLabel.setText("Overall Account Balance: Error");
            overallAccountBalanceLabel.setForeground(Color.RED);
        }
    }
}
