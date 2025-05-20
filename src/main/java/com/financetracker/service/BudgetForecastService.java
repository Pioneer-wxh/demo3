package com.financetracker.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.financetracker.model.Settings;
import com.financetracker.model.Transaction;

/**
 * 预算预测服务类，用于基于历史支出数据预测未来预算
 */
public class BudgetForecastService {
    private static final Logger LOGGER = Logger.getLogger(BudgetForecastService.class.getName());

    private final TransactionService transactionService;
    private final SettingsService settingsService;
    private final BudgetAdjustmentService budgetAdjustmentService;

    private double lastForecastedBudget = -1; // 存储最后一次预测的预算金额，初始为-1表示未预测过
    private Map<YearMonth, Double> forecastedBudgets = new HashMap<>(); // 存储每个月的预测预算

    /**
     * 构造函数
     * 
     * @param transactionService 交易服务
     * @param settingsService    设置服务
     */
    public BudgetForecastService(TransactionService transactionService, SettingsService settingsService) {
        this.transactionService = transactionService;
        this.settingsService = settingsService;
        this.budgetAdjustmentService = new BudgetAdjustmentService(settingsService);

        // 尝试初始化历史预测数据
        initializeHistoricalForecasts();
    }

    /**
     * 初始化历史预测数据
     * 基于过去的交易数据，计算并填充历史预测预算
     */
    private void initializeHistoricalForecasts() {
        Settings settings = settingsService.getSettings();
        if (settings == null) {
            LOGGER.log(Level.SEVERE, "无法获取设置，历史预测数据初始化失败");
            return;
        }

        // 获取上次关闭的月份，如果有的话
        String lastClosedMonthStr = settings.getLastMonthClosed();
        if (lastClosedMonthStr == null || lastClosedMonthStr.isEmpty()) {
            LOGGER.log(Level.INFO, "没有找到上次关闭的月份记录，跳过历史预测初始化");
            return;
        }

        try {
            // 解析上次关闭的月份
            YearMonth lastClosedMonth = YearMonth.parse(lastClosedMonthStr);

            // 获取当前月份
            YearMonth currentMonth = YearMonth.now();

            // 计算需要回溯的月数（最多12个月）
            int monthsToLookBack = 12;

            // 首先确保上次关闭月份的预测值等于当前设置的月度预算
            // 因为上次关闭月份时，应该已经计算了下个月（即当前月）的预算
            YearMonth monthAfterLastClosed = lastClosedMonth.plusMonths(1);
            double currentBudget = settings.getMonthlyBudget();
            forecastedBudgets.put(monthAfterLastClosed, currentBudget);

            LOGGER.log(Level.INFO, String.format("从设置中恢复%s的预测预算: %.2f",
                    monthAfterLastClosed.toString(), currentBudget));

            // 然后，为过去几个月创建模拟的预测数据，以便有连续的历史记录
            YearMonth tempMonth = lastClosedMonth;
            for (int i = 0; i < monthsToLookBack
                    && tempMonth.isAfter(currentMonth.minusMonths(monthsToLookBack)); i++) {
                // 为每个月计算模拟的预测值（基于该月前6个月的平均支出）
                if (!forecastedBudgets.containsKey(tempMonth)) {
                    double simulatedForecast = calculateAverageExpenseForMonth(tempMonth, 6);
                    if (simulatedForecast > 0) {
                        forecastedBudgets.put(tempMonth, simulatedForecast);
                        LOGGER.log(Level.INFO, String.format("模拟%s的预测预算: %.2f",
                                tempMonth.toString(), simulatedForecast));
                    }
                }
                tempMonth = tempMonth.minusMonths(1);
            }

            // 最后，计算当前月的下一个月预算预测（如果还没有）
            if (!forecastedBudgets.containsKey(currentMonth.plusMonths(1))) {
                double nextMonthForecast = forecastNextMonthBudget(6);
                LOGGER.log(Level.INFO, String.format("初始化%s的预测预算: %.2f",
                        currentMonth.plusMonths(1).toString(), nextMonthForecast));
            }

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "初始化历史预测数据时出错: " + e.getMessage(), e);
        }
    }

    /**
     * 基于过去几个月的支出数据预测指定月份的预算
     * 
     * @param targetMonth 目标月份
     * @param months      使用过去几个月的数据进行预测
     * @return 预测的预算金额
     */
    public double forecastBudgetForMonth(YearMonth targetMonth, int months) {
        double forecastedBudget = calculateAverageExpenseForMonth(targetMonth, months);
        forecastedBudgets.put(targetMonth, forecastedBudget);
        lastForecastedBudget = forecastedBudget;
        LOGGER.log(Level.INFO, String.format("%s的预测预算: %.2f (基于过去%d个月的支出)",
                targetMonth.toString(), forecastedBudget, months));
        return forecastedBudget;
    }

    /**
     * 基于过去几个月的支出数据预测下月预算
     * 
     * @param months 使用过去几个月的数据进行预测
     * @return 预测的预算金额
     */
    public double forecastNextMonthBudget(int months) {
        YearMonth nextMonth = YearMonth.now().plusMonths(1);
        return forecastBudgetForMonth(nextMonth, months);
    }

    /**
     * 获取指定月份的预算
     * 如果该月预算未被预测过，则返回上一个月预测的结果
     * 如果上一个月也没有，则使用当前设置中的基准预算
     * 
     * @param targetMonth 目标月份
     * @return 该月应使用的预算金额
     */
    public double getBudgetForMonth(YearMonth targetMonth) {
        // 如果目标月已有预测预算，直接返回
        if (forecastedBudgets.containsKey(targetMonth)) {
            return forecastedBudgets.get(targetMonth);
        }

        // 如果目标月没有预测预算，则查找上一个月的预测结果
        YearMonth previousMonth = targetMonth.minusMonths(1);
        if (forecastedBudgets.containsKey(previousMonth)) {
            return forecastedBudgets.get(previousMonth);
        }

        // 如果上一个月也没有预测结果，则使用设置中的基准预算
        Settings settings = settingsService.getSettings();
        if (settings != null) {
            return settings.getMonthlyBudget();
        }

        return 0.0; // 如果没有任何预算设置，则返回0
    }

    /**
     * 获取当前月份的预算（由上一个月计算得出）
     * 
     * @return 当前月份应使用的预算金额
     */
    public double getCurrentMonthBudget() {
        return getBudgetForMonth(YearMonth.now());
    }

    /**
     * 获取下一个月的预算（由当前月计算得出）
     * 
     * @return 下一个月应使用的预算金额
     */
    public double getNextMonthBudget() {
        return getBudgetForMonth(YearMonth.now().plusMonths(1));
    }

    /**
     * 获取包含特殊日期调整的下一个月的预算
     * 
     * @return 包含特殊日期调整的下一个月的预算金额
     */
    public double getAdjustedNextMonthBudget() {
        YearMonth nextMonth = YearMonth.now().plusMonths(1);
        double baseBudget = getBudgetForMonth(nextMonth);

        // 获取特殊日期调整
        Map<String, Double> categoryAdjustments = budgetAdjustmentService.getCategoryAdjustmentsForMonth(nextMonth);
        double totalAdjustments = 0;
        for (double adj : categoryAdjustments.values()) {
            totalAdjustments += adj;
        }

        return baseBudget + totalAdjustments;
    }

    /**
     * 获取最后一次预测的预算金额
     * 
     * @return 最后一次预测的预算金额，如果未预测过则返回-1
     */
    public double getLastForecastedBudget() {
        return lastForecastedBudget;
    }

    /**
     * 保存预测的预算到设置中
     * 
     * @return 保存成功返回true，否则返回false
     */
    public boolean saveForcastedBudgetToSettings() {
        if (lastForecastedBudget <= 0) {
            LOGGER.log(Level.WARNING, "没有有效的预测预算可保存");
            return false;
        }

        Settings settings = settingsService.getSettings();
        if (settings == null) {
            LOGGER.log(Level.WARNING, "无法获取设置对象");
            return false;
        }

        // 获取下个月的年月对象
        YearMonth nextMonth = YearMonth.now().plusMonths(1);

        // 考虑特殊日期调整金额
        Map<String, Double> categoryAdjustments = budgetAdjustmentService.getCategoryAdjustmentsForMonth(nextMonth);
        double totalAdjustments = 0;
        for (double adj : categoryAdjustments.values()) {
            totalAdjustments += adj;
        }

        // 将预测预算和特殊日期调整金额相加作为最终预算
        double finalBudget = lastForecastedBudget + totalAdjustments;
        settings.setMonthlyBudget(finalBudget);

        boolean result = settingsService.saveSettings();
        if (result) {
            LOGGER.log(Level.INFO, String.format("已将预测预算 %.2f 加上特殊日期调整 %.2f，最终 %.2f 保存为下月基准预算",
                    lastForecastedBudget, totalAdjustments, finalBudget));
        } else {
            LOGGER.log(Level.WARNING, "保存预测预算到设置失败");
        }
        return result;
    }

    /**
     * 为指定月份计算过去几个月的平均支出
     * 
     * @param targetMonth 目标月份
     * @param months      过去几个月
     * @return 平均月支出
     */
    private double calculateAverageExpenseForMonth(YearMonth targetMonth, int months) {
        if (months <= 0) {
            return 0;
        }

        double totalExpense = 0;
        int monthsAnalyzed = 0;

        // 从目标月份往前推算months个月
        for (int i = 1; i <= months; i++) {
            YearMonth monthToAnalyze = targetMonth.minusMonths(i);
            List<Transaction> transactions = transactionService.getTransactionsForMonth(
                    monthToAnalyze.getYear(), monthToAnalyze.getMonthValue());

            if (!transactions.isEmpty()) {
                double monthExpense = transactionService.getTotalExpense(transactions);
                totalExpense += monthExpense;
                monthsAnalyzed++;
            }
        }

        if (monthsAnalyzed == 0) {
            LOGGER.log(Level.WARNING, "无法为" + targetMonth.toString() + "找到过去" + months + "个月的交易数据用于预测");
            return 0;
        }

        return totalExpense / monthsAnalyzed;
    }

    /**
     * 计算过去几个月的平均支出
     * 
     * @param months 过去几个月
     * @return 平均月支出
     */
    private double calculateAverageExpense(int months) {
        return calculateAverageExpenseForMonth(YearMonth.now(), months);
    }

    // 预算分析结果数据结构
    public static class BudgetAnalysisResult {
        public double meanBudget; // 仅用于参考
        public double baseBudget; // 基础预算（线性回归）
        public double finalBudget; // 最终预算 = baseBudget + specialDayTotal + savingGoalTotal
        public List<SpecialDayDetail> specialDayDetails = new java.util.ArrayList<>();
        public double specialDayTotal;
        public List<SavingGoalDetail> savingGoalDetails = new java.util.ArrayList<>();
        public double savingGoalTotal;
        public Map<String, Double> categoryAllocations = new java.util.HashMap<>();
        public List<com.financetracker.model.SpecialDate> upcomingSpecialDates = new java.util.ArrayList<>();
        public double avgIncome;
        public double avgExpense;
        public double avgBalance;
    }
    public static class SpecialDayDetail {
        public String name;
        public String category;
        public double amount;
        public String description;
    }
    public static class SavingGoalDetail {
        public String name;
        public double monthlyContribution;
        public String description;
    }

    /**
     * 综合分析下月预算，返回所有明细
     */
    public BudgetAnalysisResult analyzeNextMonthBudget() {
        BudgetAnalysisResult result = new BudgetAnalysisResult();
        YearMonth nextMonth = YearMonth.now().plusMonths(1);
        int months = 3;
        // 1. 计算均值预算
        result.meanBudget = calcMeanExpense(nextMonth, months);
        // 2. 计算基础预算（线性回归）
        result.baseBudget = calcLinearRegressionExpense(nextMonth, months);
        // 3. 统计special day
        Map<String, Double> specialDayMap = budgetAdjustmentService.getCategoryAdjustmentsForMonth(nextMonth);
        result.specialDayTotal = 0;
        for (Map.Entry<String, Double> entry : specialDayMap.entrySet()) {
            SpecialDayDetail detail = new SpecialDayDetail();
            detail.category = entry.getKey();
            detail.amount = entry.getValue();
            // 查找special date name/desc
            for (com.financetracker.model.SpecialDate sd : settingsService.getSettings().getSpecialDates()) {
                if (sd.getAffectedCategory() != null && sd.getAffectedCategory().equals(entry.getKey())) {
                    YearMonth sdMonth = YearMonth.from(sd.getDate());
                    if (sdMonth.equals(nextMonth)) {
                        detail.name = sd.getName();
                        detail.description = sd.getDescription();
                        break;
                    }
                }
            }
            result.specialDayDetails.add(detail);
            result.specialDayTotal += entry.getValue();
        }
        // 4. 统计saving goal
        result.savingGoalTotal = 0;
        for (com.financetracker.model.SavingGoal goal : settingsService.getSettings().getSavingGoals()) {
            if (goal.isActive() && !goal.isCompleted() && goal.getMonthlyContribution() > 0) {
                LocalDate start = goal.getStartDate();
                LocalDate end = goal.getTargetDate();
                LocalDate monthDate = nextMonth.atDay(1);
                if ((start == null || !monthDate.isBefore(start)) && (end == null || !monthDate.isAfter(end))) {
                    SavingGoalDetail detail = new SavingGoalDetail();
                    detail.name = goal.getName();
                    detail.monthlyContribution = goal.getMonthlyContribution();
                    detail.description = goal.getDescription();
                    result.savingGoalDetails.add(detail);
                    result.savingGoalTotal += goal.getMonthlyContribution();
                }
            }
        }
        // 5. 最终预算 = 基础预算 + special day汇总 + saving goal汇总
        result.finalBudget = result.baseBudget + result.specialDayTotal + result.savingGoalTotal;
        // 6. 类别分配
        Map<String, Double> categoryRatios = calcCategoryRatios(nextMonth, months);
        for (Map.Entry<String, Double> entry : categoryRatios.entrySet()) {
            result.categoryAllocations.put(entry.getKey(), result.baseBudget * entry.getValue());
        }
        // 7. special day金额加到对应分类
        for (SpecialDayDetail detail : result.specialDayDetails) {
            result.categoryAllocations.put(detail.category, result.categoryAllocations.getOrDefault(detail.category, 0.0) + detail.amount);
        }
        // 8. 统计upcoming special date
        for (com.financetracker.model.SpecialDate sd : settingsService.getSettings().getSpecialDates()) {
            YearMonth sdMonth = YearMonth.from(sd.getDate());
            if (sdMonth.equals(nextMonth)) {
                result.upcomingSpecialDates.add(sd);
            }
        }
        // 9. 平均收入/支出/结余
        double totalIncome = 0, totalExpense = 0;
        int count = 0;
        for (int i = 1; i <= months; i++) {
            YearMonth m = nextMonth.minusMonths(i);
            List<Transaction> txs = transactionService.getTransactionsForMonth(m.getYear(), m.getMonthValue());
            double income = transactionService.getTotalIncome(txs);
            double expense = txs.stream().filter(t -> t.isExpense() && (t.getCategory() == null || !t.getCategory().equals("Saving Goal"))).mapToDouble(Transaction::getAmount).sum();
            totalIncome += income;
            totalExpense += expense;
            count++;
        }
        if (count > 0) {
            result.avgIncome = totalIncome / count;
            result.avgExpense = totalExpense / count;
            result.avgBalance = result.avgIncome - result.avgExpense;
        }
        return result;
    }

    // 计算均值预算（不含Saving Goal）
    private double calcMeanExpense(YearMonth targetMonth, int months) {
        double total = 0;
        int count = 0;
        for (int i = 1; i <= months; i++) {
            YearMonth m = targetMonth.minusMonths(i);
            List<Transaction> txs = transactionService.getTransactionsForMonth(m.getYear(), m.getMonthValue());
            double expense = txs.stream().filter(t -> t.isExpense() && (t.getCategory() == null || !t.getCategory().equals("Saving Goal"))).mapToDouble(Transaction::getAmount).sum();
            total += expense;
            count++;
        }
        return count > 0 ? total / count : 0;
    }

    // 计算线性回归预算（不含Saving Goal）
    private double calcLinearRegressionExpense(YearMonth targetMonth, int months) {
        double[] x = new double[months];
        double[] y = new double[months];
        for (int i = 1; i <= months; i++) {
            x[i-1] = i;
            YearMonth m = targetMonth.minusMonths(months - i + 1);
            List<Transaction> txs = transactionService.getTransactionsForMonth(m.getYear(), m.getMonthValue());
            y[i-1] = txs.stream().filter(t -> t.isExpense() && (t.getCategory() == null || !t.getCategory().equals("Saving Goal"))).mapToDouble(Transaction::getAmount).sum();
        }
        // 线性回归 y = a + bx, 预测x=months+1
        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
        for (int i = 0; i < months; i++) {
            sumX += x[i];
            sumY += y[i];
            sumXY += x[i] * y[i];
            sumXX += x[i] * x[i];
        }
        double b = (months * sumXY - sumX * sumY) / (months * sumXX - sumX * sumX + 1e-8);
        double a = (sumY - b * sumX) / months;
        return a + b * (months + 1);
    }

    // 计算类别占比（不含Saving Goal）
    private Map<String, Double> calcCategoryRatios(YearMonth targetMonth, int months) {
        Map<String, Double> sumByCat = new java.util.HashMap<>();
        double total = 0;
        for (int i = 1; i <= months; i++) {
            YearMonth m = targetMonth.minusMonths(i);
            List<Transaction> txs = transactionService.getTransactionsForMonth(m.getYear(), m.getMonthValue());
            for (Transaction t : txs) {
                if (t.isExpense() && t.getCategory() != null && !t.getCategory().equals("Saving Goal")) {
                    sumByCat.put(t.getCategory(), sumByCat.getOrDefault(t.getCategory(), 0.0) + t.getAmount());
                    total += t.getAmount();
                }
            }
        }
        Map<String, Double> ratio = new java.util.HashMap<>();
        for (Map.Entry<String, Double> entry : sumByCat.entrySet()) {
            ratio.put(entry.getKey(), total > 0 ? entry.getValue() / total : 0);
        }
        return ratio;
    }

    /**
     * 返回预算分解公式字符串，便于UI或控制台直接输出
     */
    public String getBudgetFormulaString(BudgetAnalysisResult result) {
        if (result == null) return "";
        return String.format(
            "基础预算（线性回归）：￥%.2f\n+ Special Day 汇总：￥%.2f\n+ Saving Goal 汇总：￥%.2f\n= 最终预算：￥%.2f",
            result.baseBudget, result.specialDayTotal, result.savingGoalTotal, result.finalBudget
        );
    }
}