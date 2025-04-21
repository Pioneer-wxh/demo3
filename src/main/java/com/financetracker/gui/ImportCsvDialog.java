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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        
        // 先读取CSV头，确保headers不为null
        this.headers = readCsvHeaders();
        if (this.headers == null || this.headers.isEmpty()) {
            // 如果读取失败，创建一个带有默认值的列表
            this.headers = new ArrayList<>();
            this.headers.add("Date");
            this.headers.add("Amount");
            this.headers.add("Description");
            this.headers.add("Category");
        }
        
        // 初始化组件
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
        String[] commonDateFormats = {
            "yyyy-MM-dd", "MM/dd/yyyy", "dd/MM/yyyy", "yyyy/MM/dd", "dd-MM-yyyy", "dd.MM.yyyy"
        };
        JComboBox<String> dateFormatComboBox = new JComboBox<>(commonDateFormats);
        dateFormatField = new JTextField(commonDateFormats[0]);
        dateFormatField.setColumns(10);
        
        JPanel dateFormatPanel = new JPanel(new BorderLayout());
        dateFormatPanel.add(dateFormatField, BorderLayout.CENTER);
        dateFormatPanel.add(dateFormatComboBox, BorderLayout.EAST);
        
        dateFormatComboBox.addActionListener(e -> {
            dateFormatField.setText((String) dateFormatComboBox.getSelectedItem());
        });
        
        contentPanel.add(dateFormatPanel, gbc);
        
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
        
        // 添加预览按钮
        JButton previewButton = new JButton("Preview");
        previewButton.addActionListener(e -> previewTransactions());
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(importButton);
        buttonPanel.add(previewButton, 0);
        
        // Add button panel to dialog
        add(buttonPanel, BorderLayout.SOUTH);
        
        // 尝试自动检测列
        autoDetectColumns(headers);
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
                // 处理不同类型的分隔符（逗号或分号）
                String separator = line.contains(",") ? "," : ";";
                String[] parts = line.split(separator);
                for (String part : parts) {
                    headers.add(part.trim());
                }
                
                // 只有在成功读取头信息后才尝试自动检测列
                if (!headers.isEmpty()) {
                    return headers;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error reading CSV file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        return headers;
    }
    
    /**
     * 自动检测并设置最可能的列映射
     */
    private void autoDetectColumns(List<String> headers) {
        // 如果headers为空或组件尚未初始化，则直接返回
        if (headers == null || headers.isEmpty() || dateColumnComboBox == null || 
            amountColumnComboBox == null || descriptionColumnComboBox == null || 
            categoryColumnComboBox == null) {
            return;
        }
        
        // 常见日期列名映射
        String[] dateKeywords = {"date", "time", "day", "日期", "时间", "transaction date"};
        // 常见金额列名映射
        String[] amountKeywords = {"amount", "sum", "value", "price", "金额", "数额", "价格", "transaction amount"};
        // 常见描述列名映射
        String[] descKeywords = {"description", "desc", "note", "memo", "名称", "描述", "备注", "摘要", "transaction description"};
        // 常见类别列名映射
        String[] categoryKeywords = {"category", "type", "group", "类别", "类型", "分类"};
        
        // 查找最匹配的日期列
        int dateIndex = findBestMatch(headers, dateKeywords);
        if (dateIndex >= 0) {
            dateColumnComboBox.setSelectedItem(headers.get(dateIndex));
        }
        
        // 查找最匹配的金额列
        int amountIndex = findBestMatch(headers, amountKeywords);
        if (amountIndex >= 0) {
            amountColumnComboBox.setSelectedItem(headers.get(amountIndex));
        }
        
        // 查找最匹配的描述列
        int descIndex = findBestMatch(headers, descKeywords);
        if (descIndex >= 0) {
            descriptionColumnComboBox.setSelectedItem(headers.get(descIndex));
        }
        
        // 查找最匹配的类别列
        int categoryIndex = findBestMatch(headers, categoryKeywords);
        if (categoryIndex >= 0) {
            categoryColumnComboBox.setSelectedItem(headers.get(categoryIndex));
        }
        
        // 查找日期格式提示
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // 跳过标题行
            reader.readLine();
            
            // 读取第一个数据行
            String dataLine = reader.readLine();
            if (dataLine != null && dateIndex >= 0) {
                String[] values = dataLine.split(dataLine.contains(",") ? "," : ";");
                if (values.length > dateIndex) {
                    String dateValue = values[dateIndex].trim();
                    dateFormatField.setText(guessDateFormat(dateValue));
                }
            }
        } catch (IOException e) {
            // 忽略错误，使用默认日期格式
        }
    }
    
    /**
     * 在标题列表中查找最匹配的列名
     */
    private int findBestMatch(List<String> headers, String[] keywords) {
        // 首先尝试精确匹配
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i).toLowerCase();
            for (String keyword : keywords) {
                if (header.equals(keyword)) {
                    return i;
                }
            }
        }
        
        // 然后尝试部分匹配
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i).toLowerCase();
            for (String keyword : keywords) {
                if (header.contains(keyword) || keyword.contains(header)) {
                    return i;
                }
            }
        }
        
        return -1; // 未找到匹配项
    }
    
    /**
     * 根据日期字符串猜测日期格式
     */
    private String guessDateFormat(String dateStr) {
        String[][] formats = {
            {"yyyy-MM-dd", "\\d{4}-\\d{1,2}-\\d{1,2}"},
            {"MM/dd/yyyy", "\\d{1,2}/\\d{1,2}/\\d{4}"},
            {"dd/MM/yyyy", "\\d{1,2}/\\d{1,2}/\\d{4}"},
            {"yyyy/MM/dd", "\\d{4}/\\d{1,2}/\\d{1,2}"},
            {"dd-MM-yyyy", "\\d{1,2}-\\d{1,2}-\\d{4}"},
            {"dd.MM.yyyy", "\\d{1,2}\\.\\d{1,2}\\.\\d{4}"}
        };
        
        for (String[] format : formats) {
            if (dateStr.matches(format[1])) {
                return format[0];
            }
        }
        
        return "yyyy-MM-dd"; // 默认格式
    }
    
    /**
     * 预览即将导入的交易记录
     */
    private void previewTransactions() {
        try {
            final String dateColumn = (String) dateColumnComboBox.getSelectedItem();
            final String amountColumn = (String) amountColumnComboBox.getSelectedItem();
            final String descriptionColumn = (String) descriptionColumnComboBox.getSelectedItem();
            String categoryColumn = (String) categoryColumnComboBox.getSelectedItem();
            final String dateFormat = dateFormatField.getText();
            
            if (dateColumn == null || amountColumn == null || descriptionColumn == null) {
                JOptionPane.showMessageDialog(this, "Please select all required columns.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 如果类别列是空字符串，则传入null
            final String finalCategoryColumn = (categoryColumn != null && categoryColumn.isEmpty()) ? null : categoryColumn;
            
            // 创建预览对话框
            JDialog previewDialog = new JDialog(this, "Import Preview", true);
            previewDialog.setLayout(new BorderLayout());
            previewDialog.setSize(600, 400);
            previewDialog.setLocationRelativeTo(this);
            
            // 创建表格模型
            String[] columns = {"Date", "Amount", "Description", "Category", "Type"};
            Object[][] data = new Object[5][5]; // 最多预览5条记录
            
            // 读取CSV数据填充表格
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                // 跳过标题行
                reader.readLine();
                
                // 存储列索引
                Map<String, Integer> headerIndices = new HashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    headerIndices.put(headers.get(i), i);
                }
                
                // 读取数据行
                String line;
                int row = 0;
                while ((line = reader.readLine()) != null && row < 5) {
                    // 解析CSV行
                    String separator = line.contains(",") ? "," : ";";
                    String[] values = line.split(separator);
                    
                    try {
                        // 读取日期
                        if (headerIndices.containsKey(dateColumn) && headerIndices.get(dateColumn) < values.length) {
                            data[row][0] = values[headerIndices.get(dateColumn)].trim();
                        }
                        
                        // 读取金额
                        if (headerIndices.containsKey(amountColumn) && headerIndices.get(amountColumn) < values.length) {
                            data[row][1] = values[headerIndices.get(amountColumn)].trim();
                        }
                        
                        // 读取描述
                        if (headerIndices.containsKey(descriptionColumn) && headerIndices.get(descriptionColumn) < values.length) {
                            data[row][2] = values[headerIndices.get(descriptionColumn)].trim();
                        }
                        
                        // 读取类别（如果有）
                        String category = "Auto-detect";
                        if (finalCategoryColumn != null && !finalCategoryColumn.isEmpty() && 
                            headerIndices.containsKey(finalCategoryColumn) && 
                            headerIndices.get(finalCategoryColumn) < values.length) {
                            category = values[headerIndices.get(finalCategoryColumn)].trim();
                        }
                        data[row][3] = category;
                        
                        // 交易类型
                        data[row][4] = expenseRadio.isSelected() ? "Expense" : "Income";
                        
                        row++;
                    } catch (Exception e) {
                        // 忽略错误的行
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(previewDialog, "Error previewing data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            
            // 创建表格
            JTable previewTable = new JTable(data, columns);
            JScrollPane scrollPane = new JScrollPane(previewTable);
            previewDialog.add(scrollPane, BorderLayout.CENTER);
            
            // 添加按钮面板
            JPanel buttonPanel = new JPanel();
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> previewDialog.dispose());
            buttonPanel.add(closeButton);
            previewDialog.add(buttonPanel, BorderLayout.SOUTH);
            
            // 显示对话框
            previewDialog.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error generating preview: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Imports transactions from the CSV file.
     */
    private void importTransactions() {
        final String dateColumn = (String) dateColumnComboBox.getSelectedItem();
        final String amountColumn = (String) amountColumnComboBox.getSelectedItem();
        final String descriptionColumn = (String) descriptionColumnComboBox.getSelectedItem();
        String categoryColumnTemp = (String) categoryColumnComboBox.getSelectedItem();
        if (categoryColumnTemp != null && categoryColumnTemp.isEmpty()) {
            categoryColumnTemp = null;
        }
        final String categoryColumn = categoryColumnTemp;
        final String dateFormat = dateFormatField.getText();
        final boolean isExpense = expenseRadio.isSelected();
        
        if (dateColumn == null || amountColumn == null || descriptionColumn == null) {
            JOptionPane.showMessageDialog(this, "请选择所有必需的列。", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // 添加导出CSV选项
        int exportOption = JOptionPane.showConfirmDialog(
            this,
            "是否同时将导入的交易记录保存到本地CSV数据库？\n这将允许您以后直接从CSV文件中读取数据。",
            "导出到CSV",
            JOptionPane.YES_NO_OPTION
        );
        final boolean exportToCsv = (exportOption == JOptionPane.YES_OPTION);
        
        // 显示进度对话框
        final JDialog progressDialog = new JDialog(this, "导入中...", true);
        progressDialog.setLayout(new BorderLayout());
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        JLabel statusLabel = new JLabel("正在导入交易记录，请稍候...");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        progressDialog.add(statusLabel, BorderLayout.NORTH);
        progressDialog.add(progressBar, BorderLayout.CENTER);
        progressDialog.setSize(300, 100);
        progressDialog.setLocationRelativeTo(this);
        
        // 在后台线程中执行导入
        SwingWorker<Integer, Void> worker = new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return transactionService.importFromCsv(filePath, dateColumn, amountColumn, 
                                                     descriptionColumn, categoryColumn, 
                                                     dateFormat, isExpense);
            }
            
            @Override
            protected void done() {
                progressDialog.dispose();
                try {
                    int count = get();
                    
                    if (exportToCsv && count > 0) {
                        // 导出到CSV数据库
                        boolean exported = transactionService.createBackup();
                        if (exported) {
                            JOptionPane.showMessageDialog(ImportCsvDialog.this, 
                                "成功导入 " + count + " 条交易记录，并已保存到本地CSV数据库。", 
                                "导入成功", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(ImportCsvDialog.this, 
                                "成功导入 " + count + " 条交易记录，但导出到CSV数据库失败。", 
                                "部分成功", JOptionPane.WARNING_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(ImportCsvDialog.this, 
                            "成功导入 " + count + " 条交易记录。", 
                            "导入成功", JOptionPane.INFORMATION_MESSAGE);
                    }
                    
                    dispose();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ImportCsvDialog.this, 
                        "导入过程中出错: " + e.getMessage(), 
                        "导入错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
        progressDialog.setVisible(true);
    }
}
