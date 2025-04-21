package com.financetracker.service;

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

/**
 * 用于将交易记录导出到CSV文件，并从CSV文件中读取交易记录
 * 这个类作为本地CSV数据库的接口
 */
public class TransactionCsvExporter {

    private static final String CSV_FILE_PATH = "E:\\code\\Java\\software_lab\\data\\transactions.csv";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    // CSV列头
    private static final String[] CSV_HEADERS = {
            "ID", "Date", "Amount", "Description", "Category", "Participant", "Notes", "IsExpense"
    };

    /**
     * 将交易记录列表导出到CSV文件
     * 
     * @param transactions 要导出的交易记录列表
     * @return 是否成功导出
     */
    public boolean exportTransactionsToCSV(List<Transaction> transactions) {
        try {
            // 确保目录存在
            Path path = Paths.get(CSV_FILE_PATH);
            Files.createDirectories(path.getParent());

            try (BufferedWriter writer = Files.newBufferedWriter(path);
                    CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(CSV_HEADERS))) {

                // 按日期排序（最新的优先）
                transactions.sort(Comparator.comparing(Transaction::getDate).reversed());

                // 写入每条交易记录
                for (Transaction transaction : transactions) {
                    csvPrinter.printRecord(
                            transaction.getId(),
                            transaction.getDate().format(DATE_FORMATTER),
                            transaction.getAmount(),
                            transaction.getDescription(),
                            transaction.getCategory(),
                            transaction.getParticipant(),
                            transaction.getNotes(),
                            transaction.isExpense());
                }

                csvPrinter.flush();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 从CSV文件读取交易记录
     * 
     * @return 读取到的交易记录列表
     */
    public List<Transaction> importTransactionsFromCSV() {
        List<Transaction> transactions = new ArrayList<>();
        Path path = Paths.get(CSV_FILE_PATH);

        if (!Files.exists(path)) {
            return transactions;
        }

        try (Reader reader = new FileReader(CSV_FILE_PATH);
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            for (CSVRecord record : csvParser) {
                String id = record.get("ID");
                LocalDate date = LocalDate.parse(record.get("Date"), DATE_FORMATTER);
                double amount = Double.parseDouble(record.get("Amount"));
                String description = record.get("Description");
                String category = record.get("Category");
                String participant = record.get("Participant");
                String notes = record.get("Notes");
                boolean isExpense = Boolean.parseBoolean(record.get("IsExpense"));

                Transaction transaction = new Transaction();
                transaction.setId(id);
                transaction.setDate(date);
                transaction.setAmount(amount);
                transaction.setDescription(description);
                transaction.setCategory(category);
                transaction.setParticipant(participant);
                transaction.setNotes(notes);
                transaction.setExpense(isExpense);

                transactions.add(transaction);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return transactions;
    }

    /**
     * 将单个交易记录添加到CSV文件
     * 
     * @param transaction 要添加的交易记录
     * @return 是否成功添加
     */
    public boolean addTransactionToCSV(Transaction transaction) {
        List<Transaction> transactions = importTransactionsFromCSV();
        transactions.add(transaction);
        return exportTransactionsToCSV(transactions);
    }

    /**
     * 从CSV文件中删除交易记录
     * 
     * @param transactionId 要删除的交易记录ID
     * @return 是否成功删除
     */
    public boolean deleteTransactionFromCSV(String transactionId) {
        List<Transaction> transactions = importTransactionsFromCSV();
        boolean removed = transactions.removeIf(t -> t.getId().equals(transactionId));
        if (removed) {
            return exportTransactionsToCSV(transactions);
        }
        return false;
    }

    /**
     * 批量删除交易记录
     * 
     * @param transactionIds 要删除的交易记录ID列表
     * @return 成功删除的数量
     */
    public int batchDeleteTransactionsFromCSV(List<String> transactionIds) {
        List<Transaction> transactions = importTransactionsFromCSV();
        int originalSize = transactions.size();
        transactions.removeIf(t -> transactionIds.contains(t.getId()));
        int removed = originalSize - transactions.size();

        if (removed > 0) {
            exportTransactionsToCSV(transactions);
        }

        return removed;
    }

    /**
     * 更新CSV文件中的交易记录
     * 
     * @param updatedTransaction 更新后的交易记录
     * @return 是否成功更新
     */
    public boolean updateTransactionInCSV(Transaction updatedTransaction) {
        List<Transaction> transactions = importTransactionsFromCSV();
        boolean updated = false;

        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getId().equals(updatedTransaction.getId())) {
                transactions.set(i, updatedTransaction);
                updated = true;
                break;
            }
        }

        if (updated) {
            return exportTransactionsToCSV(transactions);
        }
        return false;
    }

    /**
     * 检查CSV文件是否存在
     * 
     * @return 文件是否存在
     */
    public boolean csvFileExists() {
        return Files.exists(Paths.get(CSV_FILE_PATH));
    }

    /**
     * 创建CSV文件的备份
     * 
     * @return 是否成功创建备份
     */
    public boolean createCsvBackup() {
        if (!csvFileExists()) {
            return false;
        }

        String backupFilePath = CSV_FILE_PATH + "." + System.currentTimeMillis() + ".bak";
        try {
            Files.copy(Paths.get(CSV_FILE_PATH), Paths.get(backupFilePath));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将所有交易记录按月导出到指定目录
     * 
     * @param transactions  要导出的交易记录列表
     * @param directoryPath 导出目录路径
     * @return 导出结果，包含成功导出的文件数量和记录数量
     */
    public ExportResult exportTransactionsByMonth(List<Transaction> transactions, String directoryPath) {
        ExportResult result = new ExportResult();

        try {
            // 确保目录存在
            Path dir = Paths.get(directoryPath);
            Files.createDirectories(dir);

            // 按月份分组交易记录
            Map<String, List<Transaction>> transactionsByMonth = new HashMap<>();

            for (Transaction transaction : transactions) {
                // 获取年月作为分组键
                String yearMonth = transaction.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM"));

                // 添加到对应的月份组
                if (!transactionsByMonth.containsKey(yearMonth)) {
                    transactionsByMonth.put(yearMonth, new ArrayList<>());
                }
                transactionsByMonth.get(yearMonth).add(transaction);
            }

            // 导出每个月份的交易记录
            for (Map.Entry<String, List<Transaction>> entry : transactionsByMonth.entrySet()) {
                String yearMonth = entry.getKey();
                List<Transaction> monthTransactions = entry.getValue();

                // 生成文件名
                String fileName = "transactions_" + yearMonth + ".csv";
                Path filePath = dir.resolve(fileName);

                // 导出到CSV文件
                boolean success = exportTransactionsToFile(monthTransactions, filePath.toString());

                if (success) {
                    result.setSuccessFileCount(result.getSuccessFileCount() + 1);
                    result.setTotalRecordCount(result.getTotalRecordCount() + monthTransactions.size());
                    System.out
                            .println("成功导出 " + yearMonth + " 月的 " + monthTransactions.size() + " 条交易记录到文件 " + fileName);
                } else {
                    result.setFailedFileCount(result.getFailedFileCount() + 1);
                    System.err.println("导出 " + yearMonth + " 月的交易记录失败");
                }
            }

            // 导出所有交易记录到一个文件
            String allFileName = "transactions_all.csv";
            Path allFilePath = dir.resolve(allFileName);
            boolean success = exportTransactionsToFile(transactions, allFilePath.toString());

            if (success) {
                result.setSuccessFileCount(result.getSuccessFileCount() + 1);
                System.out.println("成功导出所有 " + transactions.size() + " 条交易记录到文件 " + allFileName);
            } else {
                result.setFailedFileCount(result.getFailedFileCount() + 1);
                System.err.println("导出所有交易记录失败");
            }

        } catch (Exception e) {
            System.err.println("导出交易记录时出错: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 将交易记录导出到指定文件
     * 
     * @param transactions 要导出的交易记录列表
     * @param filePath     文件路径
     * @return 是否成功导出
     */
    private boolean exportTransactionsToFile(List<Transaction> transactions, String filePath) {
        try {
            // 确保目录存在
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());

            try (BufferedWriter writer = Files.newBufferedWriter(path);
                    CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(CSV_HEADERS))) {

                // 按日期排序（最新的优先）
                transactions.sort(Comparator.comparing(Transaction::getDate).reversed());

                // 写入每条交易记录
                for (Transaction transaction : transactions) {
                    csvPrinter.printRecord(
                            transaction.getId(),
                            transaction.getDate().format(DATE_FORMATTER),
                            transaction.getAmount(),
                            transaction.getDescription(),
                            transaction.getCategory(),
                            transaction.getParticipant(),
                            transaction.getNotes(),
                            transaction.isExpense());
                }

                csvPrinter.flush();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 导出结果类
     */
    public static class ExportResult {
        private int successFileCount = 0;
        private int failedFileCount = 0;
        private int totalRecordCount = 0;

        public int getSuccessFileCount() {
            return successFileCount;
        }

        public void setSuccessFileCount(int successFileCount) {
            this.successFileCount = successFileCount;
        }

        public int getFailedFileCount() {
            return failedFileCount;
        }

        public void setFailedFileCount(int failedFileCount) {
            this.failedFileCount = failedFileCount;
        }

        public int getTotalRecordCount() {
            return totalRecordCount;
        }

        public void setTotalRecordCount(int totalRecordCount) {
            this.totalRecordCount = totalRecordCount;
        }

        @Override
        public String toString() {
            return "成功导出 " + successFileCount + " 个文件，失败 " + failedFileCount + " 个文件，共 " + totalRecordCount + " 条记录";
        }
    }
}