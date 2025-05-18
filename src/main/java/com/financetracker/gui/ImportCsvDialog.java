package com.financetracker.gui;

import com.financetracker.service.CsvBatchImporter;
import com.financetracker.service.CsvBatchImporter.ImportResult;
import com.financetracker.service.TransactionService;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Dialog for importing transactions from a CSV file.
 * 简化版本 - 自动处理CSV导入, 现在委托给 CsvBatchImporter
 */
public class ImportCsvDialog extends JDialog {
    
    private String filePath;
    private CsvBatchImporter csvBatchImporter;
    
    /**
     * Constructor for ImportCsvDialog.
     * 
     * @param owner The owner window
     * @param filePath The path to the CSV file
     * @param csvBatchImporter The CSV batch importer service
     */
    public ImportCsvDialog(Window owner, String filePath, CsvBatchImporter csvBatchImporter) {
        super(owner, "导入CSV文件", ModalityType.APPLICATION_MODAL);
        this.filePath = filePath;
        this.csvBatchImporter = csvBatchImporter;
        
        initComponents();
        setSize(400, 200);
        setLocationRelativeTo(owner);
    }
    
    /**
     * Initializes the dialog components.
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel filePathLabel = new JLabel("已选择文件: " + filePath);
        contentPanel.add(filePathLabel, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel infoLabel = new JLabel("<html>系统将自动检测文件列结构并导入数据。<br>收入/支出类型将根据交易描述自动识别。</html>");
        contentPanel.add(infoLabel, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        contentPanel.add(progressBar, gbc);
        
        add(contentPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        
        JButton cancelButton = new JButton("取消");
        cancelButton.addActionListener(e -> dispose());
        
        JButton importButton = new JButton("导入");
        importButton.addActionListener(e -> importTransactionsAction());
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(importButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Action for importing transactions.
     */
    private void importTransactionsAction() {
        SwingWorker<ImportResult, Void> worker = new SwingWorker<ImportResult, Void>() {
            @Override
            protected ImportResult doInBackground() throws Exception {
                return csvBatchImporter.importSingleCsvFile(filePath);
            }
            
            @Override
            protected void done() {
                try {
                    ImportResult result = get();
                    if (result.getSuccessFileCount() > 0 && result.getTotalRecordCount() > 0) {
                        JOptionPane.showMessageDialog(ImportCsvDialog.this, 
                            "成功导入 " + result.getTotalRecordCount() + " 条交易记录来自文件： " + new File(filePath).getName(),
                            "导入成功", JOptionPane.INFORMATION_MESSAGE);
                    } else if (result.getTotalRecordCount() == 0 && result.getSuccessFileCount() > 0){
                         JOptionPane.showMessageDialog(ImportCsvDialog.this, 
                            "文件已处理，但没有新的交易记录被导入。可能记录已存在或无法解析。", 
                            "导入提醒", JOptionPane.INFORMATION_MESSAGE);
                    } else if (result.getFailedFileCount() > 0) {
                        JOptionPane.showMessageDialog(ImportCsvDialog.this, 
                            "文件 " + new File(filePath).getName() + " 导入失败。请检查文件格式或控制台错误日志。", 
                            "导入失败", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(ImportCsvDialog.this, 
                            "没有导入任何交易记录，或者文件处理过程中出现未知问题。", 
                            "导入警告", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(ImportCsvDialog.this, 
                        "导入时发生意外错误: " + e.getMessage(), 
                        "导入失败", JOptionPane.ERROR_MESSAGE);
                }
                dispose();
            }
        };
        
        worker.execute();
    }
}
