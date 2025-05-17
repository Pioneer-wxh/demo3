package com.financetracker.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.financetracker.model.Settings;
import com.financetracker.model.SpecialDate;

/**
 * 预算调整服务类，用于根据特殊日期调整预算类别金额
 */
public class BudgetAdjustmentService {
    private static final Logger LOGGER = Logger.getLogger(BudgetAdjustmentService.class.getName());
    
    private final SettingsService settingsService;
    // private final SpecialDateService specialDateService; // Removed as SpecialDates are now in Settings
    
    /**
     * 构造函数
     * 
     * @param settingsService 设置服务
     */
    public BudgetAdjustmentService(SettingsService settingsService) {
        this.settingsService = settingsService;
        // this.specialDateService = specialDateService; // Removed
    }
    
    /**
     * 获取指定月份由于特殊日期产生的各类别预算调整总额。
     * 
     * @param targetMonth 需要计算预算调整的月份
     * @return 一个Map，键是类别名称 (String)，值是该类别因特殊日期增加的总金额 (Double)。
     */
    public Map<String, Double> getCategoryAdjustmentsForMonth(YearMonth targetMonth) {
        Map<String, Double> categoryAdjustments = new HashMap<>();
        Settings settings = settingsService.getSettings();
        if (settings == null || settings.getSpecialDates() == null) {
            LOGGER.log(Level.WARNING, "Settings or SpecialDates list is null. Cannot calculate adjustments.");
            return categoryAdjustments; // Return empty map
        }

        List<SpecialDate> specialDates = settings.getSpecialDates();
        LocalDate monthStart = targetMonth.atDay(1);
        LocalDate monthEnd = targetMonth.atEndOfMonth();

        for (SpecialDate specialDate : specialDates) {
            if (specialDate.getAffectedCategory() == null || specialDate.getAffectedCategory().trim().isEmpty()) {
                LOGGER.log(Level.FINE, "Special date ''{0}'' has no affected category, skipping.", specialDate.getName());
                continue;
            }

            LocalDate occurrence = null;
            if (!specialDate.isRecurring() || specialDate.getRecurrenceType() == SpecialDate.RecurrenceType.NONE) {
                // Non-recurring: check if its single date falls within the target month
                if (specialDate.getDate() != null && 
                    !specialDate.getDate().isBefore(monthStart) && 
                    !specialDate.getDate().isAfter(monthEnd)) {
                    occurrence = specialDate.getDate();
                }
            } else {
                // Recurring: calculate next occurrence starting from the beginning of the target month
                // The getNextOccurrence method should handle finding the date within the target month if applicable.
                // We might need to check multiple occurrences if a monthly event can happen multiple times (not typical for budget planning of one month)
                // For now, assume getNextOccurrence relative to monthStart gives the relevant one if it's in this month.
                LocalDate firstPossibleOccurrence = specialDate.getNextOccurrence(monthStart);
                if (firstPossibleOccurrence != null && !firstPossibleOccurrence.isAfter(monthEnd)) {
                     // Ensure it's still within the same targetMonth, as getNextOccurrence might return a date in a future month.
                    if (YearMonth.from(firstPossibleOccurrence).equals(targetMonth)) {
                         occurrence = firstPossibleOccurrence;
                    }
                }
            }

            if (occurrence != null) {
                String category = specialDate.getAffectedCategory();
                double amount = specialDate.getAmountIncrease();
                categoryAdjustments.merge(category, amount, Double::sum);
                LOGGER.log(Level.INFO, "Applying special date adjustment for '{0}' on {1}: Category '{2}' +{3}", 
                               new Object[]{specialDate.getName(), occurrence, category, amount});
            }
        }
        return categoryAdjustments;
    }

    // Consider if the old methods below are still needed or should be removed/refactored.
    // For now, commenting them out as they use the old logic (percentage impact and SpecialDateService).

    /*
    public double getAdjustedBudgetForDate(LocalDate date) {
        Settings settings = settingsService.getSettings();
        double baseBudget = settings.getMonthlyBudget();
        List<SpecialDate> specialDatesForDay = specialDateService.findSpecialDatesByDate(date);
        if (specialDatesForDay.isEmpty()) {
            return baseBudget;
        }
        double adjustedBudget = baseBudget;
        for (SpecialDate specialDate : specialDatesForDay) {
            double impactFactor = specialDate.getExpectedImpact() / 100.0;
            adjustedBudget = adjustedBudget * (1 + impactFactor);
            LOGGER.log(Level.INFO, "预算因特殊日期 {0} 调整: {1}% -> {2}", 
                    new Object[]{specialDate.getName(), specialDate.getExpectedImpact(), adjustedBudget});
        }
        return adjustedBudget;
    }
    */

    /*
    public double getAdjustedBudgetForMonth(YearMonth yearMonth) {
        Settings settings = settingsService.getSettings();
        double baseBudget = settings.getMonthlyBudget();
        List<SpecialDate> specialDatesForMonth = specialDateService.findSpecialDatesByMonth(yearMonth.getMonthValue());
        if (specialDatesForMonth.isEmpty()) {
            return baseBudget;
        }
        double adjustedBudget = baseBudget;
        for (SpecialDate specialDate : specialDatesForMonth) {
            double impactFactor = specialDate.getExpectedImpact() / 100.0;
            adjustedBudget = adjustedBudget * (1 + impactFactor);
            LOGGER.log(Level.INFO, "每月预算因特殊日期 {0} 调整: {1}% -> {2}", 
                    new Object[]{specialDate.getName(), specialDate.getExpectedImpact(), adjustedBudget});
        }
        return adjustedBudget;
    }
    */

    /*
    public double calculateBudgetForDateRange(LocalDate startDate, LocalDate endDate) {
        double totalBudget = 0.0;
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            YearMonth currentYearMonth = YearMonth.from(currentDate);
            int daysInMonth = currentYearMonth.lengthOfMonth();
            double monthlyBudget = getAdjustedBudgetForMonth(currentYearMonth); // This would need to use the new per-category logic
            double dailyBudget = monthlyBudget / daysInMonth;
            totalBudget += dailyBudget;
            currentDate = currentDate.plusDays(1);
        }
        return totalBudget;
    }
    */

    /*
    public double getCurrentAdjustedBudget() {
        return getAdjustedBudgetForDate(LocalDate.now());
    }
    */
} 