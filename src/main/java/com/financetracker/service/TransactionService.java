package com.financetracker.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.financetracker.model.Settings;
import com.financetracker.model.Transaction;

/**
 * Service for managing transactions (CRUD, queries, basic calculations).
 * Financial cycle operations (month-end, savings contributions) are now in FinancialCycleService.
 */
public class TransactionService {
    
    private static final Logger LOGGER = Logger.getLogger(TransactionService.class.getName());
    
    private final TransactionDataSource transactionDataSource;
    private final Settings settings; // Used for income categories, financial month start day
    
    /**
     * Constructor for TransactionService.
     * @param transactionDataSource The data source for transactions.
     * @param settings The application settings.
     */
    public TransactionService(TransactionDataSource transactionDataSource, Settings settings) {
        this.transactionDataSource = transactionDataSource;
        this.settings = settings;
    }
    
    /**
     * Gets all transactions.
     * 
     * @return The list of all transactions
     */
    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        
        // 从数据源加载
        if (transactionDataSource.dataSourceExists()) {
            transactions = transactionDataSource.loadAllTransactions();
        }
        
        // 按日期排序（最新的优先）
        transactions.sort(Comparator.comparing(Transaction::getDate).reversed());
        
        return transactions;
    }
    
    /**
     * Gets transactions for a specific date range.
     * 
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @return The list of transactions in the date range
     */
    public List<Transaction> getTransactionsForDateRange(LocalDate startDate, LocalDate endDate) {
        List<Transaction> allTransactions = getAllTransactions();
        
        return allTransactions.stream()
                .filter(t -> !t.getDate().isBefore(startDate) && !t.getDate().isAfter(endDate))
                .collect(Collectors.toList());
    }
    
    /**
     * Gets transactions for a specific month.
     * 
     * @param year The year
     * @param month The month (1-12)
     * @return The list of transactions in the month
     */
    public List<Transaction> getTransactionsForMonth(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        
        return getTransactionsForDateRange(startDate, endDate);
    }
    
    /**
     * Gets transactions for the current month.
     * 
     * @return The list of transactions in the current month
     */
    public List<Transaction> getTransactionsForCurrentMonth() {
        LocalDate today = LocalDate.now();
        return getTransactionsForMonth(today.getYear(), today.getMonthValue());
    }
    
    /**
     * Gets transactions for a specific category.
     * 
     * @param category The category
     * @return The list of transactions in the category
     */
    public List<Transaction> getTransactionsForCategory(String category) {
        List<Transaction> allTransactions = getAllTransactions();
        
        return allTransactions.stream()
                .filter(t -> t.getCategory().equals(category))
                .collect(Collectors.toList());
    }
    
    /**
     * Gets transactions for a specific category and date range.
     * 
     * @param category The category
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @return The list of transactions in the category and date range
     */
    public List<Transaction> getTransactionsForCategoryAndDateRange(String category, LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactionsInDateRange = getTransactionsForDateRange(startDate, endDate);
        
        return transactionsInDateRange.stream()
                .filter(t -> t.getCategory().equals(category))
                .collect(Collectors.toList());
    }
    
    /**
     * Gets the total amount for a list of transactions.
     * 
     * @param transactions The list of transactions
     * @param isExpense Whether to calculate expenses or income
     * @return The total amount
     */
    public double getTotalAmount(List<Transaction> transactions, boolean isExpense) {
        return transactions.stream()
                .filter(t -> t.isExpense() == isExpense)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }
    
    /**
     * Gets the total expense amount for a list of transactions.
     * 
     * @param transactions The list of transactions
     * @return The total expense amount
     */
    public double getTotalExpense(List<Transaction> transactions) {
        return getTotalAmount(transactions, true);
    }
    
    /**
     * Gets the total income amount for a list of transactions.
     * 
     * @param transactions The list of transactions
     * @return The total income amount
     */
    public double getTotalIncome(List<Transaction> transactions) {
        return getTotalAmount(transactions, false);
    }
    
    /**
     * Gets the net amount (income - expense) for a list of transactions.
     * 
     * @param transactions The list of transactions
     * @return The net amount
     */
    public double getNetAmount(List<Transaction> transactions) {
        return getTotalIncome(transactions) - getTotalExpense(transactions);
    }
    
    /**
     * Adds a transaction.
     * 
     * @param transaction The transaction to add
     * @return true if the operation was successful, false otherwise
     */
    public boolean addTransaction(Transaction transaction) {
        List<Transaction> transactions = getAllTransactions();

        // 根据类别设置 IsExpense
        if (transaction.getCategory() != null && this.settings.getIncomeCategories().contains(transaction.getCategory())) {
            transaction.setExpense(false);
        } else {
            // 如果不是明确的收入类别，可以保留原有的isExpense值，或者调用autoDetectIsExpense
            // 为简单起见，如果UI层已经设置了isExpense，我们这里可以信任它，除非它是已知收入类别
            // 如果需要更复杂的逻辑，例如总是重新检测，可以调用：
            // transaction.setExpense(autoDetectIsExpense(transaction.getDescription(), transaction.getAmount(), transaction.getCategory()));
            // 但请注意，这可能覆盖用户在UI上明确的选择。当前保留：如果不是已知收入，则信任传入的isExpense。
            // 如果传入的 transaction 本身没有正确设置 isExpense，且不是已知收入类别，则默认为支出 (true)
             if (transaction.getCategory() != null && !this.settings.getIncomeCategories().contains(transaction.getCategory())) {
                // 如果不是已知收入，且UI没有明确设置isExpense为false，可以考虑默认为true
                // 不过，Transaction对象本身在创建时应有默认的isExpense处理
                // 此处仅确保收入类别被正确设为false。
                // 若要强制非收入类别为true，则： transaction.setExpense(true);
                // 但这可能不是期望行为，因为一个非特定收入的条目也可能被用户手动设为收入。
                // 最好的做法是UI确保isExpense的初始值，这里只覆盖已知收入类别。
            }
        }

        transactions.add(transaction);
        
        // 保存到数据源
        return saveAllTransactions(transactions);
    }
    
    /**
     * Updates a transaction.
     * 
     * @param transaction The transaction to update
     * @return true if the operation was successful, false otherwise
     */
    public boolean updateTransaction(Transaction transaction) {
        List<Transaction> transactions = getAllTransactions();
        
        // 查找要更新的交易
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getId().equals(transaction.getId())) {
                // 根据类别设置 IsExpense
                if (transaction.getCategory() != null && this.settings.getIncomeCategories().contains(transaction.getCategory())) {
                    transaction.setExpense(false);
                } else {
                    // 与addTransaction中类似的考虑
                    // 此处也仅确保收入类别被正确设为false，其他情况信任传入的transaction对象的isExpense值。
                    // 如果需要对非收入类别强制设为true，则：transaction.setExpense(true);
                }
                transactions.set(i, transaction);
                
                // 保存到数据源
                return saveAllTransactions(transactions);
            }
        }
        
        LOGGER.log(Level.WARNING, "Attempted to update a non-existent transaction with ID: " + transaction.getId());
        return false;
    }
    
    /**
     * Retrieves a transaction by its unique ID.
     * 
     * @param id The ID of the transaction to retrieve.
     * @return An Optional containing the transaction if found, or an empty Optional otherwise.
     */
    public Optional<Transaction> getTransactionById(String id) {
        if (id == null || id.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Attempted to get transaction with null or empty ID.");
            return Optional.empty();
        }
        return getAllTransactions().stream()
                .filter(t -> id.equals(t.getId()))
                .findFirst();
    }
    
    /**
     * Deletes a transaction.
     * 
     * @param transaction The transaction to delete
     * @return true if the operation was successful, false otherwise
     */
    public boolean deleteTransaction(String transactionId) {
        List<Transaction> transactions = getAllTransactions();
        boolean removed = transactions.removeIf(t -> t.getId().equals(transactionId));
        if (removed) {
            return saveAllTransactions(transactions);
        }
        LOGGER.log(Level.WARNING, "Attempted to delete a non-existent transaction with ID: " + transactionId);
        return false;
    }
    
    /**
     * Saves transactions to the data file using the exporter.
     * 
     * @param transactions The transactions to save
     * @return true if the operation was successful, false otherwise
     */
    public boolean saveAllTransactions(List<Transaction> transactions) {
        // Sort transactions by date (latest first) before saving
        transactions.sort(Comparator.comparing(Transaction::getDate).reversed());
        return transactionDataSource.saveAllTransactions(transactions);
    }
    
    /**
     * Creates a backup of the transaction data file.
     * @return true if the backup was successful, false otherwise
     */
    public boolean createBackup() {
        // This method is specific to CSV backup. If other data sources are used,
        // backup mechanisms might differ. Consider a dedicated BackupService or
        // making backup a direct responsibility of the specific data source implementation.
        // For now, to retain functionality with minimal change to TransactionService's core DI:
        TransactionCsvExporter csvExporter = new TransactionCsvExporter(); 
        return csvExporter.createCsvBackup();
    }

    /**
     * Exports all transactions to the designated export file.
     * @return true if export was successful, false otherwise
     */
    public boolean exportAllTransactions() {
        List<Transaction> transactions = getAllTransactions();
        TransactionCsvExporter exporter = new TransactionCsvExporter();
        return exporter.exportAllTransactionsToFile(transactions);
    }

    /**
     * Exports transactions grouped by month to the designated classify directory.
     * @return An ExportResult object containing details of the export process.
     */
    public TransactionCsvExporter.ExportResult exportTransactionsByMonth() {
        List<Transaction> transactions = getAllTransactions();
        TransactionCsvExporter exporter = new TransactionCsvExporter();
        return exporter.exportTransactionsByMonth(transactions);
    }

    /**
     * 获取当前财务月的开始和结束日期
     * 这是根据设置中的monthStartDay来定义财务月，而不是自然月
     * 
     * @return 包含开始和结束日期的Map
     */
    public Map<String, LocalDate> getCurrentFinancialMonthRange() {
        int monthStartDay = this.settings.getMonthStartDay();
        
        LocalDate today = LocalDate.now();
        LocalDate startDate;
        
        if (today.getDayOfMonth() >= monthStartDay) {
            startDate = today.withDayOfMonth(monthStartDay);
        } else {
            startDate = today.minusMonths(1).withDayOfMonth(monthStartDay);
        }
        
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        
        Map<String, LocalDate> result = new HashMap<>();
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        
        return result;
    }

    /**
     * 获取当前财务月的交易记录
     * 使用设置中的monthStartDay而不是自然月
     * 
     * @return 当前财务月的交易记录
     */
    public List<Transaction> getTransactionsForCurrentFinancialMonth() {
        Map<String, LocalDate> dateRange = getCurrentFinancialMonthRange();
        LocalDate startDate = dateRange.get("startDate");
        LocalDate endDate = dateRange.get("endDate");
        
        return getTransactionsForDateRange(startDate, endDate);
    }

    /**
     * 获取指定财务月的开始和结束日期
     * 
     * @param year 年份
     * @param month 月份(1-12)
     * @return 包含开始和结束日期的Map
     */
    public Map<String, LocalDate> getFinancialMonthRange(int year, int month) {
        int monthStartDay = this.settings.getMonthStartDay();
        
        LocalDate startDate = LocalDate.of(year, month, monthStartDay);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        
        // Handle cases where monthStartDay might be invalid for a given month (e.g., 31 for Feb)
        // Ensure startDate is not after the end of the calendar month it's supposed to start in.
        LocalDate firstDayOfGivenCalendarMonth = LocalDate.of(year, month, 1);
        if (monthStartDay > firstDayOfGivenCalendarMonth.lengthOfMonth()) {
             // If monthStartDay is, e.g., 30 and month is February, start on the last day of Feb.
            startDate = LocalDate.of(year, month, firstDayOfGivenCalendarMonth.lengthOfMonth());
            // Recalculate endDate based on this potentially adjusted startDate
            endDate = startDate.plusMonths(1).minusDays(1); 
        } else {
            // Ensure the originally calculated startDate is valid. 
            // If monthStartDay is e.g. 15, LocalDate.of(year, month, monthStartDay) is fine.
            // endDate calculation is also fine.
        }

        Map<String, LocalDate> result = new HashMap<>();
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        
        return result;
    }

    /**
     * 获取指定财务月的交易记录
     * 
     * @param year 年份
     * @param month 月份(1-12)
     * @return 该财务月的交易记录
     */
    public List<Transaction> getTransactionsForFinancialMonth(int year, int month) {
        Map<String, LocalDate> range = getFinancialMonthRange(year, month);
        return getTransactionsForDateRange(range.get("startDate"), range.get("endDate"));
    }

    /**
     * Calculates the remaining balance for the current financial month.
     * Remaining Balance = Total Income - Total Expenses, without subtracting active savings contributions.
     *
     * @param settings The application settings, used to get saving goals.
     * @return The calculated remaining balance.
     */
    public double calculateRemainingBalanceForCurrentFinancialMonth(Settings currentSettingsFromService) {
        if (currentSettingsFromService == null) {
            // Or use this.settings if no parameter is intended for general use
            LOGGER.log(Level.WARNING, "Settings object is null. Cannot calculate remaining balance.");
            return 0.0;
        }
        double budget = currentSettingsFromService.getMonthlyBudget();
        List<Transaction> currentMonthTransactions = getTransactionsForCurrentFinancialMonth(); // Uses this.settings for range
        double expenses = getTotalExpense(currentMonthTransactions);
        return budget - expenses;
    }
}
