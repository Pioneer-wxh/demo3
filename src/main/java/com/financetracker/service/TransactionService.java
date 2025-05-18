package com.financetracker.service;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.financetracker.model.SavingGoal;
import com.financetracker.model.Settings;
import com.financetracker.model.Transaction;

/**
 * Service for managing transactions.
 */
public class TransactionService {
    
    private static final Logger LOGGER = Logger.getLogger(TransactionService.class.getName());
    // private static final Set<String> INCOME_CATEGORIES_FROM_PYTHON = Set.of("收入", "兼职", "投资"); // 已移除
    
    // 不再需要硬编码的完整路径
    // private static final String CSV_FILE_PATH = "E:\\code\\Java\\software_lab\\data\\transactions.csv";
    private final TransactionCsvExporter csvExporter;
    private final Settings settings; // 新增字段存储Settings引用
    
    /**
     * Constructor for TransactionService.
     * @param settings The application settings.
     */
    public TransactionService(Settings settings) { // 修改构造函数
        this.csvExporter = new TransactionCsvExporter();
        this.settings = settings; // 存储Settings引用
        
        // 确保数据目录存在 (不再需要在此处创建，由各服务在使用PathUtil获取路径后自行处理)
        // try {
        //     Path dataDir = Paths.get(TransactionCsvExporter.DATA_DIR); // 旧逻辑
        //     Files.createDirectories(dataDir);
        // } catch (Exception e) {
        //     System.err.println("创建数据目录时出错: " + e.getMessage());
        //     e.printStackTrace();
        // }
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

        // 根据类别设置 IsExpense
        if (transaction.getCategory() != null && this.settings.getIncomeCategories().contains(transaction.getCategory())) {
            transaction.setExpense(false);
        } else {
            // 如果不是明确的收入类别，可以保留原有的isExpense值，或者调用autoDetectIsExpense
            // 为简单起见，如果UI层已经设置了isExpense，我们这里可以信任它，除非它是已知收入类别
            // 如果需要更复杂的逻辑，例如总是重新检测，可以调用：
            // transaction.setExpense(autoDetectIsExpense(transaction.getDescription(), transaction.getAmount(), transaction.getCategory()));
            // 但请注意，这可能覆盖用户在UI上明确的选择。当前保留：如果不是已知收入，则信任传入的isExpense。
            // 如果传入的 transaction 本身没有正确设置 isExpense，且不是已知收入类别，则默认为支出 (true)
             if (transaction.getCategory() != null && !this.settings.getIncomeCategories().contains(transaction.getCategory())) {
                // 如果不是已知收入，且UI没有明确设置isExpense为false，可以考虑默认为true
                // 不过，Transaction对象本身在创建时应有默认的isExpense处理
                // 此处仅确保收入类别被正确设为false。
                // 若要强制非收入类别为true，则： transaction.setExpense(true);
                // 但这可能不是期望行为，因为一个非特定收入的条目也可能被用户手动设为收入。
                // 最好的做法是UI确保isExpense的初始值，这里只覆盖已知收入类别。
            }
        }

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
                // 根据类别设置 IsExpense
                if (transaction.getCategory() != null && this.settings.getIncomeCategories().contains(transaction.getCategory())) {
                    transaction.setExpense(false);
                } else {
                    // 与addTransaction中类似的考虑
                    // 此处也仅确保收入类别被正确设为false，其他情况信任传入的transaction对象的isExpense值。
                    // 如果需要对非收入类别强制设为true，则：transaction.setExpense(true);
                }
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
     * Saves transactions to the data file using the exporter.
     * 
     * @param transactions The transactions to save
     * @return true if the operation was successful, false otherwise
     */
    public boolean saveTransactions(List<Transaction> transactions) {
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
     * 使用规则自动检测交易类别和类型 (不再使用AI进行此内部检测)
     * 
     * @param description 交易描述
     * @param amount 交易金额
     * @return 包含类别和交易类型的Map
     */
    private Map<String, Object> detectCategoryAndType(String description, double amount) {
        // AI分析部分已移除，此方法现在仅依赖后备规则。
        // 如果将来需要在CSV导入中重新启用AI，
        // 需要重构 importFromCsv 以便能正确获取或注入 AiAssistantService 实例。
        Map<String, Object> result = new HashMap<>();
        
        // 初始假设：金额小于0为支出。这可能需要根据 autoDetectIsExpense 调整。
        // autoDetectCategory 通常也需要一个 isExpense 的初始判断。
        // 我们先基于 amount < 0 做初步判断，然后 autoDetectCategory 和 autoDetectIsExpense 会进一步精炼。
        boolean initialIsExpenseGuess = amount < 0; 
        // 注意：在 Transaction 模型和 CSV 导入逻辑中，金额通常会被处理为正数，然后用 isExpense 字段区分。
        // 此处 amount 参数是原始传入的，可能为负。
        // 为了与 autoDetectCategory 和 autoDetectIsExpense 兼容，它们可能期望正金额。
        // 但查看这两个方法的实现，它们通常处理的是正金额，并通过布尔isExpense参数判断。
        // 此处的 amount 可能是正也可能是负，autoDetectIsExpense 内部会处理。

        String category = autoDetectCategory(description, Math.abs(amount), initialIsExpenseGuess);
        boolean isExpense = autoDetectIsExpense(description, Math.abs(amount), category);
        
        result.put("category", category);
        result.put("isExpense", isExpense);
            
        return result;
    }
    
    /**
     * 从CSV文件导入交易记录
     * 
     * @param filePath CSV文件路径
     * @param dateColumn 日期列名
     * @param amountColumn 金额列名
     * @param descriptionColumn 描述列名
     * @param categoryColumn 类别列名（可选）
     * @param dateFormat 日期格式
     * @param isExpense 是否为支出（此参数已被忽略，现在会自动检测）
     * @return 导入的记录数量
     */
    public int importFromCsv(String filePath, String dateColumn, String amountColumn, 
                            String descriptionColumn, String categoryColumn, 
                            String dateFormat, boolean isExpense) {
        int importedCount = 0;
        
        try (Reader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreHeaderCase(true)
                    .setTrim(true)
                    .build())) {
            
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
                    boolean transactionIsExpense = true; // 默认为支出
                    String category = "未分类";
                    
                    // 首先基于金额判断交易类型
                    if (amount < 0) {
                        // 负数金额通常表示支出
                        transactionIsExpense = true;
                        amount = Math.abs(amount); // 取绝对值存储
                    } else {
                        // 尝试根据描述判断收入/支出
                        transactionIsExpense = autoDetectIsExpense(description, amount, "");
                    }
                    
                    // 如果类别列存在，优先使用类别列的值
                    if (categoryColumn != null && !categoryColumn.isEmpty()) {
                        try {
                            category = record.get(categoryColumn);
                            // 如果类别为空，则使用自动检测
                            if (category == null || category.trim().isEmpty()) {
                                if (useAiAssistant) {
                                    Map<String, Object> aiResult = detectCategoryAndType(description, amount);
                                    category = (String) aiResult.get("category");
                                    // 使用AI检测的交易类型
                                    transactionIsExpense = (boolean) aiResult.get("isExpense");
                                } else {
                                    category = autoDetectCategory(description, amount, transactionIsExpense);
                                    
                                    // 重新检测收入/支出类型
                                    if (!description.isEmpty()) {
                                        transactionIsExpense = autoDetectIsExpense(description, amount, category);
                                    }
                                }
                            } else {
                                // 即使有类别，也尝试根据类别名称判断交易类型
                                if (!description.isEmpty()) {
                                    // 根据类别和描述再次判断收入/支出
                                    boolean suggestedType = autoDetectIsExpense(description, amount, category);
                                    // 如果类别是典型的收入类别，优先使用类别判断结果
                                    // Use English categories for checking income type
                                    List<String> incomeCategories = getStandardIncomeCategories(); // Helper method to get defined income categories
                                    final String currentCategory = category; // Use a final variable for the lambda
                                    if (incomeCategories.stream().anyMatch(incomeCat -> incomeCat.equalsIgnoreCase(currentCategory))) {
                                        transactionIsExpense = false;
                                    } else if (suggestedType) {
                                        // 如果自动检测为支出，则采用
                                        transactionIsExpense = true;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("获取类别字段出错: " + e.getMessage() + "，使用自动分类");
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
                            category = autoDetectCategory(description, amount, transactionIsExpense);
                        }
                    } else {
                        // 使用规则分析类别
                        category = autoDetectCategory(description, amount, transactionIsExpense);
                        
                        // 根据类别再次确认交易类型
                        if (!description.isEmpty()) {
                            transactionIsExpense = autoDetectIsExpense(description, amount, category);
                        }
                    }
                    
                    // 确保金额为正数
                    amount = Math.abs(amount);
                    
                    // ---- 新增最终检查 ----
                    // 如果类别在我们定义的中文收入类别列表中，则确保 transactionIsExpense 为 false
                    if (category != null && this.settings.getIncomeCategories().contains(category)) {
                        transactionIsExpense = false;
                    }
                    // ---- 结束新增 ----
                    
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
            if (lowerDesc.contains("salary") || lowerDesc.contains("payroll")) {
                return "Salary";
            } else if (lowerDesc.contains("bonus") || lowerDesc.contains("award")) {
                return "Bonus";
            } else if (lowerDesc.contains("invest") || lowerDesc.contains("stock") || lowerDesc.contains("fund") || lowerDesc.contains("financial")) {
                return "Investment";
            } else if (lowerDesc.contains("interest") || (lowerDesc.contains("bank") && lowerDesc.contains("deposit"))) {
                return "Interest";
            } else if (lowerDesc.contains("refund") || lowerDesc.contains("reimbursement")) {
                return "Refund";
            } else if (lowerDesc.contains("gift") || lowerDesc.contains("present")) {
                return "Gift";
            } else if (lowerDesc.contains("freelance") || lowerDesc.contains("part-time job")) {
                return "Freelance/Part-time";
            } else {
                return "Other Income";
            }
        }
        
        // 支出类别
        if (lowerDesc.contains("food") || lowerDesc.contains("eat") || lowerDesc.contains("meal") || 
            lowerDesc.contains("restaurant") || lowerDesc.contains("grocery") || lowerDesc.contains("supermarket") || lowerDesc.contains("dining")) {
            return "Food";
        } else if (lowerDesc.contains("shop") || lowerDesc.contains("buy") || lowerDesc.contains("purchase") || 
                   lowerDesc.contains("store") || lowerDesc.contains("mall") || lowerDesc.contains("online") ||
                   lowerDesc.contains("taobao") || lowerDesc.contains("jd") || lowerDesc.contains("amazon") || lowerDesc.contains("apparel")) {
            if (lowerDesc.contains("cloth") || lowerDesc.contains("shoe") || 
                lowerDesc.contains("bag") || lowerDesc.contains("wear") || lowerDesc.contains("fashion")) {
                return "Clothing";
            }
            return "Shopping";
        } else if (lowerDesc.contains("traffic") || lowerDesc.contains("bus") || lowerDesc.contains("subway") || 
                   lowerDesc.contains("taxi") || lowerDesc.contains("didi") || lowerDesc.contains("train") || 
                   lowerDesc.contains("flight") || lowerDesc.contains("gas") || lowerDesc.contains("fuel") || 
                   lowerDesc.contains("parking")) {
            return "Transportation";
        } else if (lowerDesc.contains("house") || lowerDesc.contains("rent") || lowerDesc.contains("mortgage") ||
                   lowerDesc.contains("hotel") || lowerDesc.contains("property fee") || lowerDesc.contains("accommodation")) {
            return "Housing";
        } else if (lowerDesc.contains("utility") || lowerDesc.contains("water") || 
                   lowerDesc.contains("electricity") || lowerDesc.contains("gas") || 
                   lowerDesc.contains("internet") || (lowerDesc.contains("phone") && lowerDesc.contains("bill"))) {
            return "Utilities";
        } else if ((lowerDesc.contains("phone") && !lowerDesc.contains("bill")) ||
                   lowerDesc.contains("mobile") || lowerDesc.contains("communication")) {
            return "Communication";
        } else if (lowerDesc.contains("entertainment") || lowerDesc.contains("movie") || lowerDesc.contains("game") || 
                   lowerDesc.contains("travel") || lowerDesc.contains("concert") || lowerDesc.contains("sport") || 
                   lowerDesc.contains("hobby")) {
            return "Entertainment";
        } else if (lowerDesc.contains("medical") || lowerDesc.contains("medicine") || 
                   lowerDesc.contains("hospital") || lowerDesc.contains("health") || lowerDesc.contains("doctor") || 
                   lowerDesc.contains("pharmacy")) {
            return "Healthcare";
        } else if (lowerDesc.contains("education") || lowerDesc.contains("school") || 
                   lowerDesc.contains("course") || lowerDesc.contains("book") || 
                   lowerDesc.contains("training") || lowerDesc.contains("tuition")) {
            return "Education";
        } else if (lowerDesc.contains("child") || lowerDesc.contains("kid") || lowerDesc.contains("baby")) {
            return "Kids";
        } else if (lowerDesc.contains("pet") || lowerDesc.contains("cat") || lowerDesc.contains("dog")) {
            return "Pets";
        } else if (lowerDesc.contains("insurance")) {
            return "Insurance";
        } else if (lowerDesc.contains("tax")) {
            return "Taxes";
        } else if (lowerDesc.contains("donation") || lowerDesc.contains("charity")) {
            return "Donation";
        } else if (lowerDesc.contains("repair") || lowerDesc.contains("maintenance")) {
            return "Repairs/Maintenance";
        } else if (lowerDesc.contains("gym") || lowerDesc.contains("fitness")) {
            return "Fitness/Sports";
        } else if (lowerDesc.contains("subscription") || lowerDesc.contains("membership")) {
            return "Subscriptions/Memberships";
        }
        
        return "Others";
    }
    
    // Helper method to provide a standard list of income categories (in English)
    // This should ideally come from Settings or a shared constant.
    private List<String> getStandardIncomeCategories() {
        return List.of("Salary", "Bonus", "Investment", "Interest", "Refund", "Gift", "Freelance/Part-time", "Other Income");
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
        // 首先，检查是否为已知的收入类别 (来自 Settings)
        if (category != null && !category.isEmpty() && this.settings.getIncomeCategories().contains(category)) {
            return false; // 明确是收入
        }

        // 根据类别判断 (此部分为原有逻辑，现在作为已知收入类别检查后的备用)
        if (category != null && !category.isEmpty()) {
            // 使用英文类别名进行判断
            if (category.equalsIgnoreCase("Salary") || category.equalsIgnoreCase("Bonus") || 
                category.equalsIgnoreCase("Investment") || category.equalsIgnoreCase("Interest") || 
                category.equalsIgnoreCase("Refund") || category.equalsIgnoreCase("Gift") || 
                category.equalsIgnoreCase("Freelance/Part-time") || category.equalsIgnoreCase("Other Income")) {
                return false; // 这些是明确的收入类别
            }
            // 如果类别是支出类别中定义的，则肯定是支出 (虽然此函数主要用于在类别未知或不明确时辅助判断)
             if (category.equalsIgnoreCase("Food") || category.equalsIgnoreCase("Shopping") ||
                category.equalsIgnoreCase("Transportation") || category.equalsIgnoreCase("Housing") ||
                category.equalsIgnoreCase("Utilities") || category.equalsIgnoreCase("Communication") ||
                category.equalsIgnoreCase("Entertainment") || category.equalsIgnoreCase("Clothing") ||
                category.equalsIgnoreCase("Healthcare") || category.equalsIgnoreCase("Education") ||
                category.equalsIgnoreCase("Kids") || category.equalsIgnoreCase("Pets") ||
                category.equalsIgnoreCase("Insurance") || category.equalsIgnoreCase("Taxes") ||
                category.equalsIgnoreCase("Donation") || category.equalsIgnoreCase("Repairs/Maintenance") ||
                category.equalsIgnoreCase("Fitness/Sports") || category.equalsIgnoreCase("Subscriptions/Memberships") ||
                category.equalsIgnoreCase("Others")) {
                return true;
            }
        }
        
        // 根据描述中的关键词判断 (保留部分中文关键词以兼容旧数据或中文输入)
        String lowerDesc = description.toLowerCase();
        
        // 收入关键词
        String[] incomeKeywords = {
            "salary", "income", "bonus", "refund", "reimbursement", "interest", "dividend",
            "return", "rebate", "subsidy", "allowance", "benefit", "rent income", "part-time",
            "side job", "gift", "payroll", "revenue", "earnings"
        };
        
        // 支出关键词
        String[] expenseKeywords = {
            "expense", "cost", "purchase", "buy", "pay", "payment", "bill", "fee", "charge",
            "spend", "spent", "order"
        };
        
        // 检查描述中是否包含收入关键词
        for (String keyword : incomeKeywords) {
            if (lowerDesc.contains(keyword)) {
                return false; // 包含收入关键词，判定为收入
            }
        }
        
        // 检查描述中是否包含支出关键词
        for (String keyword : expenseKeywords) {
            if (lowerDesc.contains(keyword)) {
                return true; // 包含支出关键词，判定为支出
            }
        }
        
        // 判断常见收入来源名称
        String[] incomeSourceNames = {
            "工商银行", "农业银行", "建设银行", "中国银行", "交通银行", "招商银行", "邮政储蓄",
            "支付宝", "微信", "公司", "单位", "企业", "集团", "有限公司", "银行转账", "转账收入"
        };
        
        for (String source : incomeSourceNames) {
            if (lowerDesc.contains(source.toLowerCase())) {
                // 如果同时包含"转出"、"支出"等词，则仍判断为支出
                if (lowerDesc.contains("转出") || lowerDesc.contains("支出") || 
                    lowerDesc.contains("付款") || lowerDesc.contains("消费")) {
                    return true;
                }
                return false; // 可能是银行或公司转账收入
            }
        }
        
        // 默认为支出
        return true;
    }
    
    /**
     * Creates a backup of the transaction data file.
     * @return true if the backup was successful, false otherwise
     */
    public boolean createBackup() {
        return csvExporter.createCsvBackup();
    }

    /**
     * Exports all transactions to the designated export file.
     * @return true if export was successful, false otherwise
     */
    public boolean exportAllTransactions() {
        List<Transaction> transactions = getAllTransactions();
        return csvExporter.exportAllTransactionsToFile(transactions);
    }

    /**
     * Exports transactions grouped by month to the designated classify directory.
     * @return An ExportResult object containing details of the export process.
     */
    public TransactionCsvExporter.ExportResult exportTransactionsByMonth() {
        List<Transaction> transactions = getAllTransactions();
        return csvExporter.exportTransactionsByMonth(transactions);
    }

    /**
     * 获取当前财务月的开始和结束日期
     * 这是根据设置中的monthStartDay来定义财务月，而不是自然月
     * 
     * @return 包含开始和结束日期的Map
     */
    public Map<String, LocalDate> getCurrentFinancialMonthRange() {
        // 从SettingsService获取Settings
        SettingsService settingsService = new SettingsService();
        Settings settings = settingsService.getSettings();
        int monthStartDay = settings.getMonthStartDay();
        
        LocalDate today = LocalDate.now();
        LocalDate startDate;
        
        // 确定当前财务月的开始日期
        if (today.getDayOfMonth() >= monthStartDay) {
            // 如果当前日期已过本月的起始日，则财务月从本月的起始日开始
            startDate = today.withDayOfMonth(monthStartDay);
        } else {
            // 如果当前日期在本月起始日之前，则财务月从上个月的起始日开始
            startDate = today.minusMonths(1).withDayOfMonth(monthStartDay);
        }
        
        // 财务月结束日期是下个财务月开始前一天
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        
        Map<String, LocalDate> result = new HashMap<>();
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        
        return result;
    }

    /**
     * 获取当前财务月的交易记录
     * 使用设置中的monthStartDay而不是自然月
     * 
     * @return 当前财务月的交易记录
     */
    public List<Transaction> getTransactionsForCurrentFinancialMonth() {
        Map<String, LocalDate> dateRange = getCurrentFinancialMonthRange();
        LocalDate startDate = dateRange.get("startDate");
        LocalDate endDate = dateRange.get("endDate");
        
        return getTransactionsForDateRange(startDate, endDate);
    }

    /**
     * 获取指定财务月的开始和结束日期
     * 
     * @param year 年份
     * @param month 月份(1-12)
     * @return 包含开始和结束日期的Map
     */
    public Map<String, LocalDate> getFinancialMonthRange(int year, int month) {
        // 从SettingsService获取Settings
        SettingsService settingsService = new SettingsService();
        Settings settings = settingsService.getSettings();
        int monthStartDay = settings.getMonthStartDay();
        
        // 指定月的起始日
        LocalDate startDate = LocalDate.of(year, month, monthStartDay);
        
        // 财务月结束日期是下个财务月开始前一天
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        
        Map<String, LocalDate> result = new HashMap<>();
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        
        return result;
    }

    /**
     * 获取指定财务月的交易记录
     * 
     * @param year 年份
     * @param month 月份(1-12)
     * @return 该财务月的交易记录
     */
    public List<Transaction> getTransactionsForFinancialMonth(int year, int month) {
        Map<String, LocalDate> range = getFinancialMonthRange(year, month);
        return getTransactionsForDateRange(range.get("startDate"), range.get("endDate"));
    }

    /**
     * Calculates the remaining balance for the current financial month.
     * Remaining Balance = Total Income - Total Expenses, without subtracting active savings contributions.
     *
     * @param settings The application settings, used to get saving goals.
     * @return The calculated remaining balance.
     */
    public double calculateRemainingBalanceForCurrentFinancialMonth(Settings settings) {
        List<Transaction> transactions = getTransactionsForCurrentFinancialMonth();
        double totalIncome = getTotalIncome(transactions);
        double totalExpense = getTotalExpense(transactions);

        return totalIncome - totalExpense;
    }

    /**
     * Processes monthly savings contributions for all active saving goals for a given month.
     * It creates a "Savings" transaction for each goal's monthly contribution amount
     * and updates the goal's current amount.
     *
     * @param settingsService The service to access and save application settings (including saving goals).
     * @param monthToProcess The YearMonth for which to process savings contributions.
     * @param dayForSavingTransaction The day of the month on which the savings transaction should be dated.
     * @param savingsCategory The category name to be used for savings transactions (e.g., "Savings").
     * @return true if all operations were successful, false if settings are null or saving settings fails.
     */
    public boolean processMonthlySavingsContributions(SettingsService settingsService, YearMonth monthToProcess, int dayForSavingTransaction, String savingsCategory) {
        if (settingsService == null) {
            LOGGER.log(Level.SEVERE, "SettingsService is null. Cannot process monthly savings.");
            return false;
        }
        Settings settings = settingsService.getSettings();
        if (settings == null || settings.getSavingGoals() == null) {
            LOGGER.log(Level.WARNING, "Settings or SavingGoals list is null. Cannot process monthly savings.");
            return true; // No goals to process, not an error in this context.
        }

        LocalDate transactionDate;
        try {
            transactionDate = monthToProcess.atDay(dayForSavingTransaction);
        } catch (java.time.DateTimeException e) {
            LOGGER.log(Level.SEVERE, "Invalid dayForSavingTransaction " + dayForSavingTransaction + " for month " + monthToProcess, e);
            // Fallback to last day of month if provided day is invalid
            transactionDate = monthToProcess.atEndOfMonth();
            LOGGER.log(Level.INFO, "Falling back to using last day of month for savings transaction: " + transactionDate);
        }

        boolean allGoalsProcessedSuccessfully = true;
        boolean settingsNeedSaving = false;

        for (SavingGoal goal : settings.getSavingGoals()) {
            if (goal.isActive() && goal.getMonthlyContribution() > 0 && !goal.isCompleted()) {
                // Check if the contribution month is on or after the goal's start date
                // and (if a target date exists) on or before the goal's target date.
                LocalDate goalStartDate = goal.getStartDate();
                LocalDate goalTargetDate = goal.getTargetDate();

                boolean shouldContribute = !transactionDate.isBefore(goalStartDate);
                if (goalTargetDate != null && transactionDate.isAfter(goalTargetDate)) {
                    shouldContribute = false; // Past target date
                }
                
                // Additional check: ensure we don't over-contribute if very close to target
                double contributionAmount = goal.getMonthlyContribution();
                if (goal.getCurrentAmount() + contributionAmount > goal.getTargetAmount() && goal.getTargetAmount() > 0) {
                    // Adjust contribution to not exceed target.
                    // This case might also mean the goal is effectively completed by this contribution.
                    contributionAmount = goal.getTargetAmount() - goal.getCurrentAmount();
                    if (contributionAmount <= 0) { // Already met or exceeded
                        LOGGER.log(Level.INFO, "Saving goal ''{0}'' is already met or exceeded. No contribution needed.", goal.getName());
                        continue; 
                    }
                }


                if (shouldContribute) {
                    String description = String.format("Monthly contribution to savings goal: %s", goal.getName());
                    Transaction savingsTransaction = new Transaction(
                            transactionDate,
                            contributionAmount,
                            description,
                            savingsCategory, // Use the passed savingsCategory
                            null, // Participant
                            "Automatic monthly savings contribution", // Notes
                            true // isExpense = true for savings contributions
                    );

                    if (addTransaction(savingsTransaction)) {
                        goal.addContribution(contributionAmount); // This method handles currentAmount += contributionAmount
                        settingsNeedSaving = true;
                        LOGGER.log(Level.INFO, "Processed monthly savings contribution for goal: ''{0}'', Amount: {1}", new Object[]{goal.getName(), contributionAmount});
                    } else {
                        allGoalsProcessedSuccessfully = false;
                        LOGGER.log(Level.SEVERE, "Failed to add savings transaction for goal: ''{0}''", goal.getName());
                        // Decide if we should stop or continue with other goals. For now, continue.
                    }
                }
            }
        }

        if (settingsNeedSaving) {
            if (!settingsService.saveSettings()) {
                LOGGER.log(Level.SEVERE, "Failed to save settings after updating saving goals.");
                return false; // Critical failure
            }
        }
        return allGoalsProcessedSuccessfully;
    }

    /**
     * Performs month-end closing for financial months that have not yet been closed.
     * Calculates the surplus for each completed financial month since the last closing,
     * adds it to the overall account balance, and updates the last closed month in settings.
     *
     * @param settingsService The service to access and save application settings.
     * @return true if any month was successfully closed, false otherwise or if an error occurred.
     */
    public boolean performMonthEndClosing(SettingsService settingsService) {
        if (settingsService == null) {
            LOGGER.log(Level.SEVERE, "SettingsService is null. Cannot perform month-end closing.");
            return false;
        }
        Settings settings = settingsService.getSettings();
        if (settings == null) {
            LOGGER.log(Level.SEVERE, "Settings are null. Cannot perform month-end closing.");
            return false;
        }

        int monthStartDay = settings.getMonthStartDay();
        String lastClosedMonthStr = settings.getLastMonthClosed(); // Format: YYYY-MM
        LocalDate today = LocalDate.now();
        boolean monthClosedThisRun = false;

        // Determine the YearMonth of the financial month that ends *before* the current financial month starts.
        // This is the latest month we can safely close.
        LocalDate currentFinancialMonthStartDate;
        if (today.getDayOfMonth() >= monthStartDay) {
            currentFinancialMonthStartDate = today.withDayOfMonth(monthStartDay);
        } else {
            currentFinancialMonthStartDate = today.minusMonths(1).withDayOfMonth(monthStartDay);
        }
        // The latest financial month we can close is the one that started *before* currentFinancialMonthStartDate
        YearMonth latestClosableFinancialYearMonth = YearMonth.from(currentFinancialMonthStartDate.minusMonths(1));


        YearMonth startClosingFromYearMonth;
        if (lastClosedMonthStr == null || lastClosedMonthStr.trim().isEmpty()) {
            // If no month has ever been closed, we can only close the immediately preceding financial month.
            // This simplifies the first run: we don't try to close all historical months automatically.
            // A separate utility might be needed for historical bulk closing if desired.
            LOGGER.log(Level.INFO, "No previous month closed. Attempting to close the latest completed financial month: " + latestClosableFinancialYearMonth);
            startClosingFromYearMonth = latestClosableFinancialYearMonth;
        } else {
            try {
                YearMonth lastClosedYM = YearMonth.parse(lastClosedMonthStr, DateTimeFormatter.ofPattern("yyyy-MM"));
                startClosingFromYearMonth = lastClosedYM.plusMonths(1);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Invalid format for lastMonthClosed: " + lastClosedMonthStr + ". Cannot proceed with month-end closing.", e);
                return false;
            }
        }

        // Loop from startClosingFromYearMonth up to (and including) latestClosableFinancialYearMonth
        YearMonth currentMonthToAttemptClose = startClosingFromYearMonth;
        while (!currentMonthToAttemptClose.isAfter(latestClosableFinancialYearMonth)) {
            LOGGER.log(Level.INFO, "Attempting to close financial month: " + currentMonthToAttemptClose);

            // Get the date range for the financial month to close
            // The 'year' and 'month' for getFinancialMonthRange should correspond to the start of that financial month.
            Map<String, LocalDate> financialMonthDateRange = getFinancialMonthRange(currentMonthToAttemptClose.getYear(), currentMonthToAttemptClose.getMonthValue());
            LocalDate periodStartDate = financialMonthDateRange.get("startDate");
            LocalDate periodEndDate = financialMonthDateRange.get("endDate");

            if (periodEndDate.isAfter(today.minusDays(1))) { // Ensure the financial month has fully passed
                 LOGGER.log(Level.INFO, "Financial month " + currentMonthToAttemptClose + " has not fully passed. Skipping.");
                 break; // Stop if we've reached a month that isn't fully over
            }

            List<Transaction> transactionsForMonth = getTransactionsForDateRange(periodStartDate, periodEndDate);
            double surplus = getNetAmount(transactionsForMonth);

            LOGGER.log(Level.INFO, String.format("Surplus for %s (%s to %s): %.2f",
                    currentMonthToAttemptClose, periodStartDate, periodEndDate, surplus));

            settings.setOverallAccountBalance(settings.getOverallAccountBalance() + surplus);
            settings.setLastMonthClosed(currentMonthToAttemptClose.format(DateTimeFormatter.ofPattern("yyyy-MM")));

            if (settingsService.saveSettings()) {
                LOGGER.log(Level.INFO, "Successfully closed financial month " + currentMonthToAttemptClose +
                        ". New overall account balance: " + settings.getOverallAccountBalance());
                monthClosedThisRun = true;
            } else {
                LOGGER.log(Level.SEVERE, "Failed to save settings after closing month " + currentMonthToAttemptClose +
                        ". Month-end closing process halted.");
                // Potentially rollback in-memory changes to settings if save fails?
                // For now, if save fails, the loop will stop, and lastMonthClosed won't advance in memory for the next iteration.
                // The balance might be updated in memory but not persisted. This state is problematic.
                // It's better to halt.
                return false; // Critical error, stop processing.
            }
            currentMonthToAttemptClose = currentMonthToAttemptClose.plusMonths(1);
        }
        return monthClosedThisRun;
    }
}
