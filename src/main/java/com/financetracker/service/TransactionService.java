package com.financetracker.service;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.financetracker.model.Transaction;

/**
 * Service for managing transactions.
 */
public class TransactionService {
    
    private static final String CSV_FILE_PATH = "E:\\code\\Java\\software_lab\\data\\transactions.csv";
    private final TransactionCsvExporter csvExporter;
    
    /**
     * Constructor for TransactionService.
     */
    public TransactionService() {
        this.csvExporter = new TransactionCsvExporter();
        
        // 确保CSV文件目录存在
        try {
            Path path = Paths.get(CSV_FILE_PATH);
            Files.createDirectories(path.getParent());
        } catch (Exception e) {
            System.err.println("创建CSV文件目录时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Gets all transactions.
     * 
     * @return The list of all transactions
     */
    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        
        // 从CSV文件加载
        if (csvExporter.csvFileExists()) {
            transactions = csvExporter.importTransactionsFromCSV();
        }
        
        // 按日期排序（最新的优先）
        transactions.sort(Comparator.comparing(Transaction::getDate).reversed());
        
        return transactions;
    }
    
    /**
     * Gets transactions for a specific date range.
     * 
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @return The list of transactions in the date range
     */
    public List<Transaction> getTransactionsForDateRange(LocalDate startDate, LocalDate endDate) {
        List<Transaction> allTransactions = getAllTransactions();
        
        return allTransactions.stream()
                .filter(t -> !t.getDate().isBefore(startDate) && !t.getDate().isAfter(endDate))
                .collect(Collectors.toList());
    }
    
    /**
     * Gets transactions for a specific month.
     * 
     * @param year The year
     * @param month The month (1-12)
     * @return The list of transactions in the month
     */
    public List<Transaction> getTransactionsForMonth(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        
        return getTransactionsForDateRange(startDate, endDate);
    }
    
    /**
     * Gets transactions for the current month.
     * 
     * @return The list of transactions in the current month
     */
    public List<Transaction> getTransactionsForCurrentMonth() {
        LocalDate today = LocalDate.now();
        return getTransactionsForMonth(today.getYear(), today.getMonthValue());
    }
    
    /**
     * Gets transactions for a specific category.
     * 
     * @param category The category
     * @return The list of transactions in the category
     */
    public List<Transaction> getTransactionsForCategory(String category) {
        List<Transaction> allTransactions = getAllTransactions();
        
        return allTransactions.stream()
                .filter(t -> t.getCategory().equals(category))
                .collect(Collectors.toList());
    }
    
    /**
     * Gets transactions for a specific category and date range.
     * 
     * @param category The category
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @return The list of transactions in the category and date range
     */
    public List<Transaction> getTransactionsForCategoryAndDateRange(String category, LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactionsInDateRange = getTransactionsForDateRange(startDate, endDate);
        
        return transactionsInDateRange.stream()
                .filter(t -> t.getCategory().equals(category))
                .collect(Collectors.toList());
    }
    
    /**
     * Gets the total amount for a list of transactions.
     * 
     * @param transactions The list of transactions
     * @param isExpense Whether to calculate expenses or income
     * @return The total amount
     */
    public double getTotalAmount(List<Transaction> transactions, boolean isExpense) {
        return transactions.stream()
                .filter(t -> t.isExpense() == isExpense)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }
    
    /**
     * Gets the total expense amount for a list of transactions.
     * 
     * @param transactions The list of transactions
     * @return The total expense amount
     */
    public double getTotalExpense(List<Transaction> transactions) {
        return getTotalAmount(transactions, true);
    }
    
    /**
     * Gets the total income amount for a list of transactions.
     * 
     * @param transactions The list of transactions
     * @return The total income amount
     */
    public double getTotalIncome(List<Transaction> transactions) {
        return getTotalAmount(transactions, false);
    }
    
    /**
     * Gets the net amount (income - expense) for a list of transactions.
     * 
     * @param transactions The list of transactions
     * @return The net amount
     */
    public double getNetAmount(List<Transaction> transactions) {
        return getTotalIncome(transactions) - getTotalExpense(transactions);
    }
    
    /**
     * Adds a transaction.
     * 
     * @param transaction The transaction to add
     * @return true if the operation was successful, false otherwise
     */
    public boolean addTransaction(Transaction transaction) {
        List<Transaction> transactions = getAllTransactions();
        transactions.add(transaction);
        
        // 保存到CSV文件
        return csvExporter.exportTransactionsToCSV(transactions);
    }
    
    /**
     * Updates a transaction.
     * 
     * @param transaction The transaction to update
     * @return true if the operation was successful, false otherwise
     */
    public boolean updateTransaction(Transaction transaction) {
        List<Transaction> transactions = getAllTransactions();
        
        // 查找要更新的交易
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getId().equals(transaction.getId())) {
                transactions.set(i, transaction);
                
                // 保存到CSV文件
                return csvExporter.exportTransactionsToCSV(transactions);
            }
        }
        
        return false;
    }
    
    /**
     * Deletes a transaction.
     * 
     * @param transaction The transaction to delete
     * @return true if the operation was successful, false otherwise
     */
    public boolean deleteTransaction(Transaction transaction) {
        List<Transaction> transactions = getAllTransactions();
        
        // 查找要删除的交易
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getId().equals(transaction.getId())) {
                transactions.remove(i);
                
                // 保存到CSV文件
                return csvExporter.exportTransactionsToCSV(transactions);
            }
        }
        
        return false;
    }
    
    /**
     * Saves transactions to the data file.
     * 
     * @param transactions The transactions to save
     * @return true if the operation was successful, false otherwise
     */
    private boolean saveTransactions(List<Transaction> transactions) {
        // 保存到CSV文件
        return csvExporter.exportTransactionsToCSV(transactions);
    }
    
    /**
     * 检查是否可以使用AI助手
     * 
     * @return 是否可以使用AI助手
     */
    private boolean isAiAssistantAvailable() {
        try {
            Class.forName("com.financetracker.ai.AiAssistantService");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    /**
     * 使用AI自动检测交易类别和类型
     * 
     * @param description 交易描述
     * @param amount 交易金额
     * @return 包含类别和交易类型的Map
     */
    private Map<String, Object> detectCategoryAndType(String description, double amount) {
        try {
            // 使用AiAssistantService进行分析
            com.financetracker.ai.AiAssistantService aiService = new com.financetracker.ai.AiAssistantService();
            return aiService.analyzeTransaction(description, amount);
        } catch (Exception e) {
            System.err.println("使用AI分析失败: " + e.getMessage());
            e.printStackTrace();
            
            // 使用备用规则识别
            Map<String, Object> result = new HashMap<>();
            result.put("category", autoDetectCategory(description, amount, amount < 0));
            result.put("isExpense", autoDetectIsExpense(description, amount, (String)result.get("category")));
            
            return result;
        }
    }
    
    /**
     * 从CSV文件导入交易记录，自动识别日期、金额、类别和交易类型
     * 
     * @param filePath CSV文件路径
     * @param dateColumn 日期列名
     * @param amountColumn 金额列名
     * @param descriptionColumn 描述列名
     * @param categoryColumn 类别列名（可选）
     * @param dateFormat 日期格式
     * @param isExpense 是否为支出
     * @return 导入的记录数
     */
    public int importFromCsv(String filePath, String dateColumn, String amountColumn, 
                            String descriptionColumn, String categoryColumn, 
                            String dateFormat, boolean isExpense) {
        int importedCount = 0;
        
        try (Reader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {
            
            List<Transaction> existingTransactions = getAllTransactions();
            List<Transaction> importedTransactions = new ArrayList<>();
            
            // 判断是否可以使用AI助手（改为默认不使用）
            boolean useAiAssistant = false;
            
            // 尝试检查AI服务是否可用，但即使不可用也不抛出异常
            try {
                useAiAssistant = isAiAssistantAvailable();
            } catch (Exception e) {
                System.err.println("检查AI服务时出错，将不使用AI分析功能: " + e.getMessage());
                useAiAssistant = false;
            }
            
            System.out.println("开始导入CSV文件: " + filePath);
            System.out.println("日期列: " + dateColumn + ", 金额列: " + amountColumn + ", 描述列: " + descriptionColumn);
            System.out.println("类别列: " + (categoryColumn != null ? categoryColumn : "未指定") + ", 日期格式: " + dateFormat);
            
            for (CSVRecord record : csvParser) {
                try {
                    // 获取日期字段并解析
                    String dateStr = record.get(dateColumn);
                    // 处理不同格式的日期
                    LocalDate date;
                    try {
                        date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(dateFormat));
                    } catch (Exception e) {
                        // 尝试其他常见日期格式
                        date = tryParseDate(dateStr);
                        if (date == null) {
                            System.err.println("无法解析日期: " + dateStr + "，跳过此记录");
                            continue; // 跳过这条记录
                        }
                    }
                    
                    // 获取描述
                    String description = "";
                    try {
                        description = record.get(descriptionColumn);
                    } catch (Exception e) {
                        System.err.println("获取描述字段出错: " + e.getMessage() + "，使用空字符串代替");
                    }
                    
                    // 获取金额字段并解析
                    String amountStr = "";
                    try {
                        amountStr = record.get(amountColumn);
                        // 移除金额中的货币符号和其他非数字字符（除了小数点和负号）
                        amountStr = amountStr.replaceAll("[^\\d.-]", "");
                    } catch (Exception e) {
                        System.err.println("获取金额字段出错: " + e.getMessage() + "，跳过此记录");
                        continue;
                    }
                    
                    double amount;
                    try {
                        amount = Double.parseDouble(amountStr);
                    } catch (NumberFormatException e) {
                        System.err.println("无法解析金额: " + amountStr + "，跳过此记录");
                        continue; // 跳过这条记录
                    }
                    
                    // 确定交易类型（收入或支出）和类别
                    boolean transactionIsExpense = isExpense;
                    String category = "未分类";
                    
                    // 如果类别列存在，优先使用类别列的值，忽略AI服务
                    if (categoryColumn != null && !categoryColumn.isEmpty()) {
                        try {
                            category = record.get(categoryColumn);
                            // 如果类别为空，则使用默认类别
                            if (category == null || category.trim().isEmpty()) {
                                if (useAiAssistant) {
                                    Map<String, Object> aiResult = detectCategoryAndType(description, amount);
                                    category = (String) aiResult.get("category");
                                    transactionIsExpense = (boolean) aiResult.get("isExpense");
                                } else {
                                    // 如果金额为负，则视为支出，并取其绝对值
                                    if (amount < 0) {
                                        transactionIsExpense = true;
                                        amount = Math.abs(amount);
                                    }
                                    category = autoDetectCategory(description, amount, transactionIsExpense);
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("获取类别字段出错: " + e.getMessage() + "，使用自动分类");
                            // 如果获取类别失败，尝试使用规则识别
                            if (amount < 0) {
                                transactionIsExpense = true;
                                amount = Math.abs(amount);
                            }
                            category = autoDetectCategory(description, amount, transactionIsExpense);
                        }
                    } else if (useAiAssistant) {
                        // 没有提供类别列，使用AI识别
                        try {
                            Map<String, Object> aiResult = detectCategoryAndType(description, amount);
                            category = (String) aiResult.get("category");
                            transactionIsExpense = (boolean) aiResult.get("isExpense");
                        } catch (Exception e) {
                            System.err.println("AI分析出错: " + e.getMessage() + "，使用规则分析");
                            // 如果金额为负，则视为支出，并取其绝对值
                            if (amount < 0) {
                                transactionIsExpense = true;
                                amount = Math.abs(amount);
                            }
                            category = autoDetectCategory(description, amount, transactionIsExpense);
                        }
                    } else {
                        // 如果金额为负，则视为支出，并取其绝对值
                        if (amount < 0) {
                            transactionIsExpense = true;
                            amount = Math.abs(amount);
                        }
                        
                        // 使用规则分析类别
                        category = autoDetectCategory(description, amount, transactionIsExpense);
                    }
                    
                    // 确保金额为正数
                    amount = Math.abs(amount);
                    
                    // 创建新的交易记录
                    Transaction transaction = new Transaction();
                    transaction.setId(UUID.randomUUID().toString());
                    transaction.setDate(date);
                    transaction.setAmount(amount);
                    transaction.setDescription(description);
                    transaction.setCategory(category);
                    transaction.setExpense(transactionIsExpense);
                    
                    // 尝试获取参与者信息
                    try {
                        if (record.isMapped("Participant") || record.isMapped("参与者")) {
                            String participant = record.isMapped("Participant") ? 
                                record.get("Participant") : record.get("参与者");
                            transaction.setParticipant(participant);
                        } else {
                            transaction.setParticipant(""); // 设置默认值
                        }
                    } catch (Exception e) {
                        transaction.setParticipant(""); // 异常时设置默认值
                    }
                    
                    // 尝试获取备注信息
                    try {
                        if (record.isMapped("Notes") || record.isMapped("备注")) {
                            String notes = record.isMapped("Notes") ? 
                                record.get("Notes") : record.get("备注");
                            transaction.setNotes(notes);
                        } else {
                            transaction.setNotes(""); // 设置默认值
                        }
                    } catch (Exception e) {
                        transaction.setNotes(""); // 异常时设置默认值
                    }
                    
                    importedTransactions.add(transaction);
                    importedCount++;
                    System.out.println("导入记录: " + transaction.getDescription() + ", 金额: " + transaction.getAmount() + ", 类别: " + transaction.getCategory());
                } catch (Exception e) {
                    System.err.println("导入记录时出错: " + e.getMessage());
                    e.printStackTrace();
                    // 继续处理下一条记录
                }
            }
            
            // 添加所有导入的交易记录
            if (importedCount > 0) {
                existingTransactions.addAll(importedTransactions);
                boolean saved = saveTransactions(existingTransactions);
                
                if (!saved) {
                    System.err.println("保存导入的交易记录时出错");
                }
            }
            
            System.out.println("CSV导入完成，共导入 " + importedCount + " 条记录");
        } catch (IOException e) {
            System.err.println("读取CSV文件时出错: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("导入过程中出现意外错误: " + e.getMessage());
            e.printStackTrace();
        }
        
        return importedCount;
    }
    
    /**
     * 尝试使用多种常见日期格式解析日期字符串
     * 
     * @param dateStr 日期字符串
     * @return 解析后的LocalDate，如果无法解析则返回null
     */
    private LocalDate tryParseDate(String dateStr) {
        String[][] formats = {
            {"yyyy-MM-dd", "\\d{4}-\\d{1,2}-\\d{1,2}"},
            {"MM/dd/yyyy", "\\d{1,2}/\\d{1,2}/\\d{4}"},
            {"dd/MM/yyyy", "\\d{1,2}/\\d{1,2}/\\d{4}"},
            {"yyyy/MM/dd", "\\d{4}/\\d{1,2}/\\d{1,2}"},
            {"dd-MM-yyyy", "\\d{1,2}-\\d{1,2}-\\d{4}"},
            {"dd.MM.yyyy", "\\d{1,2}\\.\\d{1,2}\\.\\d{4}"}
        };
        
        // 先尝试匹配格式
        for (String[] format : formats) {
            if (dateStr.matches(format[1])) {
                try {
                    return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(format[0]));
                } catch (Exception e) {
                    // 继续尝试其他格式
                }
            }
        }
        
        // 如果没有匹配成功，尝试所有格式
        for (String[] format : formats) {
            try {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(format[0]));
            } catch (Exception e) {
                // 继续尝试其他格式
            }
        }
        
        return null;
    }
    
    /**
     * 使用AI自动检测交易类别
     * 
     * @param description 交易描述
     * @param amount 交易金额
     * @param isExpense 是否为支出
     * @return 推荐的类别
     */
    private String autoDetectCategory(String description, double amount, boolean isExpense) {
        // 简单规则匹配逻辑：根据描述中的关键词判断类别
        String lowerDesc = description.toLowerCase();
        
        // 收入类别
        if (!isExpense) {
            if (lowerDesc.contains("工资") || lowerDesc.contains("薪水") || lowerDesc.contains("salary")) {
                return "工资";
            } else if (lowerDesc.contains("奖金") || lowerDesc.contains("bonus")) {
                return "奖金";
            } else if (lowerDesc.contains("投资") || lowerDesc.contains("股票") || lowerDesc.contains("基金") || 
                       lowerDesc.contains("invest") || lowerDesc.contains("stock") || lowerDesc.contains("fund")) {
                return "投资收益";
            } else if (lowerDesc.contains("利息") || lowerDesc.contains("interest")) {
                return "利息";
            } else if (lowerDesc.contains("退款") || lowerDesc.contains("refund")) {
                return "退款";
        } else {
                return "其他收入";
            }
        }
        
        // 支出类别
        if (lowerDesc.contains("餐") || lowerDesc.contains("饭") || lowerDesc.contains("食") || 
            lowerDesc.contains("超市") || lowerDesc.contains("菜") || lowerDesc.contains("超市") ||
            lowerDesc.contains("food") || lowerDesc.contains("restaurant") || lowerDesc.contains("meal") || 
            lowerDesc.contains("supermarket") || lowerDesc.contains("grocery")) {
            return "食品";
        } else if (lowerDesc.contains("交通") || lowerDesc.contains("车") || lowerDesc.contains("公交") || 
                   lowerDesc.contains("地铁") || lowerDesc.contains("出租") || lowerDesc.contains("高铁") || 
                   lowerDesc.contains("动车") || lowerDesc.contains("飞机") || 
                   lowerDesc.contains("transport") || lowerDesc.contains("bus") || 
                   lowerDesc.contains("subway") || lowerDesc.contains("taxi") || lowerDesc.contains("train")) {
            return "交通";
        } else if (lowerDesc.contains("住") || lowerDesc.contains("房") || lowerDesc.contains("租") || 
                   lowerDesc.contains("酒店") || lowerDesc.contains("宿舍") || 
                   lowerDesc.contains("house") || lowerDesc.contains("rent") || 
                   lowerDesc.contains("hotel")) {
            return "住房";
        } else if (lowerDesc.contains("水") || lowerDesc.contains("电") || lowerDesc.contains("气") || 
                   lowerDesc.contains("网") || lowerDesc.contains("宽带") || lowerDesc.contains("通讯") || 
                   lowerDesc.contains("话费") || 
                   lowerDesc.contains("utility") || lowerDesc.contains("water") || 
                   lowerDesc.contains("electricity") || lowerDesc.contains("gas") || 
                   lowerDesc.contains("internet") || lowerDesc.contains("phone")) {
            return "水电煤";
        } else if (lowerDesc.contains("娱乐") || lowerDesc.contains("游戏") || lowerDesc.contains("电影") || 
                   lowerDesc.contains("KTV") || lowerDesc.contains("演唱会") || lowerDesc.contains("表演") || 
                   lowerDesc.contains("entertainment") || lowerDesc.contains("game") || 
                   lowerDesc.contains("movie") || lowerDesc.contains("concert")) {
            return "娱乐";
        } else if (lowerDesc.contains("衣") || lowerDesc.contains("服") || lowerDesc.contains("鞋") || 
                   lowerDesc.contains("包") || lowerDesc.contains("cloth") || lowerDesc.contains("shoe") || 
                   lowerDesc.contains("bag") || lowerDesc.contains("wear") || lowerDesc.contains("fashion")) {
            return "购物";
        } else if (lowerDesc.contains("医") || lowerDesc.contains("药") || lowerDesc.contains("病") || 
                   lowerDesc.contains("疾") || lowerDesc.contains("诊所") || lowerDesc.contains("医院") || 
                   lowerDesc.contains("medical") || lowerDesc.contains("medicine") || 
                   lowerDesc.contains("hospital") || lowerDesc.contains("health")) {
            return "医疗";
        } else if (lowerDesc.contains("教育") || lowerDesc.contains("学") || lowerDesc.contains("培训") || 
                   lowerDesc.contains("课") || lowerDesc.contains("辅导") || lowerDesc.contains("书") || 
                   lowerDesc.contains("education") || lowerDesc.contains("school") || 
                   lowerDesc.contains("course") || lowerDesc.contains("book") || 
                   lowerDesc.contains("training")) {
            return "教育";
        }
        
        // 如果没有匹配到，返回默认类别
        return "其他支出";
    }
    
    /**
     * 使用简单规则自动判断交易是收入还是支出
     * 
     * @param description 交易描述
     * @param amount 交易金额
     * @param category 交易类别（可能已经通过规则确定）
     * @return 是否为支出
     */
    private boolean autoDetectIsExpense(String description, double amount, String category) {
        // 根据类别判断
        if (category.equals("工资") || category.equals("奖金") || category.equals("投资收益") || 
            category.equals("利息") || category.equals("退款") || category.equals("其他收入")) {
            return false;
        }
        
        // 根据描述中的关键词判断
        String lowerDesc = description.toLowerCase();
        if (lowerDesc.contains("收入") || lowerDesc.contains("工资") || lowerDesc.contains("薪水") || 
            lowerDesc.contains("奖金") || lowerDesc.contains("退款") || lowerDesc.contains("报销") || 
            lowerDesc.contains("投资收益") || lowerDesc.contains("利息") || 
            lowerDesc.contains("income") || lowerDesc.contains("salary") || 
            lowerDesc.contains("bonus") || lowerDesc.contains("refund") || 
            lowerDesc.contains("reimbursement") || lowerDesc.contains("interest")) {
            return false;
        }
        
        // 默认为支出
        return true;
    }
    
    /**
     * Creates a backup of the transactions CSV file.
     * 
     * @return true if the operation was successful, false otherwise
     */
    public boolean createBackup() {
        return csvExporter.createCsvBackup();
    }
}
