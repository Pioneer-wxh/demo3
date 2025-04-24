package com.financetracker.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.financetracker.model.SpecialDate;

/**
 * 特殊日期服务类，用于管理特殊日期
 */
public class SpecialDateService {
    private static final Logger LOGGER = Logger.getLogger(SpecialDateService.class.getName());
    private static final String SPECIAL_DATES_FILE = "data/specialDates.dat";
    
    private List<SpecialDate> specialDates;
    
    /**
     * 构造函数
     */
    public SpecialDateService() {
        this.specialDates = new ArrayList<>();
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
        try {
            // 确保目录存在
            Path dataDir = Paths.get("data");
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }
            
            // 序列化对象到文件
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(SPECIAL_DATES_FILE))) {
                oos.writeObject(specialDates);
                return true;
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "保存特殊日期时出错", e);
            return false;
        }
    }
    
    /**
     * 加载特殊日期
     */
    @SuppressWarnings("unchecked")
    private boolean loadSpecialDates() {
        // 检查文件是否存在
        File file = new File(SPECIAL_DATES_FILE);
        if (!file.exists()) {
            // 如果文件不存在，保存空列表
            return saveSpecialDates();
        }
        
        try {
            // 从文件反序列化对象
            try (ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(SPECIAL_DATES_FILE))) {
                specialDates = (List<SpecialDate>) ois.readObject();
                return true;
            }
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "加载特殊日期时出错", e);
            // 如果加载出错，使用空列表
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
