package com.financetracker.service;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.financetracker.model.Settings;
import com.financetracker.util.PathUtil;

/**
 * 设置服务类，用于管理应用程序设置
 */
public class SettingsService {
    private static final Logger LOGGER = Logger.getLogger(SettingsService.class.getName());

    private Settings settings;
    private final DataService<Settings> dataService;

    /**
     * 构造函数
     */
    public SettingsService() {
        this.dataService = new SerializationService<>(Settings.class);
        loadSettings();
    }

    /**
     * 获取设置
     */
    public Settings getSettings() {
        return settings;
    }

    /**
     * 保存设置
     */
    public boolean saveSettings() {
        String filePath = PathUtil.getSettingsDatPath().toString();
        return dataService.saveItemToFile(settings, filePath);
    }

    /**
     * 加载设置
     */
    public boolean loadSettings() {
        String filePath = PathUtil.getSettingsDatPath().toString();
        Settings loadedSettings = dataService.loadItemFromFile(filePath);

        if (loadedSettings == null) {
            LOGGER.log(Level.INFO, "Settings file not found or failed to load. Creating default settings.");
            settings = new Settings();
            return saveSettings();
        } else {
            settings = loadedSettings;
            return true;
        }
    }

    /**
     * 重置为默认设置
     */
    public boolean resetToDefault() {
        settings = new Settings();
        return saveSettings();
    }

    /**
     * 添加默认分类
     */
    public boolean addDefaultCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return false;
        }

        settings.addDefaultCategory(category.trim());
        return saveSettings();
    }

    /**
     * 删除默认分类
     */
    public boolean removeDefaultCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return false;
        }

        boolean removed = settings.removeDefaultCategory(category.trim());
        if (removed) {
            return saveSettings();
        }
        return false;
    }

    /**
     * 设置月度预算
     */
    public boolean setMonthlyBudget(double budget) {
        if (budget < 0) {
            return false;
        }

        settings.setMonthlyBudget(budget);
        return saveSettings();
    }

    /**
     * 设置预算开始日
     */
    public boolean setBudgetStartDay(int day) {
        if (day < 1 || day > 28) {
            return false;
        }

        settings.setBudgetStartDay(day);
        return saveSettings();
    }
}
