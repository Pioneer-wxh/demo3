package com.financemanager.ai;

import com.financemanager.model.Transaction;
import java.io.*;
import java.util.*;

/**
 * 交易分类器类
 * 负责使用AI技术对交易进行自动分类，并允许用户手动校正
 */
public class TransactionClassifier {
    // 预定义的交易类别
    private static final List<String> DEFAULT_EXPENSE_CATEGORIES = Arrays.asList(
            "餐饮", "购物", "交通", "住房", "娱乐", "教育", "医疗", "旅行", 
            "日用品", "通讯", "服装", "礼品", "其他支出"
    );
    
    private static final List<String> DEFAULT_INCOME_CATEGORIES = Arrays.asList(
            "工资", "奖金", "投资收益", "兼职收入", "礼金", "退款", "其他收入"
    );
    
    // 用户自定义的类别关键词映射
    private Map<String, List<String>> categoryKeywords;
    private static final String KEYWORDS_FILE = "data/category_keywords.csv";
    
    public TransactionClassifier() {
        this.categoryKeywords = new HashMap<>();
        //loadDefaultKeywords();
        //loadUserKeywords();
    }
    

}