package com.financetracker.gui;

import com.financetracker.model.Transaction;
import com.financetracker.service.TransactionService;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel for managing transactions.
 */
public class TransactionPanel extends JPanel {
    
    private MainFrame mainFrame;
    private TransactionService transactionService;
    
    private JTable transactionTable;
    private DefaultTableModel tableModel;
    private JTextField dateField;
    private JTextField amountField;
    private JTextField descriptionField;
    private JComboBox<String> categoryComboBox;
    private JTextField participantField;
    private JTextField notesField;
    private JRadioButton expenseRadio;
    private JRadioButton incomeRadio;
    
    /**
     * Constructor for TransactionPanel.
     * 
     * @param mainFrame The main frame
     */
    public TransactionPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.transactionService = new TransactionService();
        initComponents();
        loadTransactions();
    }
    
    /**
     * Initializes the panel components.
     */
    private void initComponents() {
        // Set layout
        setLayout(new BorderLayout());
        
        // Create header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Add title label to header
        JLabel titleLabel = new JLabel("Transaction Record Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Add home button to header
        JButton homeButton = new JButton("HOME");
        homeButton.addActionListener(e -> mainFrame.showPanel("home"));
        headerPanel.add(homeButton, BorderLayout.EAST);
        
        // Add header to panel
        add(headerPanel, BorderLayout.NORTH);
        
        // Create main content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create table panel
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Transactions"));
        
        // Create table model with columns
        String[] columns = {"Date", "Amount", "Description", "Category", "Participant", "Type"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        // Create table
        transactionTable = new JTable(tableModel);
        transactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Create table button panel
        JPanel tableButtonPanel = new JPanel();
        tableButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        // Add buttons to table button panel
        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> deleteSelectedTransaction());
        
        JButton editButton = new JButton("Edit");
        editButton.addActionListener(e -> editSelectedTransaction());
        
        JButton importButton = new JButton("Import CSV");
        importButton.addActionListener(e -> importCsv());
        
        tableButtonPanel.add(importButton);
        tableButtonPanel.add(editButton);
        tableButtonPanel.add(deleteButton);
        
        // Add table button panel to table panel
        tablePanel.add(tableButtonPanel, BorderLayout.SOUTH);
        
        // Add table panel to content panel
        contentPanel.add(tablePanel, BorderLayout.CENTER);
        
        // Create form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BorderLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Add Transaction"));
        
        // Create form fields panel
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new GridBagLayout());
        
        // Create grid bag constraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Add date field
        gbc.gridx = 0;
        gbc.gridy = 0;
        fieldsPanel.add(new JLabel("Date (YYYY-MM-DD):"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        dateField = new JTextField(10);
        dateField.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        fieldsPanel.add(dateField, gbc);
        
        // Add amount field
        gbc.gridx = 0;
        gbc.gridy = 1;
        fieldsPanel.add(new JLabel("Amount:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        amountField = new JTextField(10);
        fieldsPanel.add(amountField, gbc);
        
        // Add description field
        gbc.gridx = 0;
        gbc.gridy = 2;
        fieldsPanel.add(new JLabel("Description:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        descriptionField = new JTextField(20);
        fieldsPanel.add(descriptionField, gbc);
        
        // Add category field
        gbc.gridx = 0;
        gbc.gridy = 3;
        fieldsPanel.add(new JLabel("Category:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        categoryComboBox = new JComboBox<>();
        for (String category : mainFrame.getSettings().getDefaultCategories()) {
            categoryComboBox.addItem(category);
        }
        fieldsPanel.add(categoryComboBox, gbc);
        
        // Add participant field
        gbc.gridx = 0;
        gbc.gridy = 4;
        fieldsPanel.add(new JLabel("Participant:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 4;
        participantField = new JTextField(20);
        fieldsPanel.add(participantField, gbc);
        
        // Add notes field
        gbc.gridx = 0;
        gbc.gridy = 5;
        fieldsPanel.add(new JLabel("Notes:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 5;
        notesField = new JTextField(20);
        fieldsPanel.add(notesField, gbc);
        
        // Add transaction type radio buttons
        gbc.gridx = 0;
        gbc.gridy = 6;
        fieldsPanel.add(new JLabel("Type:"), gbc);
        
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
        fieldsPanel.add(radioPanel, gbc);
        
        // Add fields panel to form panel
        formPanel.add(fieldsPanel, BorderLayout.CENTER);
        
        // Create form button panel
        JPanel formButtonPanel = new JPanel();
        formButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        // Add buttons to form button panel
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearForm());
        
        JButton addButton = new JButton("Add");
        addButton.addActionListener(e -> addTransaction());
        
        formButtonPanel.add(clearButton);
        formButtonPanel.add(addButton);
        
        // Add form button panel to form panel
        formPanel.add(formButtonPanel, BorderLayout.SOUTH);
        
        // Add form panel to content panel
        contentPanel.add(formPanel, BorderLayout.SOUTH);
        
        // Add content panel to main panel
        add(contentPanel, BorderLayout.CENTER);
    }
    
    /**
     * Loads transactions from the data file and displays them in the table.
     */
    private void loadTransactions() {
        // Clear the table
        tableModel.setRowCount(0);
        
        // Load transactions
        List<Transaction> transactions = transactionService.getAllTransactions();
        
        // Add transactions to the table
        for (Transaction transaction : transactions) {
            Object[] row = {
                transaction.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getCategory(),
                transaction.getParticipant(),
                transaction.isExpense() ? "Expense" : "Income"
            };
            tableModel.addRow(row);
        }
    }
    
    /**
     * Adds a new transaction based on the form data.
     */
    private void addTransaction() {
        try {
            // Parse form data
            LocalDate date = LocalDate.parse(dateField.getText(), DateTimeFormatter.ISO_LOCAL_DATE);
            double amount = Double.parseDouble(amountField.getText());
            String description = descriptionField.getText();
            String category = (String) categoryComboBox.getSelectedItem();
            String participant = participantField.getText();
            String notes = notesField.getText();
            boolean isExpense = expenseRadio.isSelected();
            
            // Create transaction
            Transaction transaction = new Transaction(date, amount, description, category, participant, notes, isExpense);
            
            // Save transaction
            transactionService.addTransaction(transaction);
            
            // Reload transactions
            loadTransactions();
            
            // Clear form
            clearForm();
            
            // Show success message
            JOptionPane.showMessageDialog(this, "Transaction added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            // Show error message
            JOptionPane.showMessageDialog(this, "Error adding transaction: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Deletes the selected transaction.
     */
    private void deleteSelectedTransaction() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a transaction to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Confirm deletion
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this transaction?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Delete transaction
        List<Transaction> transactions = transactionService.getAllTransactions();
        if (selectedRow < transactions.size()) {
            Transaction transaction = transactions.get(selectedRow);
            transactionService.deleteTransaction(transaction);
            
            // Reload transactions
            loadTransactions();
            
            // Show success message
            JOptionPane.showMessageDialog(this, "Transaction deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Edits the selected transaction.
     */
    private void editSelectedTransaction() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a transaction to edit.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Get selected transaction
        List<Transaction> transactions = transactionService.getAllTransactions();
        if (selectedRow < transactions.size()) {
            Transaction transaction = transactions.get(selectedRow);
            
            // Fill form with transaction data
            dateField.setText(transaction.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
            amountField.setText(String.valueOf(transaction.getAmount()));
            descriptionField.setText(transaction.getDescription());
            categoryComboBox.setSelectedItem(transaction.getCategory());
            participantField.setText(transaction.getParticipant());
            notesField.setText(transaction.getNotes());
            if (transaction.isExpense()) {
                expenseRadio.setSelected(true);
            } else {
                incomeRadio.setSelected(true);
            }
            
            // Delete the transaction (will be replaced when user clicks Add)
            transactionService.deleteTransaction(transaction);
            
            // Reload transactions
            loadTransactions();
        }
    }
    
    /**
     * Imports transactions from a CSV file.
     */
    private void importCsv() {
        // Create file chooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Import CSV File");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        
        // Show file chooser
        int result = fileChooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        
        // Get selected file
        File file = fileChooser.getSelectedFile();
        
        // Show import dialog
        ImportCsvDialog dialog = new ImportCsvDialog(mainFrame, file.getAbsolutePath(), transactionService);
        dialog.setVisible(true);
        
        // Reload transactions
        loadTransactions();
    }
    
    /**
     * Clears the form fields.
     */
    private void clearForm() {
        dateField.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        amountField.setText("");
        descriptionField.setText("");
        categoryComboBox.setSelectedIndex(0);
        participantField.setText("");
        notesField.setText("");
        expenseRadio.setSelected(true);
    }
}
