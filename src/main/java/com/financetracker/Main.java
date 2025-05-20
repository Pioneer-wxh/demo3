package com.financetracker;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.financetracker.gui.AppIcon;
import com.financetracker.gui.MainFrame;
import com.financetracker.gui.SplashScreenManager;
import com.financetracker.util.LookAndFeelManager;

/**
 * Main entry point for launching the Finance Tracker application using the
 * MainFrame structure.
 */
public class Main { // Renamed from AppLauncher

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final int ICON_SIZE = 48; // 图标尺寸

    public static void main(String[] args) {
        // Set the initial LookAndFeel before any UI components are created.
        LookAndFeelManager.setInitialLookAndFeel();
        final SplashScreenManager splashManager = new SplashScreenManager();
        try {
            com.financetracker.service.SerializationService<com.financetracker.model.Settings> serializationService =
                new com.financetracker.service.SerializationService<>(com.financetracker.model.Settings.class);
            com.financetracker.service.SettingsService settingsService = new com.financetracker.service.SettingsService(serializationService);
            com.financetracker.model.Settings settings = settingsService.getSettings();
            com.financetracker.service.TransactionCsvExporter transactionDataSource = new com.financetracker.service.TransactionCsvExporter();
            com.financetracker.service.TransactionService transactionService = new com.financetracker.service.TransactionService(transactionDataSource, settings);
            settingsService.setTransactionService(transactionService);
            com.financetracker.service.SpecialDateService specialDateService = new com.financetracker.service.SpecialDateService(settingsService);
            com.financetracker.service.BudgetAdjustmentService budgetAdjustmentService = new com.financetracker.service.BudgetAdjustmentService(settingsService);
            com.financetracker.service.FinancialCycleService financialCycleService = new com.financetracker.service.FinancialCycleService(transactionService, settingsService);
            com.financetracker.service.BudgetForecastService budgetForecastService = new com.financetracker.service.BudgetForecastService(transactionService, settingsService);
            financialCycleService.setBudgetForecastService(budgetForecastService);
            com.financetracker.service.CsvBatchImporter csvBatchImporter = new com.financetracker.service.CsvBatchImporter(transactionService, settings);
            com.financetracker.service.TransactionCsvExporter csvExporter = transactionDataSource;
            splashManager.show();
            SwingUtilities.invokeLater(() -> {
                try {
                    splashManager.simulateLoading();
                    MainFrame mainFrame = new MainFrame(
                        settingsService,
                        transactionService,
                        specialDateService,
                        budgetAdjustmentService,
                        financialCycleService,
                        budgetForecastService,
                        csvBatchImporter,
                        csvExporter
                    );
                    ImageIcon appIcon = AppIcon.createAppIcon(ICON_SIZE);
                    mainFrame.setIconImage(appIcon.getImage());
                    splashManager.dispose();
                    mainFrame.setVisible(true);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error starting application: " + e.getMessage(), e);
                    splashManager.dispose();
                    JOptionPane.showMessageDialog(null,
                            "Error starting application: " + e.getMessage(),
                            "Startup Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            });
            financialCycleService.performMonthEndClosing();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "自动累计历史余额失败: " + e.getMessage(), e);
        }
    }
}
