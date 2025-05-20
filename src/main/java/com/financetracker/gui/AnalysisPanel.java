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
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import com.financetracker.ai.AiAssistantService;
import com.financetracker.ai.CsvDataReader;
import com.financetracker.model.SavingGoal;
import com.financetracker.model.Settings;
import com.financetracker.model.SpecialDate;
import com.financetracker.model.Transaction;
import com.financetracker.service.BudgetAdjustmentService;
import com.financetracker.service.BudgetForecastService;
import com.financetracker.service.FinancialCycleService;
import com.financetracker.service.SettingsService;
import com.financetracker.service.SpecialDateService;
import com.financetracker.service.TransactionService;

/**
 * Panel for AI-assisted analysis.
 */
public class AnalysisPanel extends JPanel {

    private TransactionService transactionService;
    private AiAssistantService aiAssistantService;
    private SpecialDateService specialDateService;
    private BudgetAdjustmentService budgetAdjustmentService;
    private SettingsService settingsService;
    private FinancialCycleService financialCycleService;
    private BudgetForecastService budgetForecastService;
    private ActionListener panelNavigationListener;

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
     * @param transactionService      The transaction service.
     * @param settingsService         The settings service.
     * @param specialDateService      The special date service.
     * @param budgetAdjustmentService The budget adjustment service.
     * @param financialCycleService   The financial cycle service.
     * @param budgetForecastService   The budget forecast service.
     * @param panelNavigationListener Listener for panel navigation.
     */
    public AnalysisPanel(TransactionService transactionService, SettingsService settingsService,
            SpecialDateService specialDateService, BudgetAdjustmentService budgetAdjustmentService,
            FinancialCycleService financialCycleService, BudgetForecastService budgetForecastService,
            ActionListener panelNavigationListener) {
        this.transactionService = transactionService;
        this.settingsService = settingsService;
        this.specialDateService = specialDateService;
        this.budgetAdjustmentService = budgetAdjustmentService;
        this.financialCycleService = financialCycleService;
        this.budgetForecastService = budgetForecastService;
        this.panelNavigationListener = panelNavigationListener;
        this.aiAssistantService = new AiAssistantService(this.settingsService, this.financialCycleService);

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
        homeButton.setActionCommand("home");
        homeButton.addActionListener(this.panelNavigationListener);
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
        bottomAreaPanel.add(queryPanel, BorderLayout.SOUTH); // Query input at the very bottom

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
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        YearMonth nextMonthYearMonth = YearMonth.now().plusMonths(1);

        // A.4.1, A.4.2: Central panel for summary and category budget chart
        JPanel centralContentPanel = new JPanel();
        centralContentPanel.setLayout(new BoxLayout(centralContentPanel, BoxLayout.Y_AXIS));

        // Budget summary panel (existing)
        JPanel budgetSummaryPanel = new JPanel(new BorderLayout());
        budgetSummaryPanel.setBorder(BorderFactory.createTitledBorder("Budget Forecast & Adjustments"));

        this.budgetSummaryTextArea = new JTextArea();
        this.budgetSummaryTextArea.setEditable(false);
        this.budgetSummaryTextArea.setLineWrap(true);
        this.budgetSummaryTextArea.setWrapStyleWord(true);
        this.budgetSummaryTextArea.setFont(UIManager.getFont("Label.font"));
        this.budgetSummaryTextArea.setText(generateBudgetSummaryText(nextMonthYearMonth));
        JScrollPane budgetSummaryScrollPane = new JScrollPane(this.budgetSummaryTextArea);
        budgetSummaryPanel.add(budgetSummaryScrollPane, BorderLayout.CENTER);
        centralContentPanel.add(budgetSummaryPanel);
        centralContentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Category Budget Bar Chart Panel
        JPanel categoryBudgetChartPanel = new JPanel(new BorderLayout());
        categoryBudgetChartPanel.setBorder(BorderFactory.createTitledBorder("Next Month Category Budget Allocation"));
        this.categoryBudgetChartTextArea = new JTextArea();
        this.categoryBudgetChartTextArea.setEditable(false);
        this.categoryBudgetChartTextArea.setLineWrap(true);
        this.categoryBudgetChartTextArea.setWrapStyleWord(true);
        this.categoryBudgetChartTextArea.setFont(UIManager.getFont("Label.font"));
        this.categoryBudgetChartTextArea.setText(generateCategoryBudgetText(nextMonthYearMonth));
        JScrollPane categoryBudgetChartScrollPane = new JScrollPane(this.categoryBudgetChartTextArea);
        categoryBudgetChartPanel.add(categoryBudgetChartScrollPane, BorderLayout.CENTER);
        centralContentPanel.add(categoryBudgetChartPanel);

        panel.add(centralContentPanel, BorderLayout.CENTER);

        // Special Dates Display Panel as a sidebar (EAST)
        JPanel specialDatesDisplayPanel = new JPanel(new BorderLayout());
        specialDatesDisplayPanel.setBorder(BorderFactory.createTitledBorder("Upcoming Special Dates in "
                + nextMonthYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault())));
        specialDatesDisplayPanel.setPreferredSize(new Dimension(300, 0));
        this.specialDatesTextArea = new JTextArea();
        this.specialDatesTextArea.setEditable(false);
        this.specialDatesTextArea.setLineWrap(true);
        this.specialDatesTextArea.setWrapStyleWord(true);
        this.specialDatesTextArea.setFont(UIManager.getFont("Label.font"));
        this.specialDatesTextArea.setText(generateSpecialDatesText(nextMonthYearMonth));
        specialDatesDisplayPanel.add(new JScrollPane(this.specialDatesTextArea), BorderLayout.CENTER);
        panel.add(specialDatesDisplayPanel, BorderLayout.EAST);

        return panel;
    }

    /**
     * Creates the panel for displaying saving goals progress.
     * 
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
        // updateSavingGoalsProgressView(); // Call this when panel is shown or data is
        // expected to be fresh
        // Deferring initial call to when tab might be selected, or a refresh button is
        // clicked
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
            Component centerComponent = ((BorderLayout) savingGoalsProgressPanel.getLayout())
                    .getLayoutComponent(BorderLayout.CENTER);
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
                detailsPanel
                        .add(new JLabel(String.format("Monthly Contribution: %.2f", goal.getMonthlyContribution())));

                if (goal.getStartDate() != null) {
                    detailsPanel.add(
                            new JLabel("Start Date: " + goal.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE)));
                }
                if (goal.getTargetDate() != null) {
                    detailsPanel.add(new JLabel(
                            "Target Date: " + goal.getTargetDate().format(DateTimeFormatter.ISO_LOCAL_DATE)));
                } else {
                    // Estimate completion if possible
                    if (goal.getMonthlyContribution() > 0 && goal.getTargetAmount() > goal.getCurrentAmount()) {
                        double remainingAmount = goal.getTargetAmount() - goal.getCurrentAmount();
                        double monthsToTarget = Math.ceil(remainingAmount / goal.getMonthlyContribution());
                        LocalDate estimatedCompletionDate = goal.getStartDate() != null
                                ? goal.getStartDate().plusMonths((long) monthsToTarget)
                                : LocalDate.now().plusMonths((long) monthsToTarget);
                        detailsPanel.add(new JLabel(
                                "Est. Completion: " + estimatedCompletionDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                                        + " (~" + (int) monthsToTarget + " months)"));
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
     * @param year  年份
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
     * @param startDate    财务月开始日期
     * @param endDate      财务月结束日期
     */
    private void updateSummaryForFinancialMonth(List<Transaction> transactions, LocalDate startDate,
            LocalDate endDate) {
        StringBuilder summary = new StringBuilder();
        double totalIncome = transactionService.getTotalIncome(transactions);
        double totalExpense = transactionService.getTotalExpense(transactions);
        double netAmount = totalIncome - totalExpense;
        summary.append(String.format("总收入: %.2f\n", totalIncome));
        summary.append(String.format("总支出: %.2f\n", totalExpense));
        summary.append(String.format("净收支: %.2f\n", netAmount));

        // 显示当前月special day
        Settings settings = settingsService.getSettings();
        YearMonth currentMonth = YearMonth.from(startDate);
        boolean hasSpecialDay = false;
        if (settings != null && settings.getSpecialDates() != null) {
            for (SpecialDate sd : settings.getSpecialDates()) {
                LocalDate occ = sd.getNextOccurrence(currentMonth.atDay(1));
                if (occ != null && YearMonth.from(occ).equals(currentMonth)) {
                    if (!hasSpecialDay) {
                        summary.append("\n本月Special Day：\n");
                        hasSpecialDay = true;
                    }
                    summary.append(String.format("- %s: %s\n", sd.getName(), sd.getDescription() != null ? sd.getDescription() : "无"));
                }
            }
        }
        // 显示当前月saving goal
        boolean hasSavingGoal = false;
        if (settings != null && settings.getSavingGoals() != null) {
            for (SavingGoal goal : settings.getSavingGoals()) {
                if (goal.isActive() && !goal.isCompleted() && goal.getMonthlyContribution() > 0) {
                    LocalDate start = goal.getStartDate();
                    LocalDate end = goal.getTargetDate();
                    LocalDate monthDate = currentMonth.atDay(1);
                    if ((start == null || !monthDate.isBefore(start)) && (end == null || !monthDate.isAfter(end))) {
                        if (!hasSavingGoal) {
                            summary.append("\n本月Saving Goals：\n");
                            hasSavingGoal = true;
                        }
                        summary.append(String.format("- %s: %.2f\n", goal.getName(), goal.getMonthlyContribution()));
                    }
                }
            }
        }
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
        aiResponseTextArea.setText(isFinancialMode ? "正在分析您的财务数据和问题，请稍候..." : "正在处理您的问题，请稍候...");

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
        if (yearComboBox != null && monthNameComboBox != null) {
            System.out.println(
                    "AnalysisPanel.refreshCategoryList called. Current year/month selections will be used on next data refresh.");
        }
    }

    /**
     * Public method to refresh all data and views within the AnalysisPanel.
     * This should be called when underlying data (transactions, settings, special
     * dates, goals) might have changed.
     */
    public void refreshAllAnalysisData() {
        // Log or print that refresh is called
        System.out.println("AnalysisPanel: Refreshing all analysis data...");

        // 1. Refresh Current Month Tab (summary, category breakdown)
        // Determine which view to update based on mode (normal or financial)
        if (isFinancialMode) {
            // Assuming yearComboBox and monthNameComboBox hold the selected financial
            // period
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
            // For "normal" mode, it might be based on LocalDate.now() or a specific date if
            // that's a feature
            updateCurrentMonthView(); // Or updateCurrentMonthView(LocalDate.now());
        }

        // 2. Refresh Budget of Next Month Tab
        // This likely involves recalculating based on current transactions, settings,
        // and special dates.
        // Assuming createBudgetPanel() or a dedicated update method populates
        // budgetDetailsTextArea or similar components.
        // We might need a specific update method for the budget panel if
        // createBudgetPanel just builds static UI.
        // For now, let's assume re-populating the budget panel's core content is
        // needed.
        // If createBudgetPanel is lightweight and just populates text, we can call
        // parts of it or a new update method.
        updateBudgetPanelContents(); // Placeholder for the actual budget update logic

        // 3. Refresh Saving Goals Tab
        updateSavingGoalsProgressView();

        // Potentially clear AI Assistant responses if they depend on the refreshed data
        // context
        if (aiResponseTextArea != null) {
            // aiResponseTextArea.setText("Data has been refreshed. Please ask your query
            // again if needed.");
            // Or just leave it, as user might want to see previous response in old context.
        }

        System.out.println("AnalysisPanel: Refresh complete.");
    }

    // Placeholder for method to update budget panel contents. Needs to be
    // implemented.
    private void updateBudgetPanelContents() {
        if (budgetPanel == null || settingsService == null || budgetAdjustmentService == null) {
            return;
        }
        if (this.budgetSummaryTextArea != null) {
            this.budgetSummaryTextArea.setText(generateBudgetSummaryText(YearMonth.now().plusMonths(1)));
            this.budgetSummaryTextArea.setCaretPosition(0);
        } else {
            // 保留严重异常的处理
        }
        if (this.categoryBudgetChartTextArea != null) {
            this.categoryBudgetChartTextArea.setText(generateCategoryBudgetText(YearMonth.now().plusMonths(1)));
            this.categoryBudgetChartTextArea.setCaretPosition(0);
        } else {
            // 保留严重异常的处理
        }
    }

    /**
     * Generates the summary text for the budget panel.
     * 
     * @return String containing the budget summary.
     */
    private String generateBudgetSummaryText(YearMonth month) {
        // 直接调用预算分析服务，获取详细分析结果
        BudgetForecastService.BudgetAnalysisResult result = budgetForecastService.analyzeNextMonthBudget();
        StringBuilder sb = new StringBuilder();
        sb.append("预算预测与调整（下月）\n\n");
        sb.append("基于过去3个月的交易数据分析:\n");
        sb.append(String.format("- 平均月收入: %.2f\n", result.avgIncome));
        sb.append(String.format("- 平均月支出: %.2f\n", result.avgExpense));
        sb.append(String.format("- 平均月结余: %.2f\n\n", result.avgBalance));
        sb.append(String.format("基础预算（线性回归）：%.2f\n\n", result.baseBudget));
        sb.append("--- Special Day 预算调整明细 ---\n");
        if (!result.specialDayDetails.isEmpty()) {
            for (BudgetForecastService.SpecialDayDetail detail : result.specialDayDetails) {
                sb.append(String.format("- %s（%s）: +%.2f\n", detail.name != null ? detail.name : detail.category, detail.category, detail.amount));
            }
            sb.append(String.format("Special Day 增加总额: %.2f\n", result.specialDayTotal));
        } else {
            sb.append("下月无特殊日期预算调整\n");
        }
        sb.append("\n--- Saving Goal 预算调整明细 ---\n");
        if (!result.savingGoalDetails.isEmpty()) {
            for (BudgetForecastService.SavingGoalDetail detail : result.savingGoalDetails) {
                sb.append(String.format("- %s: +%.2f\n", detail.name, detail.monthlyContribution));
            }
            sb.append(String.format("Saving Goal 增加总额: %.2f\n", result.savingGoalTotal));
        } else {
            sb.append("下月无活跃的储蓄目标\n");
        }
        sb.append("\n最终预算 = 基础预算 + Special Day 汇总 + Saving Goal 汇总\n");
        sb.append(String.format("最终预算: %.2f\n", result.finalBudget));
        return sb.toString();
    }

    /**
     * Generates the category budget text (text-based bar chart) for the budget
     * panel.
     * 
     * @return String containing the category budget allocation.
     */
    private String generateCategoryBudgetText(YearMonth month) {
        // 获取预算分析结果
        BudgetForecastService.BudgetAnalysisResult result = budgetForecastService.analyzeNextMonthBudget();
        Settings currentSettings = settingsService.getSettings();
        String currency = currentSettings != null && currentSettings.getDefaultCurrency() != null ? currentSettings.getDefaultCurrency() : "";
        StringBuilder chartBuilder = new StringBuilder();
        chartBuilder.append(String.format("下月各类别预算分配（基础预算=%.2f，special day已加到对应类别）\n\n", result.baseBudget));
        // 统计所有类别
        List<String> allCategories = new ArrayList<>(result.categoryAllocations.keySet());
        if (allCategories.isEmpty() && currentSettings != null && currentSettings.getExpenseCategories() != null) {
            allCategories.addAll(currentSettings.getExpenseCategories());
        }
        double maxCategoryBudget = 0;
        for (String cat : allCategories) {
            double val = result.categoryAllocations.getOrDefault(cat, 0.0);
            if (val > maxCategoryBudget) maxCategoryBudget = val;
        }
        if (maxCategoryBudget == 0) maxCategoryBudget = 1;
        for (String cat : allCategories) {
            double val = result.categoryAllocations.getOrDefault(cat, 0.0);
            chartBuilder.append(String.format("%-18s: ", cat));
            int barLen = (int) ((val / maxCategoryBudget) * 30);
            for (int i = 0; i < barLen; i++) chartBuilder.append("*");
            chartBuilder.append(String.format(" %.2f %s\n", val, currency));
        }
        chartBuilder.append("\n--- Saving Goal 预算单独汇总 ---\n");
        if (!result.savingGoalDetails.isEmpty()) {
            for (BudgetForecastService.SavingGoalDetail detail : result.savingGoalDetails) {
                chartBuilder.append(String.format("- %s: +%.2f\n", detail.name, detail.monthlyContribution));
            }
            chartBuilder.append(String.format("Saving Goal 增加总额: %.2f\n", result.savingGoalTotal));
        } else {
            chartBuilder.append("下月无活跃的储蓄目标\n");
        }
        return chartBuilder.toString();
    }

    /**
     * Generates the special dates information text for the budget panel.
     * 
     * @return String containing upcoming special dates information.
     */
    private String generateSpecialDatesText(YearMonth month) {
        StringBuilder specialDatesInfo = new StringBuilder();
        Settings currentSettings = settingsService.getSettings();
        YearMonth nextMonthYearMonth = YearMonth.now().plusMonths(1);
        String monthName = nextMonthYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
        if (currentSettings != null) {
            List<SpecialDate> allSpecialDates = currentSettings.getSpecialDates();
            boolean foundForNextMonth = false;
            if (allSpecialDates != null) {
                for (SpecialDate sd : allSpecialDates) {
                    LocalDate occurrence = sd.getNextOccurrence(nextMonthYearMonth.atDay(1));
                    if (occurrence != null && YearMonth.from(occurrence).equals(nextMonthYearMonth)) {
                        if (!foundForNextMonth) {
                            specialDatesInfo.append("下月的Special Day如下：\n");
                            foundForNextMonth = true;
                        }
                        specialDatesInfo.append(String.format("- %s (%s)\n  描述: %s\n", sd.getName(), occurrence.format(DateTimeFormatter.ISO_LOCAL_DATE), sd.getDescription() != null ? sd.getDescription() : "无"));
                    }
                }
            }
            if (!foundForNextMonth) {
                specialDatesInfo.append(String.format("下月（%s）没有设置Special Day。\n", monthName));
            }
        } else {
            specialDatesInfo.append("无法获取设置，无法加载special day。\n");
        }
        return specialDatesInfo.toString();
    }

    public boolean isThemeable() {
        return true;
    }

    public void applyTheme(Settings settings) {
        boolean isDark = settings.isDarkModeEnabled();
        Color backgroundColor = isDark ? new Color(45, 45, 45) : UIManager.getColor("Panel.background");
        Color foregroundColor = isDark ? Color.LIGHT_GRAY : UIManager.getColor("Label.foreground");
        Color textAreaBg = isDark ? new Color(50, 50, 50) : UIManager.getColor("TextArea.background");
        Color textAreaFg = isDark ? Color.WHITE : UIManager.getColor("TextArea.foreground");

        this.setBackground(backgroundColor);

        // Theme direct child panels if they are simple JPanels
        if (currentMonthPanel != null)
            currentMonthPanel.setBackground(backgroundColor);
        if (aiAssistantPanel != null)
            aiAssistantPanel.setBackground(backgroundColor);
        if (budgetPanel != null)
            budgetPanel.setBackground(backgroundColor);
        if (savingGoalsProgressPanel != null)
            savingGoalsProgressPanel.setBackground(backgroundColor);

        // Theme text areas (example)
        Component[] components = this.getComponents();
        for (Component component : components) {
            if (component instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) component;
                scrollPane.getViewport().setBackground(backgroundColor);
                Component view = scrollPane.getViewport().getView();
                if (view instanceof JTextArea) {
                    view.setBackground(textAreaBg);
                    view.setForeground(textAreaFg);
                }
            } else if (component instanceof JTabbedPane) {
                JTabbedPane DUMMY_VARIABLE_NAME = (JTabbedPane) component; // TODO: Remove DUMMY_VARIABLE_NAME
                // Theme tabbed pane (more complex, might need to iterate through tabs)
                // For now, just its direct background
                DUMMY_VARIABLE_NAME.setBackground(backgroundColor);
                DUMMY_VARIABLE_NAME.setForeground(foregroundColor);
                for (int i = 0; i < DUMMY_VARIABLE_NAME.getTabCount(); i++) {
                    Component tabComponent = DUMMY_VARIABLE_NAME.getComponentAt(i);
                    if (tabComponent != null) {
                        tabComponent.setBackground(backgroundColor); // Theme content of each tab
                        // Recursively theme components within the tab if necessary
                        // For now, this sets the background of the direct child of the tab.
                    }
                }
            }
            // Add more specific component theming as needed
        }

        // Theme specific components if not covered by iteration
        if (summaryTextArea != null) {
            summaryTextArea.setBackground(textAreaBg);
            summaryTextArea.setForeground(textAreaFg);
        }
        if (categoryBreakdownTextArea != null) {
            categoryBreakdownTextArea.setBackground(textAreaBg);
            categoryBreakdownTextArea.setForeground(textAreaFg);
        }
        if (aiResponseTextArea != null) {
            aiResponseTextArea.setBackground(textAreaBg);
            aiResponseTextArea.setForeground(textAreaFg);
        }
        if (aiQueryField != null) {
            aiQueryField.setBackground(isDark ? new Color(60, 60, 60) : UIManager.getColor("TextField.background"));
            aiQueryField.setForeground(foregroundColor);
        }
        if (budgetSummaryTextArea != null) {
            budgetSummaryTextArea.setBackground(textAreaBg);
            budgetSummaryTextArea.setForeground(textAreaFg);
        }
        if (categoryBudgetChartTextArea != null) {
            categoryBudgetChartTextArea.setBackground(textAreaBg);
            categoryBudgetChartTextArea.setForeground(textAreaFg);
        }
        if (specialDatesTextArea != null) {
            specialDatesTextArea.setBackground(textAreaBg);
            specialDatesTextArea.setForeground(textAreaFg);
        }

        // Potentially re-apply to child components explicitly if UIManager changes are
        // not picked up
        SwingUtilities.updateComponentTreeUI(this);
        this.repaint();
    }
}
