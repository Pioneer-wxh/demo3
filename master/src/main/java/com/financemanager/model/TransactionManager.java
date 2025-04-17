package com.financemanager.model;

import java.io.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 交易记录管理类
 * 负责交易记录的增删改查和持久化存储
 */
public class TransactionManager {
    private List<Transaction> transactions;
    private static final String DEFAULT_DATA_FILE = "data/transactions.csv";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * 构造函数
     */
    public TransactionManager() {
        this.transactions = new ArrayList<>();
        //loadTransactions(); // 初始化时尝试加载已有数据
    }
    
    
}