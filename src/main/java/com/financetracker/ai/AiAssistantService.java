package com.financetracker.ai;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.financetracker.model.Settings;
import com.financetracker.model.Transaction;
import com.financetracker.service.BudgetAdjustmentService;
import com.financetracker.service.FinancialCycleService;
import com.financetracker.service.SettingsService;
import com.financetracker.service.TransactionService;

/**
 * 提供AI辅助分析功能的服务类
 */
public class AiAssistantService {
    
    private final DeepSeekAiService aiService;
    private final SettingsService settingsService;
    private final BudgetAdjustmentService budgetAdjustmentService;
    private final FinancialCycleService financialCycleService;
    
    public AiAssistantService(SettingsService settingsService, FinancialCycleService financialCycleService) {
        this.aiService = new DeepSeekAiService();
        this.settingsService = settingsService;
        this.budgetAdjustmentService = new BudgetAdjustmentService(settingsService);
        this.financialCycleService = financialCycleService;
    }
    
    /**
     * 获取对用户查询的回应
     * 
     * @param query 用户查询
     * @param transactionService 交易服务
     * @return AI的回应
     */
    public String getResponse(String query, TransactionService transactionService) {
        try {
            // 检查AI服务是否可用
            if (!aiService.isServiceAvailable()) {
                return "AI服务暂时不可用，请确保已正确配置API密钥和网络连接。";
            }
            
            // 获取交易数据
            List<Transaction> allTransactions = transactionService.getAllTransactions();
            List<Transaction> currentMonthTransactions = transactionService.getTransactionsForCurrentMonth();
            
            // 构建上下文信息
            StringBuilder context = new StringBuilder();
            context.append("Here is a summary of the current financial data:\\n\\n");
            
            // 添加当前月份的收支情况
            double currentMonthIncome = transactionService.getTotalIncome(currentMonthTransactions);
            double currentMonthExpense = transactionService.getTotalExpense(currentMonthTransactions);
            double currentMonthBalance = currentMonthIncome - currentMonthExpense;
            
            YearMonth currentMonth = YearMonth.now();
            context.append(String.format("Current month (%s) income/expense:\\n", currentMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))));
            context.append(String.format("- Total Income: %.2f\\n", currentMonthIncome));
            context.append(String.format("- Total Expense: %.2f\\n", currentMonthExpense));
            context.append(String.format("- Balance: %.2f\\n\\n", currentMonthBalance));
            
            // 添加按类别统计
            Map<String, Double> categoryExpenses = new HashMap<>();
            for (Transaction transaction : currentMonthTransactions) {
                if (transaction.isExpense()) {
                    String category = transaction.getCategory();
                    double amount = transaction.getAmount();
                    categoryExpenses.put(category, categoryExpenses.getOrDefault(category, 0.0) + amount);
                }
            }
            
            // 按支出金额排序类别
            List<Map.Entry<String, Double>> sortedCategories = categoryExpenses.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .collect(Collectors.toList());
            
            if (!sortedCategories.isEmpty()) {
                context.append("Expense breakdown by category:\\n");
                for (Map.Entry<String, Double> entry : sortedCategories) {
                    String category = entry.getKey();
                    double amount = entry.getValue();
                    double percentage = (currentMonthExpense > 0) ? (amount / currentMonthExpense) * 100 : 0; // Avoid division by zero
                    context.append(String.format("- %s: %.2f (%.1f%%)\\n", category, amount, percentage));
                }
                context.append("\\n");
            }
            
            // 组装用户查询与上下文
            StringBuilder fullQuery = new StringBuilder();
            fullQuery.append(context);
            fullQuery.append("User's question is: ").append(query);
            fullQuery.append("\\n\\nPlease provide a professional, specific, and helpful answer to the user's question based on the financial data above.");
            
            // 调用AI服务
            return aiService.chat(fullQuery.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return "抱歉，处理您的请求时遇到了错误：" + e.getMessage() + "\n请稍后再试或联系支持团队。";
        }
    }
    
    /**
     * 获取对用户查询的回应（流式）
     * 
     * @param query 用户查询
     * @param transactionService 交易服务
     * @param messageConsumer 消息处理回调
     */
    public void getResponseStream(String query, TransactionService transactionService, Consumer<String> messageConsumer) {
        try {
            // 检查AI服务是否可用
            if (!aiService.isServiceAvailable()) {
                messageConsumer.accept("AI服务暂时不可用，请确保已正确配置API密钥和网络连接。");
                return;
            }
            
            // 获取交易数据
            List<Transaction> allTransactions = transactionService.getAllTransactions();
            List<Transaction> currentMonthTransactions = transactionService.getTransactionsForCurrentMonth();
            
            // 构建上下文信息
            StringBuilder context = new StringBuilder();
            context.append("Here is a summary of the current financial data:\\n\\n");
            
            // 添加当前月份的收支情况
            double currentMonthIncome = transactionService.getTotalIncome(currentMonthTransactions);
            double currentMonthExpense = transactionService.getTotalExpense(currentMonthTransactions);
            double currentMonthBalance = currentMonthIncome - currentMonthExpense;
            
            YearMonth currentMonth = YearMonth.now();
            context.append(String.format("Current month (%s) income/expense:\\n", currentMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))));
            context.append(String.format("- Total Income: %.2f\\n", currentMonthIncome));
            context.append(String.format("- Total Expense: %.2f\\n", currentMonthExpense));
            context.append(String.format("- Balance: %.2f\\n\\n", currentMonthBalance));
            
            // 添加按类别统计
            Map<String, Double> categoryExpenses = new HashMap<>();
            for (Transaction transaction : currentMonthTransactions) {
                if (transaction.isExpense()) {
                    String category = transaction.getCategory();
                    double amount = transaction.getAmount();
                    categoryExpenses.put(category, categoryExpenses.getOrDefault(category, 0.0) + amount);
                }
            }
            
            // 按支出金额排序类别
            List<Map.Entry<String, Double>> sortedCategories = categoryExpenses.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .collect(Collectors.toList());
            
            if (!sortedCategories.isEmpty()) {
                context.append("Expense breakdown by category:\\n");
                for (Map.Entry<String, Double> entry : sortedCategories) {
                    String category = entry.getKey();
                    double amount = entry.getValue();
                    double percentage = (currentMonthExpense > 0) ? (amount / currentMonthExpense) * 100 : 0; // Avoid division by zero
                    context.append(String.format("- %s: %.2f (%.1f%%)\\n", category, amount, percentage));
                }
                context.append("\\n");
            }
            
            // 组装用户查询与上下文
            StringBuilder fullQuery = new StringBuilder();
            fullQuery.append(context);
            fullQuery.append("User's question is: ").append(query);
            fullQuery.append("\\n\\nPlease provide a professional, specific, and helpful answer to the user's question based on the financial data above.");
            
            // 调用AI服务（流式）
            aiService.chatStream(fullQuery.toString(), messageConsumer);
        } catch (Exception e) {
            e.printStackTrace();
            messageConsumer.accept("抱歉，处理您的请求时遇到了错误：" + e.getMessage() + "\n请稍后再试或联系支持团队。");
        }
    }
    
    /**
     * 分析单条交易记录，判断其类别和类型
     * 
     * @param description 交易描述
     * @param amount 交易金额
     * @return 包含类别和交易类型的Map
     */
    public Map<String, Object> analyzeTransaction(String description, double amount) {
        Map<String, Object> result = new HashMap<>();
        
        // 根据历史交易记录进行分析 (当前未使用)
        // List<Transaction> allTransactions = CsvDataReader.readAllTransactions();
        
        // 默认分类和类型
        String category = "Others";
        boolean isExpense = amount >= 0;
        
        // 简单的关键词匹配分析
        String lowerDesc = description.toLowerCase();
        
        // 优先判断是否为收入 (通常收入类别更明确)
        if (lowerDesc.contains("salary") || lowerDesc.contains("payroll")) {
            category = "Salary";
            isExpense = false;
        } else if (lowerDesc.contains("bonus") || lowerDesc.contains("award")) {
            category = "Bonus";
            isExpense = false;
        } else if (lowerDesc.contains("invest") || lowerDesc.contains("stock") || lowerDesc.contains("fund") || lowerDesc.contains("financial")) {
            category = "Investment";
            isExpense = false;
        } else if (lowerDesc.contains("refund") || lowerDesc.contains("reimbursement")) {
            category = "Refund";
            isExpense = false;
        } else if (lowerDesc.contains("interest")) {
            category = "Interest";
            isExpense = false;
        } else if (lowerDesc.contains("gift")) {
            category = "Gift";
            isExpense = false;
        } else if (lowerDesc.contains("freelance")) {
            category = "Freelance/Part-time";
            isExpense = false;
        } 
        // 如果不是明确的收入，再判断支出类别
        // (如果上面已经判断为 isExpense = false, 则这里的判断不会覆盖)
        else if (lowerDesc.contains("food") || lowerDesc.contains("eat") || lowerDesc.contains("meal") || lowerDesc.contains("dining") ||
            lowerDesc.contains("restaurant") || lowerDesc.contains("grocery")) {
            category = "Food";
            isExpense = true;
        } else if (lowerDesc.contains("traffic") || lowerDesc.contains("bus") || lowerDesc.contains("subway") || lowerDesc.contains("taxi") || 
                   lowerDesc.contains("gas") || lowerDesc.contains("fuel") || lowerDesc.contains("parking")) {
            category = "Transportation";
            isExpense = true;
        } else if (lowerDesc.contains("rent") || lowerDesc.contains("housing") || lowerDesc.contains("utility") || lowerDesc.contains("mortgage")) {
            category = "Housing"; // May include some utilities, or separate Utilities later if needed
            isExpense = true;
        } else if (lowerDesc.contains("clothing") || lowerDesc.contains("apparel") || lowerDesc.contains("shoe")) {
            category = "Clothing";
            isExpense = true;
        } else if (lowerDesc.contains("entertainment") || lowerDesc.contains("movie") || lowerDesc.contains("game") ||
                   lowerDesc.contains("hobby")) {
            category = "Entertainment";
            isExpense = true;
        } else if (lowerDesc.contains("shop") || lowerDesc.contains("buy") ||
                 lowerDesc.contains("purchase") || lowerDesc.contains("store")) {
            category = "Shopping";
            isExpense = true;
        } else if (lowerDesc.contains("medical") || lowerDesc.contains("health") || lowerDesc.contains("doctor") || lowerDesc.contains("hospital") || lowerDesc.contains("pharmacy")) {
            category = "Healthcare";
            isExpense = true;
        } else if (lowerDesc.contains("education") || lowerDesc.contains("school") || lowerDesc.contains("course") || lowerDesc.contains("book")) {
            category = "Education";
            isExpense = true;
        } else {
            // If no specific income or expense category matched, and isExpense wasn't set to false by an income match,
            // assume it's an expense and categorize as "Others".
            // If amount was used for initial isExpense detection, this might need adjustment.
            // For now, if not an income, it's an expense.
            if (isExpense) { // If not already set to false (income)
                 category = "Others";
            } else {
                // It was flagged as income by some other means but didn't match a specific income category
                category = "Other Income";
            }
        }
        
        result.put("category", category);
        result.put("isExpense", isExpense);
        
        return result;
    }
    
    /**
     * 获取当前月份的消费总览
     * 
     * @return 消费总览信息
     */
    public Map<String, Object> getCurrentMonthOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        // 获取当前月份的交易记录
        List<Transaction> currentMonthTransactions = CsvDataReader.getCurrentMonthTransactions();
        
        // 计算总收入和总支出
        double totalIncome = 0;
        double totalExpense = 0;
        
        for (Transaction transaction : currentMonthTransactions) {
            if (transaction.isExpense()) {
                totalExpense += transaction.getAmount();
            } else {
                totalIncome += transaction.getAmount();
            }
        }
        
        // 计算净收入
        double netIncome = totalIncome - totalExpense;
        
        // 设置结果
        LocalDate now = LocalDate.now();
        YearMonth yearMonth = YearMonth.of(now.getYear(), now.getMonth());
        
        overview.put("year", now.getYear());
        overview.put("month", now.getMonthValue());
        overview.put("monthName", now.getMonth().toString());
        overview.put("daysInMonth", yearMonth.lengthOfMonth());
        overview.put("currentDay", now.getDayOfMonth());
        
        overview.put("totalIncome", totalIncome);
        overview.put("totalExpense", totalExpense);
        overview.put("netIncome", netIncome);
        overview.put("transactionCount", currentMonthTransactions.size());
        
        // 计算每个类别的支出
        Map<String, Double> expenseByCategory = new HashMap<>();
        for (Transaction transaction : currentMonthTransactions) {
            if (transaction.isExpense()) {
                String category = transaction.getCategory();
                double currentAmount = expenseByCategory.getOrDefault(category, 0.0);
                expenseByCategory.put(category, currentAmount + transaction.getAmount());
            }
        }
        
        overview.put("expenseByCategory", expenseByCategory);
        
        return overview;
    }
    
    /**
     * 提供基础的财务建议
     * 
     * @return 财务建议信息
     */
    public Map<String, Object> getFinancialAdvice() {
        Map<String, Object> advice = new HashMap<>();
        
        // 获取当前月份概览
        Map<String, Object> overview = getCurrentMonthOverview();
        double totalIncome = (double) overview.get("totalIncome");
        double totalExpense = (double) overview.get("totalExpense");
        double netIncome = (double) overview.get("netIncome");
        
        // 计算支出占收入的比例
        double expenseRatio = totalIncome > 0 ? (totalExpense / totalIncome) * 100 : 0;
        
        // 基于支出比例的建议
        String budgetAdvice;
        if (expenseRatio > 90) {
            budgetAdvice = "您的支出占收入的比例过高，建议控制支出，避免财务压力。";
        } else if (expenseRatio > 70) {
            budgetAdvice = "您的支出占收入的比例偏高，可以考虑适当减少一些非必要支出。";
        } else if (expenseRatio > 50) {
            budgetAdvice = "您的支出占收入的比例适中，请继续保持良好的财务习惯。";
        } else {
            budgetAdvice = "您的支出占收入的比例较低，有较好的储蓄能力，可以考虑进行一些投资。";
        }
        
        advice.put("expenseRatio", expenseRatio);
        advice.put("budgetAdvice", budgetAdvice);
        
        // 添加类别建议
        @SuppressWarnings("unchecked")
        Map<String, Double> expenseByCategory = (Map<String, Double>) overview.get("expenseByCategory");
        if (expenseByCategory != null) {
            // 找出支出最高的类别
            String highestCategory = "";
            double highestAmount = 0;
            
            for (Map.Entry<String, Double> entry : expenseByCategory.entrySet()) {
                if (entry.getValue() > highestAmount) {
                    highestAmount = entry.getValue();
                    highestCategory = entry.getKey();
                }
            }
            
            if (!highestCategory.isEmpty()) {
                String categoryAdvice = "您在 " + highestCategory + " 类别的支出最高，占总支出的 " 
                    + String.format("%.2f", (highestAmount / totalExpense) * 100) + "%。";
                
                if (highestAmount / totalExpense > 0.5) {
                    categoryAdvice += "建议关注这一类别的支出，可能有节省空间。";
                }
                
                advice.put("highestCategory", highestCategory);
                advice.put("highestCategoryAmount", highestAmount);
                advice.put("categoryAdvice", categoryAdvice);
            }
        }
        
        return advice;
    }
    
    /**
     * 检查服务是否可用
     * 
     * @return 服务状态
     */
    public boolean isServiceAvailable() {
        // 检查CSV数据库文件是否存在
        return CsvDataReader.databaseExists();
    }
    
    /**
     * 分析当前月份交易并生成报告
     * 
     * @param transactionService 交易服务
     * @return 分析报告文本
     */
    public String generateCurrentMonthAnalysis(TransactionService transactionService) {
        // Process monthly savings for the current month first
        Settings settings = settingsService.getSettings();
        if (settings != null) {
            YearMonth currentMonthToProcess = YearMonth.now();
            int savingsTransactionDay = settings.getBudgetStartDay() > 0 ? settings.getBudgetStartDay() : 1; // Default to 1 if not set
            // It's important that TransactionService is capable of being called multiple times for the same month
            // without creating duplicate transactions, or we need a flag here.
            // For now, we assume it handles this or it's acceptable for it to run once per session/major action.
            boolean savingsProcessed = financialCycleService.processMonthlySavingsContributions(currentMonthToProcess, savingsTransactionDay, "Savings");
            if (!savingsProcessed) {
                // Log or handle the error, though the method itself logs errors.
                System.err.println("AiAssistantService: Processing monthly savings contributions might have failed for " + currentMonthToProcess);
            }
        } else {
            System.err.println("AiAssistantService: Settings are null, cannot process monthly savings.");
        }

        List<Transaction> currentMonthTransactions = transactionService.getTransactionsForCurrentMonth();
        
        // 准备数据
        StringBuilder data = new StringBuilder();
        YearMonth currentMonth = YearMonth.now();
        double totalIncome = transactionService.getTotalIncome(currentMonthTransactions);
        double totalExpense = transactionService.getTotalExpense(currentMonthTransactions);
        
        data.append(String.format("Current month: %s\n", currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))));
        data.append(String.format("Total Income: %.2f %s\n", totalIncome, settings != null ? settings.getDefaultCurrency() : ""));
        data.append(String.format("Total Expenses: %.2f %s\n", totalExpense, settings != null ? settings.getDefaultCurrency() : ""));
        data.append(String.format("Net Balance: %.2f %s\n\n", (totalIncome - totalExpense), settings != null ? settings.getDefaultCurrency() : ""));
        
        // Expense breakdown by category
        Map<String, Double> expensesByCategory = currentMonthTransactions.stream()
            .filter(Transaction::isExpense)
            .collect(Collectors.groupingBy(Transaction::getCategory, Collectors.summingDouble(Transaction::getAmount)));

        if (!expensesByCategory.isEmpty()) {
            data.append("Expense Breakdown:\n");
            expensesByCategory.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(entry -> {
                    double percentage = (totalExpense > 0) ? (entry.getValue() / totalExpense) * 100 : 0;
                    data.append(String.format("- %s: %.2f %s (%.1f%%)\n", 
                        entry.getKey(), entry.getValue(), settings != null ? settings.getDefaultCurrency() : "", percentage));
                });
            data.append("\n");
        }
        
        // 使用AI服务生成分析报告
        return aiService.generateMonthlyAnalysisReport(data.toString());
    }
    
    /**
     * 基于历史数据预测下个月预算
     * 
     * @param transactionService 交易服务
     * @return 预算建议文本
     */
    public String generateNextMonthBudget(TransactionService transactionService) {
        // Process monthly savings for the current month first, to ensure its impact is considered if relevant for next month's planning context
        Settings settings = settingsService.getSettings(); // Re-fetch settings, though it should be the same instance as above if called in same session
        if (settings != null) {
            YearMonth currentMonthToProcess = YearMonth.now(); // Process for current month
            int savingsTransactionDay = settings.getBudgetStartDay() > 0 ? settings.getBudgetStartDay() : 1; // Default to 1 if not set
            boolean savingsProcessed = financialCycleService.processMonthlySavingsContributions(currentMonthToProcess, savingsTransactionDay, "Savings");
            if (!savingsProcessed) {
                System.err.println("AiAssistantService: Processing monthly savings contributions might have failed for " + currentMonthToProcess + " before generating next month budget.");
            }
        }
         else {
            System.err.println("AiAssistantService: Settings are null, cannot process monthly savings for next month budget context.");
        }

        List<Transaction> currentMonthTransactions = transactionService.getTransactionsForCurrentMonth();
        // Settings settings = settingsService.getSettings(); // Already fetched above

        // 准备数据
        StringBuilder data = new StringBuilder();
        YearMonth currentMonth = YearMonth.now();
        YearMonth nextMonth = currentMonth.plusMonths(1);
        
        double totalIncomeCurrentMonth = transactionService.getTotalIncome(currentMonthTransactions);
        double totalExpenseCurrentMonth = transactionService.getTotalExpense(currentMonthTransactions);
        
        data.append(String.format("Current month: %s\n", currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))));
        data.append(String.format("Next month: %s\n", nextMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))));
        
        if (settings != null) {
            data.append(String.format("Base Monthly Budget: %.2f %s\n", settings.getMonthlyBudget(), settings.getDefaultCurrency()));
        } else {
            data.append("Base Monthly Budget: Not set\n");
        }

        data.append(String.format("This month's Total Income: %.2f\n", totalIncomeCurrentMonth));
        data.append(String.format("This month's Total Expenses: %.2f\n", totalExpenseCurrentMonth));
        data.append(String.format("This month's Net Balance: %.2f\n\n", totalIncomeCurrentMonth - totalExpenseCurrentMonth));
        
        // 按类别统计支出 (基于当前月数据作为预测基础)
        Map<String, Double> categoryExpensesPrediction = new HashMap<>();
        // A more sophisticated prediction might use average of past N months, or trend analysis.
        // For now, using current month's expenses as a simple base.
        for (Transaction transaction : currentMonthTransactions) {
            if (transaction.isExpense()) {
                String category = transaction.getCategory();
                double amount = transaction.getAmount();
                categoryExpensesPrediction.put(category, categoryExpensesPrediction.getOrDefault(category, 0.0) + amount);
            }
        }
        
        // 获取并应用特殊日期的预算调整
        Map<String, Double> specialDateAdjustments = budgetAdjustmentService.getCategoryAdjustmentsForMonth(nextMonth);
        if (!specialDateAdjustments.isEmpty()) {
            data.append(String.format("Next month (%s) Special Date Adjustments:\n", nextMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))));
            for (Map.Entry<String, Double> adjustmentEntry : specialDateAdjustments.entrySet()) {
                String category = adjustmentEntry.getKey();
                double adjustmentAmount = adjustmentEntry.getValue();
                categoryExpensesPrediction.merge(category, adjustmentAmount, Double::sum); // Add adjustment to predicted expense
                data.append(String.format("- %s: Predicted Increase %.2f %s\n", category, adjustmentAmount, settings != null ? settings.getDefaultCurrency() : ""));
            }
            data.append("\n");
        }
        
        // 按预测支出金额排序类别
        // List<Map.Entry<String, Double>> sortedExpenses = categoryExpenses.entrySet().stream() // Old: was current month's actual
        List<Map.Entry<String, Double>> sortedPredictedExpenses = categoryExpensesPrediction.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toList());
        
        if (!sortedPredictedExpenses.isEmpty()) {
            data.append(String.format("Next month (%s) Predicted Expense Breakdown (Based on Current Month and Special Date Adjustments):\n", nextMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))));
            double predictedTotalExpense = sortedPredictedExpenses.stream().mapToDouble(Map.Entry::getValue).sum();
            for (Map.Entry<String, Double> entry : sortedPredictedExpenses) {
                data.append(String.format("- %s: %.2f %s (Predicted Expense Percentage %.1f%%)\n", 
                        entry.getKey(), 
                        entry.getValue(), 
                        settings != null ? settings.getDefaultCurrency() : "",
                        predictedTotalExpense > 0 ? (entry.getValue() / predictedTotalExpense * 100) : 0.0));
            }
             data.append(String.format("Predicted Total Expenses: %.2f %s\n", predictedTotalExpense, settings != null ? settings.getDefaultCurrency() : ""));
            data.append("\n");
        } else if (specialDateAdjustments.isEmpty()) { // only if no other predictions were made
             data.append("Next month Predicted Expense Breakdown: Insufficient Data or No Special Date Adjustments.\n\n");
        }
        
        // 使用AI服务生成下月预算建议
        // The prompt for the AI should clearly state that these are PREDICTIONS and ADJUSTMENTS
        // and ask for advice based on this, perhaps comparing to the set monthlyBudget.
        String promptPrefix = String.format(
            "You are a financial advisor. Please generate a budget suggestion report based on the following financial prediction and budget adjustment information for %s.\n" +
            "The report should include an evaluation of each predicted expense, comparison with the set Monthly Budget Baseline (%.2f %s) if set, and suggestions on how to optimize the budget.\n\n",
            nextMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            settings != null ? settings.getMonthlyBudget() : 0.0,
            settings != null ? settings.getDefaultCurrency() : "N/A"
        );
        return aiService.generateBudgetSuggestions(promptPrefix + data.toString());
    }
    
    /**
     * 流式生成月度分析报告
     * 
     * @param transactionService 交易服务
     * @param messageConsumer 消息处理回调
     */
    public void generateCurrentMonthAnalysisStream(TransactionService transactionService, Consumer<String> messageConsumer) {
        // Process monthly savings for the current month first
        Settings settings = settingsService.getSettings();
        if (settings != null) {
            YearMonth currentMonthToProcess = YearMonth.now();
            int savingsTransactionDay = settings.getBudgetStartDay() > 0 ? settings.getBudgetStartDay() : 1;
            boolean savingsProcessed = financialCycleService.processMonthlySavingsContributions(currentMonthToProcess, savingsTransactionDay, "Savings");
            if (!savingsProcessed) {
                 messageConsumer.accept("[Warning] Error processing current month savings goal. Analysis may not include latest savings transactions.\n");
            }
        } else {
            messageConsumer.accept("[Warning] Settings information is empty, cannot process current month savings goal.\n");
        }

        List<Transaction> currentMonthTransactions = transactionService.getTransactionsForCurrentMonth();
        
        // 准备数据（与非流式方法相同）
        StringBuilder data = new StringBuilder();
        YearMonth currentMonth = YearMonth.now();
        double totalIncome = transactionService.getTotalIncome(currentMonthTransactions);
        double totalExpense = transactionService.getTotalExpense(currentMonthTransactions);
        
        data.append(String.format("Current month: %s\n", currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))));
        data.append(String.format("Total Income: %.2f\n", totalIncome));
        data.append(String.format("Total Expenses: %.2f\n", totalExpense));
        data.append(String.format("Net Balance: %.2f\n\n", totalIncome - totalExpense));
        
        // 按类别统计支出
        Map<String, Double> categoryExpenses = new HashMap<>();
        for (Transaction transaction : currentMonthTransactions) {
            if (transaction.isExpense()) {
                String category = transaction.getCategory();
                double amount = transaction.getAmount();
                categoryExpenses.put(category, categoryExpenses.getOrDefault(category, 0.0) + amount);
            }
        }
        
        // 按支出金额排序类别
        List<Map.Entry<String, Double>> sortedExpenses = categoryExpenses.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toList());
        
        if (!sortedExpenses.isEmpty()) {
            data.append("Expense Breakdown:\n");
            for (Map.Entry<String, Double> entry : sortedExpenses) {
                data.append(String.format("- %s: %.2f (%.1f%%)\n", 
                        entry.getKey(), entry.getValue(), entry.getValue() / totalExpense * 100));
            }
            data.append("\n");
        }
        
        // 使用AI服务流式生成分析报告
        String prompt = "You are a professional financial analyst. Please generate a detailed monthly analysis report based on the following financial data, including income/expense analysis, consumption trend analysis, and financial health assessment:\n\n" 
                + data.toString();
                
        aiService.chatStream(prompt, messageConsumer);
    }
    
    /**
     * 与AI进行普通对话，不添加财务上下文
     * 
     * @param query 用户查询
     * @return AI的回应
     */
    public String getChatResponse(String query) {
        try {
            // 检查AI服务是否可用
            if (!aiService.isServiceAvailable()) {
                return "AI服务暂时不可用，请确保已正确配置API密钥和网络连接。";
            }
            
            // 直接调用AI服务，不添加财务数据上下文
            return aiService.chat(query);
        } catch (Exception e) {
            e.printStackTrace();
            return "抱歉，处理您的请求时遇到了错误：" + e.getMessage() + "\n请稍后再试或联系支持团队。";
        }
    }

    /**
     * 与AI进行普通对话（流式），不添加财务上下文
     * 
     * @param query 用户查询
     * @param messageConsumer 消息处理回调
     */
    public void getChatResponseStream(String query, Consumer<String> messageConsumer) {
        try {
            // 检查AI服务是否可用
            if (!aiService.isServiceAvailable()) {
                messageConsumer.accept("AI服务暂时不可用，请确保已正确配置API密钥和网络连接。");
                return;
            }
            
            // 直接调用AI服务流式API，不添加财务数据上下文
            aiService.chatStream(query, messageConsumer);
        } catch (Exception e) {
            e.printStackTrace();
            messageConsumer.accept("抱歉，处理您的请求时遇到了错误：" + e.getMessage() + "\n请稍后再试或联系支持团队。");
        }
    }
}

