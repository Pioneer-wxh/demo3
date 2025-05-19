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
        LOGGER.info("Finance Tracker application starting...");

        // Set the initial LookAndFeel before any UI components are created.
        LookAndFeelManager.setInitialLookAndFeel();

        // 创建并显示启动画面
        SplashScreenManager splashManager = new SplashScreenManager();
        splashManager.show();

        // 确保GUI更新在事件分发线程上完成
        SwingUtilities.invokeLater(() -> {
            try {
                // LookAndFeelManager.applyLookAndFeel(); // Removed: Theme now applied by
                // MainFrame using settings

                // 模拟加载过程
                splashManager.simulateLoading();

                // 创建并显示主应用程序窗口
                LOGGER.info("Initializing main application window...");
                MainFrame mainFrame = new MainFrame();

                // 设置应用程序图标
                ImageIcon appIcon = AppIcon.createAppIcon(ICON_SIZE);
                mainFrame.setIconImage(appIcon.getImage());

                // 关闭启动画面
                splashManager.dispose();

                // 显示主窗口
                mainFrame.setVisible(true);
                LOGGER.info("Application started successfully.");

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error starting application: " + e.getMessage(), e);
                if (splashManager != null) {
                    splashManager.dispose();
                }
                JOptionPane.showMessageDialog(null,
                        "Error starting application: " + e.getMessage(),
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
