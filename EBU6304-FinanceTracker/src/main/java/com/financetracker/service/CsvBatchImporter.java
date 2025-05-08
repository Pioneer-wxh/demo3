package com.financetracker.service;
// 定义代码所属的包名，表示该类位于 com.financetracker.service 包下，用于组织和管理类

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
// 导入 Stream 类，用于处理数据流操作

/**
 * CsvBatchImporter是一个批量导入CSV文件的工具类。
 * 主要功能：
 * 1. 批量扫描并导入指定目录下的所有CSV文件
 * 2. 自动检测CSV文件的列结构（日期、金额、描述等）
 * 3. 自动判断交易类型（收入或支出）
 * 4. 智能识别日期格式并进行解析
 * 5. 详细的导入结果统计和报告
 * 
 * 内部包含两个重要的辅助类：
 * - CsvHeaderDetector：负责智能检测CSV文件的列结构和格式
 * - ImportResult：记录导入结果的统计信息
 */
// 类注释：说明 CsvBatchImporter 是一个批量导入 CSV 文件的工具类
// 列出主要功能，包括扫描导入、列结构检测、交易类型判断、日期格式识别和结果统计
// 提到两个辅助类：CsvHeaderDetector 和 ImportResult

public class CsvBatchImporter {
    // 定义 CsvBatchImporter 类，用于批量导入 CSV 文件中的交易记录

    private TransactionService transactionService;
    // 定义类成员变量 transactionService，类型为 TransactionService，用于处理交易记录的业务逻辑

    public CsvBatchImporter(TransactionService transactionService) {
        // 定义构造函数，接收 TransactionService 实例
        this.transactionService = transactionService;
        // 将传入的 transactionService 赋值给类成员变量
    }

    /**
     * 批量导入CSV文件
     * 
     * @param files 要导入的文件数组
     * @return 导入结果
     */
    public ImportResult importCsvFiles(File[] files) {
        ImportResult result = new ImportResult();

        for (File file : files) {
            if (!file.exists() || !file.isFile() || !file.getName().toLowerCase().endsWith(".csv")) {
                result.failedFileCount++;
                continue;
            }

            try {
                // 检测CSV头信息
                List<String> headers = readCsvHeaders(file.getAbsolutePath());
                if (headers == null || headers.isEmpty()) {
                    result.failedFileCount++;
                    continue;
                }

                // 自动检测列映射
                String[] columnMappings = autoDetectColumns(file.getAbsolutePath(), headers);

                if (columnMappings[0] == null || columnMappings[1] == null || columnMappings[2] == null) {
                    // 缺少必要的列
                    result.failedFileCount++;
                    continue;
                }

                // 执行导入
                int importedCount = transactionService.importFromCsv(
                        file.getAbsolutePath(),
                        columnMappings[0], // 日期列
                        columnMappings[1], // 金额列
                        columnMappings[2], // 描述列
                        columnMappings[3], // 类别列，可能为null
                        columnMappings[4], // 日期格式
                        false // isExpense参数已被忽略，由系统自动检测
                );

                if (importedCount > 0) {
                    result.successFileCount++;
                    result.totalRecordCount += importedCount;
                } else {
                    result.failedFileCount++;
                }
            } catch (Exception e) {
                result.failedFileCount++;
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * 从目录导入CSV文件
     * 
     * @param directoryPath 目录路径
     * @return 导入结果
     */
    public ImportResult importFromDirectory(String directoryPath) {
        File dir = new File(directoryPath);
        if (!dir.exists() || !dir.isDirectory()) {
            ImportResult result = new ImportResult();
            result.failedFileCount = 1;
            return result;
        }

        // 获取目录中的所有CSV文件
        File[] csvFiles = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".csv");
            }
        });

        if (csvFiles == null || csvFiles.length == 0) {
            ImportResult result = new ImportResult();
            return result;
        }

        // 执行批量导入
        return importCsvFiles(csvFiles);
    }

    /**
     * 读取CSV文件头
     * 
     * @param filePath 文件路径
     * @return 列标题列表
     */
    private List<String> readCsvHeaders(String filePath) {
        List<String> headers = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine();
            if (headerLine != null) {
                // 根据常见的CSV分隔符拆分标题行
                String separator = headerLine.contains(",") ? "," : ";";
                String[] headerArray = headerLine.split(separator);

                for (String header : headerArray) {
                    headers.add(header.trim());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return headers;
    }

    /**
     * 自动检测列映射
     * 
     * @param filePath 文件路径
     * @param headers  列标题
     * @return 自动识别的列映射数组 [日期列,金额列,描述列,类别列,日期格式]
     */
    private String[] autoDetectColumns(String filePath, List<String> headers) {
        // 常见日期列名映射
        String[] dateKeywords = { "date", "time", "day", "日期", "时间", "transaction date" };
        // 常见金额列名映射
        String[] amountKeywords = { "amount", "sum", "value", "price", "金额", "数额", "价格", "transaction amount" };
        // 常见描述列名映射
        String[] descKeywords = { "description", "desc", "note", "memo", "名称", "描述", "备注", "摘要",
                "transaction description" };
        // 常见类别列名映射
        String[] categoryKeywords = { "category", "type", "group", "类别", "类型", "分类" };

        // 查找最匹配的列
        int dateIndex = findBestMatch(headers, dateKeywords);
        int amountIndex = findBestMatch(headers, amountKeywords);
        int descIndex = findBestMatch(headers, descKeywords);
        int categoryIndex = findBestMatch(headers, categoryKeywords);

        // 如果找不到必要的列，尝试使用索引位置
        if (dateIndex < 0 && headers.size() > 0)
            dateIndex = 0;
        if (amountIndex < 0 && headers.size() > 1)
            amountIndex = 1;
        if (descIndex < 0 && headers.size() > 2)
            descIndex = 2;

        // 确定日期格式
        String dateFormat = "yyyy-MM-dd"; // 默认格式
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // 跳过标题行
            reader.readLine();

            // 读取第一个数据行
            String dataLine = reader.readLine();
            if (dataLine != null && dateIndex >= 0) {
                String[] values = dataLine.split(dataLine.contains(",") ? "," : ";");
                if (values.length > dateIndex) {
                    String dateValue = values[dateIndex].trim();
                    dateFormat = guessDateFormat(dateValue);
                }
            }
        } catch (Exception e) {
            // 忽略错误，使用默认日期格式
        }

        // 返回自动识别的列和日期格式
        String[] result = new String[5];
        result[0] = dateIndex >= 0 ? headers.get(dateIndex) : null;
        result[1] = amountIndex >= 0 ? headers.get(amountIndex) : null;
        result[2] = descIndex >= 0 ? headers.get(descIndex) : null;
        result[3] = categoryIndex >= 0 ? headers.get(categoryIndex) : null;
        result[4] = dateFormat;

        return result;
    }

    /**
     * 在标题列表中查找最匹配的列名
     * 
     * @param headers  列标题列表
     * @param keywords 关键词数组
     * @return 最匹配的列索引，如果没找到则返回-1
     */
    private int findBestMatch(List<String> headers, String[] keywords) {
        // 首先尝试精确匹配
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i).toLowerCase();
            for (String keyword : keywords) {
                if (header.equals(keyword)) {
                    return i;
                }
            }
        }

        // 然后尝试部分匹配
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i).toLowerCase();
            for (String keyword : keywords) {
                if (header.contains(keyword) || keyword.contains(header)) {
                    return i;
                }
            }
        }

        return -1; // 未找到匹配项
    }

    /**
     * 根据日期字符串猜测日期格式
     * 
     * @param dateStr 日期字符串
     * @return 最可能的日期格式
     */
    private String guessDateFormat(String dateStr) {
        String[][] formats = {
                { "yyyy-MM-dd", "\\d{4}-\\d{1,2}-\\d{1,2}" },
                { "MM/dd/yyyy", "\\d{1,2}/\\d{1,2}/\\d{4}" },
                { "dd/MM/yyyy", "\\d{1,2}/\\d{1,2}/\\d{4}" },
                { "yyyy/MM/dd", "\\d{4}/\\d{1,2}/\\d{1,2}" },
                { "dd-MM-yyyy", "\\d{1,2}-\\d{1,2}-\\d{4}" },
                { "dd.MM.yyyy", "\\d{1,2}\\.\\d{1,2}\\.\\d{4}" }
        };

        for (String[] format : formats) {
            if (dateStr.matches(format[1])) {
                return format[0];
            }
        }

        return "yyyy-MM-dd"; // 默认格式
    }

    /**
     * 导入结果类
     */
    // 类注释：说明 ImportResult 类用于存储导入操作的结果
    public static class ImportResult {
        // 定义静态内部类 ImportResult
        private int successFileCount = 0;
        // 定义成功导入的文件数，初始为 0
        private int failedFileCount = 0;
        // 定义失败导入的文件数，初始为 0
        private int totalRecordCount = 0;
        // 定义导入的总记录数，初始为 0

        public int getSuccessFileCount() {
            return successFileCount;
        }

        // 获取成功文件计数
        public void setSuccessFileCount(int successFileCount) {
            this.successFileCount = successFileCount;
        }
        // 设置成功文件计数

        public int getFailedFileCount() {
            return failedFileCount;
        }

        // 获取失败文件计数
        public void setFailedFileCount(int failedFileCount) {
            this.failedFileCount = failedFileCount;
        }
        // 设置失败文件计数

        public int getTotalRecordCount() {
            return totalRecordCount;
        }

        // 获取总记录数
        public void setTotalRecordCount(int totalRecordCount) {
            this.totalRecordCount = totalRecordCount;
        }
        // 设置总记录数

        @Override
        public String toString() {
            return "成功导入 " + successFileCount + " 个文件，失败 " + failedFileCount + " 个文件，共 " + totalRecordCount + " 条记录";
            // 重写 toString 方法，返回导入结果的描述
        }
    }
}