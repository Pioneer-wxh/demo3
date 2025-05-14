package com.financetracker.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    
    /**
     * 解析JSON字符串为Map对象
     * 
     * @param json JSON字符串
     * @return 包含JSON数据的Map对象
     */
    public static Map<String, Object> parseJson(String json) {
        return parseJsonSimple(json);
    }
    
    /**
     * 简单的JSON解析方法
     * 
     * @param json JSON字符串
     * @return 包含JSON数据的Map对象
     */
    private static Map<String, Object> parseJsonSimple(String json) {
        Map<String, Object> result = new HashMap<>();
        
        // 清理JSON字符串
        json = json.trim();
        if (json.startsWith("{") && json.endsWith("}")) {
            json = json.substring(1, json.length() - 1).trim();
        }
        
        // 匹配键值对: "key":"value" 或 "key":value 或 "key":[array] 或 "key":{object}
        StringBuilder patternStr = new StringBuilder();
        patternStr.append("\"([^\"]+)\"\\s*:\\s*"); // 匹配键
        patternStr.append("("); // 开始分组匹配不同类型的值
        patternStr.append("\"([^\"]*)\""); // 字符串值
        patternStr.append("|"); 
        patternStr.append("([-+]?[0-9]*\\.?[0-9]+)"); // 数字值
        patternStr.append("|"); 
        patternStr.append("\\[(.*?)\\]"); // 数组
        patternStr.append("|"); 
        patternStr.append("\\{(.*?)\\}"); // 对象
        patternStr.append("|"); 
        patternStr.append("(true|false|null)"); // 布尔值或null
        patternStr.append(")");
        
        Pattern pattern = Pattern.compile(patternStr.toString());
        Matcher matcher = pattern.matcher(json);
        
        while (matcher.find()) {
            String key = matcher.group(1);
            String stringValue = matcher.group(3);
            String numberValue = matcher.group(4);
            String arrayContent = matcher.group(5);
            String objectContent = matcher.group(6);
            String booleanOrNullValue = matcher.group(7);
            
            if (stringValue != null) {
                result.put(key, stringValue);
            } else if (numberValue != null) {
                try {
                    if (numberValue.contains(".")) {
                        result.put(key, Double.parseDouble(numberValue));
                    } else {
                        result.put(key, Long.parseLong(numberValue));
                    }
                } catch (NumberFormatException e) {
                    result.put(key, numberValue);
                }
            } else if (arrayContent != null) {
                result.put(key, parseJsonArray(arrayContent));
            } else if (objectContent != null) {
                result.put(key, parseJsonSimple(objectContent));
            } else if (booleanOrNullValue != null) {
                if ("true".equals(booleanOrNullValue)) {
                    result.put(key, Boolean.TRUE);
                } else if ("false".equals(booleanOrNullValue)) {
                    result.put(key, Boolean.FALSE);
                } else {
                    result.put(key, null);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 简单的JSON数组解析方法
     * 
     * @param json JSON数组字符串内容
     * @return 包含JSON数组数据的List对象
     */
    private static List<Object> parseJsonArray(String json) {
        List<Object> result = new ArrayList<>();
        
        Pattern stringPattern = Pattern.compile("\"([^\"]*)\"");
        Pattern numberPattern = Pattern.compile("([-+]?[0-9]*\\.?[0-9]+)");
        Pattern booleanPattern = Pattern.compile("(true|false|null)");
        
        Matcher stringMatcher = stringPattern.matcher(json);
        while (stringMatcher.find()) {
            result.add(stringMatcher.group(1));
        }
        
        // 如果没有找到字符串值，尝试找数字值
        if (result.isEmpty()) {
            Matcher numberMatcher = numberPattern.matcher(json);
            while (numberMatcher.find()) {
                String value = numberMatcher.group(1);
                try {
                    if (value.contains(".")) {
                        result.add(Double.parseDouble(value));
                    } else {
                        result.add(Long.parseLong(value));
                    }
                } catch (NumberFormatException e) {
                    // 跳过无法解析的数字
                }
            }
        }
        
        // 如果没有找到字符串和数字值，尝试找布尔值和null
        if (result.isEmpty()) {
            Matcher booleanMatcher = booleanPattern.matcher(json);
            while (booleanMatcher.find()) {
                String value = booleanMatcher.group(1);
                if ("true".equals(value)) {
                    result.add(Boolean.TRUE);
                } else if ("false".equals(value)) {
                    result.add(Boolean.FALSE);
                } else {
                    result.add(null);
                }
            }
        }
        
        return result;
    }
} 