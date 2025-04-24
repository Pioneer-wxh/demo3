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
 * 简化版本 - 自动处理CSV导入
 */
public class ImportCsvDialog extends JDialog {
    
    private String filePath;
    private TransactionService transactionService;
    private List<String> headers;
    
    /**
     * Constructor for ImportCsvDialog.
     * 
     * @param mainFrame The main frame
     * @param filePath The path to the CSV file
     * @param transactionService The transaction service
     */
    public ImportCsvDialog(MainFrame mainFrame, String filePath, TransactionService transactionService) {
        super(mainFrame, "导入CSV文件", true);
        this.filePath = filePath;
        this.transactionService = transactionService;
        
        // 先读取CSV头，确保headers不为null
        this.headers = readCsvHeaders();
        if (this.headers == null || this.headers.isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame, 
                "无法从CSV文件读取标题行，请确保文件格式正确。", 
                "读取错误", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        
        // 初始化简化的组件
        initComponents();
        setSize(400, 200);
        setLocationRelativeTo(mainFrame);
    }
    
    /**
     * Initializes the dialog components.
     */
    private void initComponents() {
        // 设置布局
        setLayout(new BorderLayout());
        
        // 创建主内容面板
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // 添加文件路径标签
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel filePathLabel = new JLabel("已选择文件: " + filePath);
        contentPanel.add(filePathLabel, gbc);
        
        // 添加说明标签
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel infoLabel = new JLabel("<html>系统将自动检测文件列结构并导入数据。<br>收入/支出类型将根据交易描述自动识别。</html>");
        contentPanel.add(infoLabel, gbc);
        
        // 添加进度条
        gbc.gridx = 0;
        gbc.gridy = 2;
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        contentPanel.add(progressBar, gbc);
        
        add(contentPanel, BorderLayout.CENTER);
        
        // 创建按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        
        // 添加按钮
        JButton cancelButton = new JButton("取消");
        cancelButton.addActionListener(e -> dispose());
        
        JButton importButton = new JButton("导入");
        importButton.addActionListener(e -> importTransactions());
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(importButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * 读取CSV文件头
     */
    private List<String> readCsvHeaders() {
        List<String> headers = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine();
            if (headerLine != null) {
                // 根据常见的CSV分隔符拆分标题行
                String separator = headerLine.contains(",") ? "," : ";";
                String[] headerArray = headerLine.split(separator);
                
                for (String header : headerArray) {
                    headers.add(header.trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return headers;
    }
    
    /**
     * 自动检测并获取最可能的列映射
     */
    private String[] autoDetectColumns() {
        // 常见日期列名映射
        String[] dateKeywords = {"date", "time", "day", "日期", "时间", "transaction date"};
        // 常见金额列名映射
        String[] amountKeywords = {"amount", "sum", "value", "price", "金额", "数额", "价格", "transaction amount"};
        // 常见描述列名映射
        String[] descKeywords = {"description", "desc", "note", "memo", "名称", "描述", "备注", "摘要", "transaction description"};
        // 常见类别列名映射
        String[] categoryKeywords = {"category", "type", "group", "类别", "类型", "分类"};
        
        // 查找最匹配的列
        int dateIndex = findBestMatch(headers, dateKeywords);
        int amountIndex = findBestMatch(headers, amountKeywords);
        int descIndex = findBestMatch(headers, descKeywords);
        int categoryIndex = findBestMatch(headers, categoryKeywords);
        
        // 如果找不到必要的列，尝试使用索引位置
        if (dateIndex < 0 && headers.size() > 0) dateIndex = 0;
        if (amountIndex < 0 && headers.size() > 1) amountIndex = 1;
        if (descIndex < 0 && headers.size() > 2) descIndex = 2;
        
        // 确定日期格式
        String dateFormat = "yyyy-MM-dd"; // 默认格式
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // 跳过标题行
            reader.readLine();
            
            // 读取第一个数据行
            String dataLine = reader.readLine();
            if (dataLine != null && dateIndex >= 0) {
                String[] values = dataLine.split(dataLine.contains(",") ? "," : ";");
                if (values.length > dateIndex) {
                    String dateValue = values[dateIndex].trim();
                    dateFormat = guessDateFormat(dateValue);
                }
            }
        } catch (IOException e) {
            // 忽略错误，使用默认日期格式
        }
        
        // 返回自动识别的列和日期格式
        String[] result = new String[5];
        result[0] = dateIndex >= 0 ? headers.get(dateIndex) : null;
        result[1] = amountIndex >= 0 ? headers.get(amountIndex) : null;
        result[2] = descIndex >= 0 ? headers.get(descIndex) : null;
        result[3] = categoryIndex >= 0 ? headers.get(categoryIndex) : null;
        result[4] = dateFormat;
        
        return result;
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
     * 导入交易记录
     */
    private void importTransactions() {
        // 自动检测列并获取映射
        String[] columnMappings = autoDetectColumns();
        
        final String dateColumn = columnMappings[0];
        final String amountColumn = columnMappings[1];
        final String descriptionColumn = columnMappings[2];
        final String categoryColumn = columnMappings[3];
        final String dateFormat = columnMappings[4];
        
        if (dateColumn == null || amountColumn == null || descriptionColumn == null) {
            JOptionPane.showMessageDialog(this, 
                "无法自动识别必要的列（日期、金额、描述）。请检查CSV文件格式。", 
                "导入错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // 使用SwingWorker在后台执行导入操作
        SwingWorker<Integer, Void> worker = new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                // 系统会自动检测交易类型，所以isExpense参数此处无关紧要
                return transactionService.importFromCsv(filePath, dateColumn, amountColumn, 
                                                     descriptionColumn, categoryColumn, dateFormat, false);
            }
            
            @Override
            protected void done() {
                try {
                    int count = get();
                    if (count > 0) {
                        JOptionPane.showMessageDialog(ImportCsvDialog.this, 
                            "成功导入 " + count + " 条交易记录。\n日期列: " + dateColumn + 
                            "\n金额列: " + amountColumn + "\n描述列: " + descriptionColumn + 
                            (categoryColumn != null ? "\n类别列: " + categoryColumn : "") + 
                            "\n日期格式: " + dateFormat, 
                            "导入成功", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(ImportCsvDialog.this, 
                            "没有导入任何交易记录。请检查CSV文件格式。", 
                            "导入警告", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(ImportCsvDialog.this, 
                        "导入时发生错误: " + e.getMessage(), 
                        "导入失败", JOptionPane.ERROR_MESSAGE);
                }
                dispose();
            }
        };
        
        worker.execute();
    }
}
