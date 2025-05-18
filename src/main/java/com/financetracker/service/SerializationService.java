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
import java.util.ArrayList;
import java.util.List;

/**
 * 通用的序列化数据服务实现类
 * Provides basic serialization/deserialization for Serializable objects or lists.
 */
public class SerializationService<T extends Serializable> implements ListDataService<T>, SingleItemDataService<T> {

    private final Class<T> itemType;

    /**
     * 构造函数，支持列表操作和单对象操作
     * 
     * @param type 数据项类型
     */
    public SerializationService(Class<T> type) {
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
}