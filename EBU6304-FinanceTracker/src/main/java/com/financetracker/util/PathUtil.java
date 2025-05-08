package com.financetracker.util;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.financetracker.gui.MainFrame; // Use MainFrame as a reference point

/**
 * Utility class for resolving application-specific paths.
 * Attempts to locate the application's root directory (where the JAR or classes reside)
 * and resolves data/export paths relative to that.
 */
public class PathUtil {

    private static Path baseDir = null;

    // Static initializer to determine the base directory once.
    static {
        try {
            // Get the location of the code source (JAR or classes directory)
            File sourceLocation = new File(MainFrame.class.getProtectionDomain().getCodeSource().getLocation().toURI());

            // If running from a JAR, the base directory is the directory containing the JAR.
            // If running from classes (e.g., in IDE), the base directory might be the 'target/classes' dir,
            // so we might need to go up one or two levels depending on structure.
            if (sourceLocation.isFile() && sourceLocation.getName().toLowerCase().endsWith(".jar")) {
                baseDir = sourceLocation.getParentFile().toPath();
            } else if (sourceLocation.isDirectory()) {
                // Assuming a standard Maven structure like 'target/classes' during development
                // We want the project root directory which is parent of 'target'
                if (sourceLocation.getName().equals("classes") && sourceLocation.getParentFile().getName().equals("target")) {
                     baseDir = sourceLocation.getParentFile().getParentFile().toPath(); // Go up two levels to project root
                } else {
                    // Fallback: Use the classes directory itself or its parent if structure is different
                    baseDir = sourceLocation.toPath();
                     // Or maybe: baseDir = sourceLocation.getParentFile().toPath(); - Adjust if needed!
                }
            }

            // If baseDir couldn't be determined, fallback to user directory (less ideal)
            if (baseDir == null) {
                baseDir = Paths.get(System.getProperty("user.dir"));
                System.err.println("Warning: Could not reliably determine application base directory. Using working directory: " + baseDir);
            }
            System.out.println("Application base directory resolved to: " + baseDir.toString());

        } catch (URISyntaxException | SecurityException e) {
            System.err.println("Error determining application base directory. Falling back to working directory.");
            e.printStackTrace();
            baseDir = Paths.get(System.getProperty("user.dir"));
        }
    }

    /**
     * Gets the absolute path to the base data directory (e.g., /path/to/project/data).
     * @return Absolute Path to the data directory.
     */
    public static Path getDataDir() {
        return baseDir.resolve("data").toAbsolutePath();
    }

    /**
     * Gets the absolute path to the base export directory (e.g., /path/to/project/export).
     * @return Absolute Path to the export directory.
     */
    public static Path getExportDir() {
        return baseDir.resolve("export").toAbsolutePath();
    }

    /**
     * Gets the absolute path to the transactions CSV file.
     * @return Absolute Path to transactions.csv.
     */
    public static Path getTransactionsCsvPath() {
        return getDataDir().resolve("transactions.csv");
    }

    /**
     * Gets the absolute path to the settings data file.
     * @return Absolute Path to settings.dat.
     */
    public static Path getSettingsDatPath() {
        return getDataDir().resolve("settings.dat");
    }

    /**
     * Gets the absolute path to the special dates data file.
     * @return Absolute Path to specialDates.dat.
     */
    public static Path getSpecialDatesDatPath() {
        return getDataDir().resolve("specialDates.dat");
    }

    /**
     * Gets the absolute path to the export directory for classified (monthly) reports.
     * @return Absolute Path to the export/classify directory.
     */
    public static Path getExportClassifyDirPath() {
        return getExportDir().resolve("classify"); // Assuming classify is a direct subdirectory of export
    }

     /**
     * Gets the absolute path for the file containing all exported transactions.
     * @return Absolute Path to export/transactions_all.csv
     */
    public static Path getExportAllTransactionsPath() {
        return getExportDir().resolve("transactions_all.csv");
    }

} 