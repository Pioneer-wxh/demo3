package com.financetracker.service;
// 定义代码所属的包名，表示该类位于 com.financetracker.service 包下，用于组织和管理类

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List; // Added import for Settings
import java.util.UUID; // Ensure Transaction is imported

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.financetracker.model.Settings;
import com.financetracker.model.Transaction;

/**
 * CsvBatchImporter是一个批量导入CSV文件的工具类。
 * 主要功能：
 * 1. 批量扫描并导入指定目录下的所有CSV文件
 * 2. 自动检测CSV文件的列结构（日期、金额、描述等）
 * 3. 自动判断交易类型（收入或支出）
 * 4. 智能识别日期格式并进行解析
 * 5. 详细的导入结果统计和报告
 * 
 * 内部包含两个重要的辅助类：
 * - CsvHeaderDetector：负责智能检测CSV文件的列结构和格式
 * - ImportResult：记录导入结果的统计信息
 */
// 类注释：说明 CsvBatchImporter 是一个批量导入 CSV 文件的工具类
// 列出主要功能，包括扫描导入、列结构检测、交易类型判断、日期格式识别和结果统计
// 提到两个辅助类：CsvHeaderDetector 和 ImportResult

public class CsvBatchImporter {
    // 定义 CsvBatchImporter 类，用于批量导入 CSV 文件中的交易记录

    private TransactionService transactionService;
    private final Settings settings; // Added final Settings field
    // 定义类成员变量 transactionService，类型为 TransactionService，用于处理交易记录的业务逻辑

    public CsvBatchImporter(TransactionService transactionService, Settings settings) {
        // 定义构造函数，接收 TransactionService 实例和 Settings
        this.transactionService = transactionService;
        this.settings = settings; // Store settings
    }

    /**
     * Imports transactions from a single CSV file using auto-detected column mappings.
     * 
     * @param filePath The path to the CSV file.
     * @return An ImportResult detailing the outcome of the import.
     */
    public ImportResult importSingleCsvFile(String filePath) {
        ImportResult result = new ImportResult();
        File file = new File(filePath);

        if (!file.exists() || !file.isFile() || !file.getName().toLowerCase().endsWith(".csv")) {
            result.failedFileCount = 1; // Mark this file as failed
            return result;
        }

        try {
            List<String> headers = readCsvHeaders(file.getAbsolutePath());
            if (headers == null || headers.isEmpty()) {
                result.failedFileCount = 1;
                return result;
            }
            String[] columnMappings = autoDetectColumns(file.getAbsolutePath(), headers);
            if (columnMappings[0] == null || columnMappings[1] == null || columnMappings[2] == null) {
                result.failedFileCount = 1;
                return result;
            }

            List<Transaction> importedTransactions = parseTransactionsFromSingleCsv(
                file.getAbsolutePath(), 
                columnMappings[0], // dateColumn
                columnMappings[1], // amountColumn
                columnMappings[2], // descriptionColumn
                columnMappings[3], // categoryColumn (can be null)
                columnMappings[4]  // dateFormat
            );

            if (importedTransactions != null && !importedTransactions.isEmpty()) {
                int successfullyAddedCount = 0;
                for (Transaction tx : importedTransactions) {
                    if (transactionService.addTransaction(tx)) { // Use addTransaction for individual processing
                        successfullyAddedCount++;
                    }
                }
                if (successfullyAddedCount > 0) {
                    result.successFileCount = 1;
                    result.totalRecordCount = successfullyAddedCount;
                } else {
                    // If some were parsed but none added, it's not a full file failure yet
                }
            } else if (importedTransactions != null && importedTransactions.isEmpty()) {
                // File was parsed, but no valid transactions found or all were skipped.
            } else { // importedTransactions is null, indicating parsing error
                result.failedFileCount = 1;
            }

        } catch (Exception e) {
            result.failedFileCount = 1;
        }
        return result;
    }

    /**
     * 批量导入CSV文件
     * 
     * @param files 要导入的文件数组
     * @return 导入结果
     */
    public ImportResult importCsvFiles(File[] files) {
        ImportResult result = new ImportResult();

        for (File file : files) {
            if (!file.exists() || !file.isFile() || !file.getName().toLowerCase().endsWith(".csv")) {
                result.failedFileCount++;
                continue;
            }

            try {
                List<String> headers = readCsvHeaders(file.getAbsolutePath());
                if (headers == null || headers.isEmpty()) {
                    result.failedFileCount++;
                    continue;
                }
                String[] columnMappings = autoDetectColumns(file.getAbsolutePath(), headers);
                if (columnMappings[0] == null || columnMappings[1] == null || columnMappings[2] == null) {
                    result.failedFileCount++;
                    continue;
                }

                List<Transaction> importedTransactions = parseTransactionsFromSingleCsv(
                    file.getAbsolutePath(), 
                    columnMappings[0], // dateColumn
                    columnMappings[1], // amountColumn
                    columnMappings[2], // descriptionColumn
                    columnMappings[3], // categoryColumn (can be null)
                    columnMappings[4]  // dateFormat
                );

                if (importedTransactions != null && !importedTransactions.isEmpty()) {
                    // Instead of transactionService.importFromCsv, 
                    // we now have the transactions and need to add them.
                    List<Transaction> existingTransactions = transactionService.getAllTransactions(); // Get current transactions
                    existingTransactions.addAll(importedTransactions); // Add new ones
                    if (transactionService.saveAllTransactions(existingTransactions)) { // Changed saveTransactions to saveAllTransactions
                        result.successFileCount++;
                        result.totalRecordCount += importedTransactions.size();
                    } else {
                        result.failedFileCount++;
                    }
                } else if (importedTransactions != null && importedTransactions.isEmpty()) {
                    // File was parsed, but no valid transactions found or all were skipped.
                } else { // importedTransactions is null, indicating parsing error
                    result.failedFileCount++;
                }

            } catch (Exception e) {
                result.failedFileCount++;
            }
        }

        return result;
    }

    /**
     * 从目录导入CSV文件
     * 
     * @param directoryPath 目录路径
     * @return 导入结果
     */
    public ImportResult importFromDirectory(String directoryPath) {
        File dir = new File(directoryPath);
        if (!dir.exists() || !dir.isDirectory()) {
            ImportResult result = new ImportResult();
            result.failedFileCount = 1;
            return result;
        }

        // 获取目录中的所有CSV文件
        File[] csvFiles = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".csv");
            }
        });

        if (csvFiles == null || csvFiles.length == 0) {
            ImportResult result = new ImportResult();
            return result;
        }

        // 执行批量导入
        return importCsvFiles(csvFiles);
    }

    /**
     * 读取CSV文件头
     * 
     * @param filePath 文件路径
     * @return 列标题列表
     */
    private List<String> readCsvHeaders(String filePath) {
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
        } catch (Exception e) {
            return null;
        }
        return headers;
    }

    /**
     * 自动检测列映射
     * 
     * @param filePath 文件路径
     * @param headers  列标题
     * @return 自动识别的列映射数组 [日期列,金额列,描述列,类别列,日期格式]
     */
    private String[] autoDetectColumns(String filePath, List<String> headers) {
        // 常见日期列名映射
        String[] dateKeywords = { "date", "time", "day", "日期", "时间", "transaction date" };
        // 常见金额列名映射
        String[] amountKeywords = { "amount", "sum", "value", "price", "金额", "数额", "价格", "transaction amount" };
        // 常见描述列名映射
        String[] descKeywords = { "description", "desc", "note", "memo", "名称", "描述", "备注", "摘要",
                "transaction description" };
        // 常见类别列名映射
        String[] categoryKeywords = { "category", "type", "group", "类别", "类型", "分类" };

        // 查找最匹配的列
        int dateIndex = findBestMatch(headers, dateKeywords);
        int amountIndex = findBestMatch(headers, amountKeywords);
        int descIndex = findBestMatch(headers, descKeywords);
        int categoryIndex = findBestMatch(headers, categoryKeywords);

        // 如果找不到必要的列，尝试使用索引位置
        if (dateIndex < 0 && headers.size() > 0)
            dateIndex = 0;
        if (amountIndex < 0 && headers.size() > 1)
            amountIndex = 1;
        if (descIndex < 0 && headers.size() > 2)
            descIndex = 2;

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
        } catch (Exception e) {
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
     * 
     * @param headers  列标题列表
     * @param keywords 关键词数组
     * @return 最匹配的列索引，如果没找到则返回-1
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
     * 
     * @param dateStr 日期字符串
     * @return 最可能的日期格式
     */
    private String guessDateFormat(String dateStr) {
        String[][] formats = {
                { "yyyy-MM-dd", "\\d{4}-\\d{1,2}-\\d{1,2}" },
                { "MM/dd/yyyy", "\\d{1,2}/\\d{1,2}/\\d{4}" },
                { "dd/MM/yyyy", "\\d{1,2}/\\d{1,2}/\\d{4}" },
                { "yyyy/MM/dd", "\\d{4}/\\d{1,2}/\\d{1,2}" },
                { "dd-MM-yyyy", "\\d{1,2}-\\d{1,2}-\\d{4}" },
                { "dd.MM.yyyy", "\\d{1,2}\\.\\d{1,2}\\.\\d{4}" }
        };

        for (String[] format : formats) {
            if (dateStr.matches(format[1])) {
                return format[0];
            }
        }

        return "yyyy-MM-dd"; // 默认格式
    }

    /**
     * Tries to parse a date string using multiple common date formats.
     * This method is moved from TransactionService.
     *
     * @param dateStr The date string to parse.
     * @return The parsed LocalDate, or null if parsing fails with all tried formats.
     */
    private LocalDate tryParseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        // Standard date formats
        String[] commonDatePatterns = {
            "yyyy-MM-dd", "MM/dd/yyyy", "dd/MM/yyyy", "yyyy/MM/dd", "dd-MM-yyyy",
            "MM-dd-yyyy", "yyyyMMdd", "MM.dd.yyyy", "dd.MM.yyyy",
            // Date and Time formats - LocalDate.parse will extract date part if pattern matches
            "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss", "MM/dd/yyyy HH:mm:ss",
            "dd/MM/yyyy HH:mm:ss", "yyyy/MM/dd HH:mm:ss", "yyyy-MM-dd HH:mm",
            "MM/dd/yyyy HH:mm", "dd/MM/yyyy HH:mm",
            // Formats with different separators or orders sometimes seen
            "yyyy.MM.dd", "dd-MMM-yyyy", "yyyy-MMM-dd"
        };

        for (String pattern : commonDatePatterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                return LocalDate.parse(dateStr.trim(), formatter);
            } catch (java.time.format.DateTimeParseException e) {
                // Ignore and try the next pattern
            }
        }
        
        // Fallback for dates that might have extra text or are part of a longer string
        // This is a very basic attempt and might need more sophisticated regex for robustness
        try {
             // Attempt to extract something like yyyy-MM-dd from the start of the string
            if (dateStr.length() >= 10) {
                String potentialDate = dateStr.substring(0, 10);
                if (potentialDate.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                    return LocalDate.parse(potentialDate);
                }
            }
        } catch (Exception e) {
            // ignore
        }

        return null; 
    }

    // ---- Methods moved from TransactionService ----

    private String autoDetectCategory(String description, double amount, boolean isExpense) {
        String lowerDesc = description.toLowerCase();
        if (!isExpense) { // Income categories
            if (lowerDesc.contains("salary") || lowerDesc.contains("payroll")) return "Salary";
            if (lowerDesc.contains("bonus") || lowerDesc.contains("award")) return "Bonus";
            if (lowerDesc.contains("invest") || lowerDesc.contains("stock") || lowerDesc.contains("fund")) return "Investment";
            if (lowerDesc.contains("interest")) return "Interest";
            if (lowerDesc.contains("refund") || lowerDesc.contains("reimbursement")) return "Refund";
            if (lowerDesc.contains("gift") || lowerDesc.contains("present")) return "Gift";
            if (lowerDesc.contains("freelance") || lowerDesc.contains("part-time")) return "Freelance/Part-time";
            return "Other Income";
        }
        // Expense categories (simplified for brevity in this edit, use full list from TransactionService)
        if (lowerDesc.contains("food") || lowerDesc.contains("restaurant") || lowerDesc.contains("grocery")) return "Food";
        if (lowerDesc.contains("shop") || lowerDesc.contains("taobao") || lowerDesc.contains("amazon")) return "Shopping";
        if (lowerDesc.contains("cloth")) return "Clothing";
        if (lowerDesc.contains("traffic") || lowerDesc.contains("bus") || lowerDesc.contains("subway") || lowerDesc.contains("gas")) return "Transportation";
        if (lowerDesc.contains("house") || lowerDesc.contains("rent") || lowerDesc.contains("mortgage")) return "Housing";
        if (lowerDesc.contains("utility") || lowerDesc.contains("water") || lowerDesc.contains("electricity")) return "Utilities";
        if (lowerDesc.contains("entertainment") || lowerDesc.contains("movie") || lowerDesc.contains("game")) return "Entertainment";
        if (lowerDesc.contains("medical") || lowerDesc.contains("hospital") || lowerDesc.contains("health")) return "Healthcare";
        if (lowerDesc.contains("education") || lowerDesc.contains("school") || lowerDesc.contains("book")) return "Education";
        // ... add more specific expense categories as in TransactionService ...
        return "Others";
    }

    private List<String> getStandardIncomeCategories() {
        // This uses the settings passed to CsvBatchImporter
        // If settings.getIncomeCategories() returns English categories, direct use is fine.
        // If they are in another language and autoDetectCategory maps to English, then this list should be English.
        // For now, assuming settings.getIncomeCategories() are what we need to check against.
        // OR, define a standard English list here if settings only store user-defined names.
        // The original TransactionService had a hardcoded English list.
        // Let's use the one from settings for now. This implies income categories in Settings must be comprehensive.
        if (this.settings != null && this.settings.getIncomeCategories() != null) {
            return new ArrayList<>(this.settings.getIncomeCategories()); // Return a copy
        } 
        // Fallback to a default list if settings are not available or empty, though ideally settings should be reliable.
        return List.of("Salary", "Bonus", "Investment", "Interest", "Refund", "Gift", "Freelance/Part-time", "Other Income");
    }

    private boolean autoDetectIsExpense(String description, double amount, String category) {
        if (category != null && !category.isEmpty()) {
            List<String> incomeCategories = getStandardIncomeCategories(); // Uses method above
            if (incomeCategories.stream().anyMatch(incomeCat -> incomeCat.equalsIgnoreCase(category))) {
                return false; // Known income category
            }
            // Add checks for known expense categories if needed, though description check is primary for unknown categories
        }

        String lowerDesc = description.toLowerCase();
        String[] incomeKeywords = {
            "salary", "income", "bonus", "refund", "reimbursement", "interest", "dividend",
            "revenue", "earnings", "deposit", "received"
            // Add more specific non-Chinese keywords if expecting international CSVs
        };
        String[] expenseKeywords = {
            "expense", "cost", "purchase", "buy", "pay", "payment", "bill", "fee", "charge",
            "spend", "spent", "order", "withdrawal"
        };

        for (String keyword : incomeKeywords) {
            if (lowerDesc.contains(keyword)) return false;
        }
        for (String keyword : expenseKeywords) {
            if (lowerDesc.contains(keyword)) return true;
        }
        
        // If amount was originally negative in CSV and made positive, it implies expense.
        // This information is lost by the time this method is called if only positive amount is passed.
        // The initial check in TransactionService's importFromCsv `if (amount < 0)` handled this.
        // We will need to replicate that logic when we move the main parsing loop here.

        return true; // Default to expense if no clear income indicators
    }

    private boolean isAiAssistantAvailable() {
        // This is a placeholder. In a real application, this would check if the AI service
        // is configured, available, and licensed, etc.
        // For now, to match TransactionService behavior, we can check for a class existence
        // or simply return false if AI features are not being used.
        try {
            Class.forName("com.financetracker.ai.AiAssistantService"); // Example check
            return true; 
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private java.util.Map<String, Object> detectCategoryAndType(String description, double amount) {
        // This method currently relies on non-AI autoDetectCategory and autoDetectIsExpense.
        // If AI functionality were present and enabled by isAiAssistantAvailable(),
        // this method would call the AI service.
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        
        // For consistency with the original structure, even if AI is not used,
        // we can mimic the flow. The actual classification will be rule-based.
        boolean initialIsExpenseGuess = amount < 0; // Assuming amount can be negative here
                                                  // This will be refined by autoDetectIsExpense

        String category = autoDetectCategory(description, Math.abs(amount), initialIsExpenseGuess);
        // autoDetectIsExpense should ideally take the original amount if it can be negative,
        // or handle positive amount + initialIsExpenseGuess.
        boolean isExpense = autoDetectIsExpense(description, Math.abs(amount), category);
        
        result.put("category", category);
        result.put("isExpense", isExpense);
            
        return result;
    }

    private List<Transaction> parseTransactionsFromSingleCsv(String filePath, String dateColumn, String amountColumn, 
                                                            String descriptionColumn, String categoryColumn, String dateFormat) {
        List<Transaction> importedTransactions = new ArrayList<>();
        int linesProcessed = 0;

        try (Reader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder()
                    .setHeader() // Assumes first line is header
                    .setSkipHeaderRecord(true)
                    .setIgnoreHeaderCase(true)
                    .setTrim(true)
                    .build())) {
            
            for (CSVRecord record : csvParser) {
                linesProcessed++;
                try {
                    String dateStr = record.get(dateColumn);
                    LocalDate date = null;
                    try {
                        date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(dateFormat));
                    } catch (Exception e) {
                        date = tryParseDate(dateStr); // Fallback to other common formats
                        if (date == null) {
                            continue;
                        }
                    }

                    String description = record.isMapped(descriptionColumn) ? record.get(descriptionColumn) : "";
                    
                    String amountStr = record.get(amountColumn).replaceAll("[^\\d.-]", "");
                    double amountVal;
                    try {
                        amountVal = Double.parseDouble(amountStr);
                    } catch (NumberFormatException e) {
                        continue;
                    }

                    boolean isExpenseBasedOnSign = amountVal < 0;
                    double actualAmount = Math.abs(amountVal);
                    String detectedCategory = "未分类"; // Default category

                    if (categoryColumn != null && record.isMapped(categoryColumn) && !record.get(categoryColumn).trim().isEmpty()) {
                        detectedCategory = record.get(categoryColumn).trim();
                    }
                    
                    // Determine final expense status and category
                    boolean finalIsExpense = autoDetectIsExpense(description, actualAmount, detectedCategory);
                    if (isExpenseBasedOnSign && !finalIsExpense) {
                         // If amount was negative (strong indicator of expense) but auto-detection says income, log a warning.
                         // Potentially prioritize the sign of the amount or a more sophisticated rule.
                         // For now, let autoDetectIsExpense based on description/category override if it finds income signals.
                        continue;
                    } else if (isExpenseBasedOnSign) {
                        finalIsExpense = true; // Negative amount is a strong signal for expense
                    }

                    // If a category was provided in the CSV, use it, otherwise auto-detect.
                    // If category was provided, autoDetectCategory is mostly for normalization or if it was empty.
                    if (detectedCategory.equals("未分类")) { // Only auto-detect if not provided or empty
                         detectedCategory = autoDetectCategory(description, actualAmount, finalIsExpense);
                    }
                    
                    // Final check with settings income categories
                    if (this.settings.getIncomeCategories().contains(detectedCategory)) {
                        finalIsExpense = false;
                    }

                    Transaction transaction = new Transaction();
                    transaction.setId(UUID.randomUUID().toString());
                    transaction.setDate(date);
                    transaction.setAmount(actualAmount);
                    transaction.setDescription(description);
                    transaction.setCategory(detectedCategory);
                    transaction.setExpense(finalIsExpense);
                    transaction.setParticipant(record.isMapped("Participant") ? record.get("Participant") : "");
                    transaction.setNotes(record.isMapped("Notes") ? record.get("Notes") : "");
                    
                    importedTransactions.add(transaction);
                } catch (IllegalArgumentException iae) { // Catch issues from record.get() if column name is bad
                    continue;
                } catch (Exception e) {
                    continue;
                }
            }
            return importedTransactions;

        } catch (IOException e) {
            return null; // Indicate failure to parse file
        }
    }

    /**
     * 导入结果类
     */
    // 类注释：说明 ImportResult 类用于存储导入操作的结果
    public static class ImportResult {
        // 定义静态内部类 ImportResult
        private int successFileCount = 0;
        // 定义成功导入的文件数，初始为 0
        private int failedFileCount = 0;
        // 定义失败导入的文件数，初始为 0
        private int totalRecordCount = 0;
        // 定义导入的总记录数，初始为 0

        public int getSuccessFileCount() {
            return successFileCount;
        }

        // 获取成功文件计数
        public void setSuccessFileCount(int successFileCount) {
            this.successFileCount = successFileCount;
        }
        // 设置成功文件计数

        public int getFailedFileCount() {
            return failedFileCount;
        }

        // 获取失败文件计数
        public void setFailedFileCount(int failedFileCount) {
            this.failedFileCount = failedFileCount;
        }
        // 设置失败文件计数

        public int getTotalRecordCount() {
            return totalRecordCount;
        }

        // 获取总记录数
        public void setTotalRecordCount(int totalRecordCount) {
            this.totalRecordCount = totalRecordCount;
        }
        // 设置总记录数

        @Override
        public String toString() {
            return "成功导入 " + successFileCount + " 个文件，失败 " + failedFileCount + " 个文件，共 " + totalRecordCount + " 条记录";
            // 重写 toString 方法，返回导入结果的描述
        }
    }
}