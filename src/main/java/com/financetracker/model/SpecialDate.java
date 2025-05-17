package com.financetracker.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 特殊日期类，用于表示可能影响财务的特殊日期
 */
public class SpecialDate implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id; // Unique identifier
    private String name; // e.g., "Mom's Birthday", "Christmas"
    private String description; // Optional: further details about the special date
    private LocalDate date; // Specific date if not recurring, or the template/next occurrence if recurring
    private String affectedCategory; // The category of expense that will be affected
    private double amountIncrease;   // The fixed amount by which the budget for the category increases
    private boolean recurring; // True if it's an annual/monthly event
    private RecurrenceType recurrenceType; // e.g., NONE, MONTHLY, ANNUALLY
    private int dayOfMonth; // For recurring monthly/annually on a specific day (1-31)
    private int monthOfYear; // For recurring annually on a specific month and day (1-12)

    private static final Logger LOGGER = Logger.getLogger(SpecialDate.class.getName());

    public enum RecurrenceType {
        NONE, MONTHLY, ANNUALLY
    }

    /**
     * 默认构造函数
     */
    public SpecialDate() {
        this.id = UUID.randomUUID().toString();
        this.recurrenceType = RecurrenceType.NONE;
        this.recurring = false;
        this.date = LocalDate.now(); // Default to today if not specified
    }
    
    /**
     * 构造函数
     * 
     * @param id 特殊日期ID
     * @param name 特殊日期名称
     * @param description 描述
     * @param date 日期
     * @param affectedCategory 受影响的分类
     * @param amountIncrease 固定金额增加
     * @param recurring 是否为年度或月度事件
     * @param recurrenceType 事件类型
     * @param dayOfMonth 每月或每年在特定日期的天数
     * @param monthOfYear 每年在特定月份和日期的事件
     */
    public SpecialDate(String id, String name, String description, LocalDate date, String affectedCategory, double amountIncrease, 
                       boolean recurring, RecurrenceType recurrenceType, int dayOfMonth, int monthOfYear) {
        this.id = (id == null || id.trim().isEmpty()) ? UUID.randomUUID().toString() : id;
        this.name = name;
        this.description = description;
        this.date = date; 
        this.affectedCategory = affectedCategory;
        this.amountIncrease = amountIncrease;
        this.recurring = recurring;
        this.recurrenceType = recurring ? recurrenceType : RecurrenceType.NONE;
        if (recurring) {
            if (dayOfMonth < 1 || dayOfMonth > 31) {
                throw new IllegalArgumentException("Day of month must be between 1 and 31 for recurring dates.");
            }
            this.dayOfMonth = dayOfMonth;
            if (recurrenceType == RecurrenceType.ANNUALLY) {
                if (monthOfYear < 1 || monthOfYear > 12) {
                    throw new IllegalArgumentException("Month of year must be between 1 and 12 for annual recurring dates.");
                }
                this.monthOfYear = monthOfYear;
            } else {
                this.monthOfYear = 0; // Not applicable for monthly recurrence based on this constructor
            }
        } else {
            // For non-recurring, dayOfMonth and monthOfYear can be derived from 'date' if needed for UI, but are not primary for logic
            this.dayOfMonth = (date != null) ? date.getDayOfMonth() : 0;
            this.monthOfYear = (date != null) ? date.getMonthValue() : 0;
        }
    }
    
    /**
     * 构造函数
     * 
     * @param name 特殊日期名称
     * @param date 日期
     * @param affectedCategory 受影响的分类
     * @param amountIncrease 固定金额增加
     */
    public SpecialDate(String name, LocalDate date, String affectedCategory, double amountIncrease) {
        this(UUID.randomUUID().toString(), name, null, date, affectedCategory, amountIncrease, false, RecurrenceType.NONE, 
             date != null ? date.getDayOfMonth() : 0, date != null ? date.getMonthValue() : 0);
        if (date != null) { 
            this.dayOfMonth = date.getDayOfMonth();
            this.monthOfYear = date.getMonthValue();
        }
         this.recurring = false; 
         this.recurrenceType = RecurrenceType.NONE; 
    }
    
    /**
     * 获取特殊日期名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 设置特殊日期名称
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * 获取描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 设置描述
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * 获取日期
     */
    public LocalDate getDate() {
        return date;
    }
    
    /**
     * 设置日期
     */
    public void setDate(LocalDate date) { 
        this.date = date;
        if (!this.recurring && date != null) {
            this.dayOfMonth = date.getDayOfMonth();
            this.monthOfYear = date.getMonthValue();
        }
    }
    
    /**
     * 获取受影响的分类
     */
    public String getAffectedCategory() {
        return affectedCategory;
    }
    
    /**
     * 设置受影响的分类
     */
    public void setAffectedCategory(String affectedCategory) {
        this.affectedCategory = affectedCategory;
    }
    
    /**
     * 获取固定金额增加
     */
    public double getAmountIncrease() {
        return amountIncrease;
    }
    
    /**
     * 设置固定金额增加
     */
    public void setAmountIncrease(double amountIncrease) {
        this.amountIncrease = amountIncrease;
    }
    
    /**
     * 获取是否为年度或月度事件
     */
    public boolean isRecurring() {
        return recurring;
    }
    
    /**
     * 设置是否为年度或月度事件
     */
    public void setRecurring(boolean recurring) { 
        this.recurring = recurring;
        if (!recurring) {
            this.recurrenceType = RecurrenceType.NONE;
        } else {
            if (this.recurrenceType == RecurrenceType.NONE) { 
                this.recurrenceType = RecurrenceType.ANNUALLY; // Default if switched to recurring
            }
            if (this.dayOfMonth == 0 && this.date != null) { // If recurring is set true and dayOfMonth was 0, use date's day
                 this.dayOfMonth = this.date.getDayOfMonth();
            }
            if (this.recurrenceType == RecurrenceType.ANNUALLY && this.monthOfYear == 0 && this.date != null) {
                 this.monthOfYear = this.date.getMonthValue();
            }
        }
    }
    
    /**
     * 获取事件类型
     */
    public RecurrenceType getRecurrenceType() {
        return recurrenceType;
    }
    
    /**
     * 设置事件类型
     */
    public void setRecurrenceType(RecurrenceType recurrenceType) { 
        this.recurrenceType = recurrenceType;
        if (recurrenceType == RecurrenceType.NONE) {
            this.recurring = false;
        } else {
            this.recurring = true;
            if (this.dayOfMonth == 0 && this.date != null) { 
                 this.dayOfMonth = this.date.getDayOfMonth();
            }
             if (this.recurrenceType == RecurrenceType.ANNUALLY && this.monthOfYear == 0 && this.date != null) {
                 this.monthOfYear = this.date.getMonthValue();
            }
        }
    }
    
    /**
     * 获取每月或每年在特定日期的天数
     */
    public int getDayOfMonth() {
        return dayOfMonth;
    }
    
    /**
     * 设置每月或每年在特定日期的天数
     */
    public void setDayOfMonth(int dayOfMonth) { 
        if (dayOfMonth >= 1 && dayOfMonth <= 31) {
            this.dayOfMonth = dayOfMonth; 
        } else {
            // Optionally throw an error or log, or handle appropriately
            // For now, just not setting invalid day
        }
    }
    
    /**
     * 获取每年在特定月份和日期的事件
     */
    public int getMonthOfYear() {
        return monthOfYear;
    }
    
    /**
     * 设置每年在特定月份和日期的事件
     */
    public void setMonthOfYear(int monthOfYear) { 
        if (monthOfYear >= 1 && monthOfYear <= 12) {
            this.monthOfYear = monthOfYear; 
        } else {
            // Optionally throw an error or log
        }
    }
    
    /**
     * 计算给定日期后的下一个事件。
     * 如果事件不是重复的，它将返回固定日期，如果它发生在或之后给定日期，否则为空。
     * @param fromDate 要计算下一个事件的日期。
     * @return 下一个事件的 LocalDate，如果不适用于 null。
     */
    public LocalDate getNextOccurrence(LocalDate fromDate) {
        if (!this.recurring || this.recurrenceType == RecurrenceType.NONE) {
            return (this.date != null && !this.date.isBefore(fromDate)) ? this.date : null;
        }

        if (this.dayOfMonth == 0) return null; // Invalid recurrence rule

        LocalDate nextOccurrenceDate = null;
        int year = fromDate.getYear();

        try {
            if (recurrenceType == RecurrenceType.ANNUALLY) {
                if (this.monthOfYear == 0) return null; // Invalid annual recurrence rule
                nextOccurrenceDate = LocalDate.of(year, this.monthOfYear, this.dayOfMonth);
                if (nextOccurrenceDate.isBefore(fromDate)) {
                    nextOccurrenceDate = LocalDate.of(year + 1, this.monthOfYear, this.dayOfMonth);
                }
            } else if (recurrenceType == RecurrenceType.MONTHLY) {
                YearMonth currentYm = YearMonth.from(fromDate);
                nextOccurrenceDate = LocalDate.of(currentYm.getYear(), currentYm.getMonthValue(), this.dayOfMonth);
                if (nextOccurrenceDate.isBefore(fromDate)) {
                    currentYm = currentYm.plusMonths(1);
                    nextOccurrenceDate = LocalDate.of(currentYm.getYear(), currentYm.getMonthValue(), this.dayOfMonth);
                }
                // Ensure dayOfMonth is valid for the calculated month, advance month if not.
                while (this.dayOfMonth > YearMonth.of(nextOccurrenceDate.getYear(), nextOccurrenceDate.getMonth()).lengthOfMonth()) {
                    currentYm = YearMonth.from(nextOccurrenceDate).plusMonths(1);
                    nextOccurrenceDate = LocalDate.of(currentYm.getYear(), currentYm.getMonthValue(), this.dayOfMonth);
                 }
            }
        } catch (java.time.DateTimeException e) {
            // Handle cases like Feb 29 for non-leap years if it's an annual event.
            if (recurrenceType == RecurrenceType.ANNUALLY && this.monthOfYear == 2 && this.dayOfMonth == 29) {
                int yearToTest = fromDate.getYear();
                // If Feb 29 this year has already passed or fromDate is after it, start checking from next year.
                if (YearMonth.of(yearToTest, 2).isLeapYear() && LocalDate.of(yearToTest, 2, 29).isBefore(fromDate)) {
                    yearToTest++;
                }
                while (yearToTest <= fromDate.getYear() + 4) { // Check next 4 years for a leap year
                    if (YearMonth.of(yearToTest, 2).isLeapYear()) {
                        LocalDate feb29 = LocalDate.of(yearToTest, 2, 29);
                        if (!feb29.isBefore(fromDate)) return feb29; // Found valid future Feb 29
                    }
                    yearToTest++;
                }
            }
            LOGGER.log(Level.WARNING, "Could not determine next occurrence for special date ''{0}'' from {1}", new Object[]{name, fromDate});
            return null; // Could not form a valid date with dayOfMonth/monthOfYear
        }
        return nextOccurrenceDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpecialDate that = (SpecialDate) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "SpecialDate{" +
               "id='" + id + '\'' +
               ", name='" + name + '\'' +
               (date != null ? ", date=" + date : "") +
               (recurring ? ", recurringType=" + recurrenceType + ", day=" + dayOfMonth + (recurrenceType == RecurrenceType.ANNUALLY ? ", month=" + monthOfYear : "") : "") +
               ", affectedCategory='" + affectedCategory + '\'' +
               ", amountIncrease=" + amountIncrease +
               '}';
    }

    // Getter for id
    public String getId() {
        return id;
    }

    // Setter for id (optional, usually ID is set at creation and immutable)
    public void setId(String id) {
        this.id = id;
    }
}
