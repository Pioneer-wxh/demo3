package com.financetracker.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.financetracker.model.Settings;

/**
 * Service for managing application settings.
 */
public class SettingsService {
    
    private static final String SETTINGS_FILE_PATH = "data/settings.json";
    private final JsonDataService<Settings> jsonDataService;
    
    /**
     * Constructor for SettingsService.
     */
    public SettingsService() {
        this.jsonDataService = new JsonDataService<>(Settings.class);
        
        // Create data directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get("data"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Loads settings from the settings file.
     * If the file doesn't exist, returns default settings.
     * 
     * @return The loaded settings
     */
    public Settings loadSettings() {
        if (new File(SETTINGS_FILE_PATH).exists()) {
            Settings settings = jsonDataService.loadItemFromFile(SETTINGS_FILE_PATH);
            if (settings != null) {
                return settings;
            }
        }
        
        // Return default settings if file doesn't exist or loading failed
        return new Settings();
    }
    
    /**
     * Saves settings to the settings file.
     * 
     * @param settings The settings to save
     * @return true if the operation was successful, false otherwise
     */
    public boolean saveSettings(Settings settings) {
        return jsonDataService.saveItemToFile(settings, SETTINGS_FILE_PATH);
    }
    
    /**
     * Resets settings to default values.
     * 
     * @return The default settings
     */
    public Settings resetToDefault() {
        Settings defaultSettings = new Settings();
        saveSettings(defaultSettings);
        return defaultSettings;
    }
    
    /**
     * Creates a backup of the settings file.
     * 
     * @return true if the operation was successful, false otherwise
     */
    public boolean createBackup() {
        String backupFilePath = SETTINGS_FILE_PATH + ".backup";
        return jsonDataService.createBackup(SETTINGS_FILE_PATH, backupFilePath);
    }
}
