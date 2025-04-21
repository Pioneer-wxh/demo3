package com.financetracker.ai;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.financetracker.model.Transaction;

/**
 * CSV数据读取器
 * 用于从CSV文件读取交易数据
 */
public class CsvDataReader {
    private static final String CSV_FILE_PATH = "E:\\code\\Java\\software_lab\\data\\transactions.csv";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * 检查CSV文件是否存在
     * @return 文件是否存在
     */
    public static boolean isCsvFileExists() {
        File file = new File(CSV_FILE_PATH);
        return file.exists() && file.isFile();
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
     * 从CSV文件读取所有交易记录
     * @return 交易记录列表
     */
    public static List<Transaction> readAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        
        if (!isCsvFileExists()) {
            System.err.println("CSV数据库文件不存在: " + CSV_FILE_PATH);
            return transactions;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE_PATH))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                // 跳过CSV头行
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                String[] data = line.split(",");
                if (data.length >= 5) {
                    try {
                        String id = data[0].trim();
                        LocalDate date = LocalDate.parse(data[1].trim(), DATE_FORMATTER);
                        double amount = Double.parseDouble(data[2].trim());
                        String description = data[3].trim();
                        String category = data[4].trim();
                        boolean isExpense = true;
                        
                        if (data.length > 7) {
                            isExpense = Boolean.parseBoolean(data[7].trim());
                        }
                        
                        String participant = "";
                        String notes = "";
                        
                        if (data.length > 5) {
                            participant = data[5].trim();
                        }
                        
                        if (data.length > 6) {
                            notes = data[6].trim();
                        }
                        
                        Transaction transaction = new Transaction();
                        transaction.setId(id);
                        transaction.setDate(date);
                        transaction.setAmount(amount);
                        transaction.setDescription(description);
                        transaction.setCategory(category);
                        transaction.setParticipant(participant);
                        transaction.setNotes(notes);
                        transaction.setExpense(isExpense);
                        
                        transactions.add(transaction);
                    } catch (Exception e) {
                        System.err.println("解析CSV行时出错: " + line + ", 错误: " + e.getMessage());
                    }
                }
            }
            
            // 按日期排序（最新的优先）
            transactions.sort(Comparator.comparing(Transaction::getDate).reversed());
            
        } catch (IOException e) {
            System.err.println("读取CSV文件时出错: " + e.getMessage());
        }
        
        return transactions;
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
        LocalDate now = LocalDate.now();
        return getTransactionsForMonth(now.getYear(), now.getMonthValue());
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
} 