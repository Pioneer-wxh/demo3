package com.financetracker.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import com.financetracker.ai.AiAssistantService;
import com.financetracker.ai.CsvDataReader;
import com.financetracker.model.SavingGoal;
import com.financetracker.model.Settings;
import com.financetracker.model.SpecialDate;
import com.financetracker.model.Transaction;
import com.financetracker.service.BudgetAdjustmentService;
import com.financetracker.service.SettingsService;
import com.financetracker.service.SpecialDateService;
import com.financetracker.service.TransactionService;

/**
 * Panel for AI-assisted analysis.
 */
public class AnalysisPanel extends JPanel {
    
    private MainFrame mainFrame;
    private TransactionService transactionService;
    private AiAssistantService aiAssistantService;
    private SpecialDateService specialDateService;
    private BudgetAdjustmentService budgetAdjustmentService;
    private SettingsService settingsService;
    
    private JPanel currentMonthPanel;
    private JPanel aiAssistantPanel;
    private JPanel budgetPanel;
    private JPanel savingGoalsProgressPanel;
    
    private JTextArea summaryTextArea;
    private JTextArea categoryBreakdownTextArea;
    private JTextArea aiResponseTextArea;
    private JTextField aiQueryField;
    private JComboBox<Integer> yearComboBox;
    private JComboBox<String> monthNameComboBox;
    
    private JRadioButton normalModeRadio;
    private JRadioButton financialModeRadio;
    private boolean isFinancialMode = false; // 默认为普通对话模式
    
    // Text areas for the Budget Panel
    private JTextArea budgetSummaryTextArea;
    private JTextArea categoryBudgetChartTextArea;
    private JTextArea specialDatesTextArea;
    
    // Reference to the budget panel's text area if it exists and needs updating.
    // Assuming budget panel has a JTextArea or similar to display budget info.
    private JTextArea budgetDetailsTextArea; // Example: You'll need to initialize this if it's how budget is shown
    
    /**
     * Constructor for AnalysisPanel.
     * 
     * @param transactionService The transaction service.
     * @param settingsService The settings service.
     * @param specialDateService The special date service.
     * @param budgetAdjustmentService The budget adjustment service.
     */
    public AnalysisPanel(TransactionService transactionService, SettingsService settingsService,
                          SpecialDateService specialDateService, BudgetAdjustmentService budgetAdjustmentService) {
        this.transactionService = transactionService;
        this.settingsService = settingsService;
        this.specialDateService = specialDateService;
        this.budgetAdjustmentService = budgetAdjustmentService;
        this.aiAssistantService = new AiAssistantService(settingsService);
        
        // 设置CsvDataReader的TransactionService
        CsvDataReader.setTransactionService(transactionService);
        
        initComponents();
    }
    
    /**
     * Constructor for AnalysisPanel.
     * 
     * @param transactionService The transaction service.
     * @param settingsService The settings service.
     * @param specialDateService The special date service.
     * @param budgetAdjustmentService The budget adjustment service.
     * @param mainFrame The main frame.
     */
    public AnalysisPanel(TransactionService transactionService, SettingsService settingsService,
                        SpecialDateService specialDateService, BudgetAdjustmentService budgetAdjustmentService,
                        MainFrame mainFrame) {
        this.transactionService = transactionService;
        this.settingsService = settingsService;
        this.specialDateService = specialDateService;
        this.budgetAdjustmentService = budgetAdjustmentService;
        this.mainFrame = mainFrame;
        this.aiAssistantService = new AiAssistantService(settingsService);
        
        // 设置CsvDataReader的TransactionService
        CsvDataReader.setTransactionService(transactionService);
        
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
        
        // Create Saving Goals Progress panel
        savingGoalsProgressPanel = createSavingGoalsProgressPanel();
        tabbedPane.addTab("Saving Goals", savingGoalsProgressPanel);
        
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
        
        // A.2.3: Use JSplitPane for left-right layout of summary and chart
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, summaryPanel, chartPanel);
        splitPane.setResizeWeight(0.4); // Initial proportion for the left (summary) panel
        panel.add(splitPane, BorderLayout.CENTER);
        
        // Create filter panel
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filters"));
        
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
        
        // 创建模式选择面板
        JPanel modePanel = new JPanel();
        modePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        modePanel.setBorder(BorderFactory.createTitledBorder("对话模式"));
        
        // 创建单选按钮组
        normalModeRadio = new JRadioButton("普通对话模式", true);
        financialModeRadio = new JRadioButton("财务分析模式", false);
        
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(normalModeRadio);
        modeGroup.add(financialModeRadio);
        
        // 添加事件监听
        normalModeRadio.addActionListener(e -> {
            isFinancialMode = false;
            aiResponseTextArea.setText("已切换至普通对话模式。您可以与AI进行任何对话，不会自动添加财务数据。");
        });
        
        financialModeRadio.addActionListener(e -> {
            isFinancialMode = true;
            aiResponseTextArea.setText("已切换至财务分析模式。AI将自动分析您的财务数据，以帮助您回答财务相关问题。");
        });
        
        // 添加到模式面板
        modePanel.add(normalModeRadio);
        modePanel.add(financialModeRadio);
        
        // 添加模式面板到顶部
        panel.add(modePanel, BorderLayout.NORTH);
        
        // Create query panel
        JPanel queryPanel = new JPanel();
        queryPanel.setLayout(new BorderLayout());
        queryPanel.setBorder(BorderFactory.createTitledBorder("Ask AI Assistant"));
        
        // Add query field
        aiQueryField = new JTextField();
        aiQueryField.addActionListener(e -> askAiAssistant());
        queryPanel.add(aiQueryField, BorderLayout.CENTER);
        
        // Add ask button
        JButton askButton = new JButton("Ask");
        askButton.addActionListener(e -> askAiAssistant());
        queryPanel.add(askButton, BorderLayout.EAST);
        
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
        
        // 将响应面板添加到中间面板
        panel.add(responsePanel, BorderLayout.CENTER);
        
        // Create suggestion panel
        JPanel suggestionPanel = new JPanel();
        suggestionPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        suggestionPanel.setBorder(BorderFactory.createTitledBorder("Suggested Financial Questions"));
        
        // Add suggestion buttons
        JButton suggestion1 = new JButton("How can I save more money?");
        suggestion1.addActionListener(e -> {
            aiQueryField.setText(suggestion1.getText());
            // 使用财务AI模式
            askFinancialAiAssistant(suggestion1.getText());
        });
        suggestionPanel.add(suggestion1);
        
        JButton suggestion2 = new JButton("What are my spending habits?");
        suggestion2.addActionListener(e -> {
            aiQueryField.setText(suggestion2.getText());
            // 使用财务AI模式
            askFinancialAiAssistant(suggestion2.getText());
        });
        suggestionPanel.add(suggestion2);
        
        JButton suggestion3 = new JButton("How to budget for next month?");
        suggestion3.addActionListener(e -> {
            aiQueryField.setText(suggestion3.getText());
            // 使用财务AI模式
            askFinancialAiAssistant(suggestion3.getText());
        });
        suggestionPanel.add(suggestion3);
        
        // A.3.1: Create a bottom panel for suggestions and query input
        JPanel bottomAreaPanel = new JPanel(new BorderLayout());
        bottomAreaPanel.add(suggestionPanel, BorderLayout.CENTER); // Suggestions above query
        bottomAreaPanel.add(queryPanel, BorderLayout.SOUTH);    // Query input at the very bottom

        panel.add(bottomAreaPanel, BorderLayout.SOUTH);
        
        // 显示初始提示信息
        aiResponseTextArea.setText("欢迎使用AI助手！目前处于普通对话模式。如需分析您的财务数据，请切换至财务分析模式。");
        
        return panel;
    }
    
    /**
     * Creates the budget panel.
     * 
     * @return The budget panel
     */
    private JPanel createBudgetPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10)); // Add gaps for EAST panel
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // A.4.1, A.4.2: Central panel for summary and category budget chart
        JPanel centralContentPanel = new JPanel();
        centralContentPanel.setLayout(new BoxLayout(centralContentPanel, BoxLayout.Y_AXIS));

        // Budget summary panel (existing)
        JPanel budgetSummaryPanel = new JPanel(new BorderLayout());
        budgetSummaryPanel.setBorder(BorderFactory.createTitledBorder("Budget Forecast & Adjustments"));
        
        // Initialize class member
        this.budgetSummaryTextArea = new JTextArea();
        this.budgetSummaryTextArea.setEditable(false);
        this.budgetSummaryTextArea.setLineWrap(true);
        this.budgetSummaryTextArea.setWrapStyleWord(true);
        this.budgetSummaryTextArea.setFont(UIManager.getFont("Label.font"));

        this.budgetSummaryTextArea.setText(generateBudgetSummaryText()); // Populate with generated text
        JScrollPane budgetSummaryScrollPane = new JScrollPane(this.budgetSummaryTextArea);
        budgetSummaryPanel.add(budgetSummaryScrollPane, BorderLayout.CENTER);
        centralContentPanel.add(budgetSummaryPanel);
        centralContentPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Spacing


        // A.4.1 & A.4.2: Category Budget Bar Chart Panel
        JPanel categoryBudgetChartPanel = new JPanel(new BorderLayout());
        categoryBudgetChartPanel.setBorder(BorderFactory.createTitledBorder("Next Month Category Budget Allocation"));
        
        // Initialize class member
        this.categoryBudgetChartTextArea = new JTextArea();
        this.categoryBudgetChartTextArea.setEditable(false);
        this.categoryBudgetChartTextArea.setLineWrap(true);
        this.categoryBudgetChartTextArea.setWrapStyleWord(true);
        this.categoryBudgetChartTextArea.setFont(UIManager.getFont("Label.font")); // Monospaced font might be better for text bars

        this.categoryBudgetChartTextArea.setText(generateCategoryBudgetText()); // Populate with generated text
        JScrollPane categoryBudgetChartScrollPane = new JScrollPane(this.categoryBudgetChartTextArea);
        categoryBudgetChartPanel.add(categoryBudgetChartScrollPane, BorderLayout.CENTER);
        centralContentPanel.add(categoryBudgetChartPanel);
        
        panel.add(centralContentPanel, BorderLayout.CENTER);


        // A.4.3: Special Dates Display Panel as a sidebar (EAST)
        JPanel specialDatesDisplayPanel = new JPanel(new BorderLayout());
        specialDatesDisplayPanel.setBorder(BorderFactory.createTitledBorder("Upcoming Special Dates in " + YearMonth.now().plusMonths(1).getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault())));
        specialDatesDisplayPanel.setPreferredSize(new Dimension(300, 0)); // Give it a preferred width for sidebar
        
        // Initialize class member
        this.specialDatesTextArea = new JTextArea();
        this.specialDatesTextArea.setEditable(false);
        this.specialDatesTextArea.setLineWrap(true);
        this.specialDatesTextArea.setWrapStyleWord(true);
        this.specialDatesTextArea.setFont(UIManager.getFont("Label.font"));
        
        this.specialDatesTextArea.setText(generateSpecialDatesText()); // Populate with generated text
        specialDatesDisplayPanel.add(new JScrollPane(this.specialDatesTextArea), BorderLayout.CENTER);
        
        panel.add(specialDatesDisplayPanel, BorderLayout.EAST); 
        
        return panel;
    }
    
    /**
     * Creates the panel for displaying saving goals progress.
     * @return The saving goals progress panel.
     */
    private JPanel createSavingGoalsProgressPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Main content panel that will hold individual goal panels
        JPanel goalsDisplayArea = new JPanel();
        goalsDisplayArea.setLayout(new BoxLayout(goalsDisplayArea, BoxLayout.Y_AXIS));
        
        JScrollPane scrollPane = new JScrollPane(goalsDisplayArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Button to refresh
        JButton refreshButton = new JButton("Refresh Goals Progress");
        refreshButton.addActionListener(e -> updateSavingGoalsProgressView());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Initial population
        // updateSavingGoalsProgressView(); // Call this when panel is shown or data is expected to be fresh
        // Deferring initial call to when tab might be selected, or a refresh button is clicked
        // to avoid premature loading if settings are not ready.
        return panel;
    }

    public void updateSavingGoalsProgressView() {
        if (settingsService == null || settingsService.getSettings() == null) {
            return;
        }
        Settings settings = settingsService.getSettings();
        List<SavingGoal> goals = settings.getSavingGoals();

        // Find the goalsDisplayArea panel within the savingGoalsProgressPanel structure
        JPanel goalsDisplayArea = null;
        if (savingGoalsProgressPanel != null && savingGoalsProgressPanel.getComponentCount() > 0) {
            Component centerComponent = ((BorderLayout)savingGoalsProgressPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
            if (centerComponent instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) centerComponent;
                goalsDisplayArea = (JPanel) scrollPane.getViewport().getView();
            }
        }
        if (goalsDisplayArea == null) {
            System.err.println("Could not find goalsDisplayArea to update saving goals progress.");
            return; // Should not happen if panel is constructed correctly
        }

        goalsDisplayArea.removeAll(); // Clear previous goals

        if (goals != null) {
            for (SavingGoal goal : goals) {
                if (!goal.isActive()) { // Display only active goals
                    continue;
                }

                JPanel goalPanel = new JPanel(new BorderLayout(5, 5));
                goalPanel.setBorder(BorderFactory.createTitledBorder(goal.getName()));

                JPanel detailsPanel = new JPanel();
                detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));

                detailsPanel.add(new JLabel(String.format("Target: %.2f", goal.getTargetAmount())));
                detailsPanel.add(new JLabel(String.format("Current: %.2f (%.2f%%)", 
                                                          goal.getCurrentAmount(), 
                                                          goal.getTargetAmount() > 0 ? (goal.getCurrentAmount() / goal.getTargetAmount() * 100) : 0)));
                detailsPanel.add(new JLabel(String.format("Monthly Contribution: %.2f", goal.getMonthlyContribution())));
                
                if (goal.getStartDate() != null) {
                    detailsPanel.add(new JLabel("Start Date: " + goal.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE)));
                }
                if (goal.getTargetDate() != null) {
                    detailsPanel.add(new JLabel("Target Date: " + goal.getTargetDate().format(DateTimeFormatter.ISO_LOCAL_DATE)));
                } else {
                    // Estimate completion if possible
                    if (goal.getMonthlyContribution() > 0 && goal.getTargetAmount() > goal.getCurrentAmount()) {
                        double remainingAmount = goal.getTargetAmount() - goal.getCurrentAmount();
                        double monthsToTarget = Math.ceil(remainingAmount / goal.getMonthlyContribution());
                        LocalDate estimatedCompletionDate = goal.getStartDate() != null ? goal.getStartDate().plusMonths((long)monthsToTarget) : LocalDate.now().plusMonths((long)monthsToTarget);
                        detailsPanel.add(new JLabel("Est. Completion: " + estimatedCompletionDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + " (~" + (int)monthsToTarget + " months)"));
                    }
                }

                goalPanel.add(detailsPanel, BorderLayout.CENTER);

                JProgressBar progressBar = new JProgressBar(0, 100);
                int progress = 0;
                if (goal.getTargetAmount() > 0) {
                    progress = (int) ((goal.getCurrentAmount() / goal.getTargetAmount()) * 100);
                }
                progressBar.setValue(Math.min(progress, 100)); // Cap at 100%
                progressBar.setStringPainted(true);
                goalPanel.add(progressBar, BorderLayout.SOUTH);
                
                goalsDisplayArea.add(goalPanel);
                goalsDisplayArea.add(Box.createRigidArea(new Dimension(0, 10))); // Spacing
            }
        }
        goalsDisplayArea.revalidate();
        goalsDisplayArea.repaint();
    }
    
    /**
     * Updates the current month view.
     */
    private void updateCurrentMonthView() {
        if (yearComboBox != null && monthNameComboBox != null) {
            int selectedYear = (int) yearComboBox.getSelectedItem();
            int selectedMonthIndex = monthNameComboBox.getSelectedIndex() + 1;
            
            // 使用财务月计算
            updateFinancialMonthView(selectedYear, selectedMonthIndex);
        } else {
            // 如果下拉菜单未初始化，则使用当前财务月
            updateCurrentFinancialMonthView();
        }
    }
    
    /**
     * 更新指定日期的月份视图
     * 
     * @param date 日期（月份的第一天）
     */
    private void updateCurrentMonthView(LocalDate date) {
        // 使用财务月计算
        updateFinancialMonthView(date.getYear(), date.getMonthValue());
    }
    
    /**
     * 更新当前财务月视图
     */
    private void updateCurrentFinancialMonthView() {
        // 获取当前财务月的交易记录
        List<Transaction> allTransactions = transactionService.getAllTransactions();
        Map<String, LocalDate> financialMonthRange = transactionService.getCurrentFinancialMonthRange();
        LocalDate startDate = financialMonthRange.get("startDate");
        LocalDate endDate = financialMonthRange.get("endDate");
        
        // 筛选当前财务月的交易
        List<Transaction> transactions = new ArrayList<>();
        for (Transaction transaction : allTransactions) {
            LocalDate transactionDate = transaction.getDate();
            boolean inFinancialMonth = !transactionDate.isBefore(startDate) && !transactionDate.isAfter(endDate);
            
            if (inFinancialMonth) {
                transactions.add(transaction);
            }
        }
        
        // 更新界面
        updateSummaryForFinancialMonth(transactions, startDate, endDate);
        updateCategoryBreakdown(transactions);
    }
    
    /**
     * 更新指定财务月的视图
     * 
     * @param year 年份
     * @param month 月份(1-12)
     */
    private void updateFinancialMonthView(int year, int month) {
        // 获取指定财务月的日期范围
        Map<String, LocalDate> financialMonthRange = transactionService.getFinancialMonthRange(year, month);
        LocalDate startDate = financialMonthRange.get("startDate");
        LocalDate endDate = financialMonthRange.get("endDate");
        
        // 获取交易记录
        List<Transaction> allTransactions = transactionService.getAllTransactions();
        List<Transaction> transactions = new ArrayList<>();
        
        // 筛选指定财务月的交易
        for (Transaction transaction : allTransactions) {
            LocalDate transactionDate = transaction.getDate();
            boolean inFinancialMonth = !transactionDate.isBefore(startDate) && !transactionDate.isAfter(endDate);
            
            if (inFinancialMonth) {
                transactions.add(transaction);
            }
        }
        
        // 更新界面
        updateSummaryForFinancialMonth(transactions, startDate, endDate);
        updateCategoryBreakdown(transactions);
    }
    
    /**
     * 为财务月更新摘要信息
     * 
     * @param transactions 交易列表
     * @param startDate 财务月开始日期
     * @param endDate 财务月结束日期
     */
    private void updateSummaryForFinancialMonth(List<Transaction> transactions, LocalDate startDate, LocalDate endDate) {
        StringBuilder summary = new StringBuilder();
        
        String monthName = startDate.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
        String endMonthName = endDate.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
        
        // 如果跨月，显示起止日期，否则只显示月份
        String periodText;
        if (startDate.getMonth() == endDate.getMonth()) {
            periodText = monthName + " " + startDate.getYear();
        } else {
            periodText = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) 
                       + " 至 " 
                       + endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        
        summary.append("财务月统计 (").append(periodText).append(")\n\n");
        
        // 显示设置中的财务月起始日
        SettingsService settingsService = new SettingsService();
        int monthStartDay = settingsService.getSettings().getMonthStartDay();
        summary.append("当前财务月起始日设置: 每月").append(monthStartDay).append("日\n\n");
        
        double totalIncome = transactionService.getTotalIncome(transactions);
        double totalExpense = transactionService.getTotalExpense(transactions);
        double netAmount = totalIncome - totalExpense;
        
        summary.append(String.format("总收入: %.2f\n", totalIncome));
        summary.append(String.format("总支出: %.2f\n", totalExpense));
        summary.append(String.format("净收支: %.2f\n\n", netAmount));
        
        // 计算与预算的对比
        double monthlyBudget = settingsService.getSettings().getMonthlyBudget();
        summary.append(String.format("月度预算: %.2f\n", monthlyBudget));
        if (monthlyBudget > 0) {
            double budgetUsage = (totalExpense / monthlyBudget) * 100;
            summary.append(String.format("预算使用: %.1f%%\n", budgetUsage));
            if (totalExpense > monthlyBudget) {
                summary.append(String.format("⚠️ 已超出预算: %.2f ⚠️\n", totalExpense - monthlyBudget));
            }
        } else {
            summary.append("未设置月度预算\n");
        }
        summary.append("\n");

        // A.2.2: 与上月支出对比
        // 1. 确定上一个财务月的年份和月份
        LocalDate previousMonthStartDate = startDate.minusMonths(1);
        // 使用 transactionService 获取上一个财务月的准确起止日期，因为它会考虑 monthStartDay
        Map<String, LocalDate> previousFinancialMonthRange = transactionService.getFinancialMonthRange(previousMonthStartDate.getYear(), previousMonthStartDate.getMonthValue());
        LocalDate prevStartDate = previousFinancialMonthRange.get("startDate");
        LocalDate prevEndDate = previousFinancialMonthRange.get("endDate");

        // 2. 获取上一个财务月的所有交易
        List<Transaction> allTransactions = transactionService.getAllTransactions(); // Re-fetch all or ensure service can filter by date range effectively
        List<Transaction> previousMonthTransactions = new ArrayList<>();
        for (Transaction transaction : allTransactions) {
            LocalDate transactionDate = transaction.getDate();
            boolean inPreviousFinancialMonth = !transactionDate.isBefore(prevStartDate) && !transactionDate.isAfter(prevEndDate);
            if (inPreviousFinancialMonth) {
                previousMonthTransactions.add(transaction);
            }
        }
        
        // 3. 计算上一个财务月的总支出
        double lastMonthTotalExpense = transactionService.getTotalExpense(previousMonthTransactions);
        summary.append(String.format("上月总支出: %.2f\n", lastMonthTotalExpense));

        // 4. 与当前月支出对比
        double differenceFromLastMonth = totalExpense - lastMonthTotalExpense;
        if (differenceFromLastMonth > 0) {
            summary.append(String.format("比上月多支出: %.2f\n", differenceFromLastMonth));
        } else if (differenceFromLastMonth < 0) {
            summary.append(String.format("比上月少支出: %.2f\n", -differenceFromLastMonth));
        } else {
            summary.append("与上月支出持平\n");
        }
        summary.append("\n");
        
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
        String query = aiQueryField.getText().trim();
        
        if (query.isEmpty()) {
            return;
        }
        
        // 清空响应框
        aiResponseTextArea.setText(isFinancialMode ? 
                "正在分析您的财务数据和问题，请稍候..." : 
                "正在处理您的问题，请稍候...");
        
        // 添加正在处理的指示器
        aiResponseTextArea.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        // 启动后台线程获取AI响应
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                try {
                    // 根据模式选择使用哪种对话方式
                    if (isFinancialMode) {
                        // 使用带有财务上下文的AI功能
                        return aiAssistantService.getResponse(query, transactionService);
                    } else {
                        // 使用普通对话模式，不包含财务上下文
                        return aiAssistantService.getChatResponse(query);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return "抱歉，处理请求时出现错误：" + e.getMessage();
                }
            }
            
            @Override
            protected void done() {
                try {
                    String response = get();
                    aiResponseTextArea.setText(response);
                    
                    // 如果响应包含错误信息，高亮显示
                    if (response.contains("错误") || response.contains("抱歉")) {
                        aiResponseTextArea.setBackground(new Color(255, 240, 240)); // 浅红色背景
                    } else {
                        aiResponseTextArea.setBackground(UIManager.getColor("TextArea.background"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    aiResponseTextArea.setText("获取响应时出错：" + e.getMessage());
                    aiResponseTextArea.setBackground(new Color(255, 240, 240)); // 浅红色背景
                } finally {
                    aiResponseTextArea.setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * 使用带有财务上下文的AI分析功能
     * 注意：此方法强制使用财务分析模式，无论当前选择的模式是什么
     * 
     * @param query 用户查询
     */
    private void askFinancialAiAssistant(String query) {
        if (query.isEmpty()) {
            return;
        }
        
        // 切换到财务模式
        financialModeRadio.setSelected(true);
        isFinancialMode = true;
        
        // 清空响应框
        aiResponseTextArea.setText("正在分析您的财务数据和问题，请稍候...");
        
        // 添加正在处理的指示器
        aiResponseTextArea.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        // 启动后台线程获取AI响应
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                try {
                    // 使用带有财务上下文的AI功能
                    return aiAssistantService.getResponse(query, transactionService);
                } catch (Exception e) {
                    e.printStackTrace();
                    return "抱歉，处理请求时出现错误：" + e.getMessage();
                }
            }
            
            @Override
            protected void done() {
                try {
                    String response = get();
                    aiResponseTextArea.setText(response);
                    
                    // 如果响应包含错误信息，高亮显示
                    if (response.contains("错误") || response.contains("抱歉")) {
                        aiResponseTextArea.setBackground(new Color(255, 240, 240)); // 浅红色背景
                    } else {
                        aiResponseTextArea.setBackground(UIManager.getColor("TextArea.background"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    aiResponseTextArea.setText("获取响应时出错：" + e.getMessage());
                    aiResponseTextArea.setBackground(new Color(255, 240, 240)); // 浅红色背景
                } finally {
                    aiResponseTextArea.setCursor(Cursor.getDefaultCursor());
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
    
    /**
     * 刷新类别下拉列表
     */
    public void refreshCategoryList() {
        if (mainFrame == null || mainFrame.getSettings() == null) {
            return;
        }
        
        Settings settings = mainFrame.getSettings();
        if (settings.getExpenseCategories() != null) {
            for (String category : settings.getExpenseCategories()) {
                // categoryComboBox.addItem(category);
            }
        }
        if (settings.getIncomeCategories() != null) {
            for (String category : settings.getIncomeCategories()) {
                boolean exists = false;
                // for (int i = 0; i < categoryComboBox.getItemCount(); i++) {
                // if (category.equals(categoryComboBox.getItemAt(i))) {
                // exists = true;
                // break;
                // }
                // }
                // if (!exists) {
                // categoryComboBox.addItem(category);
                // }
            }
        }
        
        // 保留之前的选择，如果它仍然存在
        // if (selectedCategory != null) {
            // boolean found = false;
            // for (int i = 0; i < categoryComboBox.getItemCount(); i++) {
                // if (selectedCategory.equals(categoryComboBox.getItemAt(i))) {
                    // categoryComboBox.setSelectedItem(selectedCategory);
                    // found = true;
                    // break;
                // }
            // }
            // if (!found) {
                // categoryComboBox.setSelectedItem("All Categories");
            // }
        // } else {
            // categoryComboBox.setSelectedItem("All Categories");
        // }
    }

    /**
     * Public method to refresh all data and views within the AnalysisPanel.
     * This should be called when underlying data (transactions, settings, special dates, goals) might have changed.
     */
    public void refreshAllAnalysisData() {
        // Log or print that refresh is called
        System.out.println("AnalysisPanel: Refreshing all analysis data...");

        // 1. Refresh Current Month Tab (summary, category breakdown)
        // Determine which view to update based on mode (normal or financial)
        if (isFinancialMode) {
            // Assuming yearComboBox and monthNameComboBox hold the selected financial period
            int selectedYear = (Integer) yearComboBox.getSelectedItem();
            // Month name needs to be parsed back to month number for financial view update
            String selectedMonthName = (String) monthNameComboBox.getSelectedItem();
            Month month = null;
            for (int i = 1; i <= 12; i++) {
                if (Month.of(i).getDisplayName(TextStyle.FULL, Locale.getDefault()).equals(selectedMonthName)) {
                    month = Month.of(i);
                    break;
                }
            }
            if (month != null) {
                updateFinancialMonthView(selectedYear, month.getValue());
            } else {
                updateCurrentFinancialMonthView(); // Fallback or default
            }
        } else {
            // For "normal" mode, it might be based on LocalDate.now() or a specific date if that's a feature
            updateCurrentMonthView(); // Or updateCurrentMonthView(LocalDate.now());
        }

        // 2. Refresh Budget of Next Month Tab
        // This likely involves recalculating based on current transactions, settings, and special dates.
        // Assuming createBudgetPanel() or a dedicated update method populates budgetDetailsTextArea or similar components.
        // We might need a specific update method for the budget panel if createBudgetPanel just builds static UI.
        // For now, let's assume re-populating the budget panel's core content is needed.
        // If createBudgetPanel is lightweight and just populates text, we can call parts of it or a new update method.
        updateBudgetPanelContents(); // Placeholder for the actual budget update logic

        // 3. Refresh Saving Goals Tab
        updateSavingGoalsProgressView();

        // Potentially clear AI Assistant responses if they depend on the refreshed data context
        if (aiResponseTextArea != null) {
            // aiResponseTextArea.setText("Data has been refreshed. Please ask your query again if needed.");
            // Or just leave it, as user might want to see previous response in old context.
        }
        
        System.out.println("AnalysisPanel: Refresh complete.");
    }

    // Placeholder for method to update budget panel contents. Needs to be implemented.
    private void updateBudgetPanelContents() {
        // This method should re-fetch settings, special dates, transactions for next month,
        // perform budget calculations, and update the UI components within the budgetPanel.
        if (budgetPanel == null || settingsService == null || budgetAdjustmentService == null) {
            System.err.println("AnalysisPanel: Cannot update budget panel, services or panel not initialized.");
            return;
        }
        System.out.println("AnalysisPanel: Updating budget panel contents...");

        if (this.budgetSummaryTextArea != null) {
            this.budgetSummaryTextArea.setText(generateBudgetSummaryText());
            this.budgetSummaryTextArea.setCaretPosition(0);
        } else {
            System.err.println("AnalysisPanel: budgetSummaryTextArea is null, cannot update.");
        }

        if (this.categoryBudgetChartTextArea != null) {
            this.categoryBudgetChartTextArea.setText(generateCategoryBudgetText());
            this.categoryBudgetChartTextArea.setCaretPosition(0);
        } else {
            System.err.println("AnalysisPanel: categoryBudgetChartTextArea is null, cannot update.");
        }

        if (this.specialDatesTextArea != null) {
            // Update the border title for specialDatesDisplayPanel as well, as month might change
            JPanel specialDatesDisplayPanel = (JPanel) this.specialDatesTextArea.getParent().getParent(); // JViewport -> JScrollPane -> JPanel
            if (specialDatesDisplayPanel != null) {
                 YearMonth nextMonth = YearMonth.now().plusMonths(1);
                 String monthName = nextMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
                 specialDatesDisplayPanel.setBorder(BorderFactory.createTitledBorder("Upcoming Special Dates in " + monthName));
            }
            this.specialDatesTextArea.setText(generateSpecialDatesText());
            this.specialDatesTextArea.setCaretPosition(0);
        } else {
            System.err.println("AnalysisPanel: specialDatesTextArea is null, cannot update.");
        }
        
        // Ensure the panel itself re-renders if necessary, though text updates usually suffice.
        budgetPanel.revalidate();
        budgetPanel.repaint();
        System.out.println("AnalysisPanel: Budget panel contents updated.");
    }

    /**
     * Generates the summary text for the budget panel.
     * @return String containing the budget summary.
     */
    private String generateBudgetSummaryText() {
        LocalDate today = LocalDate.now();
        YearMonth nextMonthYearMonth = YearMonth.from(today.plusMonths(1));
        String monthName = nextMonthYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
        
        StringBuilder summaryTextBuilder = new StringBuilder();
        summaryTextBuilder.append("Budget Forecast for ").append(monthName).append(" ").append(nextMonthYearMonth.getYear()).append("\n\n");
        
        double avgIncome = calculateAverageIncome(6);
        double avgExpense = calculateAverageExpense(6); // For context
        
        summaryTextBuilder.append("Based on your past 6 months of transactions (for context only):\n");
        summaryTextBuilder.append(String.format("- Projected Average Income: %.2f\n", avgIncome));
        summaryTextBuilder.append(String.format("- Projected Average Expenses: %.2f\n", avgExpense));
        summaryTextBuilder.append(String.format("- Projected Average Savings (historical): %.2f\n\n", avgIncome - avgExpense));
        
        Settings currentSettings = settingsService.getSettings();
        double baseBudget = 0.0;
        String currency = "";

        if (currentSettings != null) {
            baseBudget = currentSettings.getMonthlyBudget();
            currency = currentSettings.getDefaultCurrency() != null ? currentSettings.getDefaultCurrency() : "";
            summaryTextBuilder.append(String.format("Base Monthly Budget (from Settings): %.2f %s\n", baseBudget, currency));
        } else {
            summaryTextBuilder.append("Base Monthly Budget: Not Set\n");
        }
        
        Map<String, Double> categoryAdjustments = budgetAdjustmentService.getCategoryAdjustmentsForMonth(nextMonthYearMonth);
        double totalAdjustments = 0;
        
        if (!categoryAdjustments.isEmpty()) {
            summaryTextBuilder.append("\nSpecial Date Adjustments for Next Month:\n");
            for (Map.Entry<String, Double> entry : categoryAdjustments.entrySet()) {
                summaryTextBuilder.append(String.format("- %s: +%.2f %s\n", entry.getKey(), entry.getValue(), currency));
                totalAdjustments += entry.getValue();
            }
            summaryTextBuilder.append(String.format("Total Increase from Special Dates: %.2f %s\n", totalAdjustments, currency));
        } else {
            summaryTextBuilder.append("\nNo specific budget adjustments from Special Dates for next month.\n");
        }
        
        double finalProjectedExpenseBudget = baseBudget + totalAdjustments;
        summaryTextBuilder.append(String.format("\nProjected Total Expense Budget for Next Month: %.2f %s\n", finalProjectedExpenseBudget, currency));

        double projectedSavingsNextMonth = avgIncome - finalProjectedExpenseBudget;
        summaryTextBuilder.append(String.format("Projected Savings for Next Month (Avg. Income - Projected Budget): %.2f %s\n\n", projectedSavingsNextMonth, currency));

        // Add saving goals contributions
        summaryTextBuilder.append("--- Projected Savings Contributions ---\n");
        List<SavingGoal> activeSavingGoals = (currentSettings != null && currentSettings.getSavingGoals() != null) ? 
                                            currentSettings.getSavingGoals().stream()
                                                .filter(SavingGoal::isActive)
                                                .filter(g -> !g.isCompleted())
                                                .collect(java.util.stream.Collectors.toList()) 
                                            : new ArrayList<>();
        double totalProjectedSavingsContributions = 0.0;
        if (!activeSavingGoals.isEmpty()){
            for(SavingGoal goal : activeSavingGoals){
                summaryTextBuilder.append(String.format("- Goal: %s, Monthly: %.2f %s\n", 
                                                        goal.getName(), goal.getMonthlyContribution(), currency));
                totalProjectedSavingsContributions += goal.getMonthlyContribution();
            }
            summaryTextBuilder.append(String.format("Total Projected Monthly Savings Contributions: %.2f %s\n", totalProjectedSavingsContributions, currency));
        } else {
            summaryTextBuilder.append("No active saving goals to contribute to for next month.\n");
        }

        return summaryTextBuilder.toString();
    }

    /**
     * Generates the category budget text (text-based bar chart) for the budget panel.
     * @return String containing the category budget allocation.
     */
    private String generateCategoryBudgetText() {
        StringBuilder chartBuilder = new StringBuilder();
        Settings currentSettings = settingsService.getSettings();
        YearMonth nextMonthYearMonth = YearMonth.now().plusMonths(1);
        List<String> expenseCategories = new ArrayList<>();
        double baseBudget = 0.0;
        String currency = "";

        if (currentSettings != null) {
            if (currentSettings.getExpenseCategories() != null) {
                expenseCategories.addAll(currentSettings.getExpenseCategories());
            }
            baseBudget = currentSettings.getMonthlyBudget();
            currency = currentSettings.getDefaultCurrency() != null ? currentSettings.getDefaultCurrency() : "";
        }

        Map<String, Double> categoryAdjustments = budgetAdjustmentService.getCategoryAdjustmentsForMonth(nextMonthYearMonth);
        double totalAdjustments = 0;
        for (double adj : categoryAdjustments.values()) {
            totalAdjustments += adj;
        }
        double finalProjectedExpenseBudget = baseBudget + totalAdjustments;


        if (currentSettings != null && !expenseCategories.isEmpty()) {
            Map<String, Double> historicalDistPercentages = calculateCategoryDistribution(6); 
            double totalBudgetForChartMax = 0; 

            Map<String, Double> categoryTotalBudgets = new HashMap<>();
            for (String category : expenseCategories) {
                double historicalPercentage = historicalDistPercentages.getOrDefault(category, 0.0);
                double baseAllocation = baseBudget * historicalPercentage;
                double adjustment = categoryAdjustments.getOrDefault(category, 0.0);
                double totalBudgetForCategory = baseAllocation + adjustment;
                categoryTotalBudgets.put(category, totalBudgetForCategory);
                if (totalBudgetForCategory > totalBudgetForChartMax) {
                    totalBudgetForChartMax = totalBudgetForCategory;
                }
            }
            
            if (totalBudgetForChartMax == 0 && finalProjectedExpenseBudget > 0) { 
                totalBudgetForChartMax = finalProjectedExpenseBudget; 
            } else if (totalBudgetForChartMax == 0) {
                totalBudgetForChartMax = 1; 
            }

            chartBuilder.append(String.format("Allocation based on historical spending (past 6m) & special date adjustments. Total Budget: %.2f %s\n\n", finalProjectedExpenseBudget, currency));
            for (String category : expenseCategories) {
                double totalBudgetForCategory = categoryTotalBudgets.getOrDefault(category, 0.0);
                double adjustment = categoryAdjustments.getOrDefault(category, 0.0);

                chartBuilder.append(String.format("%-20s: ", category)); 
                
                int barLength = (int) ((totalBudgetForCategory / totalBudgetForChartMax) * 30); 
                if (totalBudgetForChartMax == 0) barLength = 0;

                for (int i = 0; i < barLength; i++) {
                    chartBuilder.append("*");
                }
                chartBuilder.append(String.format(" %.2f %s", totalBudgetForCategory, currency));
                
                if (adjustment > 0) {
                    chartBuilder.append(String.format(" (+%.2f from special dates)", adjustment));
                } else if (adjustment < 0) {
                     chartBuilder.append(String.format(" (%.2f from special dates)", adjustment));
                }
                chartBuilder.append("\n");
            }
        } else {
            chartBuilder.append("No expense categories defined in settings to display budget allocation.\n");
        }
        return chartBuilder.toString();
    }

    /**
     * Generates the special dates information text for the budget panel.
     * @return String containing upcoming special dates information.
     */
    private String generateSpecialDatesText() {
        StringBuilder specialDatesInfo = new StringBuilder();
        Settings currentSettings = settingsService.getSettings();
        YearMonth nextMonthYearMonth = YearMonth.now().plusMonths(1);
        String monthName = nextMonthYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
        String currency = currentSettings != null && currentSettings.getDefaultCurrency() != null ? currentSettings.getDefaultCurrency() : "";

        if (currentSettings != null) {
            List<SpecialDate> allSpecialDates = currentSettings.getSpecialDates();
            boolean foundForNextMonth = false;
            if (allSpecialDates != null) {
                for (SpecialDate sd : allSpecialDates) {
                    LocalDate occurrence = sd.getNextOccurrence(nextMonthYearMonth.atDay(1));
                    if (occurrence != null && YearMonth.from(occurrence).equals(nextMonthYearMonth)) {
                        if (!foundForNextMonth) {
                            specialDatesInfo.append("The following special dates fall in ").append(monthName).append(":\n");
                            foundForNextMonth = true;
                        }
                        specialDatesInfo.append(String.format("- %s (%s): %s. Affects '%s' by +%.2f %s.\n",
                                sd.getName(),
                                occurrence.format(DateTimeFormatter.ISO_LOCAL_DATE),
                                sd.getDescription() != null ? sd.getDescription() : "No description",
                                sd.getAffectedCategory(),
                                sd.getAmountIncrease(),
                                currency
                        ));
                    }
                }
            }
            if (!foundForNextMonth) {
                specialDatesInfo.append("No user-defined special dates found for ").append(monthName).append(".\n");
            }
        } else {
             specialDatesInfo.append("Settings not available to load special dates.\n");
        }
        specialDatesInfo.append("\nTip: You can add/manage special dates in the Settings panel.");
        return specialDatesInfo.toString();
    }
}
