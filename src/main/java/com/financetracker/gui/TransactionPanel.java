package com.financetracker.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import com.financetracker.model.Settings;
import com.financetracker.model.Transaction;
import com.financetracker.service.CsvBatchImporter;
import com.financetracker.service.SettingsService;
import com.financetracker.service.TransactionCsvExporter;
import com.financetracker.service.TransactionService;
import com.financetracker.util.PathUtil;

/**
 * Panel for managing transactions.
 */
public class TransactionPanel extends JPanel {
    
    private TransactionService transactionService;
    private SettingsService settingsService;
    private ActionListener panelNavigationListener;
    private CsvBatchImporter csvBatchImporter;
    private TransactionCsvExporter csvExporter;
    
    private JTable transactionTable;
    private DefaultTableModel tableModel;
    private JSpinner dateSpinner;
    private JTextField amountField;
    private JTextField descriptionField;
    private JComboBox<String> categoryComboBox;
    private JTextField participantField;
    private JTextField notesField;
    private JRadioButton expenseRadio;
    private JRadioButton incomeRadio;
    private JTextField editingTransactionIdField;
    
    /**
     * Constructor for TransactionPanel.
     * 
     * @param transactionService The transaction service
     * @param settingsService The settings service
     * @param panelNavigationListener The panel navigation listener
     * @param csvBatchImporter The CSV batch importer
     * @param csvExporter The CSV exporter
     */
    public TransactionPanel(TransactionService transactionService, 
                              SettingsService settingsService, 
                              ActionListener panelNavigationListener,
                              CsvBatchImporter csvBatchImporter,
                              TransactionCsvExporter csvExporter) {
        this.transactionService = transactionService;
        this.settingsService = settingsService;
        this.panelNavigationListener = panelNavigationListener;
        this.csvBatchImporter = csvBatchImporter;
        this.csvExporter = csvExporter;
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
        
        JLabel titleLabel = new JLabel("Transaction Record Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JButton homeButton = new JButton("HOME");
        homeButton.setActionCommand("home");
        homeButton.addActionListener(e -> {
            if (panelNavigationListener != null) {
                panelNavigationListener.actionPerformed(e);
            }
        });
        headerPanel.add(homeButton, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);

        // Hidden field for editing ID
        editingTransactionIdField = new JTextField();

        // Create table panel (Side A content)
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Transactions"));
        
        String[] columns = {"ID", "Date", "Amount", "Description", "Category", "Participant", "Type"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        transactionTable = new JTable(tableModel);
        transactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        transactionTable.getColumnModel().getColumn(0).setMinWidth(0);
        transactionTable.getColumnModel().getColumn(0).setMaxWidth(0);
        transactionTable.getColumnModel().getColumn(0).setPreferredWidth(0);
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel tableButtonPanel = new JPanel();
        tableButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton deleteButton = new JButton("删除选中项");
        deleteButton.addActionListener(e -> deleteSelectedTransactions());
        JButton editButton = new JButton("编辑选中项");
        editButton.addActionListener(e -> populateFormForEdit());
        JButton importButton = new JButton("导入CSV");
        importButton.addActionListener(e -> importCsv());
        JButton exportButton = new JButton("导出CSV");
        exportButton.addActionListener(e -> exportCsv());
        tableButtonPanel.add(importButton);
        tableButtonPanel.add(exportButton);
        tableButtonPanel.add(editButton);
        tableButtonPanel.add(deleteButton);
        tablePanel.add(tableButtonPanel, BorderLayout.SOUTH);

        // Create form panel (Side B content)
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BorderLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Add/Edit Transaction"));
        
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Date field
        gbc.gridx = 0; gbc.gridy = 0; fieldsPanel.add(new JLabel("Date:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        Date now = new Date();
        SpinnerDateModel dateModel = new SpinnerDateModel(now, null, null, Calendar.DAY_OF_MONTH);
        dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        fieldsPanel.add(dateSpinner, gbc);
        
        // Amount field
        gbc.gridx = 0; gbc.gridy = 1; fieldsPanel.add(new JLabel("Amount:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        amountField = new JTextField(10);
        fieldsPanel.add(amountField, gbc);
        
        // Description field
        gbc.gridx = 0; gbc.gridy = 2; fieldsPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        descriptionField = new JTextField(20);
        fieldsPanel.add(descriptionField, gbc);
        
        // Category field
        gbc.gridx = 0; gbc.gridy = 3; fieldsPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        categoryComboBox = new JComboBox<>();
        fieldsPanel.add(categoryComboBox, gbc);
        
        // Participant field
        gbc.gridx = 0; gbc.gridy = 4; fieldsPanel.add(new JLabel("Participant:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4;
        participantField = new JTextField(20);
        fieldsPanel.add(participantField, gbc);
        
        // Notes field
        gbc.gridx = 0; gbc.gridy = 5; fieldsPanel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5;
        notesField = new JTextField(20);
        fieldsPanel.add(notesField, gbc);
        
        // Type radio buttons
        gbc.gridx = 0; gbc.gridy = 6; fieldsPanel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1; gbc.gridy = 6;
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        expenseRadio = new JRadioButton("Expense");
        incomeRadio = new JRadioButton("Income");
        expenseRadio.setSelected(true);
        ButtonGroup group = new ButtonGroup();
        group.add(expenseRadio);
        group.add(incomeRadio);
        
        updateCategoryDropdown();
        
        ActionListener categoryUpdateListener = e -> updateCategoryDropdown();
        expenseRadio.addActionListener(categoryUpdateListener);
        incomeRadio.addActionListener(categoryUpdateListener);
        radioPanel.add(expenseRadio);
        radioPanel.add(incomeRadio);
        fieldsPanel.add(radioPanel, gbc);
        
        formPanel.add(fieldsPanel, BorderLayout.CENTER);
        
        JPanel formButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton clearButton = new JButton("Clear Form");
        clearButton.addActionListener(e -> clearForm());
        JButton saveButton = new JButton("Save Transaction");
        saveButton.addActionListener(e -> saveTransaction());
        formButtonPanel.add(clearButton);
        formButtonPanel.add(saveButton);
        formPanel.add(formButtonPanel, BorderLayout.SOUTH);

        // Create main content panel using JSplitPane for side-by-side layout
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setResizeWeight(0.65); // Give more space to the table initially

        // --- Side A: Transaction Table and Actions ---
        mainSplitPane.setLeftComponent(tablePanel); // Use tablePanel directly

        // --- Side B: Add Transaction Form ---
        mainSplitPane.setRightComponent(formPanel);

        // Add the split pane to the main panel
        add(mainSplitPane, BorderLayout.CENTER);
    }
    
    /**
     * Loads transactions from the data file and displays them in the table.
     */
    public void loadTransactions() {
        // Clear the table
        tableModel.setRowCount(0);
        
        // Load transactions
        List<Transaction> transactions = transactionService.getAllTransactions();
        
        // Add transactions to the table
        for (Transaction transaction : transactions) {
            tableModel.addRow(new Object[]{
                transaction.getId(),
                transaction.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getCategory(),
                transaction.getParticipant(),
                transaction.isExpense() ? "Expense" : "Income"
            });
        }
    }
    
    /**
     * 导入CSV文件，支持单文件和多文件/目录导入
     */
    private void importCsv() {
        if (csvBatchImporter == null) {
            JOptionPane.showMessageDialog(this, "CSV Importer not available.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JFileChooser fileChooser = new JFileChooser(PathUtil.getDataDir().toFile());
        fileChooser.setDialogTitle("Import CSV File(s)");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        fileChooser.setMultiSelectionEnabled(true); // Allow multiple files

        int result = fileChooser.showOpenDialog(SwingUtilities.getWindowAncestor(this));
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();
            if (selectedFiles.length == 0) return;

            // TODO: Restore SwingWorker for background processing and progress dialog.
            // This is a simplified synchronous call for now.
            try {
                List<String> filePaths = new ArrayList<>();
                for (File f : selectedFiles) filePaths.add(f.getAbsolutePath());

                CsvBatchImporter.ImportResult importResult = csvBatchImporter.importCsvFiles(selectedFiles);
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), 
                    String.format("Import Complete. Success Files: %d, Failed Files: %d, Total Records: %d", 
                                  importResult.getSuccessFileCount(), importResult.getFailedFileCount(), importResult.getTotalRecordCount()), 
                    "CSV Import Result", JOptionPane.INFORMATION_MESSAGE);
                loadTransactions(); 
            } catch (Exception e) {
                 JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), 
                    "Error during CSV import: " + e.getMessage(), "Import Error", JOptionPane.ERROR_MESSAGE);
                 e.printStackTrace();
            }
        }
    }
    
    /**
     * 导出交易记录到CSV文件，支持整体导出和按月导出
     */
    private void exportCsv() {
        if (csvExporter == null) {
            JOptionPane.showMessageDialog(this, "CSV Exporter not available.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JFileChooser fileChooser = new JFileChooser(PathUtil.getExportDir().toFile());
        fileChooser.setDialogTitle("Export Transactions to CSV");
        fileChooser.setSelectedFile(new File("transactions_export.csv"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));

        int result = fileChooser.showSaveDialog(SwingUtilities.getWindowAncestor(this));
        if (result == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            // TODO: Restore SwingWorker for background processing.
            try {
                boolean success = csvExporter.exportTransactionsToPath(transactionService.getAllTransactions(), fileToSave.getAbsolutePath());
                if (success) {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), 
                        "Transactions exported successfully to " + fileToSave.getAbsolutePath(), 
                        "Export Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), 
                        "Failed to export transactions.", "Export Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                 JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), 
                    "Error during CSV export: " + e.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
                 e.printStackTrace();
            }
        }
    }
    
    /**
     * Clears the form fields.
     */
    private void clearForm() {
        // 重置日期选择器为当前日期
        dateSpinner.setValue(new Date());
        
        amountField.setText("");
        descriptionField.setText("");
        categoryComboBox.setSelectedIndex(0);
        participantField.setText("");
        notesField.setText("");
        expenseRadio.setSelected(true);
        editingTransactionIdField.setText("");
    }
    
    /**
     * 删除选中的交易记录，支持单个或批量删除
     */
    private void deleteSelectedTransactions() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a transaction to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog( SwingUtilities.getWindowAncestor(this), // Dialog ownership 
            "Are you sure you want to delete the selected transaction?", 
            "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            String id = (String) tableModel.getValueAt(selectedRow, 0);
            if (transactionService.deleteTransaction(id)) { // deleteTransaction now takes String id
                loadTransactions(); 
                JOptionPane.showMessageDialog(this, "Transaction deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete transaction.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Updates the category dropdown based on the selected transaction type (Expense/Income).
     */
    public void updateCategoryDropdown() {
        if (settingsService == null) return;
        Settings settings = settingsService.getSettings();
        if (settings == null || categoryComboBox == null) return;
        String previouslySelected = (String) categoryComboBox.getSelectedItem();
        List<String> categories = expenseRadio.isSelected() ? settings.getExpenseCategories() : settings.getIncomeCategories();
        categoryComboBox.removeAllItems();
        if (categories != null) {
            for (String category : categories) {
                categoryComboBox.addItem(category);
            }
        }
        // Try to re-select if still valid
        if (previouslySelected != null) {
            for (int i = 0; i < categoryComboBox.getItemCount(); i++) {
                if (previouslySelected.equals(categoryComboBox.getItemAt(i))) {
                    categoryComboBox.setSelectedIndex(i);
                    break;
                }
            }
        }
    }
    
    /**
     * 刷新类别下拉列表
     */
    public void refreshCategoryList() {
        updateCategoryDropdown();
    }

    // Placeholder for theming
    public void applyTheme(Settings settings) {
        boolean isDark = settings.isDarkModeEnabled();
        this.setBackground(isDark ? new Color(50, 50, 55) : UIManager.getColor("Panel.background"));
        if (transactionTable != null && transactionTable.getParent() instanceof JViewport) {
            ((JViewport)transactionTable.getParent()).setBackground(isDark ? new Color(45,45,45) : Color.WHITE);
            transactionTable.setBackground(isDark ? new Color(60,63,65) : Color.WHITE);
            transactionTable.setForeground(isDark ? Color.LIGHT_GRAY : Color.BLACK);
            transactionTable.getTableHeader().setBackground(isDark ? new Color(60,63,80) : new Color(220,220,220));
            transactionTable.getTableHeader().setForeground(isDark ? Color.LIGHT_GRAY : Color.BLACK);
        }
        // TODO: Theme form fields more specifically if UIManager defaults are not sufficient
    }

    private void saveTransaction() {
        try {
            Date selectedDate = (Date) dateSpinner.getValue();
            LocalDate date = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            double amount = Double.parseDouble(amountField.getText());
            String description = descriptionField.getText();
            String category = (String) categoryComboBox.getSelectedItem();
            String participant = participantField.getText();
            String notes = notesField.getText();
            boolean isExpense = expenseRadio.isSelected();
            String idToSave = editingTransactionIdField.getText();

            if (description.trim().isEmpty() || category == null || category.trim().isEmpty()) {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), "Description and Category are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Transaction tx;
            if (idToSave != null && !idToSave.trim().isEmpty()) { 
                // 更新现有交易
                // 使用6参数构造函数 Transaction(String id, LocalDate date, double amount, String description, String category, boolean isExpense)
                tx = new Transaction(idToSave, date, amount, description, category, isExpense);
                // 单独设置 participant 和 notes
                tx.setParticipant(participant);
                tx.setNotes(notes);
                // isExpense 已在构造函数中根据传入值设置，但表单的选项应优先
                tx.setExpense(isExpense); // 确保使用表单中的isExpense状态

                if (transactionService.updateTransaction(tx)) {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), "Transaction updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), "Failed to update transaction.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else { 
                // 添加新交易
                // 使用7参数构造函数 Transaction(LocalDate date, double amount, String description, String category, String participant, String notes, boolean isExpense)
                // ID 会在 Transaction 类内部自动生成
                tx = new Transaction(date, amount, description, category, participant, notes, isExpense);
                if (transactionService.addTransaction(tx)) {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), "Transaction added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                     JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), "Failed to add transaction.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            loadTransactions();
            clearForm();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), "Invalid amount format.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), "Error saving transaction: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void populateFormForEdit() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), "Please select a transaction to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // 从tableModel获取ID，第0列是ID
        String id = (String) tableModel.getValueAt(selectedRow, 0); 
        
        // 从服务获取最新的交易信息
        Optional<Transaction> txOptional = transactionService.getTransactionById(id);
        
        if (txOptional.isPresent()) {
            Transaction tx = txOptional.get();
            editingTransactionIdField.setText(tx.getId()); // 设置隐藏的ID字段
            dateSpinner.setValue(Date.from(tx.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            amountField.setText(String.valueOf(tx.getAmount()));
            descriptionField.setText(tx.getDescription());
            participantField.setText(tx.getParticipant() != null ? tx.getParticipant() : ""); // 处理null
            notesField.setText(tx.getNotes() != null ? tx.getNotes() : ""); // 处理null
            
            if (tx.isExpense()) {
                expenseRadio.setSelected(true);
            } else {
                incomeRadio.setSelected(true);
            }
            // updateCategoryDropdown 应该在设置 expense/income radio 之后调用，以加载正确的类别列表
            updateCategoryDropdown(); 
            categoryComboBox.setSelectedItem(tx.getCategory());
        } else {
             JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), "Could not find transaction details for ID: " + id, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
