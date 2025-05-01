package com.financetracker.service;

import java.io.Serializable;
import java.util.List;

/**
 * DataService是数据持久化操作的接口。
 * 这是一个泛型接口，T代表要持久化的数据类型（必须可序列化）。
 * 接口定义了基本的文件读写操作，包括保存列表、加载列表、
 * 保存单项、加载单项。
 * 注意：原 CsvDataService 实现的是序列化，接口也反映此。
 */
public interface DataService<T extends Serializable> { // Constraint T to Serializable
    
    boolean saveToFile(List<T> items, String filePath);
    // 中文注释：将一组可序列化的数据项保存到指定路径的文件中。
    // 参数：items - 要保存的数据项列表；filePath - 文件路径
    // 返回：操作成功返回 true，失败返回 false
    
    List<T> loadFromFile(String filePath);
    // 中文注释：从指定路径的文件加载可序列化的数据项列表。
    // 参数：filePath - 文件路径
    // 返回：从文件中加载的数据项列表，若文件不存在或加载失败，返回空列表
    
    boolean saveItemToFile(T item, String filePath);
    // 中文注释：将单个可序列化的数据项保存到指定路径的文件中。
    // 参数：item - 要保存的数据项；filePath - 文件路径
    // 返回：操作成功返回 true，失败返回 false
    
    T loadItemFromFile(String filePath);
    // 中文注释：从指定路径的文件加载单个可序列化的数据项。
    // 参数：filePath - 文件路径
    // 返回：加载的数据项，若文件不存在或加载失败，返回 null
    
    // Removed appendToFile, fileExists, createBackup as they might not fit all implementations
    // or are better handled by the specific service needing them.
    // boolean appendToFile(T item, String filePath);
    // boolean fileExists(String filePath);
    // boolean createBackup(String filePath, String backupFilePath);
}
