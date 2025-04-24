package com.financetracker.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 特殊日期类，用于表示可能影响财务的特殊日期
 */
public class SpecialDate implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private LocalDate date;
    private String description;
    private List<String> affectedCategories;
    private double expectedImpact; // 预期影响百分比，如 20.0 表示 20%
    
    /**
     * 默认构造函数
     */
    public SpecialDate() {
        this.name = "";
        this.date = LocalDate.now();
        this.description = "";
        this.affectedCategories = new ArrayList<>();
        this.expectedImpact = 0.0;
    }
    
    /**
     * 构造函数
     * 
     * @param name 特殊日期名称
     * @param date 日期
     * @param description 描述
     * @param affectedCategories 受影响的分类
     * @param expectedImpact 预期影响百分比
     */
    public SpecialDate(String name, LocalDate date, String description, 
                       List<String> affectedCategories, double expectedImpact) {
        this.name = name;
        this.date = date;
        this.description = description;
        this.affectedCategories = affectedCategories;
        this.expectedImpact = expectedImpact;
    }
    
    /**
     * 构造函数，使用逗号分隔的分类字符串
     * 
     * @param name 特殊日期名称
     * @param date 日期
     * @param description 描述
     * @param affectedCategoriesString 逗号分隔的受影响分类字符串
     * @param expectedImpact 预期影响百分比
     */
    public SpecialDate(String name, LocalDate date, String description, 
                       String affectedCategoriesString, double expectedImpact) {
        this.name = name;
        this.date = date;
        this.description = description;
        this.affectedCategories = new ArrayList<>();
        
        // 解析逗号分隔的分类字符串
        if (affectedCategoriesString != null && !affectedCategoriesString.isEmpty()) {
            String[] categories = affectedCategoriesString.split(",");
            for (String category : categories) {
                this.affectedCategories.add(category.trim());
            }
        }
        
        this.expectedImpact = expectedImpact;
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
     * 获取受影响的分类列表
     */
    public List<String> getAffectedCategories() {
        return affectedCategories;
    }
    
    /**
     * 设置受影响的分类列表
     */
    public void setAffectedCategories(List<String> affectedCategories) {
        this.affectedCategories = affectedCategories;
    }
    
    /**
     * 获取受影响的分类，以逗号分隔的字符串形式
     */
    public String getAffectedCategoriesAsString() {
        if (affectedCategories == null || affectedCategories.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (String category : affectedCategories) {
            sb.append(category).append(", ");
        }
        
        // 去除最后的逗号和空格
        if (sb.length() > 2) {
            sb.setLength(sb.length() - 2);
        }
        
        return sb.toString();
    }
    
    /**
     * 设置受影响的分类，从逗号分隔的字符串
     */
    public void setAffectedCategoriesFromString(String categoriesString) {
        affectedCategories.clear();
        
        if (categoriesString != null && !categoriesString.isEmpty()) {
            String[] categories = categoriesString.split(",");
            for (String category : categories) {
                affectedCategories.add(category.trim());
            }
        }
    }
    
    /**
     * 获取预期影响百分比
     */
    public double getExpectedImpact() {
        return expectedImpact;
    }
    
    /**
     * 设置预期影响百分比
     */
    public void setExpectedImpact(double expectedImpact) {
        this.expectedImpact = expectedImpact;
    }
}
