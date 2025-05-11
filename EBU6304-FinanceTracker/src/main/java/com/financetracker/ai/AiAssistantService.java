package com.financetracker.ai;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

import com.financetracker.model.Transaction;
import com.financetracker.service.TransactionService;

/**
 * 提供AI辅助分析功能的服务类
 */
public class AiAssistantService {

    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";
    private final SparkAiService sparkAiService;

    public AiAssistantService() {
        this.sparkAiService = new SparkAiService();
    }

    /**
     * 获取对用户查询的回应
     * 
     * @param query 用户查询
     * @param transactionService 交易服务
     * @return AI的回应
     */
    public String getResponse(String query, TransactionService transactionService) {
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

        // 调用讯飞星火大模型API
        return sparkAiService.chat(fullQuery.toString());
    }

    /**
     * 获取对用户查询的回应（Ollama版，支持模型选择）
     * 
     * @param query 用户查询
     * @param modelName ollama模型名
     * @param transactionService 交易服务
     * @return AI的回应
     */
    public String getResponse(String query, String modelName, TransactionService transactionService) {
        // 获取交易数据
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
                .toList();

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

        // 调用 Ollama
        return callOllama(fullQuery.toString(), modelName);
    }

    /**
     * 获取对用户查询的回应（Ollama版，支持历史数据）
     * 
     * @param query 用户查询
     * @param modelName ollama模型名
     * @param allTransactions 所有交易记录
     * @return AI的回应
     */
    public String getResponse(String query, String modelName, List<Transaction> allTransactions) {
        // 构建上下文信息
        StringBuilder context = new StringBuilder();
        context.append("以下是所有历史财务数据的摘要：\n\n");

        // 统计总收入、总支出、结余
        double totalIncome = 0;
        double totalExpense = 0;
        for (Transaction transaction : allTransactions) {
            if (transaction.isExpense()) {
                totalExpense += transaction.getAmount();
            } else {
                totalIncome += transaction.getAmount();
            }
        }
        double totalBalance = totalIncome - totalExpense;

        context.append(String.format("历史总收入：%.2f\n", totalIncome));
        context.append(String.format("历史总支出：%.2f\n", totalExpense));
        context.append(String.format("历史结余：%.2f\n\n", totalBalance));

        // 按类别统计支出
        Map<String, Double> categoryExpenses = new HashMap<>();
        for (Transaction transaction : allTransactions) {
            if (transaction.isExpense()) {
                String category = transaction.getCategory();
                double amount = transaction.getAmount();
                categoryExpenses.put(category, categoryExpenses.getOrDefault(category, 0.0) + amount);
            }
        }

        // 按支出金额排序类别
        List<Map.Entry<String, Double>> sortedCategories = categoryExpenses.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .toList();

        if (!sortedCategories.isEmpty()) {
            context.append("按类别统计历史支出：\n");
            for (Map.Entry<String, Double> entry : sortedCategories) {
                String category = entry.getKey();
                double amount = entry.getValue();
                double percentage = (amount / totalExpense) * 100;
                context.append(String.format("- %s: %.2f (%.1f%%)\n", category, amount, percentage));
            }
            context.append("\n");
        }

        // 组装用户查询与上下文
        StringBuilder fullQuery = new StringBuilder();
        fullQuery.append(context);
        fullQuery.append("用户的问题是: ").append(query);
        fullQuery.append("\n\n请根据以上所有历史财务数据，对用户的问题提供专业、具体、有帮助的回答。");

        // 调用 Ollama
        return callOllama(fullQuery.toString(), modelName);
    }

    /**
     * 调用本地 Ollama API
     */
    private String callOllama(String prompt, String model) {
        try {
            // 手动构造JSON，确保换行和引号被正确转义
            String safePrompt = prompt.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
            String body = String.format("{\"model\":\"%s\",\"prompt\":\"%s\"}", model, safePrompt);

            URL url = new URL(OLLAMA_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes("UTF-8"));
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                String errorMsg;
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"))) {
                    errorMsg = br.lines().collect(Collectors.joining("\n"));
                }
                return "Ollama API 错误: " + responseCode + "\n" + errorMsg;
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    int idx = line.indexOf("\"response\":\"");
                    if (idx != -1) {
                        int start = idx + 12;
                        int end = line.indexOf("\"", start);
                        if (end > start) {
                            String resp = line.substring(start, end).replace("\\n", "\n").replace("\\\"", "\"");
                            response.append(resp);
                        }
                    }
                }
            }
            return response.length() > 0 ? response.toString() : "未获取到 Ollama 响应";
        } catch (Exception e) {
            e.printStackTrace();
            return "Ollama 调用失败: " + e.getMessage();
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
        String category = "其他";
        boolean isExpense = amount > 0;

        // 简单的关键词匹配分析
        String lowerDesc = description.toLowerCase();

        // 收入判断
        if (lowerDesc.contains("工资") || lowerDesc.contains("薪水") || lowerDesc.contains("salary")) {
            category = "工资";
            isExpense = false;
        } else if (lowerDesc.contains("奖金") || lowerDesc.contains("bonus")) {
            category = "奖金";
            isExpense = false;
        } else if (lowerDesc.contains("投资") || lowerDesc.contains("股票") || lowerDesc.contains("基金")) {
            category = "投资收益";
            isExpense = false;
        } else if (lowerDesc.contains("退款") || lowerDesc.contains("报销")) {
            category = "退款";
            isExpense = false;
        }
        // 支出判断
        else if (lowerDesc.contains("餐") || lowerDesc.contains("饭") || lowerDesc.contains("食品") || lowerDesc.contains("超市")) {
            category = "餐饮";
            isExpense = true;
        } else if (lowerDesc.contains("交通") || lowerDesc.contains("车") || lowerDesc.contains("公交") || lowerDesc.contains("地铁")) {
            category = "交通";
            isExpense = true;
        } else if (lowerDesc.contains("房租") || lowerDesc.contains("水电") || lowerDesc.contains("物业")) {
            category = "住房";
            isExpense = true;
        } else if (lowerDesc.contains("衣") || lowerDesc.contains("服装") || lowerDesc.contains("鞋")) {
            category = "服装";
            isExpense = true;
        } else if (lowerDesc.contains("娱乐") || lowerDesc.contains("电影") || lowerDesc.contains("游戏")) {
            category = "娱乐";
            isExpense = true;
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

        // 使用星火大模型生成分析报告
        return sparkAiService.generateMonthlyAnalysisReport(data.toString());
    }

    /**
     * 基于历史数据预测下个月预算
     * 
     * @param transactionService 交易服务
     * @return 预算建议文本
     */
    public String generateNextMonthBudget(TransactionService transactionService) {
        List<Transaction> allTransactions = transactionService.getAllTransactions();
        List<Transaction> currentMonthTransactions = transactionService.getTransactionsForCurrentMonth();

        // 准备数据
        StringBuilder data = new StringBuilder();
        YearMonth currentMonth = YearMonth.now();
        YearMonth nextMonth = currentMonth.plusMonths(1);

        double totalIncome = transactionService.getTotalIncome(currentMonthTransactions);
        double totalExpense = transactionService.getTotalExpense(currentMonthTransactions);

        data.append(String.format("当前月份：%s\n", currentMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月"))));
        data.append(String.format("下个月：%s\n", nextMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月"))));
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
            data.append("本月支出类别统计：\n");
            for (Map.Entry<String, Double> entry : sortedExpenses) {
                data.append(String.format("- %s: %.2f (%.1f%%)\n",
                        entry.getKey(), entry.getValue(), entry.getValue() / totalExpense * 100));
            }
            data.append("\n");
        }

        // 使用星火大模型生成下月预算建议
        return sparkAiService.generateBudgetSuggestions(data.toString());
    }
}
