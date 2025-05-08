package com.financetracker.gui;

import com.financetracker.model.Transaction;
import com.financetracker.service.TransactionService;
import com.financetracker.ai.AiAssistantService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Panel for AI-assisted analysis.
 */
public class AnalysisPanel extends JPanel {
    
    private MainFrame mainFrame;
    private TransactionService transactionService;
    private AiAssistantService aiAssistantService;
    
    private JPanel currentMonthPanel;
    private JPanel aiAssistantPanel;
    private JPanel budgetPanel;
    
    private JTextArea summaryTextArea;
    private JTextArea categoryBreakdownTextArea;
    private JTextArea aiResponseTextArea;
    private JTextField aiQueryField;
    private JComboBox<String> categoryComboBox;
    private JComboBox<Integer> yearComboBox;
    private JComboBox<String> monthNameComboBox;
    private JComboBox<String> modelComboBox; // 新增模型选择框
    
    /**
     * Constructor for AnalysisPanel.
     * 
     * @param mainFrame The main frame
     */
    public AnalysisPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.transactionService = new TransactionService();
        this.aiAssistantService = new AiAssistantService();
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
        JLabel titleLabel = new JLabel("AI Assisted Analysis");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Add home button to header
        JButton homeButton = new JButton("HOME");
        homeButton.addActionListener(e -> mainFrame.showPanel("home"));
        headerPanel.add(homeButton, BorderLayout.EAST);
        
        // Add header to panel
        add(headerPanel, BorderLayout.NORTH);
        
        // Create tab panel
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Create current month panel
        currentMonthPanel = createCurrentMonthPanel();
        tabbedPane.addTab("Current Month", currentMonthPanel);
        
        // Create AI assistant panel
        aiAssistantPanel = createAiAssistantPanel();
        tabbedPane.addTab("AI Assistant", aiAssistantPanel);
        
        // Create budget panel
        budgetPanel = createBudgetPanel();
        tabbedPane.addTab("Budget of Next Month", budgetPanel);
        
        // Add tab panel to main panel
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    /**
     * Creates the current month panel.
     * 
     * @return The current month panel
     */
    private JPanel createCurrentMonthPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create summary panel
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BorderLayout());
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Monthly Summary"));
        
        // Add summary text area
        summaryTextArea = new JTextArea();
        summaryTextArea.setEditable(false);
        summaryTextArea.setLineWrap(true);
        summaryTextArea.setWrapStyleWord(true);
        JScrollPane summaryScrollPane = new JScrollPane(summaryTextArea);
        summaryPanel.add(summaryScrollPane, BorderLayout.CENTER);
        
        // Add summary panel to current month panel
        panel.add(summaryPanel, BorderLayout.NORTH);
        
        // Create chart panel (replaced with text display)
        JPanel chartPanel = new JPanel();
        chartPanel.setLayout(new BorderLayout());
        chartPanel.setBorder(BorderFactory.createTitledBorder("Expense Distribution"));
        
        // Add category breakdown text area
        categoryBreakdownTextArea = new JTextArea();
        categoryBreakdownTextArea.setEditable(false);
        categoryBreakdownTextArea.setLineWrap(true);
        categoryBreakdownTextArea.setWrapStyleWord(true);
        JScrollPane categoryBreakdownScrollPane = new JScrollPane(categoryBreakdownTextArea);
        chartPanel.add(categoryBreakdownScrollPane, BorderLayout.CENTER);
        
        // Add chart panel to current month panel
        panel.add(chartPanel, BorderLayout.CENTER);
        
        // Create filter panel
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filters"));
        
        // Add category filter
        filterPanel.add(new JLabel("Category:"));
        categoryComboBox = new JComboBox<>();
        categoryComboBox.addItem("All Categories");
        for (String category : mainFrame.getSettings().getDefaultCategories()) {
            categoryComboBox.addItem(category);
        }
        categoryComboBox.addActionListener(e -> updateCurrentMonthView());
        filterPanel.add(categoryComboBox);
        
        // 添加月份筛选
        JPanel monthFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // 年份选择
        JLabel yearLabel = new JLabel("年份:");
        yearComboBox = new JComboBox<>();
        // 获取当前年份，然后添加从当前年份往前5年的选项
        int currentYear = LocalDate.now().getYear();
        for (int i = 0; i < 5; i++) {
            yearComboBox.addItem(currentYear - i);
        }
        
        // 月份选择
        JLabel monthLabel = new JLabel("月份:");
        monthNameComboBox = new JComboBox<>();
        for (int i = 1; i <= 12; i++) {
            Month month = Month.of(i);
            monthNameComboBox.addItem(month.getDisplayName(TextStyle.FULL, Locale.getDefault()));
        }
        
        // 设置当前月份为默认选中
        monthNameComboBox.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
        
        // 添加组件到筛选面板
        monthFilterPanel.add(yearLabel);
        monthFilterPanel.add(yearComboBox);
        monthFilterPanel.add(monthLabel);
        monthFilterPanel.add(monthNameComboBox);
        
        // 添加动作监听器
        ActionListener filterListener = e -> {
            int selectedYear = (int) yearComboBox.getSelectedItem();
            int selectedMonthIndex = monthNameComboBox.getSelectedIndex() + 1;
            LocalDate selectedDate = LocalDate.of(selectedYear, selectedMonthIndex, 1);
            updateCurrentMonthView(selectedDate);
        };
        
        yearComboBox.addActionListener(filterListener);
        monthNameComboBox.addActionListener(filterListener);
        
        filterPanel.add(monthFilterPanel);
        
        // Add refresh button
        JButton refreshButton = new JButton("刷新");
        refreshButton.addActionListener(e -> {
            // 使用当前选择的年份和月份创建日期
            if (yearComboBox != null && monthNameComboBox != null) {
                int selectedYear = (int) yearComboBox.getSelectedItem();
                int selectedMonthIndex = monthNameComboBox.getSelectedIndex() + 1;
                LocalDate selectedDate = LocalDate.of(selectedYear, selectedMonthIndex, 1);
                updateCurrentMonthView(selectedDate);
            } else {
                // 如果下拉菜单未初始化，则使用当前日期
                updateCurrentMonthView(LocalDate.now());
            }
        });
        filterPanel.add(refreshButton);
        
        // Add filter panel to current month panel
        panel.add(filterPanel, BorderLayout.SOUTH);
        
        // Update the view
        updateCurrentMonthView();
        
        return panel;
    }
    
    /**
     * Creates the AI assistant panel.
     * 
     * @return The AI assistant panel
     */
    private JPanel createAiAssistantPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 新增模型选择面板
        JPanel modelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        modelPanel.setBorder(BorderFactory.createTitledBorder("选择模型"));
        modelComboBox = new JComboBox<>();
        modelComboBox.addItem("deepseek-llm");
        modelComboBox.addItem("qwen2");
        modelPanel.add(new JLabel("Ollama模型:"));
        modelPanel.add(modelComboBox);
        panel.add(modelPanel, BorderLayout.NORTH);

        // Create query panel
        JPanel queryPanel = new JPanel();
        queryPanel.setLayout(new BorderLayout());
        queryPanel.setBorder(BorderFactory.createTitledBorder("Ask AI Assistant"));

        // Add query field
        aiQueryField = new JTextField();
        queryPanel.add(aiQueryField, BorderLayout.CENTER);

        // Add ask button
        JButton askButton = new JButton("Ask");
        askButton.addActionListener(e -> askAiAssistant());
        queryPanel.add(askButton, BorderLayout.EAST);

        // Add query panel to AI assistant panel
        panel.add(queryPanel, BorderLayout.CENTER);

        // Create response panel
        JPanel responsePanel = new JPanel();
        responsePanel.setLayout(new BorderLayout());
        responsePanel.setBorder(BorderFactory.createTitledBorder("AI Response"));

        // Add response text area
        aiResponseTextArea = new JTextArea();
        aiResponseTextArea.setEditable(false);
        aiResponseTextArea.setLineWrap(true);
        aiResponseTextArea.setWrapStyleWord(true);
        JScrollPane responseScrollPane = new JScrollPane(aiResponseTextArea);
        responsePanel.add(responseScrollPane, BorderLayout.CENTER);

        // Add response panel to AI assistant panel
        panel.add(responsePanel, BorderLayout.SOUTH);

        // Create suggestion panel
        JPanel suggestionPanel = new JPanel();
        suggestionPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        suggestionPanel.setBorder(BorderFactory.createTitledBorder("Suggested Questions"));

        // Add suggestion buttons
        JButton suggestion1 = new JButton("How can I save more money?");
        suggestion1.addActionListener(e -> {
            aiQueryField.setText(suggestion1.getText());
            askAiAssistant();
        });
        suggestionPanel.add(suggestion1);

        JButton suggestion2 = new JButton("What are my spending habits?");
        suggestion2.addActionListener(e -> {
            aiQueryField.setText(suggestion2.getText());
            askAiAssistant();
        });
        suggestionPanel.add(suggestion2);

        JButton suggestion3 = new JButton("How to budget for next month?");
        suggestion3.addActionListener(e -> {
            aiQueryField.setText(suggestion3.getText());
            askAiAssistant();
        });
        suggestionPanel.add(suggestion3);

        // Add suggestion panel to AI assistant panel
        panel.add(suggestionPanel, BorderLayout.PAGE_END);

        return panel;
    }
    
    /**
     * Creates the budget panel.
     * 
     * @return The budget panel
     */
    private JPanel createBudgetPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create budget summary panel
        JPanel budgetSummaryPanel = new JPanel();
        budgetSummaryPanel.setLayout(new BorderLayout());
        budgetSummaryPanel.setBorder(BorderFactory.createTitledBorder("Budget Summary"));
        
        // Add budget summary text area
        JTextArea budgetSummaryTextArea = new JTextArea();
        budgetSummaryTextArea.setEditable(false);
        budgetSummaryTextArea.setLineWrap(true);
        budgetSummaryTextArea.setWrapStyleWord(true);
        
        // Generate budget summary
        LocalDate today = LocalDate.now();
        LocalDate nextMonth = today.plusMonths(1);
        String monthName = nextMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
        
        StringBuilder summary = new StringBuilder();
        summary.append("Budget Forecast for ").append(monthName).append(" ").append(nextMonth.getYear()).append("\n\n");
        
        // Calculate average income and expenses from past 6 months
        double avgIncome = calculateAverageIncome(6);
        double avgExpense = calculateAverageExpense(6);
        
        summary.append("Based on your past 6 months of transactions:\n\n");
        summary.append(String.format("Projected Income: %.2f\n", avgIncome));
        summary.append(String.format("Projected Expenses: %.2f\n", avgExpense));
        summary.append(String.format("Projected Savings: %.2f\n\n", avgIncome - avgExpense));
        
        // Get category distribution
        Map<String, Double> categoryDistribution = calculateCategoryDistribution(6);
        
        summary.append("Suggested Budget Allocation:\n\n");
        
        for (Map.Entry<String, Double> entry : categoryDistribution.entrySet()) {
            double amount = avgExpense * entry.getValue();
            summary.append(String.format("%s: %.2f (%.1f%%)\n", entry.getKey(), amount, entry.getValue() * 100));
        }
        
        budgetSummaryTextArea.setText(summary.toString());
        
        JScrollPane budgetSummaryScrollPane = new JScrollPane(budgetSummaryTextArea);
        budgetSummaryPanel.add(budgetSummaryScrollPane, BorderLayout.CENTER);
        
        // Add budget summary panel to budget panel
        panel.add(budgetSummaryPanel, BorderLayout.CENTER);
        
        // Create special dates panel
        JPanel specialDatesPanel = new JPanel();
        specialDatesPanel.setLayout(new BorderLayout());
        specialDatesPanel.setBorder(BorderFactory.createTitledBorder("Special Dates"));
        
        // Add special dates text area
        JTextArea specialDatesTextArea = new JTextArea();
        specialDatesTextArea.setEditable(false);
        specialDatesTextArea.setLineWrap(true);
        specialDatesTextArea.setWrapStyleWord(true);
        
        // Add some example special dates
        StringBuilder specialDates = new StringBuilder();
        specialDates.append("Upcoming Special Dates:\n\n");
        
        // Check if next month is a holiday month
        Month nextMonthEnum = nextMonth.getMonth();
        if (nextMonthEnum == Month.JANUARY) {
            specialDates.append("Chinese New Year is coming up! Expect increased expenses for gifts and celebrations.\n");
        } else if (nextMonthEnum == Month.FEBRUARY) {
            specialDates.append("Valentine's Day is coming up! Consider budgeting for gifts or special dinner.\n");
        } else if (nextMonthEnum == Month.MAY) {
            specialDates.append("Labor Day holiday is coming up! Consider budgeting for travel or activities.\n");
        } else if (nextMonthEnum == Month.OCTOBER) {
            specialDates.append("National Day holiday is coming up! Expect increased expenses for travel and activities.\n");
        } else if (nextMonthEnum == Month.DECEMBER) {
            specialDates.append("Christmas and New Year are coming up! Consider budgeting for gifts and celebrations.\n");
        } else {
            specialDates.append("No major holidays or special events detected for next month.\n");
        }
        
        specialDatesTextArea.setText(specialDates.toString());
        
        JScrollPane specialDatesScrollPane = new JScrollPane(specialDatesTextArea);
        specialDatesPanel.add(specialDatesScrollPane, BorderLayout.CENTER);
        
        // Add special dates panel to budget panel
        panel.add(specialDatesPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Updates the current month view.
     */
    private void updateCurrentMonthView() {
        if (yearComboBox != null && monthNameComboBox != null) {
            int selectedYear = (int) yearComboBox.getSelectedItem();
            int selectedMonthIndex = monthNameComboBox.getSelectedIndex() + 1;
            LocalDate selectedDate = LocalDate.of(selectedYear, selectedMonthIndex, 1);
            updateCurrentMonthView(selectedDate);
        } else {
            // 如果下拉菜单未初始化，则使用当前日期
            updateCurrentMonthView(LocalDate.now());
        }
    }
    
    /**
     * 更新指定日期的月份视图
     * 
     * @param date 日期（月份的第一天）
     */
    private void updateCurrentMonthView(LocalDate date) {
        // 获取选择的类别
        String selectedCategory = (String) categoryComboBox.getSelectedItem();
        boolean allCategories = "All Categories".equals(selectedCategory);
        
        // 获取交易记录
        List<Transaction> allTransactions = transactionService.getAllTransactions();
        List<Transaction> transactions = new ArrayList<>();
        
        // 筛选当月交易
        for (Transaction transaction : allTransactions) {
            LocalDate transactionDate = transaction.getDate();
            boolean sameMonth = transactionDate.getMonthValue() == date.getMonthValue() 
                && transactionDate.getYear() == date.getYear();
                
            if (sameMonth && (allCategories || selectedCategory.equals(transaction.getCategory()))) {
                transactions.add(transaction);
            }
        }
        
        // 更新界面
        updateSummary(transactions, date);
        updateCategoryBreakdown(transactions);
    }
    
    /**
     * Updates the summary text area.
     * 
     * @param transactions The transactions to summarize
     * @param date The date for the summary
     */
    private void updateSummary(List<Transaction> transactions, LocalDate date) {
        StringBuilder summary = new StringBuilder();
        
        String monthName = date.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
        summary.append("Summary for ").append(monthName).append(" ").append(date.getYear()).append("\n\n");
        
        double totalIncome = transactionService.getTotalIncome(transactions);
        double totalExpense = transactionService.getTotalExpense(transactions);
        double netAmount = totalIncome - totalExpense;
        
        summary.append(String.format("Total Income: %.2f\n", totalIncome));
        summary.append(String.format("Total Expenses: %.2f\n", totalExpense));
        summary.append(String.format("Net Amount: %.2f\n\n", netAmount));
        
        summaryTextArea.setText(summary.toString());
    }
    
    /**
     * Updates the category breakdown text area.
     * 
     * @param transactions The transactions to use
     */
    private void updateCategoryBreakdown(List<Transaction> transactions) {
        StringBuilder breakdown = new StringBuilder();
        breakdown.append("Category Breakdown:\n\n");
        
        // Calculate category totals
        Map<String, Double> categoryTotals = new HashMap<>();
        double totalExpense = 0.0;
        
        for (Transaction transaction : transactions) {
            if (transaction.isExpense()) {
                String category = transaction.getCategory();
                double amount = transaction.getAmount();
                
                categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);
                totalExpense += amount;
            }
        }
        
        // Add category totals to breakdown
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            String category = entry.getKey();
            double amount = entry.getValue();
            double percentage = totalExpense > 0 ? (amount / totalExpense) * 100 : 0;
            
            breakdown.append(String.format("%s: %.2f (%.1f%%)\n", category, amount, percentage));
            
            // Add a simple bar chart using asterisks
            int barLength = (int) (percentage / 2);
            breakdown.append("[");
            for (int i = 0; i < barLength; i++) {
                breakdown.append("*");
            }
            for (int i = barLength; i < 50; i++) {
                breakdown.append(" ");
            }
            breakdown.append("]\n\n");
        }
        
        categoryBreakdownTextArea.setText(breakdown.toString());
    }
    
    /**
     * Asks the AI assistant a question.
     */
    private void askAiAssistant() {
        String query = aiQueryField.getText();
        String model = (String) modelComboBox.getSelectedItem();

        if (query == null || query.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a question.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        aiResponseTextArea.setText("Thinking...");

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return aiAssistantService.getResponse(query, model, transactionService);
            }

            @Override
            protected void done() {
                try {
                    String response = get();
                    aiResponseTextArea.setText(response);
                    // 增加弹窗显示AI回复
                    JOptionPane.showMessageDialog(AnalysisPanel.this, response, "AI助手回复", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    e.printStackTrace();
                    aiResponseTextArea.setText("Error getting response: " + e.getMessage());
                }
            }
        };

        worker.execute();
    }
    
    /**
     * Calculates the average monthly income for the past n months.
     * 
     * @param months The number of months to consider
     * @return The average monthly income
     */
    private double calculateAverageIncome(int months) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusMonths(months).withDayOfMonth(1);
        LocalDate endDate = today.withDayOfMonth(today.lengthOfMonth());
        
        List<Transaction> transactions = transactionService.getTransactionsForDateRange(startDate, endDate);
        double totalIncome = transactionService.getTotalIncome(transactions);
        
        return totalIncome / months;
    }
    
    /**
     * Calculates the average monthly expense for the past n months.
     * 
     * @param months The number of months to consider
     * @return The average monthly expense
     */
    private double calculateAverageExpense(int months) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusMonths(months).withDayOfMonth(1);
        LocalDate endDate = today.withDayOfMonth(today.lengthOfMonth());
        
        List<Transaction> transactions = transactionService.getTransactionsForDateRange(startDate, endDate);
        double totalExpense = transactionService.getTotalExpense(transactions);
        
        return totalExpense / months;
    }
    
    /**
     * Calculates the category distribution for the past n months.
     * 
     * @param months The number of months to consider
     * @return The category distribution
     */
    private Map<String, Double> calculateCategoryDistribution(int months) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusMonths(months).withDayOfMonth(1);
        LocalDate endDate = today.withDayOfMonth(today.lengthOfMonth());
        
        List<Transaction> transactions = transactionService.getTransactionsForDateRange(startDate, endDate);
        double totalExpense = transactionService.getTotalExpense(transactions);
        
        Map<String, Double> categoryTotals = new HashMap<>();
        
        for (Transaction transaction : transactions) {
            if (transaction.isExpense()) {
                String category = transaction.getCategory();
                double amount = transaction.getAmount();
                
                categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);
            }
        }
        
        // Convert to distribution
        Map<String, Double> distribution = new HashMap<>();
        
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            distribution.put(entry.getKey(), entry.getValue() / totalExpense);
        }
        
        return distribution;
    }
}
