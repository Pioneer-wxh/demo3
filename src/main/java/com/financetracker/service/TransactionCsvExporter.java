package com.financetracker.service;
// 定义代码所属的包名，表示该类位于 com.financetracker.service 包下，用于组织和管理类

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import com.financetracker.model.Transaction;
// 导入 Transaction 类，表示交易记录的模型类

/**
 * TransactionCsvExporter处理交易记录与CSV文件的相互转换，
 * 作为本地CSV数据库的接口。主要功能包括：
 * 1. 将交易记录列表导出到CSV文件
 * 2. 从CSV文件读取交易记录列表
 * 3. 单条交易记录的增删改查操作
 * 4. 按月份分组导出交易记录
 * 5. 创建数据备份
 * 
 * 特点：
 * - 使用固定的CSV文件路径作为本地数据库
 * - 实现完整的CRUD操作
 * - 提供数据分组和批量处理功能
 */
/**
 * 用于将交易记录导出到CSV文件，并从CSV文件中读取交易记录
 * 这个类作为本地CSV数据库的接口
 */
// 类注释：说明 TransactionCsvExporter 类用于处理交易记录与 CSV 文件之间的转换
// 提供 CRUD 操作、按月分组导出和备份功能，作为本地 CSV 数据库的接口

public class TransactionCsvExporter {
    // 定义 TransactionCsvExporter 类，用于管理交易记录的 CSV 文件操作

    private static final String CSV_FILE_PATH = "E:\\code\\Java\\software_lab\\data\\transactions.csv";
    // 定义静态常量，指定交易记录存储的 CSV 文件路径

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    // 定义静态常量，使用 ISO_LOCAL_DATE 格式（YYYY-MM-DD）作为日期格式化器

    // CSV列头
    private static final String[] CSV_HEADERS = {
            "ID", "Date", "Amount", "Description", "Category", "Participant", "Notes", "IsExpense"
    };
    // 定义 CSV 文件的表头数组，包含交易记录的各个字段名称

    /**
     * 将交易记录列表导出到CSV文件
     * 
     * @param transactions 要导出的交易记录列表
     * @return 是否成功导出
     */
    // 方法注释：说明该方法用于将交易记录列表导出到 CSV 文件
    public boolean exportTransactionsToCSV(List<Transaction> transactions) {
        // 定义方法，接收交易记录列表，返回是否成功导出
        try {
            // 使用 try-catch 块处理可能的 IO 异常
            // 确保目录存在
            Path path = Paths.get(CSV_FILE_PATH);
            // 将 CSV 文件路径转换为 Path 对象
            Files.createDirectories(path.getParent());
            // 确保文件所在的父目录存在，如果不存在则创建

            try (BufferedWriter writer = Files.newBufferedWriter(path);
                    CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.builder().setHeader(CSV_HEADERS).build())) {
                // 使用 try-with-resources 确保 writer 和 csvPrinter 自动关闭
                // 创建 BufferedWriter 用于高效写入 CSV 文件
                // 创建 CSVPrinter，配置为使用默认 CSV 格式并设置表头

                // 按日期排序（最新的优先）
                transactions.sort(Comparator.comparing(Transaction::getDate).reversed());
                // 对交易记录按日期排序，Comparator.comparing 使用 getDate 方法比较
                // reversed() 使排序倒序（最新日期优先）

                // 写入每条交易记录
                for (Transaction transaction : transactions) {
                    // 遍历交易记录列表
                    csvPrinter.printRecord(
                            transaction.getId(),
                            transaction.getDate().format(DATE_FORMATTER),
                            transaction.getAmount(),
                            transaction.getDescription(),
                            transaction.getCategory(),
                            transaction.getParticipant(),
                            transaction.getNotes(),
                            transaction.isExpense());
                    // 使用 printRecord 写入一条交易记录的各个字段
                    // 日期使用 DATE_FORMATTER 格式化为 YYYY-MM-DD
                }

                csvPrinter.flush();
                // 刷新 CSVPrinter，确保所有数据写入文件
                return true;
                // 导出成功，返回 true
            }
        } catch (IOException e) {
            e.printStackTrace();
            // 捕获并打印 IO 异常堆栈跟踪
            return false;
            // 导出失败，返回 false
        }
    }

    /**
     * 从CSV文件读取交易记录
     * 
     * @return 读取到的交易记录列表
     */
    // 方法注释：说明该方法用于从 CSV 文件读取交易记录
    public List<Transaction> importTransactionsFromCSV() {
        // 定义方法，返回从 CSV 文件读取的交易记录列表
        List<Transaction> transactions = new ArrayList<>();
        // 创建空 ArrayList 用于存储交易记录
        Path path = Paths.get(CSV_FILE_PATH);
        // 将 CSV 文件路径转换为 Path 对象

        if (!Files.exists(path)) {
            // 检查 CSV 文件是否存在
            return transactions;
            // 如果文件不存在，返回空列表
        }

        try (Reader reader = new FileReader(CSV_FILE_PATH);
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build())) {
            // 使用 try-with-resources 确保 reader 和 csvParser 自动关闭
            // 创建 FileReader 读取 CSV 文件
            // 创建 CSVParser，配置为使用默认 CSV 格式，设置表头并跳过表头行

            for (CSVRecord record : csvParser) {
                // 遍历 CSV 文件的每条记录
                String id = record.get("ID");
                // 获取 ID 字段
                LocalDate date = LocalDate.parse(record.get("Date"), DATE_FORMATTER);
                // 获取 Date 字段并使用 DATE_FORMATTER 解析为 LocalDate
                double amount = Double.parseDouble(record.get("Amount"));
                // 获取 Amount 字段并解析为 double
                String description = record.get("Description");
                // 获取 Description 字段
                String category = record.get("Category");
                // 获取 Category 字段
                String participant = record.get("Participant");
                // 获取 Participant 字段
                String notes = record.get("Notes");
                // 获取 Notes 字段
                boolean isExpense = Boolean.parseBoolean(record.get("IsExpense"));
                // 获取 IsExpense 字段并解析为 boolean

                Transaction transaction = new Transaction();
                // 创建新的 Transaction 对象
                transaction.setId(id);
                // 设置交易 ID
                transaction.setDate(date);
                // 设置交易日期
                transaction.setAmount(amount);
                // 设置交易金额
                transaction.setDescription(description);
                // 设置交易描述
                transaction.setCategory(category);
                // 设置交易类别
                transaction.setParticipant(participant);
                // 设置交易参与者
                transaction.setNotes(notes);
                // 设置交易备注
                transaction.setExpense(isExpense);
                // 设置是否为支出

                transactions.add(transaction);
                // 将交易添加到列表
            }
        } catch (IOException e) {
            e.printStackTrace();
            // 捕获并打印 IO 异常堆栈跟踪
        }

        return transactions;
        // 返回读取到的交易记录列表
    }

    /**
     * 将单个交易记录添加到CSV文件
     * 
     * @param transaction 要添加的交易记录
     * @return 是否成功添加
     */
    // 方法注释：说明该方法用于向 CSV 文件添加单个交易记录
    public boolean addTransactionToCSV(Transaction transaction) {
        // 定义方法，接收 Transaction 对象，返回是否添加成功
        List<Transaction> transactions = importTransactionsFromCSV();
        // 从 CSV 文件读取现有交易记录
        transactions.add(transaction);
        // 将新交易添加到列表
        return exportTransactionsToCSV(transactions);
        // 将更新后的交易列表导出到 CSV 文件，返回是否成功
    }

    /**
     * 从CSV文件中删除交易记录
     * 
     * @param transactionId 要删除的交易记录ID
     * @return 是否成功删除
     */
    // 方法注释：说明该方法用于从 CSV 文件删除指定 ID 的交易记录
    public boolean deleteTransactionFromCSV(String transactionId) {
        // 定义方法，接收交易 ID，返回是否删除成功
        List<Transaction> transactions = importTransactionsFromCSV();
        // 从 CSV 文件读取现有交易记录
        boolean removed = transactions.removeIf(t -> t.getId().equals(transactionId));
        // 使用 removeIf 删除 ID 匹配的交易记录，返回是否删除成功
        if (removed) {
            return exportTransactionsToCSV(transactions);
            // 如果删除成功，将更新后的交易列表导出到 CSV 文件
        }
        return false;
        // 如果未找到匹配的交易，返回 false
    }

    /**
     * 批量删除交易记录
     * 
     * @param transactionIds 要删除的交易记录ID列表
     * @return 成功删除的数量
     */
    // 方法注释：说明该方法用于批量删除指定 ID 列表的交易记录
    public int batchDeleteTransactionsFromCSV(List<String> transactionIds) {
        // 定义方法，接收交易 ID 列表，返回删除的记录数
        List<Transaction> transactions = importTransactionsFromCSV();
        // 从 CSV 文件读取现有交易记录
        int originalSize = transactions.size();
        // 记录原始交易列表大小
        transactions.removeIf(t -> transactionIds.contains(t.getId()));
        // 使用 removeIf 删除 ID 在 transactionIds 列表中的交易记录
        int removed = originalSize - transactions.size();
        // 计算删除的记录数

        if (removed > 0) {
            exportTransactionsToCSV(transactions);
            // 如果删除了记录，将更新后的交易列表导出到 CSV 文件
        }

        return removed;
        // 返回删除的记录数
    }

    /**
     * 更新CSV文件中的交易记录
     * 
     * @param updatedTransaction 更新后的交易记录
     * @return 是否成功更新
     */
    // 方法注释：说明该方法用于更新 CSV 文件中的交易记录
    public boolean updateTransactionInCSV(Transaction updatedTransaction) {
        // 定义方法，接收更新后的 Transaction 对象，返回是否更新成功
        List<Transaction> transactions = importTransactionsFromCSV();
        // 从 CSV 文件读取现有交易记录
        boolean updated = false;
        // 标记是否找到并更新了交易

        for (int i = 0; i < transactions.size(); i++) {
            // 遍历交易列表
            if (transactions.get(i).getId().equals(updatedTransaction.getId())) {
                // 检查当前交易的 ID 是否与更新交易的 ID 匹配
                transactions.set(i, updatedTransaction);
                // 替换匹配的交易记录
                updated = true;
                // 设置更新标志为 true
                break;
                // 退出循环
            }
        }

        if (updated) {
            return exportTransactionsToCSV(transactions);
            // 如果更新成功，将更新后的交易列表导出到 CSV 文件
        }
        return false;
        // 如果未找到匹配的交易，返回 false
    }

    /**
     * 检查CSV文件是否存在
     * 
     * @return 文件是否存在
     */
    // 方法注释：说明该方法用于检查 CSV 文件是否存在
    public boolean csvFileExists() {
        // 定义方法，返回 CSV 文件是否存在
        return Files.exists(Paths.get(CSV_FILE_PATH));
        // 使用 Files.exists 检查指定路径的文件是否存在
    }

    /**
     * 创建CSV文件的备份
     * 
     * @return 是否成功创建备份
     */
    // 方法注释：说明该方法用于创建 CSV 文件的备份
    public boolean createCsvBackup() {
        // 定义方法，返回是否成功创建备份
        if (!csvFileExists()) {
            // 检查 CSV 文件是否存在
            return false;
            // 如果文件不存在，返回 false
        }

        String backupFilePath = CSV_FILE_PATH + "." + System.currentTimeMillis() + ".bak";
        // 生成备份文件路径，添加时间戳和 .bak 后缀
        try {
            Files.copy(Paths.get(CSV_FILE_PATH), Paths.get(backupFilePath));
            // 使用 Files.copy 复制原始 CSV 文件到备份路径
            return true;
            // 备份成功，返回 true
        } catch (IOException e) {
            e.printStackTrace();
            // 捕获并打印 IO 异常堆栈跟踪
            return false;
            // 备份失败，返回 false
        }
    }

    /**
     * 将所有交易记录按月导出到指定目录
     * 
     * @param transactions  要导出的交易记录列表
     * @param directoryPath 导出目录路径
     * @return 导出结果，包含成功导出的文件数量和记录数量
     */
    // 方法注释：说明该方法用于按月分组导出交易记录到指定目录
    public ExportResult exportTransactionsByMonth(List<Transaction> transactions, String directoryPath) {
        // 定义方法，接收交易记录列表和导出目录路径，返回导出结果
        ExportResult result = new ExportResult();
        // 创建 ExportResult 对象，用于存储导出结果（成功/失败文件数和记录数）

        try {
            // 使用 try-catch 块处理可能的异常
            // 确保目录存在
            Path dir = Paths.get(directoryPath);
            // 将导出目录路径转换为 Path 对象
            Files.createDirectories(dir);
            // 确保目录存在，如果不存在则创建

            // 按月份分组交易记录
            Map<String, List<Transaction>> transactionsByMonth = new HashMap<>();
            // 创建 HashMap 用于按年月存储交易记录

            for (Transaction transaction : transactions) {
                // 遍历交易记录
                // 获取年月作为分组键
                String yearMonth = transaction.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM"));
                // 使用 yyyy-MM 格式（如 2023-12）作为分组键

                // 添加到对应的月份组
                if (!transactionsByMonth.containsKey(yearMonth)) {
                    transactionsByMonth.put(yearMonth, new ArrayList<>());
                    // 如果该年月尚未存在，创建新的 ArrayList
                }
                transactionsByMonth.get(yearMonth).add(transaction);
                // 将交易添加到对应年月的列表
            }

            // 导出每个月份的交易记录
            for (Map.Entry<String, List<Transaction>> entry : transactionsByMonth.entrySet()) {
                // 遍历按年月分组的交易记录
                String yearMonth = entry.getKey();
                // 获取年月键
                List<Transaction> monthTransactions = entry.getValue();
                // 获取该年月的交易记录列表

                // 生成文件名
                String fileName = "transactions_" + yearMonth + ".csv";
                // 创建文件名，如 transactions_2023-12.csv
                Path filePath = dir.resolve(fileName);
                // 解析为完整文件路径

                // 导出到CSV文件
                boolean success = exportTransactionsToFile(monthTransactions, filePath.toString());
                // 调用 exportTransactionsToFile 导出该月交易记录

                if (success) {
                    result.setSuccessFileCount(result.getSuccessFileCount() + 1);
                    // 增加成功文件计数
                    result.setTotalRecordCount(result.getTotalRecordCount() + monthTransactions.size());
                    // 增加总记录数
                    System.out.println("成功导出 " + yearMonth + " 月的 " + monthTransactions.size() + " 条交易记录到文件 " + fileName);
                    // 打印成功导出的信息
                } else {
                    result.setFailedFileCount(result.getFailedFileCount() + 1);
                    // 增加失败文件计数
                    System.err.println("导出 " + yearMonth + " 月的交易记录失败");
                    // 打印失败信息
                }
            }

            // 导出所有交易记录到一个文件
            String allFileName = "transactions_all.csv";
            // 定义所有交易记录的文件名
            Path allFilePath = dir.resolve(allFileName);
            // 解析为完整文件路径
            boolean success = exportTransactionsToFile(transactions, allFilePath.toString());
            // 导出所有交易记录到一个文件

            if (success) {
                result.setSuccessFileCount(result.getSuccessFileCount() + 1);
                // 增加成功文件计数
                System.out.println("成功导出所有 " + transactions.size() + " 条交易记录到文件 " + allFileName);
                // 打印成功导出的信息
            } else {
                result.setFailedFileCount(result.getFailedFileCount() + 1);
                // 增加失败文件计数
                System.err.println("导出所有交易记录失败");
                // 打印失败信息
            }

        } catch (Exception e) {
            System.err.println("导出交易记录时出错: " + e.getMessage());
            // 捕获并打印异常信息
            e.printStackTrace();
            // 打印异常堆栈跟踪
        }

        return result;
        // 返回导出结果
    }

    /**
     * 将交易记录导出到指定文件
     * 
     * @param transactions 要导出的交易记录列表
     * @param filePath     文件路径
     * @return 是否成功导出
     */
    // 方法注释：说明该方法用于将交易记录导出到指定文件
    private boolean exportTransactionsToFile(List<Transaction> transactions, String filePath) {
        // 定义私有方法，接收交易记录列表和文件路径，返回是否成功导出
        try {
            // 使用 try-catch 块处理可能的 IO 异常
            // 确保目录存在
            Path path = Paths.get(filePath);
            // 将文件路径转换为 Path 对象
            Files.createDirectories(path.getParent());
            // 确保文件所在的父目录存在

            try (BufferedWriter writer = Files.newBufferedWriter(path);
                    CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.builder().setHeader(CSV_HEADERS).build())) {
                // 使用 try-with-resources 确保 writer 和 csvPrinter 自动关闭
                // 创建 BufferedWriter 用于高效写入
                // 创建 CSVPrinter，配置为使用默认 CSV 格式并设置表头

                // 按日期排序（最新的优先）
                transactions.sort(Comparator.comparing(Transaction::getDate).reversed());
                // 对交易记录按日期倒序排序

                // 写入每条交易记录
                for (Transaction transaction : transactions) {
                    // 遍历交易记录
                    csvPrinter.printRecord(
                            transaction.getId(),
                            transaction.getDate().format(DATE_FORMATTER),
                            transaction.getAmount(),
                            transaction.getDescription(),
                            transaction.getCategory(),
                            transaction.getParticipant(),
                            transaction.getNotes(),
                            transaction.isExpense());
                    // 写入一条交易记录的各个字段
                }

                csvPrinter.flush();
                // 刷新 CSVPrinter，确保数据写入文件
                return true;
                // 导出成功，返回 true
            }
        } catch (IOException e) {
            e.printStackTrace();
            // 捕获并打印 IO 异常堆栈跟踪
            return false;
            // 导出失败，返回 false
        }
    }

    /**
     * 导出结果类
     */
    // 类注释：说明 ExportResult 类用于存储导出操作的结果
    public static class ExportResult {
        // 定义静态内部类 ExportResult
        private int successFileCount = 0;
        // 定义成功导出的文件数，初始为 0
        private int failedFileCount = 0;
        // 定义失败导出的文件数，初始为 0
        private int totalRecordCount = 0;
        // 定义导出的总记录数，初始为 0

        public int getSuccessFileCount() {
            return successFileCount;
            // 获取成功文件计数
        }

        public void setSuccessFileCount(int successFileCount) {
            this.successFileCount = successFileCount;
            // 设置成功文件计数
        }

        public int getFailedFileCount() {
            return failedFileCount;
            // 获取失败文件计数
        }

        public void setFailedFileCount(int failedFileCount) {
            this.failedFileCount = failedFileCount;
            // 设置失败文件计数
        }

        public int getTotalRecordCount() {
            return totalRecordCount;
            // 获取总记录数
        }

        public void setTotalRecordCount(int totalRecordCount) {
            this.totalRecordCount = totalRecordCount;
            // 设置总记录数
        }

        @Override
        public String toString() {
            return "成功导出 " + successFileCount + " 个文件，失败 " + failedFileCount + " 个文件，共 " + totalRecordCount + " 条记录";
            // 重写 toString 方法，返回导出结果的描述
        }
    }
}