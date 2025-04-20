package com.financetracker.service;

import com.financetracker.model.SpecialDate;
import com.google.gson.reflect.TypeToken; // Add this import

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.time.LocalDate;

/**
 * Service for managing special dates.
 */
public class SpecialDateService {
    
    private static final String SPECIAL_DATES_FILE_PATH = "data/special_dates.json";
    private final JsonDataService<SpecialDate> jsonDataService;
    
    /**
     * Constructor for SpecialDateService.
     */
    public SpecialDateService() {
        this.jsonDataService = new JsonDataService<>(SpecialDate.class, new TypeToken<List<SpecialDate>>() {});
    }
    
    /**
     * Gets all special dates.
     * 
     * @return The list of all special dates
     */
    public List<SpecialDate> getAllSpecialDates() {
        List<SpecialDate> specialDates = new ArrayList<>();
        
        // Check if file exists
        if (jsonDataService.fileExists(SPECIAL_DATES_FILE_PATH)) {
            // Load special dates from file
            try {
                specialDates = jsonDataService.loadFromFile(SPECIAL_DATES_FILE_PATH);
                if (specialDates == null) {
                    specialDates = new ArrayList<>();
                }
            } catch (Exception e) {
                e.printStackTrace();
                specialDates = new ArrayList<>();
            }
        }
        
        // Sort special dates by date
        specialDates.sort(Comparator.comparing(SpecialDate::getDate));
        
        return specialDates;
    }
    
    /**
     * Gets special dates for a specific date range.
     * 
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @return The list of special dates in the date range
     */
    public List<SpecialDate> getSpecialDatesForDateRange(LocalDate startDate, LocalDate endDate) {
        List<SpecialDate> allSpecialDates = getAllSpecialDates();
        List<SpecialDate> specialDatesInRange = new ArrayList<>();
        
        for (SpecialDate specialDate : allSpecialDates) {
            LocalDate date = specialDate.getDate();
            if (!date.isBefore(startDate) && !date.isAfter(endDate)) {
                specialDatesInRange.add(specialDate);
            }
        }
        
        return specialDatesInRange;
    }
    
    /**
     * Gets special dates for a specific month.
     * 
     * @param year The year
     * @param month The month (1-12)
     * @return The list of special dates in the month
     */
    public List<SpecialDate> getSpecialDatesForMonth(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        
        return getSpecialDatesForDateRange(startDate, endDate);
    }
    
    /**
     * Gets special dates for the current month.
     * 
     * @return The list of special dates in the current month
     */
    public List<SpecialDate> getSpecialDatesForCurrentMonth() {
        LocalDate today = LocalDate.now();
        return getSpecialDatesForMonth(today.getYear(), today.getMonthValue());
    }
    
    /**
     * Gets special dates for the next month.
     * 
     * @return The list of special dates in the next month
     */
    public List<SpecialDate> getSpecialDatesForNextMonth() {
        LocalDate nextMonth = LocalDate.now().plusMonths(1);
        return getSpecialDatesForMonth(nextMonth.getYear(), nextMonth.getMonthValue());
    }
    
    /**
     * Adds a special date.
     * 
     * @param specialDate The special date to add
     * @return true if the operation was successful, false otherwise
     */
    public boolean addSpecialDate(SpecialDate specialDate) {
        List<SpecialDate> specialDates = getAllSpecialDates();
        specialDates.add(specialDate);
        return saveSpecialDates(specialDates);
    }
    
    /**
     * Updates a special date.
     * 
     * @param specialDate The special date to update
     * @return true if the operation was successful, false otherwise
     */
    public boolean updateSpecialDate(SpecialDate specialDate) {
        List<SpecialDate> specialDates = getAllSpecialDates();
        
        // Find the special date to update
        for (int i = 0; i < specialDates.size(); i++) {
            if (specialDates.get(i).getId().equals(specialDate.getId())) {
                specialDates.set(i, specialDate);
                return saveSpecialDates(specialDates);
            }
        }
        
        return false;
    }
    
    /**
     * Deletes a special date.
     * 
     * @param specialDate The special date to delete
     * @return true if the operation was successful, false otherwise
     */
    public boolean deleteSpecialDate(SpecialDate specialDate) {
        List<SpecialDate> specialDates = getAllSpecialDates();
        
        // Find the special date to delete
        for (int i = 0; i < specialDates.size(); i++) {
            if (specialDates.get(i).getId().equals(specialDate.getId())) {
                specialDates.remove(i);
                return saveSpecialDates(specialDates);
            }
        }
        
        return false;
    }
    
    /**
     * Saves special dates to the data file.
     * 
     * @param specialDates The special dates to save
     * @return true if the operation was successful, false otherwise
     */
    private boolean saveSpecialDates(List<SpecialDate> specialDates) {
        try {
            return jsonDataService.saveToFile(specialDates, SPECIAL_DATES_FILE_PATH);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Creates a backup of the special dates file.
     * 
     * @return true if the operation was successful, false otherwise
     */
    public boolean createBackup() {
        String backupFilePath = SPECIAL_DATES_FILE_PATH + ".backup";
        return jsonDataService.createBackup(SPECIAL_DATES_FILE_PATH, backupFilePath);
    }
}
