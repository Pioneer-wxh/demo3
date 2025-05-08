package com.financetracker.model;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a budget with category-specific spending limits.
 */
public class Budget {
    private String id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private double totalBudget;
    private Map<String, Double> categoryBudgets; // Maps category ID to budget amount
    private String notes;

    public Budget() {
        this.id = java.util.UUID.randomUUID().toString();
        this.categoryBudgets = new HashMap<>();
    }

    public Budget(String name, LocalDate startDate, LocalDate endDate, 
                 double totalBudget, Map<String, Double> categoryBudgets, String notes) {
        this.id = java.util.UUID.randomUUID().toString();
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalBudget = totalBudget;
        this.categoryBudgets = categoryBudgets != null ? categoryBudgets : new HashMap<>();
        this.notes = notes;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public double getTotalBudget() {
        return totalBudget;
    }

    public void setTotalBudget(double totalBudget) {
        this.totalBudget = totalBudget;
    }

    public Map<String, Double> getCategoryBudgets() {
        return categoryBudgets;
    }

    public void setCategoryBudgets(Map<String, Double> categoryBudgets) {
        this.categoryBudgets = categoryBudgets;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Sets the budget amount for a specific category.
     * 
     * @param categoryId The ID of the category
     * @param amount The budget amount for the category
     */
    public void setCategoryBudget(String categoryId, double amount) {
        categoryBudgets.put(categoryId, amount);
    }

    /**
     * Gets the budget amount for a specific category.
     * 
     * @param categoryId The ID of the category
     * @return The budget amount for the category, or 0 if not set
     */
    public double getCategoryBudget(String categoryId) {
        return categoryBudgets.getOrDefault(categoryId, 0.0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Budget budget = (Budget) o;
        return Objects.equals(id, budget.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name + " (" + startDate + " to " + endDate + ")";
    }
}
