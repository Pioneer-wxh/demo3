package com.financetracker.service;

import java.util.ArrayList;
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
            // Ensure lists are not null after deserializing an older version
            // or if the Settings object was somehow created without these lists initialized.
            if (settings.getSpecialDates() == null) {
                settings.setSpecialDates(new ArrayList<>());
            }
            if (settings.getSavingGoals() == null) {
                settings.setSavingGoals(new ArrayList<>());
            }
            if (settings.getLastMonthClosed() == null) {
                settings.setLastMonthClosed("");
            }
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

