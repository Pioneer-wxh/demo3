package com.financetracker.service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * Implementation of DataService that uses JSON for data persistence with Gson.
 *
 * @param <T> The type of data to be persisted
 */
public class JsonDataService<T> implements DataService<T> {

    private final Gson gson;
    private final Type listType; // Type for List<T>
    private final Class<T> itemType; // Type for T

    /**
     * Constructor for JsonDataService.
     *
     * @param type The class of the type T for single item operations.
     * @param listTypeToken The TypeToken for List<T> for list operations.
     */
    public JsonDataService(Class<T> type, TypeToken<List<T>> listTypeToken) {
        this.itemType = type;
        this.listType = listTypeToken.getType();
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .setPrettyPrinting() // Make the JSON output readable
                .create();
    }

    /**
     * Constructor for JsonDataService when only single item operations are needed.
     *
     * @param type The class of the type T.
     */
     public JsonDataService(Class<T> type) {
        this.itemType = type;
        this.listType = null; // List operations not supported with this constructor
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .setPrettyPrinting()
                .create();
    }


    @Override
    public boolean saveToFile(List<T> items, String filePath) {
        if (listType == null) {
             System.err.println("Error: saveToFile called on JsonDataService instance without listType information.");
             return false;
        }
        try {
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8)) {
                gson.toJson(items, listType, writer);
                return true;
            }
        } catch (IOException e) {
            System.err.println("Error saving list to file " + filePath + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<T> loadFromFile(String filePath) {
         if (listType == null) {
             System.err.println("Error: loadFromFile called on JsonDataService instance without listType information.");
             return new ArrayList<>(); // Return empty list on error
         }
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return new ArrayList<>(); // Return empty list if file doesn't exist
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8)) {
            List<T> items = gson.fromJson(reader, listType);
            return items != null ? items : new ArrayList<>(); // Return empty list if file is empty or invalid
        } catch (IOException | com.google.gson.JsonSyntaxException e) {
            System.err.println("Error loading list from file " + filePath + ": " + e.getMessage());
            e.printStackTrace();
            // Optionally create a backup of the corrupted file here
            // createBackup(filePath, filePath + ".corrupted_" + System.currentTimeMillis());
            return new ArrayList<>(); // Return empty list on error
        }
    }

    @Override
    public boolean saveItemToFile(T item, String filePath) {
         if (itemType == null) {
             System.err.println("Error: saveItemToFile called on JsonDataService instance without itemType information.");
             return false;
         }
        try {
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8)) {
                gson.toJson(item, itemType, writer);
                return true;
            }
        } catch (IOException e) {
            System.err.println("Error saving item to file " + filePath + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public T loadItemFromFile(String filePath) {
        if (itemType == null) {
             System.err.println("Error: loadItemFromFile called on JsonDataService instance without itemType information.");
             return null;
         }
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return null; // Return null if file doesn't exist
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, itemType);
        } catch (IOException | com.google.gson.JsonSyntaxException e) {
            System.err.println("Error loading item from file " + filePath + ": " + e.getMessage());
            e.printStackTrace();
             // Optionally create a backup of the corrupted file here
            // createBackup(filePath, filePath + ".corrupted_" + System.currentTimeMillis());
            return null; // Return null on error
        }
    }

    @Override
    public boolean appendToFile(T item, String filePath) {
        // For JSON, appending isn't straightforward. Load, add, then save.
        List<T> items = loadFromFile(filePath);
        if (items == null) {
             // This might happen if loadFromFile failed or the file was invalid
             items = new ArrayList<>();
        }
        items.add(item);
        return saveToFile(items, filePath);
    }

    @Override
    public boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    @Override
    public boolean createBackup(String filePath, String backupFilePath) {
        Path sourcePath = Paths.get(filePath);
        if (!Files.exists(sourcePath)) {
            System.err.println("Backup source file does not exist: " + filePath);
            return false;
        }

        try {
            Path backupPath = Paths.get(backupFilePath);
            Files.createDirectories(backupPath.getParent());
            Files.copy(sourcePath, backupPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Backup created successfully: " + backupFilePath);
            return true;
        } catch (IOException e) {
            System.err.println("Error creating backup for " + filePath + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
