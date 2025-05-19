package com.financetracker.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.financetracker.model.SavingGoal;
import com.financetracker.model.Settings;
import com.financetracker.model.Transaction;

/**
 * Service responsible for handling financial cycle tasks like month-end closing
 * and processing monthly savings contributions.
 */
public class FinancialCycleService {

    private static final Logger LOGGER = Logger.getLogger(FinancialCycleService.class.getName());

    private final TransactionService transactionService;
    private final SettingsService settingsService;
    private BudgetForecastService budgetForecastService;

    public FinancialCycleService(TransactionService transactionService, SettingsService settingsService) {
        this.transactionService = transactionService;
        this.settingsService = settingsService;
    }

    /**
     * 设置预算预测服务
     * 
     * @param budgetForecastService 预算预测服务
     */
    public void setBudgetForecastService(BudgetForecastService budgetForecastService) {
        this.budgetForecastService = budgetForecastService;
    }

    /**
     * Processes monthly savings contributions for all active, non-completed goals.
     * Creates a new expense transaction for each contribution.
     * (Moved from TransactionService)
     *
     * @param monthToProcess          The YearMonth for which to process
     *                                contributions.
     * @param dayForSavingTransaction The day of the month to record the saving
     *                                transaction.
     * @param savingsCategory         The category to assign to the savings
     *                                transactions.
     * @return true if any contributions were processed and saved, false otherwise.
     */
    public boolean processMonthlySavingsContributions(YearMonth monthToProcess, int dayForSavingTransaction,
            String savingsCategory) {
        Settings currentSettings = settingsService.getSettings();
        if (currentSettings == null || currentSettings.getSavingGoals() == null) {
            LOGGER.log(Level.INFO, "No settings or saving goals found to process contributions.");
            return false;
        }
        List<Transaction> newSavingTransactions = new ArrayList<>();
        LocalDate transactionDate = monthToProcess
                .atDay(Math.min(dayForSavingTransaction, monthToProcess.lengthOfMonth()));
        boolean settingsChangedByContributions = false;

        for (SavingGoal goal : currentSettings.getSavingGoals()) {
            if (goal.isActive() && !goal.isCompleted() && goal.getMonthlyContribution() > 0) {
                boolean shouldContributeThisMonth = !transactionDate.isBefore(goal.getStartDate());
                if (goal.getTargetDate() != null && transactionDate.isAfter(goal.getTargetDate())) {
                    shouldContributeThisMonth = false;
                }
                if (shouldContributeThisMonth) {
                    double amountToContribute = goal.getMonthlyContribution();
                    if (goal.getCurrentAmount() + amountToContribute > goal.getTargetAmount()
                            && goal.getTargetAmount() > goal.getCurrentAmount()) {
                        amountToContribute = goal.getTargetAmount() - goal.getCurrentAmount();
                    }
                    if (amountToContribute > 0) {
                        Transaction savingTransaction = new Transaction();
                        savingTransaction.setId(UUID.randomUUID().toString());
                        savingTransaction.setDate(transactionDate);
                        savingTransaction.setAmount(amountToContribute);
                        savingTransaction.setDescription("Savings contribution for: " + goal.getName());
                        savingTransaction.setCategory(savingsCategory);
                        savingTransaction.setExpense(true);
                        savingTransaction.setParticipant("Self");
                        savingTransaction.setNotes("Automated monthly savings for goal ID: " + goal.getId());
                        newSavingTransactions.add(savingTransaction);

                        goal.setCurrentAmount(goal.getCurrentAmount() + amountToContribute);
                        settingsChangedByContributions = true;
                        if (goal.isCompleted()) {
                            LOGGER.log(Level.INFO, "Saving goal '" + goal.getName() + "' completed!");
                        }
                    }
                }
            }
        }
        if (!newSavingTransactions.isEmpty()) {
            List<Transaction> allTransactions = transactionService.getAllTransactions(); // Use injected
                                                                                         // transactionService
            allTransactions.addAll(newSavingTransactions);
            boolean saveTransactionsSuccess = transactionService.saveAllTransactions(allTransactions); // Use injected
                                                                                                       // transactionService

            boolean saveSettingsSuccess = true;
            if (settingsChangedByContributions) {
                saveSettingsSuccess = settingsService.saveSettings();
            }
            if (saveTransactionsSuccess && saveSettingsSuccess) {
                LOGGER.log(Level.INFO, "Successfully processed and saved " + newSavingTransactions.size()
                        + " monthly savings contributions.");
                return true;
            } else {
                LOGGER.log(Level.WARNING,
                        "Failed to save all data after processing savings contributions. Transactions saved: "
                                + saveTransactionsSuccess + ", Settings saved: " + saveSettingsSuccess);
                return false;
            }
        }
        return false;
    }

    /**
     * Performs month-end closing up to the last fully completed financial month.
     * Calculates surplus (Income - Expense) for each closed month and adds it to
     * Overall Account Balance.
     * Updates the LastMonthClosed in settings.
     * (Moved from TransactionService)
     *
     * @return true if any month was closed and settings were saved, false
     *         otherwise.
     */
    public boolean performMonthEndClosing() {
        Settings currentSettings = settingsService.getSettings();
        if (currentSettings == null) {
            LOGGER.log(Level.WARNING, "Settings not available for month-end closing.");
            return false;
        }
        LocalDate today = LocalDate.now();
        YearMonth currentProcessingYearMonth;
        String lastClosedMonthStr = currentSettings.getLastMonthClosed();

        if (lastClosedMonthStr == null || lastClosedMonthStr.isEmpty()) {
            List<Transaction> allTransactions = transactionService.getAllTransactions(); // Use injected
                                                                                         // transactionService
            if (allTransactions.isEmpty()) {
                LOGGER.log(Level.INFO, "No transactions available to determine the first month for closing.");
                return false;
            }
            allTransactions.sort(Comparator.comparing(Transaction::getDate));
            currentProcessingYearMonth = YearMonth.from(allTransactions.get(0).getDate());
        } else {
            try {
                currentProcessingYearMonth = YearMonth.parse(lastClosedMonthStr).plusMonths(1);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Could not parse last closed month: " + lastClosedMonthStr, e);
                List<Transaction> allTransactions = transactionService.getAllTransactions();
                if (allTransactions.isEmpty())
                    return false;
                allTransactions.sort(Comparator.comparing(Transaction::getDate));
                currentProcessingYearMonth = YearMonth.from(allTransactions.get(0).getDate());
            }
        }
        boolean anyMonthClosedThisRun = false;
        double totalSurplusFromClosedMonths = 0;

        // GetFinancialMonthRange is still in TransactionService, it needs 'Settings'
        // which TransactionService has.
        // Alternatively, FinancialCycleService could also take Settings in constructor
        // or getFinancialMonthRange could be static util taking Settings.
        // For now, assume TransactionService provides this utility based on its own
        // Settings instance.
        Map<String, LocalDate> currentFinancialMonthDateRange = transactionService
                .getFinancialMonthRange(today.getYear(), today.getMonthValue());
        LocalDate startOfCurrentFinancialMonth = currentFinancialMonthDateRange.get("startDate");
        LocalDate boundaryDateForEmptyMonthCheck = startOfCurrentFinancialMonth;

        while (currentProcessingYearMonth.atDay(1).isBefore(startOfCurrentFinancialMonth)) {
            Map<String, LocalDate> processingMonthDateRange = transactionService.getFinancialMonthRange(
                    currentProcessingYearMonth.getYear(), currentProcessingYearMonth.getMonthValue());
            LocalDate monthStartDate = processingMonthDateRange.get("startDate");
            LocalDate monthEndDate = processingMonthDateRange.get("endDate");
            List<Transaction> monthTransactions = transactionService.getTransactionsForDateRange(monthStartDate,
                    monthEndDate);

            if (monthTransactions.isEmpty()
                    && currentProcessingYearMonth.atDay(1).isBefore(boundaryDateForEmptyMonthCheck)) {
                LOGGER.log(Level.INFO,
                        "Financial month " + currentProcessingYearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))
                                + " has no transactions. Marking as processed.");
                currentSettings
                        .setLastMonthClosed(currentProcessingYearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM")));
                anyMonthClosedThisRun = true;
                currentProcessingYearMonth = currentProcessingYearMonth.plusMonths(1);
                continue;
            }
            double income = transactionService.getTotalIncome(monthTransactions); // Use injected transactionService
            double expenses = transactionService.getTotalExpense(monthTransactions); // Use injected transactionService
            double surplus = income - expenses;
            totalSurplusFromClosedMonths += surplus;

            LOGGER.log(Level.INFO,
                    "Closing financial month: "
                            + currentProcessingYearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM")) +
                            String.format(". Income: %.2f, Expense: %.2f, Surplus: %.2f", income, expenses, surplus));
            currentSettings
                    .setLastMonthClosed(currentProcessingYearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            anyMonthClosedThisRun = true;
            currentProcessingYearMonth = currentProcessingYearMonth.plusMonths(1);
        }
        if (anyMonthClosedThisRun) {
            double currentOverallBalance = currentSettings.getOverallAccountBalance();
            currentSettings.setOverallAccountBalance(currentOverallBalance + totalSurplusFromClosedMonths);
            LOGGER.log(Level.INFO, String.format("Total surplus of %.2f added to overall balance. New balance: %.2f",
                    totalSurplusFromClosedMonths, currentSettings.getOverallAccountBalance()));

            // 进行下个周期的预算预测并保存
            if (budgetForecastService != null) {
                YearMonth closedMonth = YearMonth.parse(currentSettings.getLastMonthClosed());
                YearMonth nextMonth = closedMonth.plusMonths(1);

                // 使用过去6个月的数据预测下一个月的预算
                double forecastedBudget = budgetForecastService.forecastBudgetForMonth(nextMonth, 6);

                // 将预测预算保存为新周期的基准预算
                if (forecastedBudget > 0) {
                    // 更新设置中的月度预算
                    budgetForecastService.saveForcastedBudgetToSettings();
                    LOGGER.log(Level.INFO, String.format("在%s记账周期结束时，已将预测预算 %.2f 设置为%s的预算",
                            closedMonth.toString(), forecastedBudget, nextMonth.toString()));
                }
            } else {
                LOGGER.log(Level.WARNING, "预算预测服务未设置，无法在记账周期结束时预测新预算");
            }

            return settingsService.saveSettings();
        }
        LOGGER.log(Level.INFO, "No new financial months were eligible for closing.");
        return false;
    }
}