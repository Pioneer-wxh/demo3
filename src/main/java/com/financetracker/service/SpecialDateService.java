package com.financetracker.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.financetracker.model.Settings;
import com.financetracker.model.SpecialDate;

/**
 * 特殊日期服务类，用于管理特殊日期
 */
public class SpecialDateService {
    private static final Logger LOGGER = Logger.getLogger(SpecialDateService.class.getName());
    
    private SettingsService settingsService;
    
    /**
     * 构造函数
     */
    public SpecialDateService(SettingsService settingsService) {
        this.settingsService = settingsService;
        if (this.settingsService == null) {
            LOGGER.log(Level.SEVERE, "SettingsService cannot be null in SpecialDateService constructor.");
            // Optionally throw an IllegalArgumentException here
            // For now, relying on consuming code to provide a valid service.
            // throw new IllegalArgumentException("SettingsService cannot be null");
        }
    }
    
    /**
     * 获取所有特殊日期
     */
    public List<SpecialDate> getAllSpecialDates() {
        Settings settings = settingsService.getSettings();
        if (settings != null && settings.getSpecialDates() != null) {
            return new ArrayList<>(settings.getSpecialDates());
        }
        LOGGER.log(Level.WARNING, "Settings or special dates list is null when trying to get all special dates.");
        return new ArrayList<>();
    }
    
    /**
     * 添加特殊日期
     */
    public boolean addSpecialDate(SpecialDate specialDate) {
        if (specialDate == null) {
            LOGGER.log(Level.WARNING, "Attempted to add a null special date.");
            return false;
        }
        Settings settings = settingsService.getSettings();
        if (settings == null) {
             LOGGER.log(Level.SEVERE, "Settings object is null. Cannot add special date.");
             return false;
        }
        
        // Ensure the list exists
        if (settings.getSpecialDates() == null) {
            settings.setSpecialDates(new ArrayList<>());
        }

        settings.getSpecialDates().add(specialDate);
        boolean saved = settingsService.saveSettings();
        if (!saved) {
            LOGGER.log(Level.SEVERE, "Failed to save settings after adding special date: " + specialDate.getName());
            // Optionally, remove the added date from the list if save fails to maintain consistency
            // settings.getSpecialDates().remove(specialDate); 
        }
        return saved;
    }
    
    /**
     * 更新特殊日期
     * Note: This implementation assumes SpecialDate has a proper equals/hashCode based on ID for indexOf/set.
     * If IDs can change or are not reliable, a more robust update mechanism might be needed.
     */
    public boolean updateSpecialDate(SpecialDate specialDateToUpdate) {
        if (specialDateToUpdate == null || specialDateToUpdate.getId() == null) {
            LOGGER.log(Level.WARNING, "Attempted to update a null special date or special date with null ID.");
            return false;
        }
        Settings settings = settingsService.getSettings();
        if (settings == null || settings.getSpecialDates() == null) {
             LOGGER.log(Level.SEVERE, "Settings object or special dates list is null. Cannot update special date.");
             return false;
        }
        
        List<SpecialDate> specialDates = settings.getSpecialDates();
        for (int i = 0; i < specialDates.size(); i++) {
            if (specialDates.get(i).getId().equals(specialDateToUpdate.getId())) {
                specialDates.set(i, specialDateToUpdate);
                boolean saved = settingsService.saveSettings();
                if (!saved) {
                    LOGGER.log(Level.SEVERE, "Failed to save settings after updating special date: " + specialDateToUpdate.getName());
                }
                return saved;
            }
        }
        LOGGER.log(Level.WARNING, "Special date with ID " + specialDateToUpdate.getId() + " not found for update.");
        return false;
    }
    
    /**
     * 删除特殊日期
     */
    public boolean deleteSpecialDate(SpecialDate specialDate) {
        if (specialDate == null || specialDate.getId() == null) {
             LOGGER.log(Level.WARNING, "Attempted to delete a null special date or special date with null ID.");
            return false;
        }
         Settings settings = settingsService.getSettings();
        if (settings == null || settings.getSpecialDates() == null) {
             LOGGER.log(Level.SEVERE, "Settings object or special dates list is null. Cannot delete special date.");
             return false;
        }
        
        boolean removed = settings.getSpecialDates().removeIf(sd -> sd.getId().equals(specialDate.getId()));
        
        if (removed) {
            boolean saved = settingsService.saveSettings();
            if (!saved) {
                 LOGGER.log(Level.SEVERE, "Failed to save settings after deleting special date: " + specialDate.getName());
            }
            return saved;
        }
        LOGGER.log(Level.WARNING, "Special date with ID " + specialDate.getId() + " not found for deletion.");
        return false;
    }
    
    /**
     * 根据日期查找特殊日期
     */
    public List<SpecialDate> findSpecialDatesByDate(LocalDate date) {
        if (date == null) {
            return new ArrayList<>();
        }
        Settings settings = settingsService.getSettings();
        if (settings == null || settings.getSpecialDates() == null) {
            return new ArrayList<>();
        }
        
        List<SpecialDate> result = new ArrayList<>();
        for (SpecialDate sd : settings.getSpecialDates()) {
            if (sd.getDate().equals(date)) {
                result.add(sd);
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
        Settings settings = settingsService.getSettings();
        if (settings == null || settings.getSpecialDates() == null) {
            return null;
        }
        
        for (SpecialDate sd : settings.getSpecialDates()) {
            if (sd.getName().equalsIgnoreCase(name.trim())) {
                return sd;
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
         Settings settings = settingsService.getSettings();
        if (settings == null || settings.getSpecialDates() == null) {
            return new ArrayList<>();
        }
        
        List<SpecialDate> result = new ArrayList<>();
        for (SpecialDate sd : settings.getSpecialDates()) {
            if (sd.getDate().getMonthValue() == month) {
                result.add(sd);
            }
        }
        return result;
    }
    
    /**
     * 清除所有特殊日期
     */
    public boolean clearAllSpecialDates() {
        Settings settings = settingsService.getSettings();
        if (settings == null || settings.getSpecialDates() == null) {
            LOGGER.log(Level.WARNING, "Settings object or special dates list is null. Cannot clear special dates.");
            return false; // Or true if considered 'nothing to clear'
        }
        settings.getSpecialDates().clear();
        boolean saved = settingsService.saveSettings();
        if (!saved) {
            LOGGER.log(Level.SEVERE, "Failed to save settings after clearing all special dates.");
        }
        return saved;
    }
}
