package com.financetracker.gui;

import com.financetracker.model.Transaction;
import com.financetracker.service.TransactionService;
import com.financetracker.service.CsvBatchImporter;
import com.financetracker.service.TransactionCsvExporter;
import com.financetracker.gui.AppIcon;
import com.financetracker.util.PathUtil;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Panel for managing transactions.
 */
public class TransactionPanel extends JPanel {
    
    private MainFrame mainFrame;
    private TransactionService transactionService;
    
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
    
    /**
     * Constructor for TransactionPanel.
     * 
     * @param mainFrame The main frame
     */
    public TransactionPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.transactionService = mainFrame.getTransactionService();
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
        homeButton.addActionListener(e -> mainFrame.showPanel("home"));
        headerPanel.add(homeButton, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);

        // --- Define tablePanel and formPanel BEFORE use in JSplitPane ---

        // Create table panel (Side A content)
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Transactions"));
        
        String[] columns = {"Date", "Amount", "Description", "Category", "Participant", "Type"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        transactionTable = new JTable(tableModel);
        transactionTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel tableButtonPanel = new JPanel();
        tableButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton deleteButton = new JButton("删除选中项");
        deleteButton.addActionListener(e -> deleteSelectedTransactions());
        JButton editButton = new JButton("编辑");
        editButton.addActionListener(e -> editSelectedTransaction());
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
        formPanel.setBorder(BorderFactory.createTitledBorder("Add Transaction"));
        
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
        updateCategoryDropdown(); 
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
        ActionListener categoryUpdateListener = e -> updateCategoryDropdown();
        expenseRadio.addActionListener(categoryUpdateListener);
        incomeRadio.addActionListener(categoryUpdateListener);
        radioPanel.add(expenseRadio);
        radioPanel.add(incomeRadio);
        fieldsPanel.add(radioPanel, gbc);
        
        formPanel.add(fieldsPanel, BorderLayout.CENTER);
        
        JPanel formButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearForm());
        JButton addButton = new JButton("Add");
        addButton.addActionListener(e -> addTransaction());
        formButtonPanel.add(clearButton);
        formButtonPanel.add(addButton);
        formPanel.add(formButtonPanel, BorderLayout.SOUTH);

        // Create main content panel using JSplitPane for side-by-side layout
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setResizeWeight(0.6); // Give more space to the table initially

        // --- Side A: Transaction Table and Actions ---
        //JPanel sideAPanel = new JPanel(new BorderLayout()); // Not strictly needed, tablePanel can be directly used
        //sideAPanel.add(tablePanel, BorderLayout.CENTER);
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
            // 从日期选择器获取日期
            Date selectedDate = (Date) dateSpinner.getValue();
            LocalDate date = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            
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
            
            // 设置日期选择器的值
            Date transactionDate = Date.from(transaction.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
            dateSpinner.setValue(transactionDate);
            
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
     * 导入CSV文件，支持单文件和多文件/目录导入
     */
    private void importCsv() {
        // 创建选项对话框
        String[] options = {"选择文件导入", "选择目录导入", "取消"};
        int choice = JOptionPane.showOptionDialog(this,
                "请选择导入方式：",
                "导入CSV",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
        
        // 不再需要硬编码的默认目录
        // File defaultDir = new File("E:\\code\\Java\\software_lab\\data");
        File defaultDir = null; // 或者设置为用户主目录等更合理的位置
        // File defaultDir = new File(System.getProperty("user.home")); // 示例：用户主目录

        // 根据选择执行相应操作
        if (choice == 0) {
            // 选择文件导入
            importSingleFile(defaultDir);
        } else if (choice == 1) {
            // 选择目录导入
            importDirectory(defaultDir);
        }
        // 如果是取消，不执行任何操作
    }
    
    /**
     * 导入单个CSV文件
     * @param defaultDir 默认目录
     */
    private void importSingleFile(File defaultDir) {
        try {
            // 创建文件选择器
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("选择CSV文件导入");
            fileChooser.setFileFilter(new FileNameExtensionFilter("CSV文件", "csv"));
            fileChooser.setMultiSelectionEnabled(true); // 允许多选文件
            
            // 设置默认目录 (如果提供了)
            if (defaultDir != null && defaultDir.exists() && defaultDir.isDirectory()) {
                fileChooser.setCurrentDirectory(defaultDir);
            }
            
            // 显示文件选择器
            int result = fileChooser.showOpenDialog(this);
            if (result != JFileChooser.APPROVE_OPTION) {
                return;
            }
            
            // 获取选择的文件
            File[] selectedFiles = fileChooser.getSelectedFiles();
            
            if (selectedFiles.length == 0) {
                JOptionPane.showMessageDialog(this, "未选择任何文件", "提示", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            if (selectedFiles.length == 1) {
                // 单文件导入
                File file = selectedFiles[0];
                if (!file.exists()) {
                    JOptionPane.showMessageDialog(this, "所选文件不存在", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // 显示导入对话框
                ImportCsvDialog dialog = new ImportCsvDialog(mainFrame, file.getAbsolutePath(), transactionService);
                dialog.setVisible(true);
                
                // 重新加载交易记录
                loadTransactions();
            } else {
                // 多文件导入
                processBatchImport(selectedFiles);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "导入CSV文件时出错: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * 从目录导入CSV文件
     * @param defaultDir 默认目录
     */
    private void importDirectory(File defaultDir) {
        // 创建目录选择器
        JFileChooser dirChooser = new JFileChooser();
        dirChooser.setDialogTitle("选择包含CSV文件的目录");
        dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        // 设置默认目录 (如果提供了)
        if (defaultDir != null && defaultDir.exists() && defaultDir.isDirectory()) {
            dirChooser.setCurrentDirectory(defaultDir);
            dirChooser.setSelectedFile(defaultDir);
        }
        
        // 显示目录选择器
        int result = dirChooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        
        // 获取选择的目录
        File selectedDir = dirChooser.getSelectedFile();
        final String directoryPath = selectedDir.getAbsolutePath();
        
        // 询问用户确认
        int confirm = JOptionPane.showConfirmDialog(this, 
                "是否要导入目录 " + directoryPath + " 中的所有CSV文件？\n" +
                "注意：这将自动检测并导入所有符合格式的CSV文件。", 
                "确认批量导入", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        // 显示进度对话框并执行导入
        processBatchImportFromDirectory(directoryPath);
    }
    
    /**
     * 处理多文件批量导入
     * @param files 要导入的文件数组
     */
    private void processBatchImport(File[] files) {
        // 询问用户确认
        int confirm = JOptionPane.showConfirmDialog(this, 
                "确定要导入选中的 " + files.length + " 个CSV文件吗？", 
                "确认批量导入", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        // 显示进度对话框
        final JDialog progressDialog = new JDialog(mainFrame, "批量导入中...", true);
        progressDialog.setLayout(new BorderLayout());
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        JLabel statusLabel = new JLabel("正在批量导入CSV文件，请稍候...");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        progressDialog.add(statusLabel, BorderLayout.NORTH);
        progressDialog.add(progressBar, BorderLayout.CENTER);
        progressDialog.setSize(400, 120);
        progressDialog.setLocationRelativeTo(this);
        
        // 在后台线程中执行导入
        SwingWorker<com.financetracker.service.CsvBatchImporter.ImportResult, Void> worker = 
            new SwingWorker<com.financetracker.service.CsvBatchImporter.ImportResult, Void>() {
                @Override
                protected com.financetracker.service.CsvBatchImporter.ImportResult doInBackground() throws Exception {
                    // 创建批量导入器
                    com.financetracker.service.CsvBatchImporter importer = 
                        new com.financetracker.service.CsvBatchImporter(transactionService);
                    
                    // 执行批量导入
                    return importer.importCsvFiles(files);
                }
                
                @Override
                protected void done() {
                    progressDialog.dispose();
                    try {
                        // 获取导入结果
                        com.financetracker.service.CsvBatchImporter.ImportResult result = get();
                        
                        // 根据结果显示不同的消息
                        if (result.getTotalRecordCount() > 0) {
                            JOptionPane.showMessageDialog(TransactionPanel.this, 
                                    "批量导入完成：\n" + result.toString(), 
                                    "导入成功", JOptionPane.INFORMATION_MESSAGE);
                            
                            // 重新加载交易记录
                            loadTransactions();
                        } else {
                            JOptionPane.showMessageDialog(TransactionPanel.this, 
                                    "未能成功导入任何记录。\n" +
                                    "原因可能是：\n" +
                                    "1. CSV文件格式不正确\n" +
                                    "2. CSV文件中没有有效的交易记录", 
                                    "导入失败", JOptionPane.WARNING_MESSAGE);
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(TransactionPanel.this, 
                                "批量导入过程中出错: " + e.getMessage(), 
                                "导入错误", JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                    }
                }
            };
        
        // 启动导入线程
        worker.execute();
        progressDialog.setVisible(true);
    }
    
    /**
     * 处理从目录批量导入
     * @param directoryPath 目录路径
     */
    private void processBatchImportFromDirectory(String directoryPath) {
        // 显示进度对话框
        final JDialog progressDialog = new JDialog(mainFrame, "批量导入中...", true);
        progressDialog.setLayout(new BorderLayout());
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        JLabel statusLabel = new JLabel("正在批量导入CSV文件，请稍候...");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        progressDialog.add(statusLabel, BorderLayout.NORTH);
        progressDialog.add(progressBar, BorderLayout.CENTER);
        progressDialog.setSize(400, 120);
        progressDialog.setLocationRelativeTo(this);
        
        // 在后台线程中执行导入
        SwingWorker<com.financetracker.service.CsvBatchImporter.ImportResult, Void> worker = 
            new SwingWorker<com.financetracker.service.CsvBatchImporter.ImportResult, Void>() {
                @Override
                protected com.financetracker.service.CsvBatchImporter.ImportResult doInBackground() throws Exception {
                    // 创建批量导入器
                    com.financetracker.service.CsvBatchImporter importer = 
                        new com.financetracker.service.CsvBatchImporter(transactionService);
                    
                    // 执行批量导入
                    return importer.importFromDirectory(directoryPath);
                }
                
                @Override
                protected void done() {
                    progressDialog.dispose();
                    try {
                        // 获取导入结果
                        com.financetracker.service.CsvBatchImporter.ImportResult result = get();
                        
                        // 根据结果显示不同的消息
                        if (result.getTotalRecordCount() > 0) {
                            JOptionPane.showMessageDialog(TransactionPanel.this, 
                                    "批量导入完成：\n" + result.toString(), 
                                    "导入成功", JOptionPane.INFORMATION_MESSAGE);
                            
                            // 重新加载交易记录
                            loadTransactions();
                        } else {
                            JOptionPane.showMessageDialog(TransactionPanel.this, 
                                    "未能成功导入任何记录。\n" +
                                    "原因可能是：\n" +
                                    "1. 目录中没有CSV文件\n" +
                                    "2. CSV文件格式不正确\n" +
                                    "3. CSV文件中没有有效的交易记录", 
                                    "导入失败", JOptionPane.WARNING_MESSAGE);
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(TransactionPanel.this, 
                                "批量导入过程中出错: " + e.getMessage(), 
                                "导入错误", JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                    }
                }
            };
        
        // 启动导入线程
        worker.execute();
        progressDialog.setVisible(true);
    }
    
    /**
     * 导出交易记录到CSV文件，支持整体导出和按月导出
     */
    private void exportCsv() {
        // 确保有交易记录可以导出
        List<Transaction> transactions = transactionService.getAllTransactions();
        if (transactions.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "没有交易记录可以导出。", 
                "警告", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 使用 PathUtil 获取绝对路径字符串
        String exportAllPath = PathUtil.getExportAllTransactionsPath().toString();
        String exportClassifyPath = PathUtil.getExportClassifyDirPath().toString();

        // 创建选项对话框
        String[] options = {"导出所有交易记录", "按月份导出", "取消"};
        int choice = JOptionPane.showOptionDialog(this,
                "请选择导出方式：",
                "导出CSV",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
        
        // 根据选择执行相应操作
        if (choice == 0) {
            // 导出所有交易记录
            boolean success = transactionService.exportAllTransactions();
            if (success) {
                JOptionPane.showMessageDialog(this, 
                    "交易记录已成功导出。\n文件位置: " + exportAllPath,
                    "导出成功", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "导出交易记录时出错，请稍后再试。", 
                    "导出失败", JOptionPane.ERROR_MESSAGE);
            }
        } else if (choice == 1) {
            // 按月份导出
            exportByMonth();
        }
        // 如果是取消，不执行任何操作
    }
    
    /**
     * 按月份导出交易记录
     */
    private void exportByMonth() {
        // 获取所有交易记录
        List<Transaction> transactions = transactionService.getAllTransactions();
        if (transactions.isEmpty()) {
             JOptionPane.showMessageDialog(this, 
                 "没有交易记录可以导出。", 
                 "警告", JOptionPane.WARNING_MESSAGE);
             return;
        }
        
        // 使用 PathUtil 获取按月导出的绝对路径
        final String directoryPath = PathUtil.getExportClassifyDirPath().toString();

        // 询问用户确认
        int confirm = JOptionPane.showConfirmDialog(this, 
                "是否要将交易记录按月份导出到目录 " + directoryPath + " 中？\n" +
                "注意：这将为每个月创建一个CSV文件。",
                "确认导出", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // 获取所有交易记录 (已在前面获取)
        // List<Transaction> transactions = transactionService.getAllTransactions();
        
        // 显示进度对话框
        final JDialog progressDialog = new JDialog(mainFrame, "导出中...", true);
        progressDialog.setLayout(new BorderLayout());
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        JLabel statusLabel = new JLabel("正在导出交易记录，请稍候...");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        progressDialog.add(statusLabel, BorderLayout.NORTH);
        progressDialog.add(progressBar, BorderLayout.CENTER);
        progressDialog.setSize(400, 120);
        progressDialog.setLocationRelativeTo(this);
        
        // 在后台线程中执行导出
        SwingWorker<com.financetracker.service.TransactionCsvExporter.ExportResult, Void> worker = 
            new SwingWorker<com.financetracker.service.TransactionCsvExporter.ExportResult, Void>() {
                @Override
                protected com.financetracker.service.TransactionCsvExporter.ExportResult doInBackground() throws Exception {
                    // 直接调用 Service 层的方法，不再需要创建 Exporter 实例
                    return transactionService.exportTransactionsByMonth();
                }
                
                @Override
                protected void done() {
                    progressDialog.dispose();
                    try {
                        // 获取导出结果
                        com.financetracker.service.TransactionCsvExporter.ExportResult result = get();
                        
                        // 显示结果消息
                        if (result.getSuccessFileCount() > 0) {
                            JOptionPane.showMessageDialog(TransactionPanel.this, 
                                    "导出完成：\n" + result.toString() + "\n" +
                                    "文件已保存到目录：\n" + directoryPath,
                                    "导出成功", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(TransactionPanel.this, 
                                    "导出失败，未能成功创建任何文件。", 
                                    "导出失败", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(TransactionPanel.this, 
                                "导出过程中出错: " + e.getMessage(), 
                                "导出错误", JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                    }
                }
            };
        
        // 启动导出线程
        worker.execute();
        progressDialog.setVisible(true);
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
    }
    
    /**
     * 删除选中的交易记录，支持单个或批量删除
     */
    private void deleteSelectedTransactions() {
        int[] selectedRows = transactionTable.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "请选择要删除的交易记录", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // 确认删除，根据所选行数显示不同的消息
        String confirmMessage = selectedRows.length == 1 
            ? "确定要删除选中的交易记录吗？" 
            : "确定要删除选中的 " + selectedRows.length + " 条交易记录吗？";
            
        int confirm = JOptionPane.showConfirmDialog(this, 
                confirmMessage, 
                "确认删除", JOptionPane.YES_NO_OPTION);
                
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        try {
            // 获取所有交易
            List<Transaction> transactions = transactionService.getAllTransactions();
            
            // 按降序排列索引，防止删除时索引变化
            java.util.Arrays.sort(selectedRows);
            for (int i = selectedRows.length - 1; i >= 0; i--) {
                int row = selectedRows[i];
                if (row < transactions.size()) {
                    Transaction transaction = transactions.get(row);
                    transactionService.deleteTransaction(transaction);
                }
            }
            
            // 重新加载交易
            loadTransactions();
            
            // 显示成功消息
            String successMessage = selectedRows.length == 1 
                ? "已成功删除交易记录" 
                : "已成功删除 " + selectedRows.length + " 条交易记录";
                
            JOptionPane.showMessageDialog(this, 
                    successMessage, 
                    "成功", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                    "删除交易记录时出错: " + e.getMessage(), 
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Updates the category dropdown based on the selected transaction type (Expense/Income).
     */
    public void updateCategoryDropdown() {
        if (categoryComboBox == null || mainFrame == null || mainFrame.getSettings() == null) {
            return; // Or handle error appropriately
        }

        String selectedCategory = (String) categoryComboBox.getSelectedItem();
        categoryComboBox.removeAllItems();

        List<String> categories;
        if (expenseRadio != null && expenseRadio.isSelected()) {
            categories = mainFrame.getSettings().getExpenseCategories();
        } else if (incomeRadio != null && incomeRadio.isSelected()) {
            categories = mainFrame.getSettings().getIncomeCategories();
        } else {
            // Default to expense categories if no radio button is somehow selected, or handle error
            categories = mainFrame.getSettings().getExpenseCategories();
        }

        if (categories != null) {
            for (String category : categories) {
                categoryComboBox.addItem(category);
            }
        }

        // Try to reselect the previously selected category if it exists in the new list
        if (selectedCategory != null) {
            for (int i = 0; i < categoryComboBox.getItemCount(); i++) {
                if (selectedCategory.equals(categoryComboBox.getItemAt(i))) {
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
        updateCategoryDropdown(); // Now calls the new method that respects transaction type
        // No need to explicitly save/restore selected item here, updateCategoryDropdown handles it.
    }
}
