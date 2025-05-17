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
    private List<String> expenseCategories;
    private List<String> incomeCategories;
    private String dataStoragePath; // Path where data files are stored
    private boolean autoBackupEnabled; // Whether to automatically backup data
    private int backupFrequencyDays; // How often to backup data (in days)
    private boolean aiAssistanceEnabled; // Whether AI features are enabled
    private double monthlyBudget;
    private int budgetStartDay;

    // New fields for Special Dates and Saving Goals
    private List<SpecialDate> specialDates;
    private List<SavingGoal> savingGoals;
    private double overallAccountBalance;
    private String lastMonthClosed; // Stores YYYY-MM of the last financial month closed
    
    /**
     * 默认构造函数
     */
    public Settings() {
        // Set default values
        this.monthStartDay = 1;
        this.defaultCurrency = "CNY";
        this.dateFormat = "yyyy-MM-dd";
        this.darkModeEnabled = false;
        
        // Initialize default expense categories
        this.expenseCategories = new ArrayList<>(Arrays.asList(
            "Food", "Shopping", "Transportation", "Housing", "Entertainment", 
            "Healthcare", "Education", "Communication", "Utilities", "Clothing", 
            "Savings", "Others" // Added Savings here as an expense category
        ));
        // Initialize default income categories
        this.incomeCategories = new ArrayList<>(Arrays.asList(
            "Salary", "Bonus", "Investment", "Gift", "Freelance/Part-time", "Other Income"
        ));
        
        this.dataStoragePath = "data/";
        this.autoBackupEnabled = true;
        this.backupFrequencyDays = 7;
        this.aiAssistanceEnabled = true;
        this.monthlyBudget = 5000.0;
        this.budgetStartDay = 1;

        // Initialize new lists
        this.specialDates = new ArrayList<>();
        this.savingGoals = new ArrayList<>();
        this.overallAccountBalance = 0.0;
        this.lastMonthClosed = ""; // Or a sensible default like one month before app's first possible use
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

    public List<String> getExpenseCategories() {
        if (this.expenseCategories == null) { // Defensive null check
            this.expenseCategories = new ArrayList<>();
        }
        return expenseCategories;
    }

    public void setExpenseCategories(List<String> expenseCategories) {
        this.expenseCategories = expenseCategories;
    }

    public List<String> getIncomeCategories() {
        if (this.incomeCategories == null) { // Defensive null check
            this.incomeCategories = new ArrayList<>();
        }
        return incomeCategories;
    }

    public void setIncomeCategories(List<String> incomeCategories) {
        this.incomeCategories = incomeCategories;
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
    public void addExpenseCategory(String category) {
        if (category != null && !category.trim().isEmpty() && !this.expenseCategories.contains(category.trim())) {
            this.expenseCategories.add(category.trim());
        }
    }
    
    /**
     * 删除默认分类
     */
    public boolean removeExpenseCategory(String category) {
        return this.expenseCategories.remove(category);
    }
    
    /**
     * 重置为默认设置
     */
    public void resetToDefault() {
        this.monthStartDay = 1;
        this.defaultCurrency = "CNY";
        this.dateFormat = "yyyy-MM-dd";
        this.darkModeEnabled = false;
        // Reset to new default categories
        this.expenseCategories = new ArrayList<>(Arrays.asList(
            "Food", "Shopping", "Transportation", "Housing", "Entertainment", 
            "Healthcare", "Education", "Communication", "Utilities", "Clothing", 
            "Savings", "Others"
        ));
        this.incomeCategories = new ArrayList<>(Arrays.asList(
            "Salary", "Bonus", "Investment", "Gift", "Freelance/Part-time", "Other Income"
        ));
        this.dataStoragePath = "data/";
        this.autoBackupEnabled = true;
        this.monthlyBudget = 5000.0;
        this.budgetStartDay = 1;
        this.specialDates = new ArrayList<>();
        this.savingGoals = new ArrayList<>();
        this.overallAccountBalance = 0.0;
        this.lastMonthClosed = "";
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

    // --- SpecialDate Management ---
    public List<SpecialDate> getSpecialDates() {
        if (this.specialDates == null) { // Ensure list is not null
            this.specialDates = new ArrayList<>();
        }
        return specialDates;
    }

    public void setSpecialDates(List<SpecialDate> specialDates) {
        this.specialDates = specialDates;
    }

    public void addSpecialDate(SpecialDate specialDate) {
        if (specialDate != null) {
            if (this.specialDates == null) {
                this.specialDates = new ArrayList<>();
            }
            // Avoid duplicates by ID if necessary, or allow multiple with same name
            if (!this.specialDates.stream().anyMatch(sd -> sd.getId().equals(specialDate.getId()))) {
                 this.specialDates.add(specialDate);
            } else {
                // Update existing one if ID matches
                updateSpecialDate(specialDate);
            }
        }
    }

    public boolean removeSpecialDate(String specialDateId) {
        if (this.specialDates == null || specialDateId == null) return false;
        return this.specialDates.removeIf(sd -> sd.getId().equals(specialDateId));
    }

    public void updateSpecialDate(SpecialDate specialDateToUpdate) {
        if (this.specialDates == null || specialDateToUpdate == null) return;
        for (int i = 0; i < this.specialDates.size(); i++) {
            if (this.specialDates.get(i).getId().equals(specialDateToUpdate.getId())) {
                this.specialDates.set(i, specialDateToUpdate);
                return;
            }
        }
        // If not found by ID, add it (optional behavior)
        // this.specialDates.add(specialDateToUpdate);
    }

    // --- SavingGoal Management ---
    public List<SavingGoal> getSavingGoals() {
        if (this.savingGoals == null) { // Ensure list is not null
            this.savingGoals = new ArrayList<>();
        }
        return savingGoals;
    }

    public void setSavingGoals(List<SavingGoal> savingGoals) {
        this.savingGoals = savingGoals;
    }

    public void addSavingGoal(SavingGoal savingGoal) {
        if (savingGoal != null) {
            if (this.savingGoals == null) {
                this.savingGoals = new ArrayList<>();
            }
            if (!this.savingGoals.stream().anyMatch(sg -> sg.getId().equals(savingGoal.getId()))) {
                this.savingGoals.add(savingGoal);
            } else {
                updateSavingGoal(savingGoal);
            }
        }
    }

    public boolean removeSavingGoal(String savingGoalId) {
        if (this.savingGoals == null || savingGoalId == null) return false;
        return this.savingGoals.removeIf(sg -> sg.getId().equals(savingGoalId));
    }

    public void updateSavingGoal(SavingGoal savingGoalToUpdate) {
        if (this.savingGoals == null || savingGoalToUpdate == null) return;
        for (int i = 0; i < this.savingGoals.size(); i++) {
            if (this.savingGoals.get(i).getId().equals(savingGoalToUpdate.getId())) {
                this.savingGoals.set(i, savingGoalToUpdate);
                return;
            }
        }
    }

    // Methods to manage income categories
    public void addIncomeCategory(String category) {
        if (category != null && !category.trim().isEmpty() && !this.incomeCategories.contains(category.trim())) {
            this.incomeCategories.add(category.trim());
        }
    }

    public boolean removeIncomeCategory(String category) {
        return this.incomeCategories.remove(category);
    }

    // Getters and Setters for new fields
    public double getOverallAccountBalance() {
        return overallAccountBalance;
    }

    public void setOverallAccountBalance(double overallAccountBalance) {
        this.overallAccountBalance = overallAccountBalance;
    }

    public String getLastMonthClosed() {
        return lastMonthClosed;
    }

    public void setLastMonthClosed(String lastMonthClosed) {
        this.lastMonthClosed = lastMonthClosed;
    }
}
