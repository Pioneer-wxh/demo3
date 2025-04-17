package com.financemanager.ai;

import com.financemanager.model.Transaction;
import com.financemanager.model.BudgetManager;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 支出分析器类
 * 负责分析用户的消费习惯，提供支出洞察和预算建议
 */
public class ExpenseAnalyzer {
    private static final int MONTHS_TO_ANALYZE = 6; // 分析最近6个月的数据
    private static final double SEASONAL_THRESHOLD = 1.5; // 季节性支出阈值（相对于平均值）
    
   
}