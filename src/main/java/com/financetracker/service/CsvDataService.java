package com.financetracker.service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV数据服务实现类，用于替代JSON数据服务
 * 注意：实际上这是一个简单的序列化数据服务，不是真正的CSV格式
 * 因为快速替换JSON实现，使用Java序列化作为临时解决方案
 */
public class CsvDataService<T extends Serializable> implements DataService<T> {

    private final Class<T> itemType;

    /**
     * 构造函数，支持列表操作和单对象操作
     * 
     * @param type 数据项类型
     */
    public CsvDataService(Class<T> type) {
        this.itemType = type;
    }

    @Override
    public boolean saveToFile(List<T> items, String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
                oos.writeObject(items);
                return true;
            }
        } catch (IOException e) {
            System.err.println("保存列表到文件时出错 " + filePath + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> loadFromFile(String filePath) {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            Object obj = ois.readObject();
            if (obj instanceof List) {
                return (List<T>) obj;
            }
            return new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("从文件加载列表时出错 " + filePath + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public boolean saveItemToFile(T item, String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
                oos.writeObject(item);
                return true;
            }
        } catch (IOException e) {
            System.err.println("保存项目到文件时出错 " + filePath + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T loadItemFromFile(String filePath) {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return null;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            Object obj = ois.readObject();
            if (itemType.isInstance(obj)) {
                return (T) obj;
            }
            return null;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("从文件加载项目时出错 " + filePath + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean appendToFile(T item, String filePath) {
        List<T> items = loadFromFile(filePath);
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
        return saveToFile(items, filePath);
    }

    @Override
    public boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    @Override
    public boolean createBackup(String filePath, String backupFilePath) {
        Path sourcePath = Paths.get(filePath);
        if (!Files.exists(sourcePath)) {
            System.err.println("备份源文件不存在: " + filePath);
            return false;
        }
        try {
            Path backupPath = Paths.get(backupFilePath);
            Files.createDirectories(backupPath.getParent());
            Files.copy(sourcePath, backupPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("备份成功创建: " + backupFilePath);
            return true;
        } catch (IOException e) {
            System.err.println("为文件创建备份时出错 " + filePath + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}