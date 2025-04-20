package com.financetracker.gui;

import com.financetracker.service.TransactionService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog for importing transactions from a CSV file.
 */
public class ImportCsvDialog extends JDialog {
    
    private String filePath;
    private TransactionService transactionService;
    
    private JComboBox<String> dateColumnComboBox;
    private JComboBox<String> amountColumnComboBox;
    private JComboBox<String> descriptionColumnComboBox;
    private JComboBox<String> categoryColumnComboBox;
    private JTextField dateFormatField;
    private JRadioButton expenseRadio;
    private JRadioButton incomeRadio;
    
    private List<String> headers;
    
    /**
     * Constructor for ImportCsvDialog.
     * 
     * @param mainFrame The main frame
     * @param filePath The path to the CSV file
     * @param transactionService The transaction service
     */
    public ImportCsvDialog(MainFrame mainFrame, String filePath, TransactionService transactionService) {
        super(mainFrame, "Import CSV File", true);
        this.filePath = filePath;
        this.transactionService = transactionService;
        this.headers = readCsvHeaders();
        
        initComponents();
        setSize(500, 400);
        setLocationRelativeTo(mainFrame);
    }
    
    /**
     * Initializes the dialog components.
     */
    private void initComponents() {
        // Set layout
        setLayout(new BorderLayout());
        
        // Create header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Add title label to header
        JLabel titleLabel = new JLabel("Import Transactions from CSV");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerPanel.add(titleLabel);
        
        // Add header to dialog
        add(headerPanel, BorderLayout.NORTH);
        
        // Create main content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create grid bag constraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Add file path label
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        contentPanel.add(new JLabel("File: " + filePath), gbc);
        
        // Add date column field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        contentPanel.add(new JLabel("Date Column:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        dateColumnComboBox = new JComboBox<>(headers.toArray(new String[0]));
        contentPanel.add(dateColumnComboBox, gbc);
        
        // Add amount column field
        gbc.gridx = 0;
        gbc.gridy = 2;
        contentPanel.add(new JLabel("Amount Column:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        amountColumnComboBox = new JComboBox<>(headers.toArray(new String[0]));
        contentPanel.add(amountColumnComboBox, gbc);
        
        // Add description column field
        gbc.gridx = 0;
        gbc.gridy = 3;
        contentPanel.add(new JLabel("Description Column:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        descriptionColumnComboBox = new JComboBox<>(headers.toArray(new String[0]));
        contentPanel.add(descriptionColumnComboBox, gbc);
        
        // Add category column field
        gbc.gridx = 0;
        gbc.gridy = 4;
        contentPanel.add(new JLabel("Category Column (optional):"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 4;
        categoryColumnComboBox = new JComboBox<>();
        categoryColumnComboBox.addItem(""); // Empty option
        for (String header : headers) {
            categoryColumnComboBox.addItem(header);
        }
        contentPanel.add(categoryColumnComboBox, gbc);
        
        // Add date format field
        gbc.gridx = 0;
        gbc.gridy = 5;
        contentPanel.add(new JLabel("Date Format:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 5;
        dateFormatField = new JTextField("yyyy-MM-dd");
        contentPanel.add(dateFormatField, gbc);
        
        // Add transaction type radio buttons
        gbc.gridx = 0;
        gbc.gridy = 6;
        contentPanel.add(new JLabel("Transaction Type:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 6;
        JPanel radioPanel = new JPanel();
        radioPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        expenseRadio = new JRadioButton("Expense");
        incomeRadio = new JRadioButton("Income");
        expenseRadio.setSelected(true);
        
        ButtonGroup group = new ButtonGroup();
        group.add(expenseRadio);
        group.add(incomeRadio);
        
        radioPanel.add(expenseRadio);
        radioPanel.add(incomeRadio);
        contentPanel.add(radioPanel, gbc);
        
        // Add content panel to dialog
        add(contentPanel, BorderLayout.CENTER);
        
        // Create button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Add buttons to button panel
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        JButton importButton = new JButton("Import");
        importButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                importTransactions();
            }
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(importButton);
        
        // Add button panel to dialog
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Reads the headers from the CSV file.
     * 
     * @return The list of headers
     */
    private List<String> readCsvHeaders() {
        List<String> headers = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine();
            if (line != null) {
                String[] parts = line.split(",");
                for (String part : parts) {
                    headers.add(part.trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error reading CSV file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        return headers;
    }
    
    /**
     * Imports transactions from the CSV file.
     */
    private void importTransactions() {
        try {
            String dateColumn = (String) dateColumnComboBox.getSelectedItem();
            String amountColumn = (String) amountColumnComboBox.getSelectedItem();
            String descriptionColumn = (String) descriptionColumnComboBox.getSelectedItem();
            String categoryColumn = (String) categoryColumnComboBox.getSelectedItem();
            String dateFormat = dateFormatField.getText();
            boolean isExpense = expenseRadio.isSelected();
            
            if (dateColumn == null || amountColumn == null || descriptionColumn == null) {
                JOptionPane.showMessageDialog(this, "Please select all required columns.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int count = transactionService.importFromCsv(filePath, dateColumn, amountColumn, descriptionColumn, categoryColumn, dateFormat, isExpense);
            
            if (count > 0) {
                JOptionPane.showMessageDialog(this, count + " transactions imported successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "No transactions were imported.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error importing transactions: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
