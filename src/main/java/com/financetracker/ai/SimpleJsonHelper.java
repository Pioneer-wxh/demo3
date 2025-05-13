package com.financetracker.ai;

import java.util.List;
import java.util.Map;

/**
 * 简单的JSON帮助类，用于生成和解析基本的JSON格式
 */
public class SimpleJsonHelper {
    
    /**
     * 创建一个基本的JSON字符串
     * 
     * @param entries 键值对
     * @return JSON字符串
     */
    public static String createJsonObject(Object... entries) {
        if (entries.length % 2 != 0) {
            throw new IllegalArgumentException("需要偶数个参数");
        }
        
        StringBuilder json = new StringBuilder();
        json.append("{");
        
        for (int i = 0; i < entries.length; i += 2) {
            String key = entries[i].toString();
            Object value = entries[i + 1];
            
            if (i > 0) {
                json.append(",");
            }
            
            json.append("\"").append(key).append("\":");
            
            if (value == null) {
                json.append("null");
            } else if (value instanceof Boolean) {
                json.append(value);
            } else if (value instanceof Number) {
                json.append(value);
            } else if (value instanceof String) {
                json.append("\"").append(escapeJson((String) value)).append("\"");
            } else if (value instanceof List) {
                json.append(createJsonArray((List<?>) value));
            } else if (value instanceof Map) {
                json.append(createJsonFromMap((Map<?, ?>) value));
            } else {
                json.append("\"").append(escapeJson(value.toString())).append("\"");
            }
        }
        
        json.append("}");
        return json.toString();
    }
    
    /**
     * 从Map创建JSON字符串
     * 
     * @param map 包含键值对的Map
     * @return JSON字符串
     */
    public static String createJsonFromMap(Map<?, ?> map) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) {
                json.append(",");
            }
            first = false;
            
            String key = entry.getKey().toString();
            Object value = entry.getValue();
            
            json.append("\"").append(key).append("\":");
            
            if (value == null) {
                json.append("null");
            } else if (value instanceof Boolean) {
                json.append(value);
            } else if (value instanceof Number) {
                json.append(value);
            } else if (value instanceof String) {
                json.append("\"").append(escapeJson((String) value)).append("\"");
            } else if (value instanceof List) {
                json.append(createJsonArray((List<?>) value));
            } else if (value instanceof Map) {
                json.append(createJsonFromMap((Map<?, ?>) value));
            } else {
                json.append("\"").append(escapeJson(value.toString())).append("\"");
            }
        }
        
        json.append("}");
        return json.toString();
    }
    
    /**
     * 创建JSON数组字符串
     * 
     * @param list 对象列表
     * @return JSON数组字符串
     */
    public static String createJsonArray(List<?> list) {
        StringBuilder json = new StringBuilder();
        json.append("[");
        
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                json.append(",");
            }
            
            Object value = list.get(i);
            
            if (value == null) {
                json.append("null");
            } else if (value instanceof Boolean) {
                json.append(value);
            } else if (value instanceof Number) {
                json.append(value);
            } else if (value instanceof String) {
                json.append("\"").append(escapeJson((String) value)).append("\"");
            } else if (value instanceof List) {
                json.append(createJsonArray((List<?>) value));
            } else if (value instanceof Map) {
                json.append(createJsonFromMap((Map<?, ?>) value));
            } else {
                json.append("\"").append(escapeJson(value.toString())).append("\"");
            }
        }
        
        json.append("]");
        return json.toString();
    }
    
    /**
     * 转义JSON字符串中的特殊字符
     * 
     * @param string 原始字符串
     * @return 转义后的字符串
     */
    private static String escapeJson(String string) {
        return string.replace("\\", "\\\\")
                     .replace("\"", "\\\"")
                     .replace("\b", "\\b")
                     .replace("\f", "\\f")
                     .replace("\n", "\\n")
                     .replace("\r", "\\r")
                     .replace("\t", "\\t");
    }
    
    /**
     * 获取JSON对象中的字符串值
     * 
     * @param json JSON字符串
     * @param key 键
     * @return 对应的字符串值，如果不存在则返回null
     */
    public static String getStringValue(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]*)\"";
        java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = r.matcher(json);
        
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }
} 