package com.financetracker.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 应用程序设置类
 */
public class Settings implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int monthStartDay; // Day of month when a new "financial month" starts (default: 1)
    private String defaultCurrency; // Default currency for transactions
    private String dateFormat; // Preferred date format
    private boolean darkModeEnabled; // UI theme preference
    private List<String> defaultCategories; // List of default category names
    private String dataStoragePath; // Path where data files are stored
    private boolean autoBackupEnabled; // Whether to automatically backup data
    private int backupFrequencyDays; // How often to backup data (in days)
    private boolean aiAssistanceEnabled; // Whether AI features are enabled
    private double monthlyBudget;
    private int budgetStartDay;
    
    /**
     * 默认构造函数
     */
    public Settings() {
        // Set default values
        this.monthStartDay = 1;
        this.defaultCurrency = "CNY";
        this.dateFormat = "yyyy-MM-dd";
        this.darkModeEnabled = false;
        this.defaultCategories = new ArrayList<>(Arrays.asList(
            "餐饮", "购物", "交通", "住房", "娱乐", "医疗", "教育", "通讯", "其他"
        ));
        this.dataStoragePath = "data/";
        this.autoBackupEnabled = true;
        this.backupFrequencyDays = 7;
        this.aiAssistanceEnabled = true;
        this.monthlyBudget = 5000.0;
        this.budgetStartDay = 1;
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
     * 获取月度预算
     */
    public double getMonthlyBudget() {
        return monthlyBudget;
    }
    
    /**
     * 设置月度预算
     */
    public void setMonthlyBudget(double monthlyBudget) {
        this.monthlyBudget = monthlyBudget;
    }
    
    /**
     * 获取预算开始日期
     */
    public int getBudgetStartDay() {
        return budgetStartDay;
    }
    
    /**
     * 设置预算开始日期
     */
    public void setBudgetStartDay(int budgetStartDay) {
        if (budgetStartDay < 1) {
            budgetStartDay = 1;
        } else if (budgetStartDay > 28) {
            budgetStartDay = 28;
        }
        this.budgetStartDay = budgetStartDay;
    }
    
    /**
     * 添加默认分类
     */
    public void addDefaultCategory(String category) {
        if (!defaultCategories.contains(category)) {
            defaultCategories.add(category);
        }
    }
    
    /**
     * 删除默认分类
     */
    public boolean removeDefaultCategory(String category) {
        return defaultCategories.remove(category);
    }
    
    /**
     * 重置为默认设置
     */
    public void resetToDefault() {
        this.monthlyBudget = 5000.0;
        this.budgetStartDay = 1;
        this.defaultCategories = new ArrayList<>(Arrays.asList(
            "餐饮", "购物", "交通", "住房", "娱乐", "医疗", "教育", "通讯", "其他"
        ));
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
