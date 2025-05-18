package com.financetracker.service;

import java.io.Serializable;
import java.util.List;

/**
 * Interface for data persistence operations on a list of items.
 * @param <T> The type of data to persist (must be Serializable).
 */
public interface ListDataService<T extends Serializable> {
    /**
     * Saves a list of serializable items to the specified file path.
     * @param items The list of items to save.
     * @param filePath The path to the file.
     * @return true if the operation was successful, false otherwise.
     */
    boolean saveToFile(List<T> items, String filePath);

    /**
     * Loads a list of serializable items from the specified file path.
     * @param filePath The path to the file.
     * @return The list of items loaded from the file, or an empty list if the file
     *         does not exist or loading fails.
     */
    List<T> loadFromFile(String filePath);
} 