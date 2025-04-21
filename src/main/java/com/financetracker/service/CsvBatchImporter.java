package com.financetracker.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 批量CSV文件导入工具类
 * 用于导入指定目录下的所有CSV文件
 */
public class CsvBatchImporter {
    
    private TransactionService transactionService;
    
    public CsvBatchImporter(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    
    /**
     * 导入指定目录下的所有CSV文件
     * 
     * @param directoryPath 目录路径
     * @return 导入结果，包含成功导入的文件数量和记录数量
     */
    public ImportResult importAllCsvFiles(String directoryPath) {
        ImportResult result = new ImportResult();
        Path dir = Paths.get(directoryPath);
        
        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            System.err.println("指定的路径不存在或不是目录: " + directoryPath);
            return result;
        }
        
        List<Path> csvFiles = findCsvFiles(dir);
        System.out.println("在目录 " + directoryPath + " 中找到 " + csvFiles.size() + " 个CSV文件");
        
        for (Path csvFile : csvFiles) {
            try {
                System.out.println("开始处理文件: " + csvFile.toString());
                int importedCount = importSingleCsvFile(csvFile.toString());
                
                if (importedCount > 0) {
                    result.setSuccessFileCount(result.getSuccessFileCount() + 1);
                    result.setTotalRecordCount(result.getTotalRecordCount() + importedCount);
                    System.out.println("成功从文件 " + csvFile.getFileName() + " 导入 " + importedCount + " 条记录");
                } else {
                    result.setFailedFileCount(result.getFailedFileCount() + 1);
                    System.out.println("文件 " + csvFile.getFileName() + " 导入失败或无记录");
                }
            } catch (Exception e) {
                result.setFailedFileCount(result.getFailedFileCount() + 1);
                System.err.println("处理文件 " + csvFile.toString() + " 时出错: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return result;
    }
    
    /**
     * 查找指定目录下的所有CSV文件
     * 
     * @param directory 目录路径
     * @return CSV文件列表
     */
    private List<Path> findCsvFiles(Path directory) {
        List<Path> result = new ArrayList<>();
        
        try (Stream<Path> paths = Files.walk(directory)) {
            result = paths
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().toLowerCase().endsWith(".csv"))
                .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("查找CSV文件时出错: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * 导入单个CSV文件
     * 自动检测CSV文件的列结构
     * 
     * @param filePath CSV文件路径
     * @return 导入的记录数量
     */
    private int importSingleCsvFile(String filePath) {
        try {
            // 使用CsvHeaderDetector检测CSV文件的列结构
            CsvHeaderDetector detector = new CsvHeaderDetector(filePath);
            detector.detect();
            
            // 根据检测结果导入CSV文件
            if (detector.isValid()) {
                return transactionService.importFromCsv(
                    filePath,
                    detector.getDateColumn(),
                    detector.getAmountColumn(),
                    detector.getDescriptionColumn(),
                    detector.getCategoryColumn(),
                    detector.getDateFormat(),
                    detector.isExpense()
                );
            } else {
                System.err.println("CSV文件 " + filePath + " 格式无效，无法检测到必要的列");
                return 0;
            }
        } catch (Exception e) {
            System.err.println("导入文件 " + filePath + " 时出错: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * CSV文件列结构检测器
     * 用于自动检测CSV文件的列结构
     */
    public static class CsvHeaderDetector {
        private String filePath;
        private String dateColumn;
        private String amountColumn;
        private String descriptionColumn;
        private String categoryColumn;
        private String dateFormat = "yyyy-MM-dd";
        private boolean expense = true;
        private boolean valid = false;
        
        public CsvHeaderDetector(String filePath) {
            this.filePath = filePath;
        }
        
        /**
         * 检测CSV文件的列结构
         */
        public void detect() {
            try {
                List<String> headers = readCsvHeaders(filePath);
                if (headers.isEmpty()) {
                    System.err.println("CSV文件 " + filePath + " 没有标题行");
                    return;
                }
                
                // 自动检测列
                dateColumn = findColumnByKeywords(headers, new String[]{"date", "日期", "时间", "day", "time"});
                amountColumn = findColumnByKeywords(headers, new String[]{"amount", "金额", "数额", "价格", "sum", "value", "price"});
                descriptionColumn = findColumnByKeywords(headers, new String[]{"description", "desc", "名称", "描述", "备注", "摘要", "memo", "note"});
                categoryColumn = findColumnByKeywords(headers, new String[]{"category", "类别", "分类", "type", "类型", "group"});
                
                // 检测是否有足够的列信息
                valid = dateColumn != null && amountColumn != null && (descriptionColumn != null || categoryColumn != null);
                
                // 如果文件名包含"收入"、"income"等关键词，默认为收入
                String fileName = new File(filePath).getName().toLowerCase();
                if (fileName.contains("收入") || fileName.contains("income") || fileName.contains("revenue")) {
                    expense = false;
                }
                
                // 检测日期格式（如果有日期列）
                if (dateColumn != null) {
                    String sampleDate = readFirstDateValue(filePath, dateColumn);
                    if (sampleDate != null && !sampleDate.isEmpty()) {
                        dateFormat = guessDateFormat(sampleDate);
                    }
                }
                
                System.out.println("CSV文件 " + filePath + " 检测结果:");
                System.out.println("日期列: " + dateColumn);
                System.out.println("金额列: " + amountColumn);
                System.out.println("描述列: " + descriptionColumn);
                System.out.println("类别列: " + categoryColumn);
                System.out.println("日期格式: " + dateFormat);
                System.out.println("交易类型: " + (expense ? "支出" : "收入"));
                System.out.println("有效性: " + valid);
            } catch (Exception e) {
                System.err.println("检测CSV文件 " + filePath + " 时出错: " + e.getMessage());
                e.printStackTrace();
                valid = false;
            }
        }
        
        /**
         * 读取CSV文件的标题行
         */
        private List<String> readCsvHeaders(String filePath) {
            List<String> headers = new ArrayList<>();
            
            try {
                List<String> allLines = Files.readAllLines(Paths.get(filePath));
                if (!allLines.isEmpty()) {
                    String headerLine = allLines.get(0);
                    String separator = headerLine.contains(",") ? "," : 
                                      (headerLine.contains(";") ? ";" : "\t");
                    
                    String[] parts = headerLine.split(separator);
                    for (String part : parts) {
                        headers.add(part.trim());
                    }
                }
            } catch (IOException e) {
                System.err.println("读取CSV文件标题行时出错: " + e.getMessage());
                e.printStackTrace();
            }
            
            return headers;
        }
        
        /**
         * 读取CSV文件的第一个日期值
         */
        private String readFirstDateValue(String filePath, String dateColumn) {
            try {
                List<String> allLines = Files.readAllLines(Paths.get(filePath));
                if (allLines.size() > 1) {
                    String headerLine = allLines.get(0);
                    String dataLine = allLines.get(1);
                    
                    String separator = headerLine.contains(",") ? "," : 
                                      (headerLine.contains(";") ? ";" : "\t");
                    
                    String[] headers = headerLine.split(separator);
                    String[] values = dataLine.split(separator);
                    
                    for (int i = 0; i < headers.length && i < values.length; i++) {
                        if (headers[i].trim().equalsIgnoreCase(dateColumn)) {
                            return values[i].trim();
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("读取CSV文件第一个日期值时出错: " + e.getMessage());
            }
            
            return null;
        }
        
        /**
         * 根据关键词在标题列表中查找列名
         */
        private String findColumnByKeywords(List<String> headers, String[] keywords) {
            // 精确匹配
            for (String header : headers) {
                String lower = header.toLowerCase();
                for (String keyword : keywords) {
                    if (lower.equals(keyword.toLowerCase())) {
                        return header;
                    }
                }
            }
            
            // 部分匹配
            for (String header : headers) {
                String lower = header.toLowerCase();
                for (String keyword : keywords) {
                    if (lower.contains(keyword.toLowerCase()) || 
                        keyword.toLowerCase().contains(lower)) {
                        return header;
                    }
                }
            }
            
            return null;
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
        
        // Getters
        public String getDateColumn() { return dateColumn; }
        public String getAmountColumn() { return amountColumn; }
        public String getDescriptionColumn() { return descriptionColumn; }
        public String getCategoryColumn() { return categoryColumn; }
        public String getDateFormat() { return dateFormat; }
        public boolean isExpense() { return expense; }
        public boolean isValid() { return valid; }
    }
    
    /**
     * 导入结果类
     */
    public static class ImportResult {
        private int successFileCount = 0;
        private int failedFileCount = 0;
        private int totalRecordCount = 0;
        
        public int getSuccessFileCount() { return successFileCount; }
        public void setSuccessFileCount(int successFileCount) { this.successFileCount = successFileCount; }
        
        public int getFailedFileCount() { return failedFileCount; }
        public void setFailedFileCount(int failedFileCount) { this.failedFileCount = failedFileCount; }
        
        public int getTotalRecordCount() { return totalRecordCount; }
        public void setTotalRecordCount(int totalRecordCount) { this.totalRecordCount = totalRecordCount; }
        
        @Override
        public String toString() {
            return "成功导入 " + successFileCount + " 个文件，失败 " + failedFileCount + " 个文件，共 " + totalRecordCount + " 条记录";
        }
    }
} 