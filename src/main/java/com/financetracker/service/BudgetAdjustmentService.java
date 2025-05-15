package com.financetracker.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.financetracker.model.Settings;
import com.financetracker.model.SpecialDate;

/**
 * 预算调整服务类，用于根据特殊日期调整预算
 */
public class BudgetAdjustmentService {
    private static final Logger LOGGER = Logger.getLogger(BudgetAdjustmentService.class.getName());
    
    private final SettingsService settingsService;
    private final SpecialDateService specialDateService;
    
    /**
     * 构造函数
     * 
     * @param settingsService 设置服务
     * @param specialDateService 特殊日期服务
     */
    public BudgetAdjustmentService(SettingsService settingsService, SpecialDateService specialDateService) {
        this.settingsService = settingsService;
        this.specialDateService = specialDateService;
    }
    
    /**
     * 获取指定日期的调整后预算
     * 
     * @param date 需要计算预算的日期
     * @return 调整后的预算金额
     */
    public double getAdjustedBudgetForDate(LocalDate date) {
        // 获取基础预算
        Settings settings = settingsService.getSettings();
        double baseBudget = settings.getMonthlyBudget();
        
        // 查找该日期的特殊日期
        List<SpecialDate> specialDatesForDay = specialDateService.findSpecialDatesByDate(date);
        
        // 如果没有特殊日期，返回基础预算
        if (specialDatesForDay.isEmpty()) {
            return baseBudget;
        }
        
        // 计算调整后的预算
        double adjustedBudget = baseBudget;
        for (SpecialDate specialDate : specialDatesForDay) {
            double impactFactor = specialDate.getExpectedImpact() / 100.0; // 转换为系数
            adjustedBudget = adjustedBudget * (1 + impactFactor); // 正数为增加，负数为减少
            
            LOGGER.log(Level.INFO, "预算因特殊日期 {0} 调整: {1}% -> {2}", 
                    new Object[]{specialDate.getName(), specialDate.getExpectedImpact(), adjustedBudget});
        }
        
        return adjustedBudget;
    }
    
    /**
     * 获取指定月份的调整后预算
     * 
     * @param yearMonth 年月
     * @return 调整后的月度预算金额
     */
    public double getAdjustedBudgetForMonth(YearMonth yearMonth) {
        // 获取基础预算
        Settings settings = settingsService.getSettings();
        double baseBudget = settings.getMonthlyBudget();
        
        // 查找该月份的特殊日期
        List<SpecialDate> specialDatesForMonth = specialDateService.findSpecialDatesByMonth(yearMonth.getMonthValue());
        
        // 如果没有特殊日期，返回基础预算
        if (specialDatesForMonth.isEmpty()) {
            return baseBudget;
        }
        
        // 计算调整后的预算
        double adjustedBudget = baseBudget;
        for (SpecialDate specialDate : specialDatesForMonth) {
            double impactFactor = specialDate.getExpectedImpact() / 100.0; // 转换为系数
            adjustedBudget = adjustedBudget * (1 + impactFactor); // 正数为增加，负数为减少
            
            LOGGER.log(Level.INFO, "每月预算因特殊日期 {0} 调整: {1}% -> {2}", 
                    new Object[]{specialDate.getName(), specialDate.getExpectedImpact(), adjustedBudget});
        }
        
        return adjustedBudget;
    }
    
    /**
     * 计算特定日期范围内的总预算
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 日期范围内的总预算
     */
    public double calculateBudgetForDateRange(LocalDate startDate, LocalDate endDate) {
        double totalBudget = 0.0;
        
        // 当前遍历的日期
        LocalDate currentDate = startDate;
        
        // 遍历日期范围
        while (!currentDate.isAfter(endDate)) {
            YearMonth currentYearMonth = YearMonth.from(currentDate);
            
            // 获取当月的天数
            int daysInMonth = currentYearMonth.lengthOfMonth();
            
            // 获取当月调整后的预算
            double monthlyBudget = getAdjustedBudgetForMonth(currentYearMonth);
            
            // 计算每天的预算
            double dailyBudget = monthlyBudget / daysInMonth;
            
            // 添加到总预算
            totalBudget += dailyBudget;
            
            // 移动到下一天
            currentDate = currentDate.plusDays(1);
        }
        
        return totalBudget;
    }
    
    /**
     * 根据当前日期获取调整后的预算
     * 
     * @return 调整后的预算金额
     */
    public double getCurrentAdjustedBudget() {
        return getAdjustedBudgetForDate(LocalDate.now());
    }
} 