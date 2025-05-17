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
import com.financetracker.service.SettingsService;
import com.financetracker.service.TransactionService;

/**
 * 提供AI辅助分析功能的服务类
 */
public class AiAssistantService {
    
    private final DeepSeekAiService aiService;
    private final SettingsService settingsService;
    private final BudgetAdjustmentService budgetAdjustmentService;
    
    public AiAssistantService(SettingsService settingsService) {
        this.aiService = new DeepSeekAiService();
        this.settingsService = settingsService;
        this.budgetAdjustmentService = new BudgetAdjustmentService(settingsService);
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
            context.append("以下是当前财务数据的摘要：\n\n");
            
            // 添加当前月份的收支情况
            double currentMonthIncome = transactionService.getTotalIncome(currentMonthTransactions);
            double currentMonthExpense = transactionService.getTotalExpense(currentMonthTransactions);
            double currentMonthBalance = currentMonthIncome - currentMonthExpense;
            
            YearMonth currentMonth = YearMonth.now();
            context.append(String.format("当前月份(%s)收支：\n", currentMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月"))));
            context.append(String.format("- 总收入：%.2f\n", currentMonthIncome));
            context.append(String.format("- 总支出：%.2f\n", currentMonthExpense));
            context.append(String.format("- 结余：%.2f\n\n", currentMonthBalance));
            
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
                context.append("按类别统计支出：\n");
                for (Map.Entry<String, Double> entry : sortedCategories) {
                    String category = entry.getKey();
                    double amount = entry.getValue();
                    double percentage = (amount / currentMonthExpense) * 100;
                    context.append(String.format("- %s: %.2f (%.1f%%)\n", category, amount, percentage));
                }
                context.append("\n");
            }
            
            // 组装用户查询与上下文
            StringBuilder fullQuery = new StringBuilder();
            fullQuery.append(context);
            fullQuery.append("用户的问题是: ").append(query);
            fullQuery.append("\n\n请根据以上财务数据，对用户的问题提供专业、具体、有帮助的回答。");
            
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
            context.append("以下是当前财务数据的摘要：\n\n");
            
            // 添加当前月份的收支情况
            double currentMonthIncome = transactionService.getTotalIncome(currentMonthTransactions);
            double currentMonthExpense = transactionService.getTotalExpense(currentMonthTransactions);
            double currentMonthBalance = currentMonthIncome - currentMonthExpense;
            
            YearMonth currentMonth = YearMonth.now();
            context.append(String.format("当前月份(%s)收支：\n", currentMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月"))));
            context.append(String.format("- 总收入：%.2f\n", currentMonthIncome));
            context.append(String.format("- 总支出：%.2f\n", currentMonthExpense));
            context.append(String.format("- 结余：%.2f\n\n", currentMonthBalance));
            
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
                context.append("按类别统计支出：\n");
                for (Map.Entry<String, Double> entry : sortedCategories) {
                    String category = entry.getKey();
                    double amount = entry.getValue();
                    double percentage = (amount / currentMonthExpense) * 100;
                    context.append(String.format("- %s: %.2f (%.1f%%)\n", category, amount, percentage));
                }
                context.append("\n");
            }
            
            // 组装用户查询与上下文
            StringBuilder fullQuery = new StringBuilder();
            fullQuery.append(context);
            fullQuery.append("用户的问题是: ").append(query);
            fullQuery.append("\n\n请根据以上财务数据，对用户的问题提供专业、具体、有帮助的回答。");
            
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
        if (lowerDesc.contains("工资") || lowerDesc.contains("薪水") || lowerDesc.contains("salary") || lowerDesc.contains("payroll")) {
            category = "Salary";
            isExpense = false;
        } else if (lowerDesc.contains("奖金") || lowerDesc.contains("bonus") || lowerDesc.contains("award")) {
            category = "Bonus";
            isExpense = false;
        } else if (lowerDesc.contains("投资") || lowerDesc.contains("股票") || lowerDesc.contains("基金") || 
                   lowerDesc.contains("invest") || lowerDesc.contains("stock") || lowerDesc.contains("fund") || lowerDesc.contains("financial")) {
            category = "Investment";
            isExpense = false;
        } else if (lowerDesc.contains("退款") || lowerDesc.contains("报销") || lowerDesc.contains("refund") || lowerDesc.contains("reimbursement")) {
            category = "Refund";
            isExpense = false;
        } else if (lowerDesc.contains("利息") || lowerDesc.contains("interest")) {
            category = "Interest";
            isExpense = false;
        } else if (lowerDesc.contains("礼物") || lowerDesc.contains("礼金") || lowerDesc.contains("gift")) {
            category = "Gift";
            isExpense = false;
        } else if (lowerDesc.contains("兼职") || lowerDesc.contains("副业") || lowerDesc.contains("freelance")) {
            category = "Freelance/Part-time";
            isExpense = false;
        } 
        // 如果不是明确的收入，再判断支出类别
        // (如果上面已经判断为 isExpense = false, 则这里的判断不会覆盖)
        else if (lowerDesc.contains("餐") || lowerDesc.contains("饭") || lowerDesc.contains("食品") || lowerDesc.contains("超市") ||
            lowerDesc.contains("food") || lowerDesc.contains("eat") || lowerDesc.contains("meal") || lowerDesc.contains("dining") ||
            lowerDesc.contains("restaurant") || lowerDesc.contains("grocery")) {
            category = "Food";
            isExpense = true;
        } else if (lowerDesc.contains("交通") || lowerDesc.contains("车") || lowerDesc.contains("公交") || lowerDesc.contains("地铁") ||
                   lowerDesc.contains("traffic") || lowerDesc.contains("bus") || lowerDesc.contains("subway") || lowerDesc.contains("taxi") || 
                   lowerDesc.contains("gas") || lowerDesc.contains("fuel") || lowerDesc.contains("parking")) {
            category = "Transportation";
            isExpense = true;
        } else if (lowerDesc.contains("房租") || lowerDesc.contains("水电") || lowerDesc.contains("物业") || lowerDesc.contains("住房") ||
                   lowerDesc.contains("rent") || lowerDesc.contains("housing") || lowerDesc.contains("utility") || lowerDesc.contains("mortgage")) {
            category = "Housing"; // May include some utilities, or separate Utilities later if needed
            isExpense = true;
        } else if (lowerDesc.contains("衣") || lowerDesc.contains("服装") || lowerDesc.contains("鞋") ||
                   lowerDesc.contains("clothing") || lowerDesc.contains("apparel") || lowerDesc.contains("shoe")) {
            category = "Clothing";
            isExpense = true;
        } else if (lowerDesc.contains("娱乐") || lowerDesc.contains("电影") || lowerDesc.contains("游戏") ||
                   lowerDesc.contains("entertainment") || lowerDesc.contains("movie") || lowerDesc.contains("game") || lowerDesc.contains("hobby")) {
            category = "Entertainment";
            isExpense = true;
        } else if (lowerDesc.contains("购物") || lowerDesc.contains("买") ||
                 lowerDesc.contains("shop") || lowerDesc.contains("buy") || lowerDesc.contains("purchase") || lowerDesc.contains("store")) {
            category = "Shopping";
            isExpense = true;
        } else if (lowerDesc.contains("医疗") || lowerDesc.contains("药") || lowerDesc.contains("医院") ||
                   lowerDesc.contains("medical") || lowerDesc.contains("health") || lowerDesc.contains("doctor")) {
            category = "Healthcare";
            isExpense = true;
        } else if (lowerDesc.contains("教育") || lowerDesc.contains("学费") || lowerDesc.contains("课程") ||
                   lowerDesc.contains("education") || lowerDesc.contains("school") || lowerDesc.contains("course")) {
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
            boolean savingsProcessed = transactionService.processMonthlySavingsContributions(settingsService, currentMonthToProcess, savingsTransactionDay, "Savings");
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
        
        data.append(String.format("当前月份：%s\n", currentMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月"))));
        data.append(String.format("总收入：%.2f\n", totalIncome));
        data.append(String.format("总支出：%.2f\n", totalExpense));
        data.append(String.format("结余：%.2f\n\n", totalIncome - totalExpense));
        
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
            data.append("支出类别统计：\n");
            for (Map.Entry<String, Double> entry : sortedExpenses) {
                data.append(String.format("- %s: %.2f (%.1f%%)\n", 
                        entry.getKey(), entry.getValue(), entry.getValue() / totalExpense * 100));
            }
            data.append("\n");
        }
        
        // 按类别统计收入
        Map<String, Double> categoryIncomes = new HashMap<>();
        for (Transaction transaction : currentMonthTransactions) {
            if (!transaction.isExpense()) {
                String category = transaction.getCategory();
                double amount = transaction.getAmount();
                categoryIncomes.put(category, categoryIncomes.getOrDefault(category, 0.0) + amount);
            }
        }
        
        // 按收入金额排序类别
        List<Map.Entry<String, Double>> sortedIncomes = categoryIncomes.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .collect(Collectors.toList());
            
        if (!sortedIncomes.isEmpty()) {
            data.append("收入来源统计：\n");
            for (Map.Entry<String, Double> entry : sortedIncomes) {
                data.append(String.format("- %s: %.2f (%.1f%%)\n", 
                        entry.getKey(), entry.getValue(), entry.getValue() / totalIncome * 100));
            }
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
            boolean savingsProcessed = transactionService.processMonthlySavingsContributions(settingsService, currentMonthToProcess, savingsTransactionDay, "Savings");
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
        
        data.append(String.format("当前月份：%s\n", currentMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月"))));
        data.append(String.format("下个月：%s\n", nextMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月"))));
        
        if (settings != null) {
            data.append(String.format("月度总预算基准 (来自设置): %.2f %s\n", settings.getMonthlyBudget(), settings.getDefaultCurrency()));
        } else {
            data.append("月度总预算基准: 未设置\n");
        }

        data.append(String.format("本月总收入：%.2f\n", totalIncomeCurrentMonth));
        data.append(String.format("本月总支出：%.2f\n", totalExpenseCurrentMonth));
        data.append(String.format("本月结余：%.2f\n\n", totalIncomeCurrentMonth - totalExpenseCurrentMonth));
        
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
            data.append(String.format("下个月 (%s) 特殊日期预算调整：\n", nextMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月"))));
            for (Map.Entry<String, Double> adjustmentEntry : specialDateAdjustments.entrySet()) {
                String category = adjustmentEntry.getKey();
                double adjustmentAmount = adjustmentEntry.getValue();
                categoryExpensesPrediction.merge(category, adjustmentAmount, Double::sum); // Add adjustment to predicted expense
                data.append(String.format("- %s: 预计增加 %.2f %s\n", category, adjustmentAmount, settings != null ? settings.getDefaultCurrency() : ""));
            }
            data.append("\n");
        }
        
        // 按预测支出金额排序类别
        // List<Map.Entry<String, Double>> sortedExpenses = categoryExpenses.entrySet().stream() // Old: was current month's actual
        List<Map.Entry<String, Double>> sortedPredictedExpenses = categoryExpensesPrediction.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toList());
        
        if (!sortedPredictedExpenses.isEmpty()) {
            data.append(String.format("下个月 (%s) 预测支出类别统计 (基于本月及特殊日期调整)：\n", nextMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月"))));
            double predictedTotalExpense = sortedPredictedExpenses.stream().mapToDouble(Map.Entry::getValue).sum();
            for (Map.Entry<String, Double> entry : sortedPredictedExpenses) {
                data.append(String.format("- %s: %.2f %s (占预测总支出 %.1f%%)\n", 
                        entry.getKey(), 
                        entry.getValue(), 
                        settings != null ? settings.getDefaultCurrency() : "",
                        predictedTotalExpense > 0 ? (entry.getValue() / predictedTotalExpense * 100) : 0.0));
            }
             data.append(String.format("预测总支出: %.2f %s\n", predictedTotalExpense, settings != null ? settings.getDefaultCurrency() : ""));
            data.append("\n");
        } else if (specialDateAdjustments.isEmpty()) { // only if no other predictions were made
             data.append("下个月预测支出类别统计：暂无足够数据或无特殊日期调整。\n\n");
        }
        
        // 使用AI服务生成下月预算建议
        // The prompt for the AI should clearly state that these are PREDICTIONS and ADJUSTMENTS
        // and ask for advice based on this, perhaps comparing to the set monthlyBudget.
        String promptPrefix = String.format(
            "你是一位财务顾问。请根据以下为 %s 做的财务预测和预算调整信息，生成一份预算建议报告。\n" +
            "报告应包括对各项预测支出的评估，与设定的月度总预算基准 (%.2f %s) 的对比分析 (如果已设置), 以及如何优化预算的建议。\n\n",
            nextMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月")),
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
            boolean savingsProcessed = transactionService.processMonthlySavingsContributions(settingsService, currentMonthToProcess, savingsTransactionDay, "Savings");
            if (!savingsProcessed) {
                 messageConsumer.accept("[警告] 处理当月储蓄目标时可能发生错误。分析可能未包含最新储蓄交易。\n");
            }
        } else {
            messageConsumer.accept("[警告] 设置信息为空，无法处理当月储蓄目标。\n");
        }

        List<Transaction> currentMonthTransactions = transactionService.getTransactionsForCurrentMonth();
        
        // 准备数据（与非流式方法相同）
        StringBuilder data = new StringBuilder();
        YearMonth currentMonth = YearMonth.now();
        double totalIncome = transactionService.getTotalIncome(currentMonthTransactions);
        double totalExpense = transactionService.getTotalExpense(currentMonthTransactions);
        
        data.append(String.format("当前月份：%s\n", currentMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月"))));
        data.append(String.format("总收入：%.2f\n", totalIncome));
        data.append(String.format("总支出：%.2f\n", totalExpense));
        data.append(String.format("结余：%.2f\n\n", totalIncome - totalExpense));
        
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
            data.append("支出类别统计：\n");
            for (Map.Entry<String, Double> entry : sortedExpenses) {
                data.append(String.format("- %s: %.2f (%.1f%%)\n", 
                        entry.getKey(), entry.getValue(), entry.getValue() / totalExpense * 100));
            }
            data.append("\n");
        }
        
        // 使用AI服务流式生成分析报告
        String prompt = "你是一位专业的财务分析师。根据以下财务数据，生成一份详细的月度分析报告，包括收支情况分析、消费趋势分析和财务健康状况评估：\n\n" 
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

