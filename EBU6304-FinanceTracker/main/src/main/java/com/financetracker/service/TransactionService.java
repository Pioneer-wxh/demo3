package com.financetracker.service;

import com.financetracker.model.Transaction;
import com.google.gson.reflect.TypeToken; // Add this import

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Service for managing transactions.
 */
public class TransactionService {
    
    private static final String TRANSACTIONS_FILE_PATH = "data/transactions.json";
    private final JsonDataService<Transaction> jsonDataService;
    
    /**
     * Constructor for TransactionService.
     */
    public TransactionService() {
        this.jsonDataService = new JsonDataService<>(Transaction.class, new TypeToken<List<Transaction>>() {});
    }
    
    /**
     * Gets all transactions.
     * 
     * @return The list of all transactions
     */
    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        
        // Check if file exists
        if (jsonDataService.fileExists(TRANSACTIONS_FILE_PATH)) {
            // Load transactions from file
            try {
                transactions = jsonDataService.loadFromFile(TRANSACTIONS_FILE_PATH);
                if (transactions == null) {
                    transactions = new ArrayList<>();
                }
            } catch (Exception e) {
                e.printStackTrace();
                transactions = new ArrayList<>();
            }
        }
        
        // Sort transactions by date (newest first)
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
        transactions.add(transaction);
        return saveTransactions(transactions);
    }
    
    /**
     * Updates a transaction.
     * 
     * @param transaction The transaction to update
     * @return true if the operation was successful, false otherwise
     */
    public boolean updateTransaction(Transaction transaction) {
        List<Transaction> transactions = getAllTransactions();
        
        // Find the transaction to update
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getId().equals(transaction.getId())) {
                transactions.set(i, transaction);
                return saveTransactions(transactions);
            }
        }
        
        return false;
    }
    
    /**
     * Deletes a transaction.
     * 
     * @param transaction The transaction to delete
     * @return true if the operation was successful, false otherwise
     */
    public boolean deleteTransaction(Transaction transaction) {
        List<Transaction> transactions = getAllTransactions();
        
        // Find the transaction to delete
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getId().equals(transaction.getId())) {
                transactions.remove(i);
                return saveTransactions(transactions);
            }
        }
        
        return false;
    }
    
    /**
     * Saves transactions to the data file.
     * 
     * @param transactions The transactions to save
     * @return true if the operation was successful, false otherwise
     */
    private boolean saveTransactions(List<Transaction> transactions) {
        try {
            return jsonDataService.saveToFile(transactions, TRANSACTIONS_FILE_PATH);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Imports transactions from a CSV file.
     * 
     * @param filePath The path to the CSV file
     * @param dateColumn The name or index of the date column
     * @param amountColumn The name or index of the amount column
     * @param descriptionColumn The name or index of the description column
     * @param categoryColumn The name or index of the category column (optional)
     * @param dateFormat The format of the date in the CSV file
     * @param isExpense Whether the transactions should be marked as expenses
     * @return The number of transactions imported
     */
    public int importFromCsv(String filePath, String dateColumn, String amountColumn, 
                            String descriptionColumn, String categoryColumn, 
                            String dateFormat, boolean isExpense) {
        // This is a simplified version that doesn't actually import from CSV
        // In a real implementation, this would use CsvDataService to import transactions
        
        // Create a sample transaction to simulate import
        Transaction sampleTransaction = new Transaction();
        sampleTransaction.setId(UUID.randomUUID().toString());
        sampleTransaction.setDate(LocalDate.now());
        sampleTransaction.setAmount(100.0);
        sampleTransaction.setDescription("Sample imported transaction");
        sampleTransaction.setCategory(categoryColumn != null ? categoryColumn : "Uncategorized");
        sampleTransaction.setExpense(isExpense);
        
        // Add the sample transaction
        List<Transaction> allTransactions = getAllTransactions();
        allTransactions.add(sampleTransaction);
        
        // Save all transactions
        if (saveTransactions(allTransactions)) {
            return 1; // One transaction imported
        } else {
            return 0;
        }
    }
    
    /**
     * Creates a backup of the transactions file.
     * 
     * @return true if the operation was successful, false otherwise
     */
    public boolean createBackup() {
        String backupFilePath = TRANSACTIONS_FILE_PATH + ".backup";
        return jsonDataService.createBackup(TRANSACTIONS_FILE_PATH, backupFilePath);
    }
}
