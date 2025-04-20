package com.financetracker.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a special date that might affect spending patterns.
 * Examples include holidays, festivals, or other significant events.
 */
public class SpecialDate {
    private String id;
    private String name;
    private LocalDate date;
    private String description;
    private String affectedCategories; // Comma-separated list of category IDs that might be affected
    private double expectedImpact; // Percentage increase/decrease in spending (e.g., +20% for a holiday)

    public SpecialDate() {
        this.id = java.util.UUID.randomUUID().toString();
    }

    public SpecialDate(String name, LocalDate date, String description, 
                      String affectedCategories, double expectedImpact) {
        this.id = java.util.UUID.randomUUID().toString();
        this.name = name;
        this.date = date;
        this.description = description;
        this.affectedCategories = affectedCategories;
        this.expectedImpact = expectedImpact;
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAffectedCategories() {
        return affectedCategories;
    }

    public void setAffectedCategories(String affectedCategories) {
        this.affectedCategories = affectedCategories;
    }

    public double getExpectedImpact() {
        return expectedImpact;
    }

    public void setExpectedImpact(double expectedImpact) {
        this.expectedImpact = expectedImpact;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpecialDate that = (SpecialDate) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name + " (" + date + ")";
    }
}
