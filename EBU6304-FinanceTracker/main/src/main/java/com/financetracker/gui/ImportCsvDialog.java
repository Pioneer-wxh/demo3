package com.financetracker.gui;

import com.financetracker.service.TransactionService;
import java.util.List;
import javax.swing.*;

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
    
    
}
