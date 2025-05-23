package com.financetracker.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a financial transaction in the system.
 */
public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private LocalDate date;
    private double amount;
    private String description;
    private String category;
    private String participant; // payer or payee
    private String notes;
    private boolean isExpense; // true for expense, false for income

    public Transaction() {
        this.id = java.util.UUID.randomUUID().toString();
        this.date = LocalDate.now();
    }

    /**
     * Constructor for creating a transaction from imported CSV data.
     * 
     * @param id The transaction ID (usually a UUID)
     * @param date The transaction date
     * @param amount The transaction amount
     * @param description The transaction description
     * @param category The transaction category
     * @param isExpense Whether the transaction is an expense
     */
    public Transaction(String id, LocalDate date, double amount, String description, 
                      String category, boolean isExpense) {
        this.id = id;
        this.date = date;
        this.amount = amount;
        this.description = description;
        this.category = category;
        this.participant = null; // Not provided in CSV import
        this.notes = null; // Not provided in CSV import
        this.isExpense = isExpense;
    }

    public Transaction(LocalDate date, double amount, String description, 
                      String category, String participant, String notes, boolean isExpense) {
        this.id = java.util.UUID.randomUUID().toString();
        this.date = date;
        this.amount = amount;
        this.description = description;
        this.category = category;
        this.participant = participant;
        this.notes = notes;
        this.isExpense = isExpense;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getParticipant() {
        return participant;
    }

    public void setParticipant(String participant) {
        this.participant = participant;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isExpense() {
        return isExpense;
    }

    public void setExpense(boolean expense) {
        isExpense = expense;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + id + '\'' +
                ", date=" + date +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", participant='" + participant + '\'' +
                ", notes='" + notes + '\'' +
                ", isExpense=" + isExpense +
                '}';
    }
}
