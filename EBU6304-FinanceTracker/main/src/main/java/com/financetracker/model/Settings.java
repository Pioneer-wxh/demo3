package com.financetracker.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents user preferences and application settings.
 */
public class Settings {
    private int monthStartDay; // Day of month when a new "financial month" starts (default: 1)
    private String defaultCurrency; // Default currency for transactions
    private String dateFormat; // Preferred date format
    private boolean darkModeEnabled; // UI theme preference
    private List<String> defaultCategories; // List of default category names
    private String dataStoragePath; // Path where data files are stored
    private boolean autoBackupEnabled; // Whether to automatically backup data
    private int backupFrequencyDays; // How often to backup data (in days)
    private boolean aiAssistanceEnabled; // Whether AI features are enabled
    
    public Settings() {
        // Set default values
        this.monthStartDay = 1;
        this.defaultCurrency = "CNY";
        this.dateFormat = "yyyy-MM-dd";
        this.darkModeEnabled = false;
        this.defaultCategories = new ArrayList<>();
        this.dataStoragePath = "data/";
        this.autoBackupEnabled = true;
        this.backupFrequencyDays = 7;
        this.aiAssistanceEnabled = true;
        
        // Add some default categories
        defaultCategories.add("Food");
        defaultCategories.add("Transportation");
        defaultCategories.add("Housing");
        defaultCategories.add("Utilities");
        defaultCategories.add("Entertainment");
        defaultCategories.add("Shopping");
        defaultCategories.add("Healthcare");
        defaultCategories.add("Education");
        defaultCategories.add("Salary");
        defaultCategories.add("Investment");
        defaultCategories.add("Gift");
        defaultCategories.add("Other");
    }

    // Getters and Setters
    public int getMonthStartDay() {
        return monthStartDay;
    }

    public void setMonthStartDay(int monthStartDay) {
        if (monthStartDay < 1 || monthStartDay > 31) {
            throw new IllegalArgumentException("Month start day must be between 1 and 31");
        }
        this.monthStartDay = monthStartDay;
    }

    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    public void setDefaultCurrency(String defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public boolean isDarkModeEnabled() {
        return darkModeEnabled;
    }

    public void setDarkModeEnabled(boolean darkModeEnabled) {
        this.darkModeEnabled = darkModeEnabled;
    }

    public List<String> getDefaultCategories() {
        return defaultCategories;
    }

    public void setDefaultCategories(List<String> defaultCategories) {
        this.defaultCategories = defaultCategories;
    }

    public String getDataStoragePath() {
        return dataStoragePath;
    }

    public void setDataStoragePath(String dataStoragePath) {
        this.dataStoragePath = dataStoragePath;
    }

    public boolean isAutoBackupEnabled() {
        return autoBackupEnabled;
    }

    public void setAutoBackupEnabled(boolean autoBackupEnabled) {
        this.autoBackupEnabled = autoBackupEnabled;
    }

    public int getBackupFrequencyDays() {
        return backupFrequencyDays;
    }

    public void setBackupFrequencyDays(int backupFrequencyDays) {
        this.backupFrequencyDays = backupFrequencyDays;
    }

    public boolean isAiAssistanceEnabled() {
        return aiAssistanceEnabled;
    }

    public void setAiAssistanceEnabled(boolean aiAssistanceEnabled) {
        this.aiAssistanceEnabled = aiAssistanceEnabled;
    }
    
    /**
     * Determines if a given date is within the current "financial month" based on the month start day setting.
     * 
     * @param date The date to check
     * @return true if the date is in the current financial month, false otherwise
     */
    public boolean isInCurrentFinancialMonth(LocalDate date) {
        LocalDate today = LocalDate.now();
        LocalDate currentMonthStart;
        
        // Determine the start of the current financial month
        if (today.getDayOfMonth() >= monthStartDay) {
            // We're past the start day of this month, so the financial month started this calendar month
            currentMonthStart = today.withDayOfMonth(monthStartDay);
        } else {
            // We're before the start day of this month, so the financial month started in the previous calendar month
            currentMonthStart = today.minusMonths(1).withDayOfMonth(monthStartDay);
        }
        
        // Determine the end of the current financial month (exclusive)
        LocalDate nextMonthStart = currentMonthStart.plusMonths(1);
        
        // Check if the date is within the current financial month
        return !date.isBefore(currentMonthStart) && date.isBefore(nextMonthStart);
    }
}
