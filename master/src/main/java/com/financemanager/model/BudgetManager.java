package com.financemanager.model;

import java.io.*;
import java.util.*;

/**
 * 预算管理类
 * 负责管理用户的预算目标和储蓄计划
 */
public class BudgetManager {
    private Map<String, Double> categoryBudgets; // 各类别的预算限额
    private double monthlyBudget; // 月度总预算
    private double savingsGoal; // 储蓄目标
    private static final String BUDGET_FILE = "data/budget.csv";
    
    
}