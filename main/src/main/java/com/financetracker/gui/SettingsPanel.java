package com.financetracker.gui;

import com.financetracker.model.Settings;
import com.financetracker.model.SpecialDate;
import com.financetracker.service.SettingsService;
import com.financetracker.service.SpecialDateService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Panel for managing application settings.
 */
public class SettingsPanel extends JPanel {
    
    private MainFrame mainFrame;
    private SettingsService settingsService;
    private SpecialDateService specialDateService;
    
    private JTable specialDatesTable;
    private DefaultTableModel specialDatesTableModel;
    private JTextField specialDateNameField;
    private JTextField specialDateField;
    private JTextField specialDateDescriptionField;
    private JTextField specialDateCategoriesField;
    private JTextField specialDateImpactField;
    
    private JTextField budgetAmountField;
    private JTextField budgetDateField;
    
    private JTextField newCategoryField;
    
    private JSpinner monthStartDaySpinner;
    
    /**
     * Constructor for SettingsPanel.
     * 
     * @param mainFrame The main frame
     */
    public SettingsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.settingsService = mainFrame.getSettingsService();
        this.specialDateService = new SpecialDateService();
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
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Add title label to header
        JLabel titleLabel = new JLabel("Settings");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Add home button to header
        JButton homeButton = new JButton("HOME");
        homeButton.addActionListener(e -> mainFrame.showPanel("home"));
        headerPanel.add(homeButton, BorderLayout.EAST);
        
        // Add header to panel
        add(headerPanel, BorderLayout.NORTH);
        
        // Create main content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create special dates panel
        JPanel specialDatesPanel = createSpecialDatesPanel();
        contentPanel.add(specialDatesPanel);
        
        // Add some vertical space
        contentPanel.add(Box.createVerticalStrut(20));
        
        // Create budget goals panel
        JPanel budgetGoalsPanel = createBudgetGoalsPanel();
        contentPanel.add(budgetGoalsPanel);
        
        // Add some vertical space
        contentPanel.add(Box.createVerticalStrut(20));
        
        // Create category management panel
        JPanel categoryManagementPanel = createCategoryManagementPanel();
        contentPanel.add(categoryManagementPanel);
        
        // Add some vertical space
        contentPanel.add(Box.createVerticalStrut(20));
        
        // Create month start day panel
        JPanel monthStartDayPanel = createMonthStartDayPanel();
        contentPanel.add(monthStartDayPanel);
        
        // Add content panel to scroll pane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        add(scrollPane, BorderLayout.CENTER);
        
        // Create button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Add buttons to button panel
        JButton resetButton = new JButton("Reset to Default");
        resetButton.addActionListener(e -> resetToDefault());
        
        JButton saveButton = new JButton("Save Changes");
        saveButton.addActionListener(e -> saveChanges());
        
        buttonPanel.add(resetButton);
        buttonPanel.add(saveButton);
        
        // Add button panel to main panel
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Creates the special dates panel.
     * 
     * @return The special dates panel
     */
    private JPanel createSpecialDatesPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Special Dates Management"));
        
        // Create table panel
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());
        
        // Create table model with columns
        String[] columns = {"Name", "Date", "Description", "Affected Categories", "Expected Impact"};
        specialDatesTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        // Create table
        specialDatesTable = new JTable(specialDatesTableModel);
        specialDatesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(specialDatesTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add table panel to special dates panel
        panel.add(tablePanel, BorderLayout.CENTER);
        
        // Create form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create grid bag constraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Add name field
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Name:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        specialDateNameField = new JTextField(20);
        formPanel.add(specialDateNameField, gbc);
        
        // Add date field
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Date (YYYY-MM-DD):"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        specialDateField = new JTextField(10);
        formPanel.add(specialDateField, gbc);
        
        // Add description field
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Description:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        specialDateDescriptionField = new JTextField(20);
        formPanel.add(specialDateDescriptionField, gbc);
        
        // Add affected categories field
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Affected Categories:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        specialDateCategoriesField = new JTextField(20);
        formPanel.add(specialDateCategoriesField, gbc);
        
        // Add expected impact field
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Expected Impact (%):"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 4;
        specialDateImpactField = new JTextField(10);
        formPanel.add(specialDateImpactField, gbc);
        
        // Create button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        // Add buttons to button panel
        JButton addButton = new JButton("Add");
        addButton.addActionListener(e -> addSpecialDate());
        
        JButton editButton = new JButton("Edit");
        editButton.addActionListener(e -> editSpecialDate());
        
        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> deleteSpecialDate());
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        
        // Add button panel to form panel
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);
        
        // Add form panel to special dates panel
        panel.add(formPanel, BorderLayout.SOUTH);
        
        // Load special dates
        loadSpecialDates();
        
        return panel;
    }
    
    /**
     * Creates the budget goals panel.
     * 
     * @return The budget goals panel
     */
    private JPanel createBudgetGoalsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Budget Goals"));
        
        // Create form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create grid bag constraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Add budget amount field
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Budget Amount:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        budgetAmountField = new JTextField(10);
        formPanel.add(budgetAmountField, gbc);
        
        // Add budget date field
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Target Date (YYYY-MM-DD):"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        budgetDateField = new JTextField(10);
        formPanel.add(budgetDateField, gbc);
        
        // Create button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        // Add buttons to button panel
        JButton setBudgetButton = new JButton("Set Budget Goal");
        setBudgetButton.addActionListener(e -> setBudgetGoal());
        
        buttonPanel.add(setBudgetButton);
        
        // Add button panel to form panel
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);
        
        // Add form panel to budget goals panel
        panel.add(formPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Creates the category management panel.
     * 
     * @return The category management panel
     */
    private JPanel createCategoryManagementPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Category Management"));
        
        // Create categories panel
        JPanel categoriesPanel = new JPanel();
        categoriesPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        // Add categories
        Settings settings = mainFrame.getSettings();
        for (String category : settings.getDefaultCategories()) {
            JLabel categoryLabel = new JLabel(category);
            categoryLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.GRAY),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
            categoriesPanel.add(categoryLabel);
        }
        
        // Add categories panel to category management panel
        panel.add(categoriesPanel, BorderLayout.CENTER);
        
        // Create form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create grid bag constraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Add new category field
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("New Category:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        newCategoryField = new JTextField(20);
        formPanel.add(newCategoryField, gbc);
        
        // Create button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        // Add buttons to button panel
        JButton addCategoryButton = new JButton("Add Category");
        addCategoryButton.addActionListener(e -> addCategory());
        
        buttonPanel.add(addCategoryButton);
        
        // Add button panel to form panel
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);
        
        // Add form panel to category management panel
        panel.add(formPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Creates the month start day panel.
     * 
     * @return The month start day panel
     */
    private JPanel createMonthStartDayPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Month Start Day"));
        
        // Create form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create grid bag constraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Add month start day field
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Month Start Day:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(
                mainFrame.getSettings().getMonthStartDay(), // initial value
                1, // min
                28, // max
                1 // step
        );
        monthStartDaySpinner = new JSpinner(spinnerModel);
        formPanel.add(monthStartDaySpinner, gbc);
        
        // Add description
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        formPanel.add(new JLabel("This setting determines when a new \"financial month\" starts."), gbc);
        
        // Add form panel to month start day panel
        panel.add(formPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Loads special dates from the data file and displays them in the table.
     */
    private void loadSpecialDates() {
        // Clear the table
        specialDatesTableModel.setRowCount(0);
        
        // Load special dates
        List<SpecialDate> specialDates = specialDateService.getAllSpecialDates();
        
        // Add special dates to the table
        for (SpecialDate specialDate : specialDates) {
            Object[] row = {
                specialDate.getName(),
                specialDate.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                specialDate.getDescription(),
                specialDate.getAffectedCategories(),
                specialDate.getExpectedImpact()
            };
            specialDatesTableModel.addRow(row);
        }
    }
    
    /**
     * Adds a new special date.
     */
    private void addSpecialDate() {
        try {
            // Parse form data
            String name = specialDateNameField.getText();
            LocalDate date = LocalDate.parse(specialDateField.getText(), DateTimeFormatter.ISO_LOCAL_DATE);
            String description = specialDateDescriptionField.getText();
            String affectedCategories = specialDateCategoriesField.getText();
            double expectedImpact = Double.parseDouble(specialDateImpactField.getText());
            
            // Validate form data
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a name.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Create special date
            SpecialDate specialDate = new SpecialDate(name, date, description, affectedCategories, expectedImpact);
            
            // Save special date
            specialDateService.addSpecialDate(specialDate);
            
            // Reload special dates
            loadSpecialDates();
            
            // Clear form
            clearSpecialDateForm();
            
            // Show success message
            JOptionPane.showMessageDialog(this, "Special date added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid expected impact. Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding special date: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Edits the selected special date.
     */
    private void editSpecialDate() {
        int selectedRow = specialDatesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a special date to edit.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Get selected special date
        List<SpecialDate> specialDates = specialDateService.getAllSpecialDates();
        if (selectedRow < specialDates.size()) {
            SpecialDate specialDate = specialDates.get(selectedRow);
            
            // Fill form with special date data
            specialDateNameField.setText(specialDate.getName());
            specialDateField.setText(specialDate.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
            specialDateDescriptionField.setText(specialDate.getDescription());
            specialDateCategoriesField.setText(specialDate.getAffectedCategories());
            specialDateImpactField.setText(String.valueOf(specialDate.getExpectedImpact()));
            
            // Delete the special date (will be replaced when user clicks Add)
            specialDateService.deleteSpecialDate(specialDate);
            
            // Reload special dates
            loadSpecialDates();
        }
    }
    
    /**
     * Deletes the selected special date.
     */
    private void deleteSpecialDate() {
        int selectedRow = specialDatesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a special date to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Confirm deletion
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this special date?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Delete special date
        List<SpecialDate> specialDates = specialDateService.getAllSpecialDates();
        if (selectedRow < specialDates.size()) {
            SpecialDate specialDate = specialDates.get(selectedRow);
            specialDateService.deleteSpecialDate(specialDate);
            
            // Reload special dates
            loadSpecialDates();
            
            // Show success message
            JOptionPane.showMessageDialog(this, "Special date deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Clears the special date form.
     */
    private void clearSpecialDateForm() {
        specialDateNameField.setText("");
        specialDateField.setText("");
        specialDateDescriptionField.setText("");
        specialDateCategoriesField.setText("");
        specialDateImpactField.setText("");
    }
    
    /**
     * Sets a budget goal.
     */
    private void setBudgetGoal() {
        try {
            // Parse form data
            double amount = Double.parseDouble(budgetAmountField.getText());
            LocalDate date = LocalDate.parse(budgetDateField.getText(), DateTimeFormatter.ISO_LOCAL_DATE);
            
            // Validate form data
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Please enter a positive amount.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (date.isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(this, "Please enter a future date.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // TODO: Save budget goal
            
            // Clear form
            budgetAmountField.setText("");
            budgetDateField.setText("");
            
            // Show success message
            JOptionPane.showMessageDialog(this, "Budget goal set successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount. Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error setting budget goal: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Adds a new category.
     */
    private void addCategory() {
        try {
            // Get category name
            String categoryName = newCategoryField.getText();
            
            // Validate category name
            if (categoryName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a category name.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Add category to settings
            Settings settings = mainFrame.getSettings();
            List<String> categories = settings.getDefaultCategories();
            
            if (categories.contains(categoryName)) {
                JOptionPane.showMessageDialog(this, "Category already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            categories.add(categoryName);
            settings.setDefaultCategories(categories);
            
            // Save settings
            settingsService.saveSettings(settings);
            
            // Clear form
            newCategoryField.setText("");
            
            // Show success message
            JOptionPane.showMessageDialog(this, "Category added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            
            // Refresh panel
            mainFrame.setSettings(settings);
            mainFrame.showPanel("settings");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding category: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Resets settings to default values.
     */
    private void resetToDefault() {
        // Confirm reset
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to reset all settings to default values?", "Confirm Reset", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Reset settings
        Settings settings = settingsService.resetToDefault();
        
        // Update main frame
        mainFrame.setSettings(settings);
        
        // Show success message
        JOptionPane.showMessageDialog(this, "Settings reset to default values.", "Success", JOptionPane.INFORMATION_MESSAGE);
        
        // Refresh panel
        mainFrame.showPanel("settings");
    }
    
    /**
     * Saves changes to settings.
     */
    private void saveChanges() {
        try {
            // Get settings
            Settings settings = mainFrame.getSettings();
            
            // Update month start day
            int monthStartDay = (int) monthStartDaySpinner.getValue();
            settings.setMonthStartDay(monthStartDay);
            
            // Save settings
            settingsService.saveSettings(settings);
            
            // Update main frame
            mainFrame.setSettings(settings);
            
            // Show success message
            JOptionPane.showMessageDialog(this, "Settings saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving settings: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
