package com.financetracker.ai;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.financetracker.model.Transaction;
import com.financetracker.service.TransactionService;
import com.financetracker.util.PathUtil;

/**
 * CSV数据读取器
 * 用于从CSV文件读取交易数据
 */
public class CsvDataReader {
    // private static final String CSV_FILE_PATH = "E:\\code\\Java\\software_lab\\data\\transactions.csv";
    // Use PathUtil directly for path info
    // private static final String DATA_DIR = TransactionCsvExporter.DATA_DIR; // No longer valid
    private static final String CSV_FILE_NAME = "transactions.csv"; // Keep filename constant
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private static TransactionService transactionService;
    
    /**
     * 设置事务服务
     * 
     * @param service 事务服务实例
     */
    public static void setTransactionService(TransactionService service) {
        transactionService = service;
    }

    // 获取完整绝对路径
    private static Path getCsvFilePath() {
        // return Paths.get(DATA_DIR, CSV_FILE_NAME);
        return PathUtil.getTransactionsCsvPath(); // Use PathUtil
    }

    /**
     * 检查CSV文件是否存在
     * @return 文件是否存在
     */
    public static boolean isCsvFileExists() {
        return Files.exists(getCsvFilePath()); // 使用 Path 对象检查
    }
    
    /**
     * 检查CSV数据库文件是否存在（与isCsvFileExists相同，为兼容性添加）
     * 
     * @return 文件是否存在
     */
    public static boolean databaseExists() {
        return isCsvFileExists();
    }

    /**
     * 获取所有交易记录
     * 
     * @return 所有交易记录列表
     */
    public static List<Transaction> readAllTransactions() {
        if (transactionService != null) {
            return transactionService.getAllTransactions();
        }
        return new ArrayList<>();
    }
    
    /**
     * 获取指定月份的交易记录
     * 
     * @param year 年份
     * @param month 月份（1-12）
     * @return 指定月份的交易记录列表
     */
    public static List<Transaction> getTransactionsForMonth(int year, int month) {
        List<Transaction> allTransactions = readAllTransactions();
        
        return allTransactions.stream()
            .filter(t -> t.getDate().getYear() == year && t.getDate().getMonthValue() == month)
            .collect(Collectors.toList());
    }
    
    /**
     * 获取当前月份的交易记录
     * 
     * @return 当前月份的交易记录列表
     */
    public static List<Transaction> getCurrentMonthTransactions() {
        if (transactionService != null) {
            return transactionService.getTransactionsForCurrentMonth();
        }
        return new ArrayList<>();
    }
    
    /**
     * 获取指定类别的交易记录
     * 
     * @param category 类别名称
     * @return 指定类别的交易记录列表
     */
    public static List<Transaction> getTransactionsByCategory(String category) {
        List<Transaction> allTransactions = readAllTransactions();
        
        return allTransactions.stream()
            .filter(t -> t.getCategory().equalsIgnoreCase(category))
            .collect(Collectors.toList());
    }
    
    /**
     * 获取指定年月的交易记录
     * 
     * @param year 年份
     * @param month 月份
     * @return 指定年月的交易记录
     */
    public static List<Transaction> getTransactionsByYearMonth(int year, int month) {
        if (transactionService != null) {
            return transactionService.getTransactionsForMonth(year, month);
        }
        return new ArrayList<>();
    }
} 