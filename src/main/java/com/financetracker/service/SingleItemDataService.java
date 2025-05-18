package com.financetracker.service;

import java.io.Serializable;

/**
 * Interface for data persistence operations on a single item.
 * @param <T> The type of data to persist (must be Serializable).
 */
public interface SingleItemDataService<T extends Serializable> {
    /**
     * Saves a single serializable item to the specified file path.
     * @param item The item to save.
     * @param filePath The path to the file.
     * @return true if the operation was successful, false otherwise.
     */
    boolean saveItemToFile(T item, String filePath);

    /**
     * Loads a single serializable item from the specified file path.
     * @param filePath The path to the file.
     * @return The item loaded from the file, or null if the file does not exist
     *         or loading fails.
     */
    T loadItemFromFile(String filePath);
} 