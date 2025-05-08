package com.financetracker.service;

import java.util.List;

/**
 * Interface for data persistence operations.
 * 
 * @param <T> The type of data to be persisted
 */
public interface DataService<T> {
    
    /**
     * Saves a list of items to a file.
     * 
     * @param items The items to save
     * @param filePath The path to the file
     * @return true if the operation was successful, false otherwise
     */
    boolean saveToFile(List<T> items, String filePath);
    
    /**
     * Loads a list of items from a file.
     * 
     * @param filePath The path to the file
     * @return The list of items loaded from the file
     */
    List<T> loadFromFile(String filePath);
    
    /**
     * Saves a single item to a file.
     * 
     * @param item The item to save
     * @param filePath The path to the file
     * @return true if the operation was successful, false otherwise
     */
    boolean saveItemToFile(T item, String filePath);
    
    /**
     * Loads a single item from a file.
     * 
     * @param filePath The path to the file
     * @return The item loaded from the file
     */
    T loadItemFromFile(String filePath);
    
    /**
     * Appends an item to an existing file.
     * 
     * @param item The item to append
     * @param filePath The path to the file
     * @return true if the operation was successful, false otherwise
     */
    boolean appendToFile(T item, String filePath);
    
    /**
     * Checks if a file exists.
     * 
     * @param filePath The path to the file
     * @return true if the file exists, false otherwise
     */
    boolean fileExists(String filePath);
    
    /**
     * Creates a backup of a file.
     * 
     * @param filePath The path to the file
     * @param backupFilePath The path to the backup file
     * @return true if the operation was successful, false otherwise
     */
    boolean createBackup(String filePath, String backupFilePath);
}
