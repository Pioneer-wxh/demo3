package com.financetracker.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class SavingGoal implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String name; // e.g., "New Car Fund", "Vacation to Hawaii"
    private String description; // Optional: further details about the goal
    private double targetAmount; // The total amount to save
    private double currentAmount; // Amount saved so far
    private double monthlyContribution; // The amount to save each month as a transaction
    private LocalDate startDate; // When the goal/monthly contributions start
    private LocalDate targetDate; // Optional: when the goal should ideally be reached
    private boolean isActive; // To enable/disable automatic monthly contributions
    private String associatedAccount; // Optional: if savings are tied to a specific (virtual) account

    public SavingGoal() {
        this.id = UUID.randomUUID().toString();
        this.isActive = true; // Default to active
        this.startDate = LocalDate.now();
        this.currentAmount = 0.0;
        this.targetAmount = 0.0; // Default target to 0, user must set
        this.monthlyContribution = 0.0; // Default contribution to 0
    }

    public SavingGoal(String name, double targetAmount, double monthlyContribution, LocalDate startDate) {
        this(); // Calls the default constructor for ID, isActive, etc.
        this.name = name;
        setTargetAmount(targetAmount); // Use setter for validation
        setMonthlyContribution(monthlyContribution); // Use setter for validation
        this.startDate = (startDate != null) ? startDate : LocalDate.now();
    }

    // Full constructor
    public SavingGoal(String id, String name, String description, double targetAmount, double currentAmount, 
                      double monthlyContribution, LocalDate startDate, LocalDate targetDate, 
                      boolean isActive, String associatedAccount) {
        this.id = (id == null || id.trim().isEmpty()) ? UUID.randomUUID().toString() : id;
        this.name = name;
        this.description = description;
        setTargetAmount(targetAmount);
        setCurrentAmount(currentAmount); // Allow setting initial current amount
        setMonthlyContribution(monthlyContribution);
        this.startDate = (startDate != null) ? startDate : LocalDate.now();
        this.targetDate = targetDate;
        this.isActive = isActive;
        this.associatedAccount = associatedAccount;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getTargetAmount() { return targetAmount; }
    public double getCurrentAmount() { return currentAmount; }
    public double getMonthlyContribution() { return monthlyContribution; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getTargetDate() { return targetDate; }
    public boolean isActive() { return isActive; }
    public String getAssociatedAccount() { return associatedAccount; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setTargetAmount(double targetAmount) {
        if (targetAmount < 0) {
            // Silently set to 0 or throw exception based on desired behavior.
            // For now, let's default to 0 if negative, as per bug report (avoid negative target)
            this.targetAmount = 0;
            // throw new IllegalArgumentException("Target amount cannot be negative.");
        } else {
            this.targetAmount = targetAmount;
        }
    }
    public void setCurrentAmount(double currentAmount) { 
        // Current amount can be anything, even negative if money was withdrawn somehow
        this.currentAmount = currentAmount; 
    }
    public void setMonthlyContribution(double monthlyContribution) {
        if (monthlyContribution < 0) {
            this.monthlyContribution = 0;
            // throw new IllegalArgumentException("Monthly contribution cannot be negative.");
        } else {
            this.monthlyContribution = monthlyContribution;
        }
    }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }
    public void setActive(boolean active) { isActive = active; }
    public void setAssociatedAccount(String associatedAccount) { this.associatedAccount = associatedAccount; }

    // Business logic methods
    public void addContribution(double amount) {
        if (amount < 0) {
             // Allow negative contributions (withdrawals from goal) if necessary
             // For now, assuming positive contributions for simplicity from auto-savings
             // throw new IllegalArgumentException("Contribution amount cannot be negative for addContribution.");
        }
        this.currentAmount += amount;
    }

    public double getProgressPercentage() {
        if (targetAmount <= 0) {
            return 0.0; 
        }
        double progress = (currentAmount / targetAmount) * 100.0;
        return Math.min(Math.max(progress, 0.0), 100.0); // Cap between 0 and 100
    }

    public double getRemainingAmount() {
        return Math.max(0, targetAmount - currentAmount);
    }
    
    public boolean isCompleted() {
        return currentAmount >= targetAmount && targetAmount > 0; 
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SavingGoal that = (SavingGoal) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "SavingGoal{" +
               "id='" + id + '\'' +
               ", name='" + name + '\'' +
               ", targetAmount=" + targetAmount +
               ", currentAmount=" + currentAmount +
               ", monthlyContribution=" + monthlyContribution +
               ", startDate=" + startDate +
               ", targetDate=" + targetDate +
               ", isActive=" + isActive +
               '}';
    }
} 