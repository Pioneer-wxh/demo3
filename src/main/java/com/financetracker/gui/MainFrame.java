package com.financetracker.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.financetracker.model.Settings;
import com.financetracker.service.BudgetAdjustmentService;
import com.financetracker.service.BudgetForecastService;
import com.financetracker.service.CsvBatchImporter;
import com.financetracker.service.FinancialCycleService;
import com.financetracker.service.SettingsService;
import com.financetracker.service.SpecialDateService;
import com.financetracker.service.TransactionCsvExporter;
import com.financetracker.service.TransactionDataSource;
import com.financetracker.service.TransactionService;
import com.financetracker.util.LookAndFeelManager;

/**
 * The main application window that contains all GUI components.
 */
public class MainFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private HomePanel homePanel;
    private TransactionPanel transactionPanel;
    private AnalysisPanel analysisPanel;
    private SettingsPanel settingsPanel;

    private Settings settings;
    private SettingsService settingsService;
    private TransactionDataSource transactionDataSource;
    private SpecialDateService specialDateService;
    private BudgetAdjustmentService budgetAdjustmentService;
    private TransactionService transactionService;
    private FinancialCycleService financialCycleService;
    private BudgetForecastService budgetForecastService;
    private CsvBatchImporter csvBatchImporter;
    private TransactionCsvExporter csvExporter;

    // private JPanel navigationPanel; // 已移除, 由 AppNavigationBar 代替
    private AppNavigationBar appNavigationBar; // 新增 AppNavigationBar 成员
    private AppStatusBar appStatusBar; // New AppStatusBar field

    // 定义主题颜色 (这些颜色现在也存在于 AppNavigationBar 中，考虑统一管理)
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color PRIMARY_DARK_COLOR = new Color(31, 97, 141); // 可能可以移除，如果AppNavigationBar自己管理悬停
    private static final Color ACCENT_COLOR = new Color(26, 188, 156); // 可能可以移除，如果AppNavigationBar自己管理激活状态

    // Theme colors used by MainFrame for its components if not solely relying on
    // UIManager
    private static final Color MF_PRIMARY_COLOR = new Color(41, 128, 185); // Example
    private static final Color MF_ACCENT_COLOR = new Color(26, 188, 156); // Example
    private static final Color MF_PRIMARY_DARK_COLOR = new Color(31, 97, 141); // Example

    /**
     * Constructor for MainFrame.
     */
    public MainFrame(SettingsService settingsService, TransactionService transactionService, SpecialDateService specialDateService, BudgetAdjustmentService budgetAdjustmentService, FinancialCycleService financialCycleService, BudgetForecastService budgetForecastService, CsvBatchImporter csvBatchImporter, TransactionCsvExporter csvExporter) {
        this.settingsService = settingsService;
        this.transactionService = transactionService;
        this.specialDateService = specialDateService;
        this.budgetAdjustmentService = budgetAdjustmentService;
        this.financialCycleService = financialCycleService;
        this.budgetForecastService = budgetForecastService;
        this.csvBatchImporter = csvBatchImporter;
        this.csvExporter = csvExporter;
        this.settings = settingsService.getSettings();

        // Set up the frame
        setTitle("个人财务跟踪器");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 600));

        // 创建主面板，使用边界布局
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 创建顶部导航面板
        ActionListener panelSwitcher = e -> {
            String panelName = e.getActionCommand();
            String buttonText = "";
            switch (panelName) {
                case "home": buttonText = "首页"; break;
                case "transactions": buttonText = "交易记录"; break;
                case "analysis": buttonText = "分析"; break;
                case "settings": buttonText = "设置"; break;
                default: buttonText = panelName; break;
            }
            showPanel(panelName);
            if (appStatusBar != null) {
                appStatusBar.showTemporaryMessage("显示 " + buttonText + " 面板");
            }
        };
        appNavigationBar = new AppNavigationBar(panelSwitcher);
        mainPanel.add(appNavigationBar, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        appStatusBar = new AppStatusBar();
        mainPanel.add(appStatusBar, BorderLayout.SOUTH);

        homePanel = new HomePanel(transactionService, settingsService, panelSwitcher);
        transactionPanel = new TransactionPanel(transactionService, settingsService, panelSwitcher, csvBatchImporter, csvExporter);
        analysisPanel = new AnalysisPanel(transactionService, settingsService, specialDateService, budgetAdjustmentService, financialCycleService, budgetForecastService, panelSwitcher);
        settingsPanel = new SettingsPanel(
            settingsService,
            specialDateService,
            budgetAdjustmentService,
            financialCycleService,
            panelSwitcher,
            this::refreshCategoryLists,
            this::triggerAnalysisPanelRefresh,
            () -> { if (homePanel != null) homePanel.refreshData(); }
        );

        contentPanel.add(homePanel, "home");
        contentPanel.add(transactionPanel, "transactions");
        contentPanel.add(analysisPanel, "analysis");
        contentPanel.add(settingsPanel, "settings");

        cardLayout.show(contentPanel, "home");
        appNavigationBar.updateNavigationButtons("home");
        if (appStatusBar != null) {
            appStatusBar.showTemporaryMessage("欢迎使用个人财务跟踪器");
        }
        add(mainPanel);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                settingsService.saveSettings();
                if (appStatusBar != null) {
                    appStatusBar.dispose();
                }
            }
        });
        applyTheme();
    }

    /**
     * 应用主题到应用程序组件。
     * This method might need to be updated to interact with AppNavigationBar's
     * theme application.
     */
    private void applyTheme() {
        if (settings == null) {
            // Fallback or log error if settings are not loaded - this shouldn't happen if
            // constructor is correct
            System.err.println("MainFrame.applyTheme: Settings not loaded!");
            return;
        }

        // 1. Apply global L&F theme settings (Dark/Light mode UIManager properties)
        LookAndFeelManager.applyTheme(this.settings);

        // 2. Apply theme to specific custom components if they don't fully adopt
        // UIManager changes
        // These colors might come from a centralized Theme object or constants based on
        // settings.isDarkModeEnabled()
        Color navPrimary = settings.isDarkModeEnabled() ? new Color(50, 50, 50) : MF_PRIMARY_COLOR;
        Color navDark = settings.isDarkModeEnabled() ? new Color(30, 30, 30) : MF_PRIMARY_DARK_COLOR;
        Color navAccent = settings.isDarkModeEnabled() ? new Color(0, 150, 136) : MF_ACCENT_COLOR;

        if (appNavigationBar != null) {
            appNavigationBar.applyThemeColors(navPrimary, navDark, navAccent);
        }

        if (appStatusBar != null) {
            Color statusBarBg = settings.isDarkModeEnabled() ? new Color(45, 45, 45)
                    : UIManager.getColor("Panel.background");
            Color statusBarFg = settings.isDarkModeEnabled() ? Color.LIGHT_GRAY
                    : UIManager.getColor("Label.foreground");
            Color statusBarBorder = settings.isDarkModeEnabled() ? new Color(80, 80, 80) : Color.LIGHT_GRAY;
            appStatusBar.applyThemeColors(statusBarBg, statusBarFg, statusBarBorder);
        }

        // 3. Theme content panels (example - each panel might need its own applyTheme
        // method)
        if (contentPanel != null) {
            // Example: Set background for the contentPanel itself based on theme
            contentPanel.setBackground(settings.isDarkModeEnabled() ? new Color(40, 40, 40) : Color.WHITE);

            for (Component comp : contentPanel.getComponents()) {
                // General theming for JPanels
                if (comp instanceof JPanel && !(comp instanceof AppNavigationBar || comp instanceof AppStatusBar)) {
                    comp.setBackground(settings.isDarkModeEnabled() ? new Color(50, 50, 50) : new Color(230, 230, 230));
                }
                // Call specific applyTheme for custom panels
                if (comp instanceof HomePanel) {
                    ((HomePanel) comp).applyTheme(settings);
                }
                if (comp instanceof TransactionPanel) {
                    ((TransactionPanel) comp).applyTheme(settings);
                }
                // Temporarily comment out calls to AnalysisPanel and SettingsPanel
                if (comp instanceof AnalysisPanel) {
                    ((AnalysisPanel) comp).applyTheme(settings);
                }
                if (comp instanceof SettingsPanel) {
                    ((SettingsPanel) comp).applyTheme(settings);
                }
            }
        }

        // 4. Theme the MainFrame itself if needed (e.g., background of JFrame's content
        // pane)
        // getContentPane().setBackground(settings.isDarkModeEnabled() ? new
        // Color(30,30,30) : new Color(220,220,220));

        // 5. IMPORTANT: Update the entire component tree to reflect L&F and UIManager
        // changes.
        SwingUtilities.updateComponentTreeUI(this);
        repaint(); // Additional repaint just in case
    }

    /**
     * Shows the specified panel in the CardLayout.
     * 
     * @param panelName the name of the panel to show
     */
    public void showPanel(String panelName) {
        if (cardLayout != null && contentPanel != null && panelName != null) {
            cardLayout.show(contentPanel, panelName);
            if (appNavigationBar != null)
                appNavigationBar.updateNavigationButtons(panelName);

            // Panel-specific refresh/update logic when shown
            if ("home".equals(panelName) && homePanel != null)
                homePanel.refreshData();
            if ("transactions".equals(panelName) && transactionPanel != null)
                transactionPanel.loadTransactions();
            if ("analysis".equals(panelName) && analysisPanel != null)
                analysisPanel.refreshAllAnalysisData();
            if ("settings".equals(panelName) && settingsPanel != null)
                settingsPanel.loadSettingsData();
        }
    }

    // Getter and Setter methods for services and panels (from outline)
    // These remain for now, but their usage by child panels should be reviewed
    // later
    // to promote loose coupling (e.g., by passing services via constructors).

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
        applyTheme();

        if (transactionPanel != null)
            transactionPanel.loadTransactions(); // Refresh transactions as currency/date format might change
        if (analysisPanel != null)
            analysisPanel.refreshAllAnalysisData(); // Analysis might depend on new settings
        if (settingsPanel != null)
            settingsPanel.loadSettingsData(); // Settings panel UI should reflect new settings
        if (homePanel != null)
            homePanel.refreshData(); // Home panel might depend on new settings

        if (appStatusBar != null) {
            // appStatusBar.setPersistentMessage(settings.getWelcomeMessage()); // Example
        }
        // refreshCategoryLists(); // This is likely called by
        // settingsPanel.loadSettingsData() or when categories actually change
    }

    public SettingsService getSettingsService() {
        return settingsService;
    }

    public SpecialDateService getSpecialDateService() {
        return specialDateService;
    }

    public BudgetAdjustmentService getBudgetAdjustmentService() {
        return budgetAdjustmentService;
    }

    public TransactionService getTransactionService() {
        return transactionService;
    }

    public FinancialCycleService getFinancialCycleService() {
        return financialCycleService;
    }

    public HomePanel getHomePanel() {
        return homePanel;
    }

    /**
     * Triggers a refresh of the AnalysisPanel.
     * This is an example of MainFrame acting as a mediator.
     */
    public void triggerAnalysisPanelRefresh() {
        if (analysisPanel != null) {
            analysisPanel.refreshAllAnalysisData();
        }
    }

    /**
     * Refreshes category lists in relevant panels.
     * This method is now a target for a callback.
     */
    public void refreshCategoryLists() {
        if (transactionPanel != null) {
            transactionPanel.updateCategoryDropdown();
        }
        // SettingsPanel's category display is refreshed internally or via
        // loadSettingsData()
        // if (settingsPanel != null) {
        // settingsPanel.refreshCategoryDisplay();
        // }
        if (analysisPanel != null) {
            analysisPanel.refreshCategoryList(); // This was for the dropdowns in analysis, ensure it still works
        }
    }

    /**
     * 获取预算预测服务
     */
    public BudgetForecastService getBudgetForecastService() {
        return this.budgetForecastService;
    }

    // Ensure all other methods from the original file that were not shown are
    // preserved
    // The tool will handle this by applying changes on top of the original.
}
