package com.financetracker.service;

import java.util.List;

import com.financetracker.model.Transaction;

/**
 * Interface for accessing and persisting transaction data.
 */
public interface TransactionDataSource {
    /**
     * Loads all transactions from the data source.
     * @return A list of all transactions.
     */
    List<Transaction> loadAllTransactions();

    /**
     * Saves all transactions to the data source.
     * @param transactions The list of transactions to save.
     * @return true if the operation was successful, false otherwise.
     */
    boolean saveAllTransactions(List<Transaction> transactions);

    /**
     * Checks if the transaction data source exists (e.g., if the CSV file exists).
     * @return true if the data source exists, false otherwise.
     */
    boolean dataSourceExists();
} 