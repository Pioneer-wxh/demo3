package com.financetracker.service;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 * Implementation of DataService that uses CSV for data persistence.
 * 
 * @param <T> The type of data to be persisted
 */
public class CsvDataService<T> implements DataService<T> {
    
    private final CsvConverter<T> converter;
    
    /**
     * Constructor for CsvDataService.
     * 
     * @param type The class of the type to be persisted
     * @param converter The converter to convert between objects and CSV
     */
    private CsvDataService(Class<T> type, CsvConverter<T> converter) {
        this.converter = converter;
    }
    
    @Override
    public boolean saveToFile(List<T> items, String filePath) {
        try {
            // Create directories if they don't exist
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            
            try (Writer writer = new FileWriter(filePath);
                 CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(converter.getHeader()))) {
                
                for (T item : items) {
                    csvPrinter.printRecord((Object[]) converter.toCsvRecord(item));
                }
                
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public List<T> loadFromFile(String filePath) {
        List<T> items = new ArrayList<>();
        
        if (!Files.exists(Paths.get(filePath))) {
            return items;
        }
        
        try (Reader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            for (CSVRecord record : csvParser) {
                String[] values = new String[record.size()];
                for (int i = 0; i < record.size(); i++) {
                    values[i] = record.get(i);
                }
                T item = converter.fromCsvRecord(values);
                if (item != null) {
                    items.add(item);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return items;
    }
    
    @Override
    public boolean saveItemToFile(T item, String filePath) {
        try {
            // Create directories if they don't exist
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            
            boolean isNewFile = !Files.exists(path);
            
            try (Writer writer = new FileWriter(filePath, !isNewFile);
                 CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(converter.getHeader()))) {
                
                if (isNewFile) {
                    csvPrinter.printRecord((Object[]) converter.getHeader());
                }
                
                csvPrinter.printRecord((Object[]) converter.toCsvRecord(item));
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public T loadItemFromFile(String filePath) {
        if (!Files.exists(Paths.get(filePath))) {
            return null;
        }
        
        try (Reader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            for (CSVRecord record : csvParser) {
                String[] values = new String[record.size()];
                for (int i = 0; i < record.size(); i++) {
                    values[i] = record.get(i);
                }
                return converter.fromCsvRecord(values);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    @Override
    public boolean appendToFile(T item, String filePath) {
        try {
            // Create directories if they don't exist
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            
            boolean isNewFile = !Files.exists(path);
            
            try (Writer writer = new FileWriter(filePath, !isNewFile);
                 CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
                
                if (isNewFile) {
                    csvPrinter.printRecord((Object[]) converter.getHeader());
                }
                
                csvPrinter.printRecord((Object[]) converter.toCsvRecord(item));
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }
    
    @Override
    public boolean createBackup(String filePath, String backupFilePath) {
        try {
            if (!fileExists(filePath)) {
                return false;
            }
            
            // Create directories if they don't exist
            Path backupPath = Paths.get(backupFilePath);
            Files.createDirectories(backupPath.getParent());
            
            // Copy the file
            Files.copy(Paths.get(filePath), backupPath, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Creates a new instance of CsvDataService for a specific type.
     * 
     * @param <T> The type of data to be persisted
     * @param type The class of the type to be persisted
     * @param converter The converter to convert between objects and CSV
     * @return A new instance of CsvDataService
     */
    public static <T> CsvDataService<T> forType(Class<T> type, CsvConverter<T> converter) {
        return new CsvDataService<>(type, converter);
    }
    
    /**
     * Interface for converting between objects and CSV.
     * 
     * @param <T> The type of object to convert
     */
    public interface CsvConverter<T> {
        /**
         * Converts an object to a CSV record.
         * 
         * @param item The object to convert
         * @return The CSV record
         */
        String[] toCsvRecord(T item);
        
        /**
         * Converts a CSV record to an object.
         * 
         * @param record The CSV record
         * @return The object
         */
        T fromCsvRecord(String[] record);
        
        /**
         * Gets the CSV header.
         * 
         * @return The CSV header
         */
        String[] getHeader();
    }
}
