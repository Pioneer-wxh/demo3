package com.financetracker.model;

import java.util.Objects;

/**
 * Represents a transaction category in the system.
 */
public class Category {
    private String id;
    private String name;
    private String description;
    private boolean isExpenseCategory; // true for expense categories, false for income categories

    public Category() {
        this.id = java.util.UUID.randomUUID().toString();
    }

    public Category(String name, String description, boolean isExpenseCategory) {
        this.id = java.util.UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.isExpenseCategory = isExpenseCategory;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isExpenseCategory() {
        return isExpenseCategory;
    }

    public void setExpenseCategory(boolean expenseCategory) {
        isExpenseCategory = expenseCategory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(id, category.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name;
    }
}
