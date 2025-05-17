package com.financetracker.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

import com.financetracker.model.SavingGoal;
import com.financetracker.model.Settings;
import com.financetracker.model.SpecialDate;
import com.financetracker.service.BudgetAdjustmentService;
import com.financetracker.service.SettingsService;
import com.financetracker.service.SpecialDateService;

// Enum to distinguish category types
enum CategoryType {
    EXPENSE, INCOME
}

/**
 * Panel for managing application settings.
 */
public class SettingsPanel extends JPanel {
    
    private MainFrame mainFrame;
    private SettingsService settingsService;
    private SpecialDateService specialDateService;
    private BudgetAdjustmentService budgetAdjustmentService;
    
    private JTable specialDatesTable;
    private DefaultTableModel specialDatesTableModel;
    private JTextField specialDateNameField;
    private JSpinner specialDateSpinner;
    private JTextField specialDateDescriptionField;
    private JTextField specialDateCategoriesField;
    private JTextField specialDateImpactField;
    
    private JTextField savingsAmountField;
    private JSpinner savingsDateSpinner;
    
    private JTextField newExpenseCategoryField;
    private JTextField newIncomeCategoryField;
    private JPanel expenseCategoriesDisplayPanel;
    private JPanel incomeCategoriesDisplayPanel;
    
    private JSpinner monthStartDaySpinner;

    // --- Components for Saving Goals Management ---
    private JTable savingGoalsTable;
    private DefaultTableModel savingGoalsTableModel;
    private JTextField sgNameField;
    private JTextField sgDescriptionField;
    private JSpinner sgTargetAmountSpinner;
    private JSpinner sgMonthlyContributionSpinner;
    private JSpinner sgStartDateSpinner;
    private JSpinner sgTargetDateSpinner; // Optional
    private JCheckBox sgIsActiveCheckBox;
    private SavingGoal currentEditingSavingGoal = null; // To hold goal being edited
    private JButton addGoalButton;
    private JButton saveGoalButton;
    // --- End Components for Saving Goals Management ---
    
    /**
     * Constructor for SettingsPanel.
     * 
     * @param mainFrame The main frame
     */
    public SettingsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.settingsService = mainFrame.getSettingsService();
        this.specialDateService = mainFrame.getSpecialDateService();
        this.budgetAdjustmentService = mainFrame.getBudgetAdjustmentService();
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
        
        // Create savings goals panel
        JPanel savingsGoalsPanel = createSavingsGoalsPanel();
        contentPanel.add(savingsGoalsPanel);
        
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
        String[] columns = {"Name", "Date", "Description", "Affected Category", "Amount Increase", "Budget Effect (Example)"};
        specialDatesTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        // Create table
        specialDatesTable = new JTable(specialDatesTableModel);
        specialDatesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Add explanation label
        JLabel explanationLabel = new JLabel("<html>特殊日期允许您标记特定日期并设置其对预算的影响。<br>正数表示预算增加，负数表示预算减少。</html>");
        explanationLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        tablePanel.add(explanationLabel, BorderLayout.NORTH);
        
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
        formPanel.add(new JLabel("Date:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        Date now = new Date();
        SpinnerDateModel specialDateModel = new SpinnerDateModel(now, null, null, Calendar.DAY_OF_MONTH);
        specialDateSpinner = new JSpinner(specialDateModel);
        JSpinner.DateEditor specialDateEditor = new JSpinner.DateEditor(specialDateSpinner, "yyyy-MM-dd");
        specialDateSpinner.setEditor(specialDateEditor);
        formPanel.add(specialDateSpinner, gbc);
        
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
        formPanel.add(new JLabel("Affected Category:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        specialDateCategoriesField = new JTextField(20);
        formPanel.add(specialDateCategoriesField, gbc);
        
        // Add expected impact field
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Amount Increase:"), gbc);
        
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
     * Creates the savings goals panel.
     * 
     * @return The savings goals panel
     */
    private JPanel createSavingsGoalsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Saving Goals Management"));

        // --- Table for displaying saving goals ---
        String[] goalColumns = {"Name", "Target Amount", "Current Amount", "Monthly Contribution", "Start Date", "Target Date", "Active"};
        savingGoalsTableModel = new DefaultTableModel(goalColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Table is for display, editing via form
            }
        };
        savingGoalsTable = new JTable(savingGoalsTableModel);
        savingGoalsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableScrollPane = new JScrollPane(savingGoalsTable);
        panel.add(tableScrollPane, BorderLayout.CENTER);

        // --- Form for adding/editing saving goals ---
        JPanel formOuterPanel = new JPanel(new BorderLayout(5,5));
        formOuterPanel.setBorder(BorderFactory.createEmptyBorder(10,0,0,0)); // Add some top margin
        
        JPanel formFieldsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        int y = 0;

        // Name
        gbc.gridx = 0; gbc.gridy = y; formFieldsPanel.add(new JLabel("Goal Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = y++; sgNameField = new JTextField(20); formFieldsPanel.add(sgNameField, gbc);

        // Description
        gbc.gridx = 0; gbc.gridy = y; formFieldsPanel.add(new JLabel("Description (Optional):"), gbc);
        gbc.gridx = 1; gbc.gridy = y++; sgDescriptionField = new JTextField(20); formFieldsPanel.add(sgDescriptionField, gbc);

        // Target Amount
        gbc.gridx = 0; gbc.gridy = y; formFieldsPanel.add(new JLabel("Target Amount:"), gbc);
        sgTargetAmountSpinner = new JSpinner(new SpinnerNumberModel(1000.0, 0.0, Double.MAX_VALUE, 100.0));
        gbc.gridx = 1; gbc.gridy = y++; formFieldsPanel.add(sgTargetAmountSpinner, gbc);

        // Monthly Contribution
        gbc.gridx = 0; gbc.gridy = y; formFieldsPanel.add(new JLabel("Monthly Contribution:"), gbc);
        sgMonthlyContributionSpinner = new JSpinner(new SpinnerNumberModel(50.0, 0.0, Double.MAX_VALUE, 10.0));
        gbc.gridx = 1; gbc.gridy = y++; formFieldsPanel.add(sgMonthlyContributionSpinner, gbc);

        // Start Date
        gbc.gridx = 0; gbc.gridy = y; formFieldsPanel.add(new JLabel("Start Date:"), gbc);
        sgStartDateSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        sgStartDateSpinner.setEditor(new JSpinner.DateEditor(sgStartDateSpinner, "yyyy-MM-dd"));
        gbc.gridx = 1; gbc.gridy = y++; formFieldsPanel.add(sgStartDateSpinner, gbc);

        // Target Date (Optional)
        gbc.gridx = 0; gbc.gridy = y; formFieldsPanel.add(new JLabel("Target Date (Optional):"), gbc);
        // Allow null for target date by not setting a lower bound on date spinner if it was strict
        // For now, standard date spinner, user can ignore or clear if logic allows null persistence
        sgTargetDateSpinner = new JSpinner(new SpinnerDateModel(Date.from(LocalDate.now().plusYears(1).atStartOfDay(ZoneId.systemDefault()).toInstant()), null, null, Calendar.DAY_OF_MONTH));
        sgTargetDateSpinner.setEditor(new JSpinner.DateEditor(sgTargetDateSpinner, "yyyy-MM-dd"));
        // To make it clear it's optional, perhaps add a checkbox to enable/disable it or clear button.
        // For now, relying on user not setting a past date or a very far future if not intended.
        gbc.gridx = 1; gbc.gridy = y++; formFieldsPanel.add(sgTargetDateSpinner, gbc);
        // We can add a small button or checkbox later to truly nullify this if needed.
        // For now, it will always have a date.

        // Is Active
        gbc.gridx = 0; gbc.gridy = y; formFieldsPanel.add(new JLabel("Active Goal:"), gbc);
        sgIsActiveCheckBox = new JCheckBox();
        sgIsActiveCheckBox.setSelected(true); // Default to active
        gbc.gridx = 1; gbc.gridy = y++; formFieldsPanel.add(sgIsActiveCheckBox, gbc);
        
        formOuterPanel.add(new JScrollPane(formFieldsPanel), BorderLayout.CENTER); // Make form scrollable if it gets too long

        // --- Buttons for form actions ---
        JPanel formButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        this.addGoalButton = new JButton("Add New Goal");
        this.saveGoalButton = new JButton("Save Edited Goal");
        this.saveGoalButton.setEnabled(false);
        JButton clearFormButton = new JButton("Clear Form / Cancel Edit");
        clearFormButton.addActionListener(e -> clearSavingGoalForm());

        formButtonPanel.add(addGoalButton);
        formButtonPanel.add(saveGoalButton);
        formButtonPanel.add(clearFormButton);
        formOuterPanel.add(formButtonPanel, BorderLayout.SOUTH);

        panel.add(formOuterPanel, BorderLayout.SOUTH);

        // --- Buttons for table actions ---
        JPanel tableActionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton editSelectedGoalButton = new JButton("Edit Selected");
        editSelectedGoalButton.addActionListener(e -> editSelectedSavingGoal());
        JButton deleteSelectedGoalButton = new JButton("Delete Selected");
        deleteSelectedGoalButton.addActionListener(e -> deleteSelectedSavingGoal());
        tableActionsPanel.add(editSelectedGoalButton);
        tableActionsPanel.add(deleteSelectedGoalButton);
        // Add this panel above or below the table, or combine with form buttons if layout prefers.
        // Let's put it as a small bar between table and form.
        panel.add(tableActionsPanel, BorderLayout.NORTH); // This places it above the table, which is not ideal.
        // Let's reconsider: put table actions below the table, form below that.
        // To do this, the main panel needs a different layout or nested panels.
        
        // Revised structure: Main Panel (BorderLayout)
        // CENTER: tableScrollPane
        // SOUTH: new JPanel(BorderLayout) that contains:
        //      CENTER: tableActionsPanel
        //      SOUTH: formOuterPanel

        // Let's adjust the main panel structure for better button placement
        panel.remove(tableScrollPane);
        panel.remove(formOuterPanel);
        panel.remove(tableActionsPanel); // remove if it was added already

        JPanel southOfTablePanel = new JPanel(new BorderLayout(5,5));
        southOfTablePanel.add(tableActionsPanel, BorderLayout.NORTH);
        southOfTablePanel.add(formOuterPanel, BorderLayout.CENTER);

        panel.add(tableScrollPane, BorderLayout.CENTER);
        panel.add(southOfTablePanel, BorderLayout.SOUTH);


        // Initialize and load data
        loadSavingGoals(); 
        return panel;
    }
    
    /**
     * Loads special dates into the table.
     */
    public void loadSpecialDates() {
        // Clear table
        specialDatesTableModel.setRowCount(0);
        
        // Get all special dates
        List<SpecialDate> specialDates = specialDateService.getAllSpecialDates();
        
        // Add rows to table
        for (SpecialDate specialDate : specialDates) {
            Settings settings = settingsService.getSettings();
            double baseBudget = settings.getMonthlyBudget();
            double amountIncreaseValue = specialDate.getAmountIncrease();
            double adjustedBudget = baseBudget + amountIncreaseValue;
            String budgetEffect = String.format("%.2f + %.2f = %.2f", baseBudget, amountIncreaseValue, adjustedBudget);
            
            Object[] row = {
                specialDate.getName(),
                specialDate.getDate().toString(),
                specialDate.getDescription(),
                specialDate.getAffectedCategory(),
                String.format("%.2f", specialDate.getAmountIncrease()),
                budgetEffect
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
            String name = specialDateNameField.getText().trim();
            
            // 从日期选择器获取日期
            Date selectedDateValue = (Date) specialDateSpinner.getValue();
            LocalDate date = selectedDateValue.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            
            String description = specialDateDescriptionField.getText().trim();
            String affectedCategory = specialDateCategoriesField.getText().trim();
            double amountIncrease = 0.0;
            try {
                amountIncrease = Double.parseDouble(specialDateImpactField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount for special date impact.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validate form data
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a name for the special date.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (affectedCategory.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter an affected category for the special date.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Create special date using appropriate constructor
            SpecialDate specialDate = new SpecialDate();
            specialDate.setName(name);
            specialDate.setDate(date);
            specialDate.setDescription(description);
            specialDate.setAffectedCategory(affectedCategory);
            specialDate.setAmountIncrease(amountIncrease);
            specialDate.setRecurring(false);
            specialDate.setRecurrenceType(SpecialDate.RecurrenceType.NONE);
            
            // Add special date
            boolean success = specialDateService.addSpecialDate(specialDate);
            
            if (success) {
                // Clear form
                clearSpecialDateForm();
                
                // Reload table
                loadSpecialDates();
                
                // Show success message
                JOptionPane.showMessageDialog(this, "Special date added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add special date.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount format.", "Error", JOptionPane.ERROR_MESSAGE);
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

        // Retrieve data from table (assuming order of columns)
        String name = (String) specialDatesTableModel.getValueAt(selectedRow, 0);
        LocalDate date = LocalDate.parse((String) specialDatesTableModel.getValueAt(selectedRow, 1));
        String description = (String) specialDatesTableModel.getValueAt(selectedRow, 2);
        String affectedCategory = (String) specialDatesTableModel.getValueAt(selectedRow, 3);
        double amountIncrease;
        try {
            String amountStr = ((String) specialDatesTableModel.getValueAt(selectedRow, 4)).replaceAll("[^\\d.-]", "");
            amountIncrease = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
             JOptionPane.showMessageDialog(this, "Could not parse amount increase from table for editing.", "Error", JOptionPane.ERROR_MESSAGE);
             return;
        }

        // Populate form fields
        specialDateNameField.setText(name);
        specialDateSpinner.setValue(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        specialDateDescriptionField.setText(description);
        specialDateCategoriesField.setText(affectedCategory);
        specialDateImpactField.setText(String.format("%.2f", amountIncrease));
        
        // For simplicity, editing means deleting the old one and adding a new one with current form data
        // This is not ideal for preserving ID or complex recurrence settings not exposed in this simple form.
        // A more robust edit would fetch the full SpecialDate object and allow modifying its properties.
        JOptionPane.showMessageDialog(this, 
            "Editing a special date: Modify the details in the form and click \"Add Special Date\".\n" +
            "You may need to manually delete the old entry if the name/date changes significantly.\n" +
            "(A more direct edit feature will be improved later)", 
            "Edit Information", 
            JOptionPane.INFORMATION_MESSAGE);

        // A proper edit would require identifying the SpecialDate object (e.g., by ID if stored in table model)
        // and then calling specialDateService.updateSpecialDate(updatedSpecialDateObject);
        // For now, prompt user to re-add after deleting or rely on Add to overwrite if name/date matches (if logic supports that)
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
        specialDateSpinner.setValue(new Date());
        specialDateDescriptionField.setText("");
        specialDateCategoriesField.setText("");
        specialDateImpactField.setText("");
    }
    
    /**
     * Sets a savings goal.
     */
    private void setSavingsGoal() {
        JOptionPane.showMessageDialog(this, "Savings Goal functionality not yet fully implemented in Settings Panel.", "Info", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Adds a new category to the settings based on the type.
     * @param type The type of category to add (EXPENSE or INCOME).
     */
    private void addCategory(CategoryType type) {
        String categoryName;
        List<String> categories;
        Settings settings = mainFrame.getSettings();

        if (type == CategoryType.EXPENSE) {
            categoryName = newExpenseCategoryField.getText().trim();
            categories = settings.getExpenseCategories();
        } else { // INCOME
            categoryName = newIncomeCategoryField.getText().trim();
            categories = settings.getIncomeCategories();
        }

        if (categoryName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a category name.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (categories.contains(categoryName)) {
            JOptionPane.showMessageDialog(this, "Category already exists.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (type == CategoryType.EXPENSE) {
            settings.addExpenseCategory(categoryName);
            newExpenseCategoryField.setText("");
        } else {
            settings.addIncomeCategory(categoryName);
            newIncomeCategoryField.setText("");
        }
        
        refreshCategoryDisplay();
        mainFrame.refreshCategoryLists(); // Refresh lists in other panels like TransactionPanel

        JOptionPane.showMessageDialog(this, "Category added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Deletes a category of the specified type.
     * @param categoryToDelete The name of the category to delete.
     * @param type The type of category (EXPENSE or INCOME).
     */
    private void deleteCategory(String categoryToDelete, CategoryType type) {
        try {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete the category \\\"" + categoryToDelete + "\\\"?\\n" +
                    "Transactions using this category will remain unchanged.",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            Settings settings = mainFrame.getSettings();
            boolean removed;

            if (type == CategoryType.EXPENSE) {
                if (settings.getExpenseCategories().size() <= 1) {
                    JOptionPane.showMessageDialog(this, "At least one expense category must be kept.", "Cannot Delete", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                removed = settings.removeExpenseCategory(categoryToDelete);
            } else { // INCOME
                if (settings.getIncomeCategories().size() <= 1) {
                    JOptionPane.showMessageDialog(this, "At least one income category must be kept.", "Cannot Delete", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                removed = settings.removeIncomeCategory(categoryToDelete);
            }

            if (removed) {
                refreshCategoryDisplay();
                mainFrame.refreshCategoryLists();
                JOptionPane.showMessageDialog(this, "Category deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Category not found or could not be deleted.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error deleting category: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Refreshes the display of both expense and income categories.
     */
    public void refreshCategoryDisplay() {
        if (expenseCategoriesDisplayPanel == null || incomeCategoriesDisplayPanel == null || mainFrame == null || mainFrame.getSettings() == null) {
            return;
        }
        Settings settings = mainFrame.getSettings();
        populateCategoryPanel(expenseCategoriesDisplayPanel, settings.getExpenseCategories(), CategoryType.EXPENSE);
        populateCategoryPanel(incomeCategoriesDisplayPanel, settings.getIncomeCategories(), CategoryType.INCOME);
    }

    /**
     * Helper method to populate a given panel with category items.
     * @param displayPanel The JPanel to populate.
     * @param categories The list of category strings.
     * @param type The type of category (for the delete action).
     */
    private void populateCategoryPanel(JPanel displayPanel, List<String> categories, CategoryType type) {
        displayPanel.removeAll();
        if (categories != null) {
            for (String category : categories) {
                JPanel categoryItemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
                // Optional: add a small border or background to distinguish items visually
                // categoryItemPanel.setBorder(BorderFactory.createEtchedBorder()); 
                
                JLabel categoryLabel = new JLabel(category);
                categoryItemPanel.add(categoryLabel);

                JButton deleteButton = new JButton("×");
                // Styling for a smaller delete button
                deleteButton.setFont(new Font(deleteButton.getFont().getName(), Font.BOLD, 12));
                deleteButton.setMargin(new Insets(0, 2, 0, 2)); 
                deleteButton.setToolTipText("Delete this category");
                deleteButton.addActionListener(e -> deleteCategory(category, type));
                categoryItemPanel.add(deleteButton);

                displayPanel.add(categoryItemPanel);
            }
        }
        displayPanel.revalidate();
        displayPanel.repaint();
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
        settingsService.resetToDefault();
        Settings settings = settingsService.getSettings();
        
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
            settingsService.getSettings().setMonthStartDay(monthStartDay);
            settingsService.saveSettings();
            
            // Update main frame
            mainFrame.setSettings(settings);
            
            // Show success message
            JOptionPane.showMessageDialog(this, "Settings saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving settings: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 保存财务月设置
     */
    private void saveFinancialMonthSettings() {
        try {
            // 获取设置值
            int monthStartDay = (int) monthStartDaySpinner.getValue();
            
            // 更新设置
            Settings settings = mainFrame.getSettings();
            settings.setMonthStartDay(monthStartDay);
            
            // 保存设置
            settingsService.getSettings().setMonthStartDay(monthStartDay);
            boolean saved = settingsService.saveSettings();
            
            if (saved) {
                // 更新主窗口设置
                mainFrame.setSettings(settings);
                
                // 显示成功消息
                JOptionPane.showMessageDialog(this, 
                        "财务月设置已保存。系统将使用每月" + monthStartDay + "日作为财务月起始日。", 
                        "保存成功", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                        "保存设置失败，请稍后再试。", 
                        "保存失败", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                    "保存设置时出错: " + e.getMessage(), 
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 刷新类别列表
     */
    public void refreshCategoryList() {
        refreshCategoryDisplay();
    }

    public void loadSavingGoals() {
        if (savingGoalsTableModel == null || mainFrame == null || mainFrame.getSettings() == null) {
            if (savingGoalsTableModel != null) savingGoalsTableModel.setRowCount(0); // Clear if possible
            return;
        }
        savingGoalsTableModel.setRowCount(0); // Clear existing rows

        List<SavingGoal> goals = mainFrame.getSettings().getSavingGoals();
        if (goals != null) {
            for (SavingGoal goal : goals) {
                Object[] rowData = {
                    goal.getName(),
                    String.format("%.2f", goal.getTargetAmount()),
                    String.format("%.2f", goal.getCurrentAmount()),
                    String.format("%.2f", goal.getMonthlyContribution()),
                    goal.getStartDate() != null ? goal.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "N/A",
                    goal.getTargetDate() != null ? goal.getTargetDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "N/A",
                    goal.isActive()
                };
                savingGoalsTableModel.addRow(rowData);
            }
        }
    }

    private void addSavingGoal() {
        String name = sgNameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Goal name cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
            sgNameField.requestFocus();
            return;
        }

        String description = sgDescriptionField.getText().trim();
        double targetAmount = ((Number) sgTargetAmountSpinner.getValue()).doubleValue();
        double monthlyContribution = ((Number) sgMonthlyContributionSpinner.getValue()).doubleValue();
        
        Date startDateSelected = (Date) sgStartDateSpinner.getValue();
        LocalDate startDate = startDateSelected.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        LocalDate targetDate = null;
        try {
            // Target date spinner might not have a null option by default in JSpinner<Date>
            // We need to handle this gracefully if user means "no target date"
            // For now, we take what is in the spinner. If it needs to be optional,
            // a JCheckBox to enable/disable or a clear button for the date would be better.
            Date targetDateSelected = (Date) sgTargetDateSpinner.getValue();
            if (targetDateSelected != null) { // Check if a date is actually set
                targetDate = targetDateSelected.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }
        } catch (Exception e) {
            // Could be null or invalid date if spinner was modified to allow it
            // Log this if necessary, for now, null targetDate is acceptable
            System.err.println("Could not parse target date: " + e.getMessage());
        }
        
        boolean isActive = sgIsActiveCheckBox.isSelected();

        if (targetAmount <= 0) {
            JOptionPane.showMessageDialog(this, "Target amount must be greater than zero.", "Input Error", JOptionPane.ERROR_MESSAGE);
            sgTargetAmountSpinner.requestFocus();
            return;
        }
        if (monthlyContribution < 0) { // Allow 0 for manual saving goals not auto-contributed
            JOptionPane.showMessageDialog(this, "Monthly contribution cannot be negative.", "Input Error", JOptionPane.ERROR_MESSAGE);
            sgMonthlyContributionSpinner.requestFocus();
            return;
        }
        if (targetDate != null && targetDate.isBefore(startDate)) {
            JOptionPane.showMessageDialog(this, "Target date cannot be before start date.", "Input Error", JOptionPane.ERROR_MESSAGE);
            sgTargetDateSpinner.requestFocus();
            return;
        }

        SavingGoal newGoal = new SavingGoal();
        newGoal.setId(java.util.UUID.randomUUID().toString()); // Ensure a new ID
        newGoal.setName(name);
        newGoal.setDescription(description);
        newGoal.setTargetAmount(targetAmount);
        newGoal.setCurrentAmount(0); // New goals start with 0 current amount
        newGoal.setMonthlyContribution(monthlyContribution);
        newGoal.setStartDate(startDate);
        newGoal.setTargetDate(targetDate);
        newGoal.setActive(isActive);

        Settings settings = mainFrame.getSettings();
        settings.addSavingGoal(newGoal);
        
        if (settingsService.saveSettings()) {
            JOptionPane.showMessageDialog(this, "Saving goal added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadSavingGoals(); // Refresh the table
            clearSavingGoalForm(); // Clear the form
            mainFrame.refreshCategoryLists(); // Also refresh other panels if needed
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save the new saving goal.", "Error", JOptionPane.ERROR_MESSAGE);
            // Rollback adding to settings if save fails? Or rely on user to try save again later.
            // For simplicity, currently it remains in the settings object in memory.
        }
    }

    private void clearSavingGoalForm() {
        sgNameField.setText("");
        sgDescriptionField.setText("");
        sgTargetAmountSpinner.setValue(1000.0);
        sgMonthlyContributionSpinner.setValue(50.0);
        sgStartDateSpinner.setValue(new Date());
        sgTargetDateSpinner.setValue(Date.from(LocalDate.now().plusYears(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        sgIsActiveCheckBox.setSelected(true);
        
        currentEditingSavingGoal = null;
        if (addGoalButton != null) addGoalButton.setEnabled(true);
        if (saveGoalButton != null) saveGoalButton.setEnabled(false);
        sgNameField.setEditable(true); // Allow editing name for new entries
    }

    private void editSelectedSavingGoal() {
        int selectedRow = savingGoalsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a saving goal to edit.", "Selection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String goalName = (String) savingGoalsTableModel.getValueAt(selectedRow, 0);
        Settings settings = mainFrame.getSettings();
        SavingGoal goalToEdit = null;

        for (SavingGoal goal : settings.getSavingGoals()) {
            if (goal.getName().equals(goalName)) {
                goalToEdit = goal;
                break;
            }
        }

        if (goalToEdit != null) {
            currentEditingSavingGoal = goalToEdit;

            sgNameField.setText(goalToEdit.getName());
            sgDescriptionField.setText(goalToEdit.getDescription());
            sgTargetAmountSpinner.setValue(goalToEdit.getTargetAmount());
            sgMonthlyContributionSpinner.setValue(goalToEdit.getMonthlyContribution());
            sgStartDateSpinner.setValue(Date.from(goalToEdit.getStartDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            if (goalToEdit.getTargetDate() != null) {
                sgTargetDateSpinner.setValue(Date.from(goalToEdit.getTargetDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            } else {
                // Set to a default or clear if spinner supports null (JSpinner<Date> typically doesn't directly support null text)
                // For now, setting a default far future date or current date as placeholder if null
                sgTargetDateSpinner.setValue(new Date()); 
            }
            sgIsActiveCheckBox.setSelected(goalToEdit.isActive());
            
            sgNameField.setEditable(false); // Optionally prevent editing name/ID field of existing goal
            if (addGoalButton != null) addGoalButton.setEnabled(false);
            if (saveGoalButton != null) saveGoalButton.setEnabled(true);

        } else {
            JOptionPane.showMessageDialog(this, "Could not find the selected saving goal for editing.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveEditedSavingGoal() {
        if (currentEditingSavingGoal == null) {
            JOptionPane.showMessageDialog(this, "No goal selected for editing.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate and retrieve form data (similar to addSavingGoal, but update currentEditingSavingGoal)
        String description = sgDescriptionField.getText().trim();
        double targetAmount = ((Number) sgTargetAmountSpinner.getValue()).doubleValue();
        double monthlyContribution = ((Number) sgMonthlyContributionSpinner.getValue()).doubleValue();
        Date startDateSelected = (Date) sgStartDateSpinner.getValue();
        LocalDate startDate = startDateSelected.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate targetDate = null;
        try {
            Date targetDateSelected = (Date) sgTargetDateSpinner.getValue();
            if (targetDateSelected != null) {
                targetDate = targetDateSelected.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }
        } catch (Exception e) {System.err.println("Could not parse target date for edit: " + e.getMessage());}
        boolean isActive = sgIsActiveCheckBox.isSelected();

        if (targetAmount <= 0) { /* ... validation ... */ return; }
        if (monthlyContribution < 0) { /* ... validation ... */ return; }
        if (targetDate != null && targetDate.isBefore(startDate)) { /* ... validation ... */ return; }

        // Update the currentEditingSavingGoal object
        // Name (ID) is not changed here, as it was used for lookup and set to non-editable.
        // If name change is allowed, then removeSavingGoal(oldId) and addSavingGoal(newGoalWithNewName) might be needed.
        currentEditingSavingGoal.setDescription(description);
        currentEditingSavingGoal.setTargetAmount(targetAmount);
        currentEditingSavingGoal.setMonthlyContribution(monthlyContribution);
        currentEditingSavingGoal.setStartDate(startDate);
        currentEditingSavingGoal.setTargetDate(targetDate);
        currentEditingSavingGoal.setActive(isActive);
        // CurrentAmount is not edited here, it's managed by transactions.

        if (settingsService.saveSettings()) {
            JOptionPane.showMessageDialog(this, "Saving goal updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadSavingGoals();
            clearSavingGoalForm(); // This will also reset button states and currentEditingSavingGoal
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save updated saving goal.", "Error", JOptionPane.ERROR_MESSAGE);
            // Potentially rollback changes to currentEditingSavingGoal in memory if save fails,
            // or rely on user to try saving again / reload settings.
        }
    }

    private void deleteSelectedSavingGoal() {
        int selectedRow = savingGoalsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a saving goal to delete.", "Selection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Assuming the name is in the first column (index 0) and is unique enough to identify the goal.
        // A more robust way would be to store goal IDs in a hidden column or map row to ID.
        String goalName = (String) savingGoalsTableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the saving goal: '" + goalName + "'?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        Settings settings = mainFrame.getSettings();
        SavingGoal goalToDelete = null;
        String goalIdToDelete = null;

        // Find the goal by name to get its ID for reliable deletion
        for (SavingGoal goal : settings.getSavingGoals()) {
            if (goal.getName().equals(goalName)) {
                // Found the goal, get its ID. We assume names in the table match one goal.
                // If multiple goals can have the same name, this logic needs to be more robust (e.g., use full row data to match or ensure unique names when adding)
                goalIdToDelete = goal.getId();
                break;
            }
        }

        if (goalIdToDelete != null) {
            boolean removed = settings.removeSavingGoal(goalIdToDelete);
            if (removed) {
                if (settingsService.saveSettings()) {
                    JOptionPane.showMessageDialog(this, "Saving goal '" + goalName + "' deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadSavingGoals(); // Refresh the table
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to save settings after deleting the goal.", "Error", JOptionPane.ERROR_MESSAGE);
                    // If save fails, the goal is removed from in-memory settings but not persisted.
                    // The user would need to try saving settings again later or the change might be lost on app close if not handled elsewhere.
                }
            } else {
                // This case should ideally not happen if we found it by name and then tried to remove by ID obtained from that found goal.
                JOptionPane.showMessageDialog(this, "Could not delete the saving goal (it might have been removed by another process or an ID mismatch occurred).", "Deletion Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Could not find the saving goal '" + goalName + "' in the current settings. It might have already been deleted.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createCategoryManagementPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Category Management"));

        // --- Expense Categories ---
        JPanel expensePanel = new JPanel(new BorderLayout(5, 5));
        expensePanel.setBorder(BorderFactory.createTitledBorder("Expense Categories"));

        expenseCategoriesDisplayPanel = new JPanel();
        expenseCategoriesDisplayPanel.setLayout(new BoxLayout(expenseCategoriesDisplayPanel, BoxLayout.Y_AXIS));
        JScrollPane expenseScrollPane = new JScrollPane(expenseCategoriesDisplayPanel);
        expenseScrollPane.setPreferredSize(new Dimension(300, 100)); // Set preferred size for scroll pane

        JPanel addExpensePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        newExpenseCategoryField = new JTextField(15);
        JButton addExpenseButton = new JButton("Add Expense Category");
        addExpenseButton.addActionListener(e -> addCategory(CategoryType.EXPENSE));
        addExpensePanel.add(new JLabel("New Expense Category:"));
        addExpensePanel.add(newExpenseCategoryField);
        addExpensePanel.add(addExpenseButton);

        expensePanel.add(expenseScrollPane, BorderLayout.CENTER);
        expensePanel.add(addExpensePanel, BorderLayout.SOUTH);
        panel.add(expensePanel);

        // --- Income Categories ---
        JPanel incomePanel = new JPanel(new BorderLayout(5, 5));
        incomePanel.setBorder(BorderFactory.createTitledBorder("Income Categories"));

        incomeCategoriesDisplayPanel = new JPanel();
        incomeCategoriesDisplayPanel.setLayout(new BoxLayout(incomeCategoriesDisplayPanel, BoxLayout.Y_AXIS));
        JScrollPane incomeScrollPane = new JScrollPane(incomeCategoriesDisplayPanel);
        incomeScrollPane.setPreferredSize(new Dimension(300, 100)); // Set preferred size for scroll pane

        JPanel addIncomePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        newIncomeCategoryField = new JTextField(15);
        JButton addIncomeButton = new JButton("Add Income Category");
        addIncomeButton.addActionListener(e -> addCategory(CategoryType.INCOME));
        addIncomePanel.add(new JLabel("New Income Category:"));
        addIncomePanel.add(newIncomeCategoryField);
        addIncomePanel.add(addIncomeButton);

        incomePanel.add(incomeScrollPane, BorderLayout.CENTER);
        incomePanel.add(addIncomePanel, BorderLayout.SOUTH);
        panel.add(Box.createVerticalStrut(10)); // Spacing
        panel.add(incomePanel);

        refreshCategoryDisplay(); // Initial population
        return panel;
    }

    private JPanel createMonthStartDayPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Financial Month Start Day"));

        panel.add(new JLabel("Select the day your financial month starts:"));
        // Allow days from 1 to 28.
        monthStartDaySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 28, 1));
        panel.add(monthStartDaySpinner);

        // Load current setting
        if (mainFrame != null && mainFrame.getSettings() != null) {
            monthStartDaySpinner.setValue(mainFrame.getSettings().getMonthStartDay());
        }
        
        // Note: The save for this is handled by the main "Save Changes" button,
        // which calls saveChanges(), which in turn reads from monthStartDaySpinner.
        // If a dedicated save button for this setting is needed, it can be added here.

        return panel;
    }
}
