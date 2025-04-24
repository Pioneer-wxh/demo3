package com.financetracker.service;

import java.util.List;

/**
 * DataService是数据持久化操作的接口。
 * 这是一个泛型接口，T代表要持久化的数据类型。
 * 接口定义了基本的文件读写操作，包括保存列表、加载列表、
 * 保存单项、加载单项、追加项目、检查文件存在和创建备份。
 * 这是整个持久化架构的基础，允许不同的实现（如JSON或CSV）
 * 提供相同的基本功能。
 */




public interface DataService<T> {
    
    boolean saveToFile(List<T> items, String filePath);
    // 中文注释：将一组数据项保存到指定路径的文件中。
    // 参数：items - 要保存的数据项列表；filePath - 文件路径
    // 返回：操作成功返回 true，失败返回 false
    
    List<T> loadFromFile(String filePath);
    // 中文注释：从指定路径的文件加载数据项列表。
    // 参数：filePath - 文件路径
    // 返回：从文件中加载的数据项列表，若文件不存在或加载失败，返回空列表
    
    boolean saveItemToFile(T item, String filePath);
    // 中文注释：将单个数据项保存到指定路径的文件中。
    // 参数：item - 要保存的数据项；filePath - 文件路径
    // 返回：操作成功返回 true，失败返回 false
    
    T loadItemFromFile(String filePath);
    // 中文注释：从指定路径的文件加载单个数据项。
    // 参数：filePath - 文件路径
    // 返回：加载的数据项，若文件不存在或加载失败，返回 null
    
    boolean appendToFile(T item, String filePath);
    // 中文注释：将单个数据项追加到已有文件中。
    // 参数：item - 要追加的数据项；filePath - 文件路径
    // 返回：操作成功返回 true，失败返回 false
    
    boolean fileExists(String filePath);
    // 中文注释：检查指定路径的文件是否存在。
    // 参数：filePath - 文件路径
    // 返回：文件存在返回 true，不存在返回 false
    
    boolean createBackup(String filePath, String backupFilePath);
    // 中文注释：为指定文件创建备份。
    // 参数：filePath - 源文件路径；backupFilePath - 备份文件路径
    // 返回：备份操作成功返回 true，失败返回 false
}
