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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.financetracker.model.Settings;

/**
 * 设置服务类，用于管理应用程序设置
 */
public class SettingsService {
    private static final Logger LOGGER = Logger.getLogger(SettingsService.class.getName());
    private static final String SETTINGS_FILE = "data/settings.dat";

    private Settings settings;

    /**
     * 构造函数
     */
    public SettingsService() {
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
        try {
            // 确保目录存在
            Path dataDir = Paths.get("data");
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }

            // 序列化对象到文件
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(SETTINGS_FILE))) {
                oos.writeObject(settings);
                return true;
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "保存设置时出错", e);
            return false;
        }
    }

    /**
     * 加载设置
     */
    public boolean loadSettings() {
        // 检查文件是否存在
        File file = new File(SETTINGS_FILE);
        if (!file.exists()) {
            // 如果文件不存在，创建默认设置
            settings = new Settings();
            return saveSettings();
        }

        try {
            // 从文件反序列化对象
            try (ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(SETTINGS_FILE))) {
                settings = (Settings) ois.readObject();
                return true;
            }
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "加载设置时出错", e);
            // 如果加载出错，创建默认设置
            settings = new Settings();
            return false;
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
