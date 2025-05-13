package com.financetracker.ai;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * 配置加载器，用于读取配置文件
 */
public class ConfigLoader {
    
    private static final Properties properties = new Properties();
    private static boolean isLoaded = false;
    
    // 配置文件的可能位置
    private static final String[] CONFIG_PATHS = {
        "src/main/resources/config.properties",  // 开发环境
        "config/config.properties",              // 项目根目录下的配置
        "config.properties"                      // 当前目录
    };
    
    /**
     * 加载配置文件
     */
    public static synchronized void loadConfig() {
        if (isLoaded) {
            return;
        }
        
        // 首先尝试从类路径加载
        try (InputStream is = ConfigLoader.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (is != null) {
                properties.load(is);
                isLoaded = true;
                return;
            }
        } catch (IOException e) {
            System.err.println("无法从类路径加载配置: " + e.getMessage());
        }
        
        // 然后尝试从文件系统加载
        for (String path : CONFIG_PATHS) {
            Path configPath = Paths.get(path);
            if (Files.exists(configPath)) {
                try (FileInputStream fis = new FileInputStream(configPath.toFile())) {
                    properties.load(fis);
                    isLoaded = true;
                    System.out.println("已从 " + configPath + " 加载配置");
                    return;
                } catch (IOException e) {
                    System.err.println("无法加载配置文件 " + path + ": " + e.getMessage());
                }
            }
        }
        
        System.err.println("警告: 未找到配置文件，将使用默认配置或环境变量");
    }
    
    /**
     * 获取配置项
     * 
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值，如果不存在则返回默认值
     */
    public static String getProperty(String key, String defaultValue) {
        if (!isLoaded) {
            loadConfig();
        }
        
        // 首先尝试从系统环境变量获取
        String envValue = System.getenv(key.replace('.', '_').toUpperCase());
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }
        
        // 然后从配置文件获取
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * 获取配置项
     * 
     * @param key 配置键
     * @return 配置值，如果不存在则返回null
     */
    public static String getProperty(String key) {
        return getProperty(key, null);
    }
} 