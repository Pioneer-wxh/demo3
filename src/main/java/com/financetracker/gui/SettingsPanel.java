package com.financetracker.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
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
    
    // CardLayout and panel for switching views - REMOVED
    // private CardLayout settingsCardLayout;
    // private JPanel settingsContentPanel;

    private JTabbedPane tabbedPane; // New JTabbedPane

    // Panels for each settings section (many are already fields)
    private JPanel specialDatesPanel;
    private JPanel savingsGoalsPanel;
    private JPanel categoryManagementPanel;
    private JPanel monthStartDayPanel;
    private JPanel monthEndClosingPanel;
    private JPanel generalSettingsPanelHolder; // New panel to hold combined general settings

    private JTable specialDatesTable;
    private DefaultTableModel specialDatesTableModel;
    private JTextField specialDateNameField;
    private JSpinner specialDateSpinner;
    private JTextField specialDateDescriptionField;
    private JComboBox<String> specialDateCategoryComboBox;
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
        setLayout(new BorderLayout(10, 10)); // Main layout for SettingsPanel

        // --- Header Panel (Title and HOME button) ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel titleLabel = new JLabel("Settings");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        JButton homeButton = new JButton("HOME");
        homeButton.addActionListener(e -> mainFrame.showPanel("home"));
        headerPanel.add(homeButton, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // --- JTabbedPane for Settings Content ---
        tabbedPane = new JTabbedPane();
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Create individual settings panels
        specialDatesPanel = createSpecialDatesPanel();
        savingsGoalsPanel = createSavingsGoalsPanel();
        
        // Create the holder panel for general settings
        generalSettingsPanelHolder = new JPanel();
        generalSettingsPanelHolder.setLayout(new BoxLayout(generalSettingsPanelHolder, BoxLayout.Y_AXIS));
        generalSettingsPanelHolder.setBorder(BorderFactory.createEmptyBorder(10,10,10,10)); // Add some padding inside the scrollpane for this tab
        
        categoryManagementPanel = createCategoryManagementPanel();
        monthStartDayPanel = createMonthStartDayPanel();
        monthEndClosingPanel = createMonthEndClosingPanel();
        
        generalSettingsPanelHolder.add(categoryManagementPanel);
        generalSettingsPanelHolder.add(Box.createVerticalStrut(15));
        generalSettingsPanelHolder.add(monthStartDayPanel);
        generalSettingsPanelHolder.add(Box.createVerticalStrut(15));
        generalSettingsPanelHolder.add(monthEndClosingPanel);
        generalSettingsPanelHolder.add(Box.createVerticalGlue()); // Pushes content to top

        // Add panels as tabs to JTabbedPane, wrapped in JScrollPanes
        tabbedPane.addTab("Special Dates", new JScrollPane(specialDatesPanel));
        tabbedPane.addTab("Saving Goals", new JScrollPane(savingsGoalsPanel));
        tabbedPane.addTab("General Settings", new JScrollPane(generalSettingsPanelHolder));
        
        // --- Container for Navigation and Content - REMOVED / REPLACED by JTabbedPane ---
        // JPanel centerAreaPanel = new JPanel(new BorderLayout(0, 5));
        // centerAreaPanel.add(settingsNavigationPanel, BorderLayout.NORTH);
        // centerAreaPanel.add(settingsContentPanel, BorderLayout.CENTER);

        add(tabbedPane, BorderLayout.CENTER); // Add JTabbedPane to the center
        
        // --- Bottom Button Panel (Reset, Save) ---
        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomButtonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JButton resetButton = new JButton("Reset to Default");
        resetButton.addActionListener(e -> resetToDefault());
        JButton saveButton = new JButton("Save Changes");
        saveButton.addActionListener(e -> saveChanges());
        bottomButtonPanel.add(resetButton);
        bottomButtonPanel.add(saveButton);
        add(bottomButtonPanel, BorderLayout.SOUTH);

        // Show the first settings tab by default - JTabbedPane does this automatically
        // settingsCardLayout.show(settingsContentPanel, "SpecialDates"); 
    }
    
    /**
     * Creates the special dates panel.
     * 
     * @return The special dates panel
     */
    private JPanel createSpecialDatesPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10,10));
        
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
        // scrollPane.setPreferredSize(new Dimension(400, 150)); // Example of removing fixed size
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
        
        // Add affected categories field (now a JComboBox)
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Affected Category (Expense):"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        specialDateCategoryComboBox = new JComboBox<>();
        updateSpecialDateCategoryComboBox(); // Populate the combo box
        formPanel.add(specialDateCategoryComboBox, gbc);
        
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
        
        // --- Table for displaying saving goals ---
        String[] goalColumns = {"Name", "Target Amount", "Current Amount", "Monthly Contribution", "Start Date", "Target Date", "Active"};
        savingGoalsTableModel = new DefaultTableModel(goalColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Table is for display only
            }
        };
        savingGoalsTable = new JTable(savingGoalsTableModel);
        savingGoalsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        savingGoalsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = savingGoalsTable.rowAtPoint(evt.getPoint());
                    if (row >= 0) {
                        showSavingGoalDetails(row);
                    }
                }
            }
        });
        JScrollPane tableScrollPane = new JScrollPane(savingGoalsTable);
        // tableScrollPane.setPreferredSize(new Dimension(400,150)); // Example of removing fixed size
        panel.add(tableScrollPane, BorderLayout.CENTER);

        // --- Bottom Panel: Holds table actions and the form ---
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));

        // --- Table Actions Panel (Edit, Delete) ---
        JPanel tableActionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton editSelectedGoalButton = new JButton("Edit Selected");
        editSelectedGoalButton.addActionListener(e -> editSelectedSavingGoal());
        JButton deleteSelectedGoalButton = new JButton("Delete Selected");
        deleteSelectedGoalButton.addActionListener(e -> deleteSelectedSavingGoal());
        tableActionsPanel.add(editSelectedGoalButton);
        tableActionsPanel.add(deleteSelectedGoalButton);
        bottomPanel.add(tableActionsPanel, BorderLayout.NORTH);

        // --- Form for adding/editing saving goals ---
        JPanel formOuterPanel = new JPanel(new BorderLayout(5,5));
        formOuterPanel.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
        
        JPanel formFieldsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints formFieldsGbc = new GridBagConstraints();
        formFieldsGbc.fill = GridBagConstraints.HORIZONTAL;
        formFieldsGbc.insets = new Insets(5, 5, 5, 5);
        formFieldsGbc.weightx = 1.0; // Allow fields to use horizontal space
        formFieldsGbc.gridx = 0; // Label column
        formFieldsGbc.anchor = GridBagConstraints.WEST;
        
        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = new Insets(5, 5, 5, 5);
        fieldConstraints.weightx = 1.0;
        fieldConstraints.gridx = 1; // Field column

        int y = 0;

        // Name
        formFieldsGbc.gridy = y; formFieldsPanel.add(new JLabel("Goal Name:"), formFieldsGbc);
        fieldConstraints.gridy = y++; sgNameField = new JTextField(); formFieldsPanel.add(sgNameField, fieldConstraints);

        // Description
        formFieldsGbc.gridy = y; formFieldsPanel.add(new JLabel("Description (Optional):"), formFieldsGbc);
        fieldConstraints.gridy = y++; sgDescriptionField = new JTextField(); formFieldsPanel.add(sgDescriptionField, fieldConstraints);

        // Target Amount
        formFieldsGbc.gridy = y; formFieldsPanel.add(new JLabel("Target Amount:"), formFieldsGbc);
        sgTargetAmountSpinner = new JSpinner(new SpinnerNumberModel(1000.0, 0.0, Double.MAX_VALUE, 100.0)); // Restore default
        fieldConstraints.gridy = y++; formFieldsPanel.add(sgTargetAmountSpinner, fieldConstraints);

        // Monthly Contribution
        formFieldsGbc.gridy = y; formFieldsPanel.add(new JLabel("Monthly Contribution:"), formFieldsGbc);
        sgMonthlyContributionSpinner = new JSpinner(new SpinnerNumberModel(50.0, 0.0, Double.MAX_VALUE, 10.0)); // Restore default
        fieldConstraints.gridy = y++; formFieldsPanel.add(sgMonthlyContributionSpinner, fieldConstraints);

        // Start Date
        formFieldsGbc.gridy = y; formFieldsPanel.add(new JLabel("Start Date:"), formFieldsGbc);
        sgStartDateSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        sgStartDateSpinner.setEditor(new JSpinner.DateEditor(sgStartDateSpinner, "yyyy-MM-dd"));
        fieldConstraints.gridy = y++; formFieldsPanel.add(sgStartDateSpinner, fieldConstraints);

        // Target Date (Optional)
        formFieldsGbc.gridy = y; formFieldsPanel.add(new JLabel("Target Date (Optional):"), formFieldsGbc);
        sgTargetDateSpinner = new JSpinner(new SpinnerDateModel(Date.from(LocalDate.now().plusYears(1).atStartOfDay(ZoneId.systemDefault()).toInstant()), null, null, Calendar.DAY_OF_MONTH)); // Restore default
        sgTargetDateSpinner.setEditor(new JSpinner.DateEditor(sgTargetDateSpinner, "yyyy-MM-dd"));
        fieldConstraints.gridy = y++; formFieldsPanel.add(sgTargetDateSpinner, fieldConstraints);

        // Is Active
        formFieldsGbc.gridy = y; formFieldsPanel.add(new JLabel("Active Goal:"), formFieldsGbc);
        sgIsActiveCheckBox = new JCheckBox();
        sgIsActiveCheckBox.setSelected(true); // Default to active
        fieldConstraints.gridy = y++; formFieldsPanel.add(sgIsActiveCheckBox, fieldConstraints);
        
        // Wrap formFieldsPanel in a JScrollPane for flexibility
        JScrollPane formFieldsScrollPane = new JScrollPane(formFieldsPanel);
        formFieldsScrollPane.setBorder(BorderFactory.createEmptyBorder()); 
        // formFieldsScrollPane.setPreferredSize(new Dimension(400, 200)); // Example of removing fixed size
        formOuterPanel.add(formFieldsScrollPane, BorderLayout.CENTER);

        // Buttons for form actions (Add, Save, Clear)
        JPanel formButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        this.addGoalButton = new JButton("Add New Goal");
        this.addGoalButton.addActionListener(e -> addSavingGoal()); // Ensure ActionListener is set
        this.saveGoalButton = new JButton("Save Edited Goal");
        this.saveGoalButton.addActionListener(e -> saveEditedSavingGoal()); // Ensure ActionListener is set
        this.saveGoalButton.setEnabled(false);
        JButton clearFormButton = new JButton("Clear Form / Cancel Edit");
        clearFormButton.addActionListener(e -> clearSavingGoalForm());

        formButtonPanel.add(addGoalButton);
        formButtonPanel.add(saveGoalButton);
        formButtonPanel.add(clearFormButton);
        formOuterPanel.add(formButtonPanel, BorderLayout.SOUTH);
        
        bottomPanel.add(formOuterPanel, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

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
            double baseBudget = (settings != null) ? settings.getMonthlyBudget() : 0.0; // Handle null settings
            double amountIncreaseValue = specialDate.getAmountIncrease();
            double adjustedBudget = baseBudget + amountIncreaseValue; // This example might be too simplistic if category specific
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
            String affectedCategory = (String) specialDateCategoryComboBox.getSelectedItem();
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
            if (affectedCategory == null || affectedCategory.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select an affected category for the special date.", "Error", JOptionPane.ERROR_MESSAGE);
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
        String affectedCategoryFromTable = (String) specialDatesTableModel.getValueAt(selectedRow, 3);
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
        specialDateCategoryComboBox.setSelectedItem(affectedCategoryFromTable);
        specialDateImpactField.setText(String.format("%.2f", amountIncrease));
        
        // For simplicity, editing means deleting the old one and adding a new one with current form data
        // This is not ideal for preserving ID or complex recurrence settings not exposed in this simple form.
        // A more robust edit would fetch the full SpecialDate object and allow modifying its properties.
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
        if (specialDateCategoryComboBox.getItemCount() > 0) {
            specialDateCategoryComboBox.setSelectedIndex(0);
        }
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
        updateSpecialDateCategoryComboBox(); // Update the combo box when categories are refreshed
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
        if (savingGoalsTableModel == null) return; // Guard against null if called too early
        savingGoalsTableModel.setRowCount(0);
        Settings settings = settingsService.getSettings();
        if (settings != null && settings.getSavingGoals() != null) {
            for (SavingGoal goal : settings.getSavingGoals()) {
                savingGoalsTableModel.addRow(new Object[]{
                    goal.getName(),
                    goal.getTargetAmount(),
                    goal.getCurrentAmount(),
                    goal.getMonthlyContribution(),
                    goal.getStartDate() != null ? goal.getStartDate().format(DateTimeFormatter.ISO_DATE) : "",
                    goal.getTargetDate() != null ? goal.getTargetDate().format(DateTimeFormatter.ISO_DATE) : "N/A",
                    goal.isActive() ? "Yes" : "No",
                    String.format("%.2f%%", goal.getProgressPercentage())
                });
            }
        }
    }

    private SavingGoal prepareSavingGoalFromInputs(SavingGoal existingGoal) {
        String name = sgNameField.getText();
        String description = sgDescriptionField.getText();

        // Restore direct value fetching
        double targetAmount = ((Number) sgTargetAmountSpinner.getValue()).doubleValue();
        double monthlyContributionRaw = ((Number) sgMonthlyContributionSpinner.getValue()).doubleValue();
        
        Date startDateRaw = (Date) sgStartDateSpinner.getValue();
        Date targetDateRaw = (Date) sgTargetDateSpinner.getValue(); 
        boolean isActive = sgIsActiveCheckBox.isSelected();

        // --- Validations ---
        if (name.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Saving goal name cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        // Target amount value validation
        if (targetAmount <= 0) {
            JOptionPane.showMessageDialog(this, "Target amount must be greater than zero.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        if (startDateRaw == null) {
            JOptionPane.showMessageDialog(this, "Start date cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        LocalDate startDate = startDateRaw.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate targetDate = null; 
        
        if (targetDateRaw != null) {
            LocalDate tempTargetDate = targetDateRaw.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (tempTargetDate.isAfter(startDate)) {
                targetDate = tempTargetDate; 
            }
            // No explicit "else" with JOptionPane here for invalid date, let the logic flow.
        }

        double finalMonthlyContribution = 0;
        LocalDate finalTargetDate = null;
        
        // Restore the logic from the first successful fix for calculation bug
        LocalDate initialTargetDateDefault = LocalDate.now().plusYears(1);
        boolean targetDateIsEffectivelyInitialDefault = false;
        if (targetDateRaw != null) {
            LocalDate currentTargetDateInSpinner = targetDateRaw.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (currentTargetDateInSpinner.isEqual(initialTargetDateDefault)) {
                 targetDateIsEffectivelyInitialDefault = true;
            }
        }
        
        boolean monthlyContributionEffectivelyProvided = monthlyContributionRaw > 0.001; 
        boolean userHasExplicitlySetTargetDate = (targetDate != null && !targetDateIsEffectivelyInitialDefault);

        if (userHasExplicitlySetTargetDate) {
            // Case A: User explicitly set the target date. Calculate Monthly Contribution.
            finalTargetDate = targetDate;
            long numberOfMonths = ChronoUnit.MONTHS.between(startDate, finalTargetDate);

            if (numberOfMonths == 0 && startDate.isBefore(finalTargetDate)) { // Less than a full month but valid period
                numberOfMonths = 1;
            }
            if (numberOfMonths <= 0) {
                JOptionPane.showMessageDialog(this, "The period between start and target date must result in at least one contribution month.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            if (targetAmount <= 0) {
                 JOptionPane.showMessageDialog(this, "Target amount must be greater than zero to calculate monthly contribution.", "Input Error", JOptionPane.ERROR_MESSAGE);
                 return null;
            }
            finalMonthlyContribution = targetAmount / numberOfMonths;

            // Update UI: Set the calculated monthly contribution
            sgMonthlyContributionSpinner.setValue(finalMonthlyContribution);
            // Ensure target date spinner reflects the date used for calculation (it should already, but good for consistency)
            if (finalTargetDate != null) { // finalTargetDate should be non-null here
                sgTargetDateSpinner.setValue(Date.from(finalTargetDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }

            // If monthly contribution was also effectively provided by user (e.g., they typed it before changing target date),
            // check for discrepancy with the newly calculated one.
            if (monthlyContributionEffectivelyProvided) {
                double tolerance = 0.01; // Tolerance for floating point comparison (e.g., 1 cent)
                if (Math.abs(finalMonthlyContribution - monthlyContributionRaw) > tolerance) { 
                     // Ask user if they want to use the new calculated monthly contribution or stick to what they might have typed previously.
                     // For simplicity in this version, we will prioritize the calculation based on the user-set Target Date.
                     // The sgMonthlyContributionSpinner is already updated with finalMonthlyContribution.
                     // A more complex dialog could offer choices, but here we assume user changing TargetDate implies they want contribution to adjust.
                }
            }

        } else if (monthlyContributionEffectivelyProvided && !userHasExplicitlySetTargetDate) {
            // Case B: Monthly contribution is provided, and target date was NOT explicitly set by user (it's default or invalid).
            // Calculate Target Date (this is the original bug fix you requested).
            finalMonthlyContribution = monthlyContributionRaw;
            if (finalMonthlyContribution <= 0) { 
                 JOptionPane.showMessageDialog(this, "Monthly contribution must be a positive value to calculate target date.", "Input Error", JOptionPane.ERROR_MESSAGE);
                 return null;
            }
            if (targetAmount <= 0) {
                 JOptionPane.showMessageDialog(this, "Target amount must be greater than zero to calculate target date.", "Input Error", JOptionPane.ERROR_MESSAGE);
                 return null;
            }
            long numberOfMonths = (long) Math.ceil(targetAmount / finalMonthlyContribution);
            if (numberOfMonths <= 0) numberOfMonths = 1; 
            finalTargetDate = startDate.plusMonths(numberOfMonths);
            
            sgTargetDateSpinner.setValue(Date.from(finalTargetDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            sgMonthlyContributionSpinner.setValue(finalMonthlyContribution); // Ensure it reflects the value used

        } else if (monthlyContributionEffectivelyProvided && userHasExplicitlySetTargetDate) {
            // Case C: Both were effectively provided (e.g., user set target date, calculation set monthly, or user explicitly set both).
            // This now acts as a final check, primarily for when user might have set both fields manually and there's a conflict.
            // Or, if the logic from Case A (after user sets target date) leads here because monthly contribution was also present.
            // We will use the values that are currently in `finalMonthlyContribution` and `finalTargetDate` if they have been set by prior blocks,
            // otherwise, fall back to raw inputs if this is the first block hit.

            // If this block is reached directly (e.g., user typed both), initialize from raw inputs.
            if (finalTargetDate == null) finalTargetDate = targetDate; // From user input or default
            if (finalMonthlyContribution == 0 && monthlyContributionRaw > 0.001) finalMonthlyContribution = monthlyContributionRaw; // from user input
            
            // If still one is missing after trying to populate from prior blocks or raw, it's an issue.
            if (finalTargetDate == null || finalMonthlyContribution <= 0.001) {
                 JOptionPane.showMessageDialog(this, "Both target date and monthly contribution need to be effectively set to check for discrepancies or save.", "Input Error", JOptionPane.ERROR_MESSAGE);
                 return null;
            }

            long monthsBetween = ChronoUnit.MONTHS.between(startDate, finalTargetDate);
            if (monthsBetween <= 0) {
                JOptionPane.showMessageDialog(this, "Target date must be after start date for a valid calculation.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            if (targetAmount <=0) { 
                JOptionPane.showMessageDialog(this, "Target amount must be greater than zero.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            double expectedContribution = targetAmount / monthsBetween;
            double tolerance = 0.01; 

            if (Math.abs(expectedContribution - finalMonthlyContribution) > tolerance) {
                 int response = JOptionPane.showConfirmDialog(this,
                         String.format("Warning: Based on your inputs, the values may be inconsistent.\nTarget Amount: %.2f\nStart Date: %s\nTarget Date: %s (implies approx. %.2f monthly)\nMonthly Contribution: %.2f (implies reaching target in approx. %d months)\n\nDo you want to proceed with your entered/currently displayed values (Monthly: %.2f, Target Date: %s)?",
                                 targetAmount, startDate.format(DateTimeFormatter.ISO_LOCAL_DATE), 
                                 finalTargetDate.format(DateTimeFormatter.ISO_LOCAL_DATE), 
                                 expectedContribution, 
                                 finalMonthlyContribution, 
                                 (long)Math.ceil(targetAmount/finalMonthlyContribution),
                                 finalMonthlyContribution, 
                                 finalTargetDate.format(DateTimeFormatter.ISO_LOCAL_DATE)),
                         "Potential Discrepancy", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                 if (response == JOptionPane.NO_OPTION) {
                     return null; 
                 }
             }
            // If proceeding, ensure UI reflects values used for the final SavingGoal object
            sgMonthlyContributionSpinner.setValue(finalMonthlyContribution);
            sgTargetDateSpinner.setValue(Date.from(finalTargetDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        } else { 
            // Case D: Insufficient information to proceed with any calculation.
            // This means neither target date was explicitly set nor was a monthly contribution provided.
            JOptionPane.showMessageDialog(this, "Please provide either a positive Monthly Contribution or an explicit Target Date (after start date).", "Input Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        String id = (existingGoal != null) ? existingGoal.getId() : null;
        double currentAmount = (existingGoal != null) ? existingGoal.getCurrentAmount() : 0.0;
        String associatedAccount = (existingGoal != null) ? existingGoal.getAssociatedAccount() : null;

        if (finalMonthlyContribution < 0) finalMonthlyContribution = 0; // Should not happen if logic above is correct

        return new SavingGoal(id, name, description, targetAmount, currentAmount, finalMonthlyContribution, startDate, finalTargetDate, isActive, associatedAccount);
    }

    private void addSavingGoal() {
        SavingGoal newGoal = prepareSavingGoalFromInputs(null);
        if (newGoal != null) {
            Settings settings = settingsService.getSettings();
            settings.addSavingGoal(newGoal);
            if (settingsService.saveSettings()) {
                loadSavingGoals(); // Refresh table
                clearSavingGoalForm();
                JOptionPane.showMessageDialog(this, "Saving goal added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save new saving goal.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearSavingGoalForm() {
        sgNameField.setText("");
        sgDescriptionField.setText("");
        sgTargetAmountSpinner.setValue(1000.0); // Restore default
        sgMonthlyContributionSpinner.setValue(50.0); // Restore default
        sgStartDateSpinner.setValue(new Date()); 
        sgTargetDateSpinner.setValue(Date.from(LocalDate.now().plusYears(1).atStartOfDay(ZoneId.systemDefault()).toInstant())); // Restore default
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
                sgTargetDateSpinner.setValue(Date.from(LocalDate.now().plusYears(1).atStartOfDay(ZoneId.systemDefault()).toInstant())); // Restore default if null
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
            JOptionPane.showMessageDialog(this, "No saving goal selected for editing.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        SavingGoal updatedGoal = prepareSavingGoalFromInputs(currentEditingSavingGoal);
        if (updatedGoal != null) {
            Settings settings = settingsService.getSettings();
            settings.updateSavingGoal(updatedGoal); // Assumes updateSavingGoal handles finding by ID
            if (settingsService.saveSettings()) {
                loadSavingGoals();
                clearSavingGoalForm();
                currentEditingSavingGoal = null; // Reset editing state
                addGoalButton.setText("Add New Goal");
                addGoalButton.setEnabled(true);
                saveGoalButton.setEnabled(false);
                JOptionPane.showMessageDialog(this, "Saving goal updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update saving goal.", "Error", JOptionPane.ERROR_MESSAGE);
            }
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
        
        // --- Expense Categories ---
        JPanel expensePanel = new JPanel(new BorderLayout(5, 5));
        expensePanel.setBorder(BorderFactory.createTitledBorder("Expense Categories"));

        expenseCategoriesDisplayPanel = new JPanel();
        expenseCategoriesDisplayPanel.setLayout(new BoxLayout(expenseCategoriesDisplayPanel, BoxLayout.Y_AXIS));
        JScrollPane expenseScrollPane = new JScrollPane(expenseCategoriesDisplayPanel);
        // expenseScrollPane.setPreferredSize(new Dimension(300, 80)); // Example of removing/adjusting fixed size

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
        // incomeScrollPane.setPreferredSize(new Dimension(300, 80)); // Example of removing/adjusting fixed size

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

    private JPanel createMonthEndClosingPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton performClosingButton = new JButton("Perform Month-End Closing");
        performClosingButton.setToolTipText("Calculates surplus of previous financial month(s) and adds to Overall Account Balance.");
        performClosingButton.addActionListener(e -> performMonthEndClosingAction());

        panel.add(performClosingButton);
        return panel;
    }

    private void performMonthEndClosingAction() {
        if (mainFrame == null || mainFrame.getTransactionService() == null || mainFrame.getSettingsService() == null) {
            JOptionPane.showMessageDialog(this, "Required services are not available.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Ask for confirmation
        int confirmation = JOptionPane.showConfirmDialog(
            this,
            "This will close all unclosed financial months up to the last completed one.\n" +
            "Surplus from these months will be added to your Overall Account Balance.\n\n" +
            "Do you want to proceed?",
            "Confirm Month-End Closing",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean result = mainFrame.getTransactionService().performMonthEndClosing(mainFrame.getSettingsService());
            if (result) {
                // Refresh the overall balance display on HomePanel
                if (mainFrame.getHomePanel() != null) {
                    mainFrame.getHomePanel().updateOverallAccountBalance();
                }
                Settings currentSettings = mainFrame.getSettingsService().getSettings();
                String lastClosed = currentSettings.getLastMonthClosed();
                double newOverallBalance = currentSettings.getOverallAccountBalance();
                JOptionPane.showMessageDialog(this, 
                    "Month-end closing process completed successfully.\n" +
                    "Last closed financial month: " + (lastClosed.isEmpty() ? "N/A" : lastClosed) + "\n" +
                    String.format("New Overall Account Balance: %.2f %s", newOverallBalance, currentSettings.getDefaultCurrency()),
                    "Month-End Closing Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Month-end closing process completed, but no new months were closed.\n" +
                    "This might be because all eligible months are already closed, or there was an issue.", 
                    "Month-End Closing Info", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An error occurred during month-end closing: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // Method to update the JComboBox for Affected Category in Special Dates
    private void updateSpecialDateCategoryComboBox() {
        if (specialDateCategoryComboBox == null || settingsService == null || settingsService.getSettings() == null) {
            return;
        }
        String previouslySelected = (String) specialDateCategoryComboBox.getSelectedItem();
        specialDateCategoryComboBox.removeAllItems();
        
        List<String> expenseCategories = settingsService.getSettings().getExpenseCategories();
        if (expenseCategories != null) {
            for (String category : expenseCategories) {
                specialDateCategoryComboBox.addItem(category);
            }
        }
        // Optionally, add income categories too, or decide if special dates only affect expenses.
        // For now, only expense categories are added based on the label "Affected Category (Expense):"

        if (previouslySelected != null) {
            specialDateCategoryComboBox.setSelectedItem(previouslySelected);
        } else if (specialDateCategoryComboBox.getItemCount() > 0) {
            specialDateCategoryComboBox.setSelectedIndex(0);
        }
    }

    // Method to show details of a saving goal in a dialog
    private void showSavingGoalDetails(int rowIndex) {
        if (mainFrame == null || mainFrame.getSettings() == null || mainFrame.getSettings().getSavingGoals() == null) {
            return;
        }
        List<SavingGoal> goals = mainFrame.getSettings().getSavingGoals();
        if (rowIndex < 0 || rowIndex >= goals.size()) {
            // Attempt to find by table data if list is somehow out of sync (less ideal)
            // This part is tricky if table isn't perfectly mirroring the list order after sorts/filters (not an issue here currently)
            System.err.println("Row index out of bounds for saving goal details.");
            return;
        }

        // It's safer to get the goal by matching unique ID or name from the table if the underlying list order might change.
        // For now, assuming the table row directly corresponds to the list index from loadSavingGoals.
        // String goalNameFromTable = (String) savingGoalsTableModel.getValueAt(rowIndex, 0);
        // SavingGoal goalToShow = goals.stream().filter(g -> g.getName().equals(goalNameFromTable)).findFirst().orElse(null);
        // A more robust way if the list is not guaranteed to be in the same order as the table:
        // Find the goal by ID from the table. First, ensure an ID column exists or fetch it.
        // For simplicity, we'll rely on the current loading mechanism where table order matches list order after load.
        SavingGoal goalToShow = goals.get(rowIndex); // This assumes the table is a direct reflection of the order in `goals` list.

        if (goalToShow == null) {
            JOptionPane.showMessageDialog(this, "Could not retrieve details for the selected saving goal.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        StringBuilder details = new StringBuilder();
        details.append("<html><body>");
        details.append("<h1>Saving Goal Details</h1>");
        details.append("<p><b>Name:</b> ").append(goalToShow.getName()).append("</p>");
        details.append("<p><b>Description:</b> ").append(goalToShow.getDescription() != null ? goalToShow.getDescription() : "N/A").append("</p>");
        details.append(String.format("<p><b>Target Amount:</b> %.2f</p>", goalToShow.getTargetAmount()));
        details.append(String.format("<p><b>Current Amount:</b> %.2f (%.2f%%)</p>", goalToShow.getCurrentAmount(), goalToShow.getProgressPercentage()));
        details.append(String.format("<p><b>Monthly Contribution:</b> %.2f</p>", goalToShow.getMonthlyContribution()));
        details.append("<p><b>Start Date:</b> ").append(goalToShow.getStartDate() != null ? goalToShow.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "N/A").append("</p>");
        details.append("<p><b>Target Date:</b> ").append(goalToShow.getTargetDate() != null ? goalToShow.getTargetDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "N/A").append("</p>");
        details.append("<p><b>Active:</b> ").append(goalToShow.isActive() ? "Yes" : "No").append("</p>");
        details.append("</body></html>");

        JOptionPane.showMessageDialog(this, details.toString(), "Saving Goal: " + goalToShow.getName(), JOptionPane.INFORMATION_MESSAGE);
    }
}
