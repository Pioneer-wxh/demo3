package com.financetracker.gui;

import com.financetracker.model.Settings;
import com.financetracker.service.BudgetAdjustmentService;
import com.financetracker.service.SettingsService;
import com.financetracker.service.SpecialDateService;
import com.financetracker.service.TransactionService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
    private SpecialDateService specialDateService;
    private BudgetAdjustmentService budgetAdjustmentService;
    private TransactionService transactionService;

    private JPanel navigationPanel;
    private JLabel statusLabel;
    private Timer statusTimer;

    // 定义主题颜色
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color PRIMARY_DARK_COLOR = new Color(31, 97, 141);
    private static final Color ACCENT_COLOR = new Color(26, 188, 156);

    /**
     * Constructor for MainFrame.
     */
    public MainFrame() {
        // Initialize services
        settingsService = new SettingsService();
        settings = settingsService.getSettings();
        specialDateService = new SpecialDateService(settingsService);
        budgetAdjustmentService = new BudgetAdjustmentService(settingsService);
        transactionService = new TransactionService(settings);

        // Set up the frame
        setTitle("个人财务跟踪器");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 600));

        // 创建主面板，使用边界布局
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 创建顶部导航面板
        createNavigationPanel();
        mainPanel.add(navigationPanel, BorderLayout.NORTH);

        // Set up the content panel with card layout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // 创建底部状态栏
        createStatusBar();
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        // Initialize panels
        homePanel = new HomePanel(this);
        transactionPanel = new TransactionPanel(this);
        analysisPanel = new AnalysisPanel(transactionService, settingsService, specialDateService, budgetAdjustmentService, this);
        settingsPanel = new SettingsPanel(this);

        // Add panels to the content panel
        contentPanel.add(homePanel, "home");
        contentPanel.add(transactionPanel, "transactions");
        contentPanel.add(analysisPanel, "analysis");
        contentPanel.add(settingsPanel, "settings");

        // Show the home panel by default
        cardLayout.show(contentPanel, "home");
        updateNavigationButtons("home");

        // Add the main panel to the frame
        add(mainPanel);

        // Add window listener to save settings on close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                settingsService.saveSettings();
                // 停止状态栏计时器
                if (statusTimer != null && statusTimer.isRunning()) {
                    statusTimer.stop();
                }
            }
        });

        // Apply theme based on settings
        applyTheme();
    }

    /**
     * 创建顶部导航面板
     */
    private void createNavigationPanel() {
        navigationPanel = new JPanel();
        navigationPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
        navigationPanel.setBackground(PRIMARY_COLOR);
        navigationPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        // 添加导航按钮
        addNavigationButton("首页", "home", "返回应用主页");
        addNavigationButton("交易记录", "transactions", "管理您的交易记录");
        addNavigationButton("分析", "analysis", "查看AI辅助分析");
        addNavigationButton("设置", "settings", "配置应用程序设置");

        // 添加应用程序图标和标题在左侧
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setOpaque(false);

        // 创建图标
        JLabel iconLabel = new JLabel(AppIcon.createAppIcon(24));
        titlePanel.add(iconLabel);

        // 创建标题
        JLabel titleLabel = new JLabel("个人财务跟踪器");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titlePanel.add(titleLabel);

        // 左对齐放置标题面板
        navigationPanel.setLayout(new BorderLayout());
        navigationPanel.add(titlePanel, BorderLayout.WEST);

        // 中间放置按钮
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonsPanel.setOpaque(false);

        // 将按钮添加到按钮面板
        for (Component comp : navigationPanel.getComponents()) {
            if (comp instanceof JButton) {
                buttonsPanel.add(comp);
                navigationPanel.remove(comp);
            }
        }

        navigationPanel.add(buttonsPanel, BorderLayout.CENTER);
    }

    /**
     * 添加导航按钮
     */
    private void addNavigationButton(String text, String panelName, String tooltip) {
        JButton button = new JButton(text);
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY_COLOR);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setToolTipText(tooltip);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        // 添加鼠标悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!button.getName().equals("active")) {
                    button.setBackground(PRIMARY_DARK_COLOR);
                }
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!button.getName().equals("active")) {
                    button.setBackground(PRIMARY_COLOR);
                }
            }
        });

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPanel(panelName);
                updateNavigationButtons(panelName);
                updateStatusText("显示" + text + "面板");
            }
        });

        navigationPanel.add(button);
    }

    /**
     * 更新导航按钮状态
     */
    private void updateNavigationButtons(String activePanelName) {
        for (Component comp : navigationPanel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                for (Component panelComp : panel.getComponents()) {
                    if (panelComp instanceof JButton) {
                        JButton button = (JButton) panelComp;
                        String buttonText = button.getText().toLowerCase();

                        if ((buttonText.equals("首页") && activePanelName.equals("home")) ||
                                (buttonText.equals("交易记录") && activePanelName.equals("transactions")) ||
                                (buttonText.equals("分析") && activePanelName.equals("analysis")) ||
                                (buttonText.equals("设置") && activePanelName.equals("settings"))) {

                            button.setBackground(ACCENT_COLOR);
                            button.setName("active");
                        } else {
                            button.setBackground(PRIMARY_COLOR);
                            button.setName("inactive");
                        }
                    }
                }
            }
        }
    }

    /**
     * 创建底部状态栏
     */
    private void createStatusBar() {
        // 创建状态标签
        statusLabel = new JLabel("欢迎使用个人财务跟踪器");
        statusLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        // 创建时间更新定时器
        statusTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String timeStr = now.format(formatter);
                String currentText = statusLabel.getText();

                // 保留非时间部分的状态文本
                int timeIndex = currentText.indexOf(" | 时间: ");
                if (timeIndex > 0) {
                    currentText = currentText.substring(0, timeIndex);
                }

                statusLabel.setText(currentText + " | 时间: " + timeStr);
            }
        });
        statusTimer.start();
    }

    /**
     * 更新状态栏文本
     */
    private void updateStatusText(String text) {
        String currentText = statusLabel.getText();
        int timeIndex = currentText.indexOf(" | 时间: ");
        if (timeIndex > 0) {
            statusLabel.setText(text + currentText.substring(timeIndex));
        } else {
            statusLabel.setText(text);
        }
    }

    /**
     * Applies the theme based on the settings.
     */
    private void applyTheme() {
        try {
            if (settings.isDarkModeEnabled()) {
                // 设置深色主题
                UIManager.put("control", new Color(45, 45, 45));
                UIManager.put("info", new Color(45, 45, 45));
                UIManager.put("nimbusBase", new Color(25, 25, 25));
                UIManager.put("nimbusAlertYellow", new Color(248, 187, 0));
                UIManager.put("nimbusDisabledText", new Color(128, 128, 128));
                UIManager.put("nimbusFocus", new Color(115, 164, 209));
                UIManager.put("nimbusGreen", new Color(176, 179, 50));
                UIManager.put("nimbusInfoBlue", new Color(66, 139, 221));
                UIManager.put("nimbusLightBackground", new Color(25, 25, 25));
                UIManager.put("nimbusOrange", new Color(191, 98, 4));
                UIManager.put("nimbusRed", new Color(169, 46, 34));
                UIManager.put("nimbusSelectedText", new Color(255, 255, 255));
                UIManager.put("nimbusSelectionBackground", new Color(104, 93, 156));
                UIManager.put("text", new Color(230, 230, 230));

                try {
                    UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
                } catch (Exception e) {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                }
            } else {
                // 设置浅色主题
                try {
                    UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
                } catch (Exception e) {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                }
            }
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows the specified panel in the content area.
     *
     * @param panelName The name of the panel to show
     */
    public void showPanel(String panelName) {
        cardLayout.show(contentPanel, panelName);
        updateNavigationButtons(panelName); // Keep navigation buttons in sync
        updateStatusText("Navigated to " + panelName);

        // Update specific panels when they are shown
        if ("home".equals(panelName) && homePanel != null) {
            homePanel.updateRemainingBalance();
        }
        if ("analysis".equals(panelName) && analysisPanel != null) {
            analysisPanel.updateSavingGoalsProgressView(); // Refresh saving goals when analysis panel is shown
            analysisPanel.refreshCategoryList(); // Refresh category dropdowns
        }
        if ("transactions".equals(panelName) && transactionPanel != null) {
            transactionPanel.loadTransactions(); // Refresh transactions
            transactionPanel.updateCategoryDropdown(); // Refresh categories in dropdown
        }
        if ("settings".equals(panelName) && settingsPanel != null) {
            settingsPanel.loadSpecialDates(); // Refresh special dates
            settingsPanel.loadSavingGoals(); // Refresh saving goals
            settingsPanel.refreshCategoryDisplay(); // Refresh categories
        }
    }

    /**
     * Gets the settings.
     * 
     * @return The settings
     */
    public Settings getSettings() {
        return settings;
    }

    /**
     * Sets the settings.
     * 
     * @param settings The settings to set
     */
    public void setSettings(Settings settings) {
        this.settings = settings;
        applyTheme();
    }

    /**
     * Gets the settings service.
     * 
     * @return The settings service
     */
    public SettingsService getSettingsService() {
        return settingsService;
    }

    /**
     * Gets the special date service.
     * 
     * @return The special date service
     */
    public SpecialDateService getSpecialDateService() {
        return specialDateService;
    }

    /**
     * Gets the budget adjustment service.
     * 
     * @return The budget adjustment service
     */
    public BudgetAdjustmentService getBudgetAdjustmentService() {
        return budgetAdjustmentService;
    }

    /**
     * Gets the transaction service.
     * 
     * @return The transaction service
     */
    public TransactionService getTransactionService() {
        return transactionService;
    }

    public HomePanel getHomePanel() {
        return homePanel;
    }

    /**
     * Triggers a refresh of all data and views within the AnalysisPanel.
     */
    public void triggerAnalysisPanelRefresh() {
        if (analysisPanel != null) {
            System.out.println("MainFrame: Triggering AnalysisPanel refresh.");
            analysisPanel.refreshAllAnalysisData();
        } else {
            System.err.println("MainFrame: AnalysisPanel is null, cannot trigger refresh.");
        }
    }

    /**
     * 刷新所有面板中的类别列表
     */
    public void refreshCategoryLists() {
        // 每次刷新前重新从设置服务获取最新设置
        this.settings = settingsService.getSettings();
        
        // 通知所有需要刷新类别列表的面板
        if (transactionPanel != null) {
            try {
                transactionPanel.refreshCategoryList();
            } catch (Exception e) {
                System.err.println("刷新交易面板类别列表出错: " + e.getMessage());
            }
        }
        
        if (analysisPanel != null) {
            try {
                analysisPanel.refreshCategoryList();
            } catch (Exception e) {
                System.err.println("刷新分析面板类别列表出错: " + e.getMessage());
            }
        }
        
        if (settingsPanel != null) {
            try {
                settingsPanel.refreshCategoryList();
            } catch (Exception e) {
                System.err.println("刷新设置面板类别列表出错: " + e.getMessage());
            }
        }
    }
}
