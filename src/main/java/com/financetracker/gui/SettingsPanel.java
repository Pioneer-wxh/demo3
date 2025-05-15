package com.financetracker.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import com.financetracker.model.Settings;
import com.financetracker.model.SpecialDate;
import com.financetracker.model.Transaction;
import com.financetracker.service.BudgetAdjustmentService;
import com.financetracker.service.SettingsService;
import com.financetracker.service.SpecialDateService;
import com.financetracker.service.TransactionService;

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
        String[] columns = {"Name", "Date", "Description", "Affected Categories", "Expected Impact", "Budget Effect"};
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
     * Creates the savings goals panel.
     * 
     * @return The savings goals panel
     */
    private JPanel createSavingsGoalsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("存款目标"));
        
        // Create form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create grid bag constraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Add savings amount field
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("目标存款金额:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        savingsAmountField = new JTextField(10);
        formPanel.add(savingsAmountField, gbc);
        
        // Add current savings label
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("当前月存款进度:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        // 获取本财务月的收支情况
        double currentSavings = 0.0;
        try {
            TransactionService transactionService = new TransactionService();
            List<Transaction> transactions = transactionService.getTransactionsForCurrentFinancialMonth();
            double income = transactionService.getTotalIncome(transactions);
            double expense = transactionService.getTotalExpense(transactions);
            currentSavings = income - expense;
        } catch (Exception e) {
            System.err.println("计算当前存款时出错: " + e.getMessage());
        }
        
        JLabel currentSavingsLabel = new JLabel(String.format("%.2f", currentSavings));
        currentSavingsLabel.setForeground(currentSavings >= 0 ? Color.BLUE : Color.RED);
        formPanel.add(currentSavingsLabel, gbc);
        
        // Add target date field
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("目标达成日期:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        Date futureDate = Date.from(LocalDate.now().plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        SpinnerDateModel savingsDateModel = new SpinnerDateModel(futureDate, null, null, Calendar.DAY_OF_MONTH);
        savingsDateSpinner = new JSpinner(savingsDateModel);
        JSpinner.DateEditor savingsDateEditor = new JSpinner.DateEditor(savingsDateSpinner, "yyyy-MM-dd");
        savingsDateSpinner.setEditor(savingsDateEditor);
        formPanel.add(savingsDateSpinner, gbc);
        
        // 添加说明文本
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        JLabel explanationLabel = new JLabel("<html>设置您的存款目标和计划达成日期，系统将帮助您追踪进度。</html>");
        explanationLabel.setFont(new Font("Dialog", Font.ITALIC, 12));
        explanationLabel.setForeground(Color.DARK_GRAY);
        formPanel.add(explanationLabel, gbc);
        
        // Create button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        // Add buttons to button panel
        JButton setSavingsButton = new JButton("设置存款目标");
        setSavingsButton.addActionListener(e -> setSavingsGoal());
        
        buttonPanel.add(setSavingsButton);
        
        // Add button panel to form panel
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);
        
        // Add form panel to savings goals panel
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
        
        // Add categories - 修改为包含删除按钮的形式
        Settings settings = mainFrame.getSettings();
        for (String category : settings.getDefaultCategories()) {
            // 创建类别面板，包含标签和删除按钮
            JPanel categoryPanel = new JPanel();
            categoryPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
            categoryPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.GRAY),
                    BorderFactory.createEmptyBorder(2, 5, 2, 5)
            ));
            
            // 添加类别名称标签
            JLabel categoryLabel = new JLabel(category);
            categoryPanel.add(categoryLabel);
            
            // 添加删除按钮
            JButton deleteButton = new JButton("×");
            deleteButton.setFont(new Font("Arial", Font.BOLD, 10));
            deleteButton.setMargin(new Insets(0, 3, 0, 3));
            deleteButton.setToolTipText("删除此类别");
            deleteButton.addActionListener(e -> deleteCategory(category));
            categoryPanel.add(deleteButton);
            
            // 将类别面板添加到类别容器中
            categoriesPanel.add(categoryPanel);
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
        panel.setBorder(BorderFactory.createTitledBorder("财务月设置"));
        
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
        formPanel.add(new JLabel("财务月起始日:"), gbc);
        
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
        
        // Add explanation
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        JTextArea explanationArea = new JTextArea(
                "此设置决定了每个「财务月」的开始日期。例如，若设置为15日，则每个财务月\n" +
                "将从当月15日开始，到下月14日结束。系统将根据这个设置来计算月度预算和\n" +
                "统计数据，而不是使用自然月（1日到月末）。\n\n" +
                "这对于与月薪发放日期不一致的情况特别有用，可以更准确地跟踪和规划您的预算。"
        );
        explanationArea.setEditable(false);
        explanationArea.setBackground(panel.getBackground());
        explanationArea.setFont(new Font("Dialog", Font.PLAIN, 12));
        explanationArea.setLineWrap(true);
        explanationArea.setWrapStyleWord(true);
        explanationArea.setForeground(Color.DARK_GRAY);
        formPanel.add(explanationArea, gbc);
        
        // Add save button
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        JButton saveFinancialMonthButton = new JButton("保存财务月设置");
        saveFinancialMonthButton.addActionListener(e -> saveFinancialMonthSettings());
        formPanel.add(saveFinancialMonthButton, gbc);
        
        // Add form panel to month start day panel
        panel.add(formPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Loads special dates into the table.
     */
    private void loadSpecialDates() {
        // Clear table
        specialDatesTableModel.setRowCount(0);
        
        // Get all special dates
        List<SpecialDate> specialDates = specialDateService.getAllSpecialDates();
        
        // Add rows to table
        for (SpecialDate specialDate : specialDates) {
            Settings settings = settingsService.getSettings();
            double baseBudget = settings.getMonthlyBudget();
            double impactPercentage = specialDate.getExpectedImpact();
            double adjustedBudget = baseBudget * (1 + impactPercentage / 100.0);
            String budgetEffect = String.format("%.2f → %.2f", baseBudget, adjustedBudget);
            
            Object[] row = {
                specialDate.getName(),
                specialDate.getDate().toString(),
                specialDate.getDescription(),
                specialDate.getAffectedCategoriesAsString(),
                String.format("%.1f%%", specialDate.getExpectedImpact()),
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
            Date selectedDate = (Date) specialDateSpinner.getValue();
            LocalDate date = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            
            String description = specialDateDescriptionField.getText().trim();
            String categoriesString = specialDateCategoriesField.getText().trim();
            double impact = Double.parseDouble(specialDateImpactField.getText());
            
            // Validate form data
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "请输入类别名称", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Create special date
            SpecialDate specialDate = new SpecialDate(name, date, description, categoriesString, impact);
            
            // Add special date
            boolean success = specialDateService.addSpecialDate(specialDate);
            
            if (success) {
                // Clear form
                clearSpecialDateForm();
                
                // Reload table
                loadSpecialDates();
                
                // Show success message
                JOptionPane.showMessageDialog(this, "特殊日期添加成功", "成功", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "添加特殊日期失败", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "无效的影响值，请输入数字", "错误", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "添加特殊日期时出错: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Edits the selected special date.
     */
    private void editSpecialDate() {
        int selectedRow = specialDatesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要编辑的特殊日期", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Get selected special date
        List<SpecialDate> specialDates = specialDateService.getAllSpecialDates();
        if (selectedRow < specialDates.size()) {
            SpecialDate specialDate = specialDates.get(selectedRow);
            
            // Fill form with special date data
            specialDateNameField.setText(specialDate.getName());
            
            // 设置日期选择器的值
            Date specialDateValue = Date.from(specialDate.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
            specialDateSpinner.setValue(specialDateValue);
            
            specialDateDescriptionField.setText(specialDate.getDescription());
            specialDateCategoriesField.setText(specialDate.getAffectedCategoriesAsString());
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
        specialDateSpinner.setValue(new Date());
        specialDateDescriptionField.setText("");
        specialDateCategoriesField.setText("");
        specialDateImpactField.setText("");
    }
    
    /**
     * Sets a savings goal.
     */
    private void setSavingsGoal() {
        try {
            // Parse form data
            double amount = Double.parseDouble(savingsAmountField.getText());
            
            // 从日期选择器获取日期
            Date selectedDate = (Date) savingsDateSpinner.getValue();
            LocalDate date = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            
            // Validate form data
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "请输入一个正数金额", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (date.isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(this, "请输入一个未来的日期", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // TODO: Save savings goal
            // 存储到settings中的monthlyBudget字段(复用现有字段)
            Settings settings = mainFrame.getSettings();
            settings.setMonthlyBudget(amount);
            boolean saved = settingsService.setMonthlyBudget(amount);
            
            if (!saved) {
                JOptionPane.showMessageDialog(this, "保存存款目标失败", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Clear form
            savingsAmountField.setText("");
            // 重置日期选择器为下个月的日期
            Date futureDate = Date.from(LocalDate.now().plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            savingsDateSpinner.setValue(futureDate);
            
            // Show success message
            JOptionPane.showMessageDialog(this, "存款目标设置成功", "成功", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "无效的金额，请输入数字", "错误", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "设置存款目标时出错: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Adds a new category.
     */
    private void addCategory() {
        try {
            // 获取类别名称
            String categoryName = newCategoryField.getText();
            
            // 验证类别名称
            if (categoryName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "请输入类别名称", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 添加类别到设置
            Settings settings = mainFrame.getSettings();
            List<String> categories = settings.getDefaultCategories();
            
            if (categories.contains(categoryName)) {
                JOptionPane.showMessageDialog(this, "类别已存在", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            categories.add(categoryName);
            settings.setDefaultCategories(categories);
            
            // 保存设置
            boolean saved = settingsService.setMonthlyBudget(settings.getMonthlyBudget()); // 触发设置保存
            if (!saved) {
                JOptionPane.showMessageDialog(this, "保存设置失败", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 清空表单
            newCategoryField.setText("");
            
            // 刷新类别显示
            refreshCategoryDisplay();
            
            // 刷新所有面板的类别列表
            mainFrame.refreshCategoryLists();
            
            // 显示成功消息
            JOptionPane.showMessageDialog(this, "类别添加成功", "成功", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "添加类别出错: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 删除类别
     * 
     * @param categoryToDelete 要删除的类别名称
     */
    private void deleteCategory(String categoryToDelete) {
        try {
            // 确认删除
            int confirm = JOptionPane.showConfirmDialog(this, 
                    "确定要删除类别 \"" + categoryToDelete + "\" 吗？\n" + 
                    "注意：使用此类别的交易记录将保持不变。", 
                    "确认删除", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            
            // 获取当前类别列表
            Settings settings = mainFrame.getSettings();
            List<String> categories = settings.getDefaultCategories();
            
            // 检查是否只剩下一个类别，不允许删除所有类别
            if (categories.size() <= 1) {
                JOptionPane.showMessageDialog(this, 
                        "至少需要保留一个类别，无法删除。", 
                        "无法删除", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 删除类别
            if (categories.contains(categoryToDelete)) {
                categories.remove(categoryToDelete);
                settings.setDefaultCategories(categories);
                
                // 保存设置
                boolean saved = settingsService.setMonthlyBudget(settings.getMonthlyBudget()); // 触发设置保存
                if (!saved) {
                    JOptionPane.showMessageDialog(this, "保存设置失败", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // 刷新类别显示
                refreshCategoryDisplay();
                
                // 刷新所有面板的类别列表
                mainFrame.refreshCategoryLists();
                
                // 显示成功消息
                JOptionPane.showMessageDialog(this, "类别已成功删除", "成功", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "删除类别出错: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 刷新类别显示
     */
    private void refreshCategoryDisplay() {
        // 重新创建类别管理面板
        JPanel contentPanel = (JPanel) ((JScrollPane) getComponent(1)).getViewport().getView();
        
        // 尝试找到类别管理面板
        for (Component component : contentPanel.getComponents()) {
            if (component instanceof JPanel) {
                JPanel panel = (JPanel) component;
                if (panel.getBorder() != null && 
                    panel.getBorder() instanceof TitledBorder && 
                    "Category Management".equals(((TitledBorder) panel.getBorder()).getTitle())) {
                    
                    // 找到了类别管理面板，现在找到类别显示面板
                    for (Component innerComp : panel.getComponents()) {
                        if (innerComp instanceof JPanel && innerComp.getParent() == panel) {
                            JPanel categoriesPanel = (JPanel) innerComp;
                            categoriesPanel.removeAll();
                            
                            // 添加类别 - 使用与createCategoryManagementPanel相同的方式
                            Settings settings = mainFrame.getSettings();
                            for (String category : settings.getDefaultCategories()) {
                                // 创建类别面板，包含标签和删除按钮
                                JPanel categoryPanel = new JPanel();
                                categoryPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
                                categoryPanel.setBorder(BorderFactory.createCompoundBorder(
                                        BorderFactory.createLineBorder(Color.GRAY),
                                        BorderFactory.createEmptyBorder(2, 5, 2, 5)
                                ));
                                
                                // 添加类别名称标签
                                JLabel categoryLabel = new JLabel(category);
                                categoryPanel.add(categoryLabel);
                                
                                // 添加删除按钮
                                JButton deleteButton = new JButton("×");
                                deleteButton.setFont(new Font("Arial", Font.BOLD, 10));
                                deleteButton.setMargin(new Insets(0, 3, 0, 3));
                                deleteButton.setToolTipText("删除此类别");
                                deleteButton.addActionListener(e -> deleteCategory(category));
                                categoryPanel.add(deleteButton);
                                
                                // 将类别面板添加到类别容器中
                                categoriesPanel.add(categoryPanel);
                            }
                            
                            // 重新验证和绘制面板
                            categoriesPanel.revalidate();
                            categoriesPanel.repaint();
                            return;
                        }
                    }
                }
            }
        }
        
        // 如果没有找到面板，重新加载整个设置面板
        mainFrame.showPanel("settings");
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
}
