package com.financetracker.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.financetracker.model.SpecialDate;
import com.financetracker.util.PathUtil;

/**
 * 特殊日期服务类，用于管理特殊日期
 */
public class SpecialDateService {
    private static final Logger LOGGER = Logger.getLogger(SpecialDateService.class.getName());
    
    private List<SpecialDate> specialDates;
    private final DataService<SpecialDate> dataService;
    
    /**
     * 构造函数
     */
    public SpecialDateService() {
        this.specialDates = new ArrayList<>();
        this.dataService = new SerializationService<>(SpecialDate.class);
        loadSpecialDates();
    }
    
    /**
     * 获取所有特殊日期
     */
    public List<SpecialDate> getAllSpecialDates() {
        return new ArrayList<>(specialDates);
    }
    
    /**
     * 添加特殊日期
     */
    public boolean addSpecialDate(SpecialDate specialDate) {
        if (specialDate == null) {
            return false;
        }
        
        specialDates.add(specialDate);
        return saveSpecialDates();
    }
    
    /**
     * 更新特殊日期
     */
    public boolean updateSpecialDate(SpecialDate oldDate, SpecialDate newDate) {
        if (oldDate == null || newDate == null) {
            return false;
        }
        
        int index = specialDates.indexOf(oldDate);
        if (index != -1) {
            specialDates.set(index, newDate);
            return saveSpecialDates();
        }
        return false;
    }
    
    /**
     * 删除特殊日期
     */
    public boolean deleteSpecialDate(SpecialDate specialDate) {
        if (specialDate == null) {
            return false;
        }
        
        boolean removed = specialDates.remove(specialDate);
        if (removed) {
            return saveSpecialDates();
        }
        return false;
    }
    
    /**
     * 根据日期查找特殊日期
     */
    public List<SpecialDate> findSpecialDatesByDate(LocalDate date) {
        if (date == null) {
            return new ArrayList<>();
        }
        
        List<SpecialDate> result = new ArrayList<>();
        for (SpecialDate specialDate : specialDates) {
            if (specialDate.getDate().equals(date)) {
                result.add(specialDate);
            }
        }
        return result;
    }
    
    /**
     * 通过名称查找特殊日期
     */
    public SpecialDate findSpecialDateByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        
        for (SpecialDate specialDate : specialDates) {
            if (specialDate.getName().equalsIgnoreCase(name.trim())) {
                return specialDate;
            }
        }
        return null;
    }
    
    /**
     * 查找特定月份的特殊日期
     */
    public List<SpecialDate> findSpecialDatesByMonth(int month) {
        if (month < 1 || month > 12) {
            return new ArrayList<>();
        }
        
        List<SpecialDate> result = new ArrayList<>();
        for (SpecialDate specialDate : specialDates) {
            if (specialDate.getDate().getMonthValue() == month) {
                result.add(specialDate);
            }
        }
        return result;
    }
    
    /**
     * 保存特殊日期
     */
    private boolean saveSpecialDates() {
        String filePath = PathUtil.getSpecialDatesDatPath().toString();
        return dataService.saveToFile(specialDates, filePath);
    }
    
    /**
     * 加载特殊日期
     */
    private boolean loadSpecialDates() {
        String filePath = PathUtil.getSpecialDatesDatPath().toString();
        List<SpecialDate> loadedDates = dataService.loadFromFile(filePath);
        
        if (loadedDates != null) {
            specialDates = loadedDates;
            if (specialDates.isEmpty()) {
                 LOGGER.log(Level.INFO, "Special dates file not found or empty. Initialized with empty list.");
            }
            return true;
        } else {
            LOGGER.log(Level.SEVERE, "Failed to load special dates, service returned null.");
             specialDates = new ArrayList<>();
             return false;
        }
    }
    
    /**
     * 清除所有特殊日期
     */
    public boolean clearAllSpecialDates() {
        specialDates.clear();
        return saveSpecialDates();
    }
}
